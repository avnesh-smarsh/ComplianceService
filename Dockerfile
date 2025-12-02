# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy the built JAR (wildcard is OK if there's only one jar in target/)
COPY --from=build /app/target/*.jar app.jar
# Default Spring Boot port (overridden via SERVER_PORT env)
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
