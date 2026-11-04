# syntax=docker/dockerfile:1

# ---- Build stage: compile + test + package with Maven on JDK 21 ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache dependencies first: this layer only changes when pom.xml does.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Tests run inside the image build, so a broken test can never ship.
RUN mvn -B -q verify -DskipTests=false

# ---- Runtime stage: slim JRE, non-root, just the fat jar ----
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Cloud Run sends SIGTERM; the JVM should shut down gracefully and fast.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/target/*.jar app.jar
USER app

# Cloud Run injects PORT (default 8080); Spring reads it via application.properties.
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
