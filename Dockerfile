# ----------------------------------------------------------------------------------
# Stage 1: Build the Spring Boot application using Maven
# ----------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory inside the container
WORKDIR /app

# Copy only pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .

# Copy the entire source folder
COPY src ./src

# Build the Spring Boot JAR without running tests
RUN mvn clean package -DskipTests

# ----------------------------------------------------------------------------------
# Stage 2: Runtime using a lightweight JRE
# ----------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

# Set working directory for runtime
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Default entrypoint to run the Spring Boot app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
