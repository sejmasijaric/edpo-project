FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY mqtt-kafka-bridge/pom.xml mqtt-kafka-bridge/pom.xml
COPY mqtt-kafka-bridge/src mqtt-kafka-bridge/src

RUN mvn -f mqtt-kafka-bridge/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/mqtt-kafka-bridge/target/mqtt-kafka-bridge.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
