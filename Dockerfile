# ──────────────────────────────────────────────
# Stage 1 – Build the JAR with Maven
# ──────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy dependency manifests first for better layer caching
COPY pom.xml .
# Download dependencies (cached unless pom.xml changes)
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B

# Copy source and build the fat JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ──────────────────────────────────────────────
# Stage 2 – Lean runtime image
# ──────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

# Add a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy only the built JAR from the builder stage
COPY --from=builder /app/target/mood-journal-*.jar app.jar

# Switch to non-root user
USER appuser

# Render exposes the PORT env var; Spring Boot reads it via ${PORT:8080}
EXPOSE 8080

# JVM tuning for container environments:
#   -XX:+UseContainerSupport  → respects cgroup memory limits
#   -XX:MaxRAMPercentage=75   → use at most 75% of container RAM for heap
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
