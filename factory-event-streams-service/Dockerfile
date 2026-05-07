FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY factory-event-streams-service/pom.xml factory-event-streams-service/pom.xml
COPY factory-event-streams-service/src factory-event-streams-service/src

RUN mvn -f factory-event-streams-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/factory-event-streams-service/target/factory-event-streams-service.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
