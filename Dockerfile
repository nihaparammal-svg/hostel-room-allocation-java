# ==============================================================================
# Multi-Stage Dockerfile for Hostel Management System (Java 21 Spring Boot)
# ==============================================================================

# Stage 1: Build JAR using Maven & Eclipse Temurin JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy Maven wrapper & POM
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Copy source code and build production package
COPY src src
RUN chmod +x ./mvnw || true
RUN apk add --no-cache maven && mvn clean package -DskipTests

# Stage 2: Production JRE Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root system user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built artifact from builder
COPY --from=builder /workspace/target/*.jar app.jar

# Expose standard application port
EXPOSE 8080

# Environment defaults (override in deployment environment)
ENV SPRING_PROFILES_ACTIVE=mysql \
    SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/hostel_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true \
    SPRING_DATASOURCE_USERNAME=root \
    SPRING_DATASOURCE_PASSWORD=

ENTRYPOINT ["java", "-jar", "app.jar"]
