# ---- Dockerfile (production) ----
# Clean, portable build. Relies only on normal Docker layer caching.
# No BuildKit cache mounts, so it behaves the same in CI and locally.

# ---- build stage ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy Gradle wrapper FIRST so layers cache well
COPY gradlew gradlew.bat ./
COPY gradle/wrapper/ ./gradle/wrapper/

# Build scripts (dependency graph inputs)
COPY settings.gradle.kts build.gradle.kts ./

# Prime Gradle dependency resolution.
# This layer stays cached as long as build scripts do not change.
RUN chmod +x gradlew && ./gradlew --no-daemon -x test help

# Sources (change most often)
COPY src ./src

# Build the Spring Boot executable jar (bootJar).
# Gradle config sets bootJar archiveFileName to app.jar.
RUN ./gradlew --no-daemon -x test clean bootJar

# ---- runtime stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy the single boot jar output (no wildcards).
COPY --from=build /app/build/libs/app.jar app.jar

# must start java at entry point so it runs on PID 1, ECS send SIGTERM cleanly this way.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
