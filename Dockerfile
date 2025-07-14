# --- Stage 1: Build the application using Maven ---
# Use a Maven image that includes JDK 21 as the builder
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml file first to leverage Docker's layer caching.
# Dependencies will only be re-downloaded if pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of your application's source code
COPY src ./src

# Package the application, skipping the tests
RUN mvn clean package -DskipTests

# --- Stage 2: Create the final, lightweight runtime image ---
# Use a JRE (Java Runtime Environment) image which is smaller than the full JDK
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# This ensures all Java date/time operations use the correct timezone.
ENV TZ=America/Los_Angeles

# Copy only the built JAR file from the 'build' stage into the final image
COPY --from=build /app/target/ai-agent-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# 【MODIFIED】: Set the command to run the application using the correct JAR file name.
# The JAR file is now 'app.jar' in the current working directory ('/app').
ENTRYPOINT ["java", "-jar", "app.jar"]
