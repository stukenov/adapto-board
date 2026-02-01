# Build stage
FROM gradle:8.10-jdk17 AS build
WORKDIR /app

# Copy gradle files first for better caching
COPY settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Copy source code
COPY libs ./libs
COPY apps/server ./apps/server

# Build the application
RUN gradle :apps:server:installDist --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built application
COPY --from=build /app/apps/server/build/install/server ./

# Create storage directory
RUN mkdir -p /app/storage

EXPOSE 8080

ENTRYPOINT ["./bin/server"]
