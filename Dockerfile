# ==============================================================================
# Multi-Stage Dockerfile for Hostel Management System (Java 21 Spring Boot)
# Optimized for Render Web Service + Render PostgreSQL
# ==============================================================================

# Stage 1: Build JAR using Maven & Eclipse Temurin JDK 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy POM and pre-fetch dependencies to optimize Docker caching
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy application source code and compile production package
COPY src src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Production JRE Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create secure non-root system user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built executable JAR from builder stage
COPY --from=builder /workspace/target/*.jar app.jar

# Expose standard application port (Render routes dynamic PORT automatically)
EXPOSE 8080

# Environment defaults (override in Render Environment settings)
ENV SPRING_PROFILES_ACTIVE=postgres \
    PORT=8080

# Execute Spring Boot with container-aware memory allocation (critical for Render 512MB RAM free tier)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
