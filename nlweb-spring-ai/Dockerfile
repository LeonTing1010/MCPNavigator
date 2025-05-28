# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /app

# Copy the Maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN ./mvnw package -DskipTests

# Stage 2: Create a slimmer runtime image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the executable JAR from the builder stage
COPY --from=builder /app/target/nlweb-spring-ai-0.0.1-SNAPSHOT.jar ./app.jar

# Expose the application port
EXPOSE 8080

# Set the entrypoint for the application
ENTRYPOINT ["java", "-jar", "./app.jar"]
