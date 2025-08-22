# Use a multi-stage build to create a small, secure, and production-ready Docker image.

# Stage 1: Build the application
# Use a more stable Maven image tag that is less likely to be deprecated.
FROM maven:3-openjdk-17-slim AS builder

# Set the working directory inside the container.
WORKDIR /app

# Copy the pom.xml and download dependencies. This helps with caching.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the application source code.
COPY src ./src

# Build the Spring Boot application and create the executable JAR.
# The 'package' goal will also run the tests.
RUN mvn -B clean package -DskipTests

# Stage 2: Create the final, lightweight runtime image
# Use a minimalist OpenJDK runtime image to reduce the image size.
FROM openjdk:17-slim

# Set the working directory.
WORKDIR /app

# Expose the port that your Spring Boot application runs on.
EXPOSE 8080

# Copy the executable JAR from the 'builder' stage.
# The `*.jar` will match the JAR created by the Maven build.
COPY --from=builder /app/target/*.jar ./app.jar

# Define the entrypoint command to run the JAR file.
# The `java` command will start your Spring Boot application.
ENTRYPOINT ["java", "-jar", "app.jar"]

# This is an optional but good practice to define a non-root user for security.
# USER 1001
