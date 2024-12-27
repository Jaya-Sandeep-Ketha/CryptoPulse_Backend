# Use a lightweight JDK base image
FROM openjdk:17-jdk-slim

# Create a volume for temporary files (optional)
VOLUME /tmp

# Argument for the JAR file location
ARG JAR_FILE=target/my-springboot-app.jar

# Copy the JAR file into the container
COPY ${JAR_FILE} app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]
