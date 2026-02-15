# syntax=docker/dockerfile:1.7

# ---- Dockerfile (production) ----
# Clean, portable build. Relies only on normal Docker layer caching.
# No BuildKit cache mounts, so it behaves the same in CI and locally.

ARG BUILD_IMAGE=eclipse-temurin:25-jdk
ARG RUNTIME_IMAGE=eclipse-temurin:25-jre

# ---- build stage ----
FROM ${BUILD_IMAGE} AS build
WORKDIR /app

# arguments passed in
ARG CODEARTIFACT_AUTH_TOKEN
ARG CODEARTIFACT_ENDPOINT

# Get DTO from pointed repo
ENV DISABLE_LOCAL_DTO=true
ENV CODEARTIFACT_AUTH_TOKEN=${CODEARTIFACT_AUTH_TOKEN}
ENV CODEARTIFACT_ENDPOINT=${CODEARTIFACT_ENDPOINT}

# Copy Gradle wrapper FIRST so layers cache well
COPY gradlew gradlew.bat ./
COPY gradle/wrapper/ ./gradle/wrapper/

# Build scripts (dependency graph inputs)
COPY settings.gradle.kts build.gradle.kts ./

# Prime Gradle dependency resolution.
# This layer stays cached as long as build scripts do not change.
# mounts
RUN --mount=type=cache,target=/root/.gradle \
    chmod +x gradlew && ./gradlew --no-daemon -x test help

# Sources (change most often)
COPY src ./src

# Build the Spring Boot executable jar (bootJar).
# Gradle config sets bootJar archiveFileName to app.jar.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon -x test clean bootJar

# ---- runtime stage ----
FROM ${RUNTIME_IMAGE}
WORKDIR /app

# Copy the single boot jar output (no wildcards).
COPY --from=build /app/build/libs/app.jar app.jar

# must start java at entry point so it runs on PID 1, ECS send SIGTERM cleanly this way.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
