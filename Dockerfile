# Use Eclipse Temurin JDK 21 as the base image
FROM eclipse-temurin:21-jdk AS runtime

# Set working directory
WORKDIR /app

# Copy the built Spring Boot JAR from Maven target directory
COPY target/*.jar app.jar

# Expose application port
EXPOSE 8080

# Run Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
