# Use a lightweight JDK base image
FROM openjdk:17-jdk-slim

# Create a directory for the application
WORKDIR /app

# Argument for the JAR file location
ARG JAR_FILE

# Copy the JAR file into the container
COPY ${JAR_FILE} app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]
