package com.college.hostel.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Universal DataSource configuration supporting:
 * 1. Render Managed PostgreSQL via dynamic DATABASE_URL (postgres://user:pass@host:5432/dbname)
 * 2. Standard Spring Datasource properties (SPRING_DATASOURCE_URL, USERNAME, PASSWORD)
 * 3. H2 In-Memory Database for local development and automated testing
 */
@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    /**
     * Parse raw database URL (postgres:// or postgresql://) into HikariConfig.
     */
    public static HikariConfig parsePostgreSqlUrl(String dbUrl) {
        if (!StringUtils.hasText(dbUrl) || (!dbUrl.startsWith("postgres://") && !dbUrl.startsWith("postgresql://"))) {
            return null;
        }

        try {
            URI uri = new URI(dbUrl);
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath();
            String dbName = (path != null && path.length() > 1) ? path.substring(1) : "hostel_db";

            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
            String query = uri.getQuery();
            if (StringUtils.hasText(query)) {
                jdbcUrl += "?" + query;
            }

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(jdbcUrl);
            hikariConfig.setDriverClassName("org.postgresql.Driver");

            if (uri.getUserInfo() != null) {
                String[] userInfo = uri.getUserInfo().split(":", 2);
                hikariConfig.setUsername(userInfo[0]);
                if (userInfo.length > 1) {
                    hikariConfig.setPassword(userInfo[1]);
                }
            }

            // Connection pool sizing for cloud deployments
            hikariConfig.setMaximumPoolSize(5);
            hikariConfig.setMinimumIdle(1);
            hikariConfig.setIdleTimeout(300000);
            hikariConfig.setMaxLifetime(600000);
            hikariConfig.setConnectionTimeout(20000);
            // Allow pool to retry connections if PostgreSQL is cold-starting
            hikariConfig.setInitializationFailTimeout(30000);

            return hikariConfig;
        } catch (Exception e) {
            log.error("Failed to parse PostgreSQL DATABASE_URL: {}", dbUrl, e);
            return null;
        }
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties properties) {
        String dbUrl = StringUtils.hasText(databaseUrl) ? databaseUrl : System.getenv("DATABASE_URL");

        HikariConfig parsedConfig = parsePostgreSqlUrl(dbUrl);
        if (parsedConfig != null) {
            log.info("Configured PostgreSQL DataSource successfully from DATABASE_URL: {}", parsedConfig.getJdbcUrl());
            return new HikariDataSource(parsedConfig);
        }

        // Standard Spring Boot DataSource initialization (uses spring.datasource.* from active profile)
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }
}
