package com.college.hostel.config;

import com.zaxxer.hikari.HikariConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTest {

    @Test
    @DisplayName("Should parse Render postgres:// DATABASE_URL into valid JDBC URL and credentials")
    void testRenderDatabaseUrlParsing() {
        String renderUrl = "postgres://test_user:secret_pass@dpg-abc123-a.oregon-postgres.render.com:5432/hostel_db";

        HikariConfig config = DatabaseConfig.parsePostgreSqlUrl(renderUrl);

        assertNotNull(config);
        assertEquals("jdbc:postgresql://dpg-abc123-a.oregon-postgres.render.com:5432/hostel_db", config.getJdbcUrl());
        assertEquals("test_user", config.getUsername());
        assertEquals("secret_pass", config.getPassword());
        assertEquals("org.postgresql.Driver", config.getDriverClassName());
        assertEquals(5, config.getMaximumPoolSize());
        assertEquals(1, config.getMinimumIdle());
    }

    @Test
    @DisplayName("Should preserve query parameters such as sslmode=require in Render PostgreSQL URL")
    void testRenderDatabaseUrlWithSslMode() {
        String renderUrl = "postgresql://admin:mypassword@db.render.internal:5432/prod_db?sslmode=require";

        HikariConfig config = DatabaseConfig.parsePostgreSqlUrl(renderUrl);

        assertNotNull(config);
        assertEquals("jdbc:postgresql://db.render.internal:5432/prod_db?sslmode=require", config.getJdbcUrl());
        assertEquals("admin", config.getUsername());
        assertEquals("mypassword", config.getPassword());
        assertEquals("org.postgresql.Driver", config.getDriverClassName());
    }

    @Test
    @DisplayName("Should return null for non-postgres URLs or empty strings to allow fallback")
    void testNonPostgresUrlFallback() {
        assertNull(DatabaseConfig.parsePostgreSqlUrl(null));
        assertNull(DatabaseConfig.parsePostgreSqlUrl(""));
        assertNull(DatabaseConfig.parsePostgreSqlUrl("jdbc:mysql://localhost:3306/hostel_db"));
        assertNull(DatabaseConfig.parsePostgreSqlUrl("jdbc:h2:mem:hosteldb"));
    }
}
