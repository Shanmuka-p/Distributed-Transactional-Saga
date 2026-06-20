# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY order-service/pom.xml .
RUN mvn dependency:go-offline
COPY order-service/src ./src
RUN mvn package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY submission.json /app/submission.json
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
