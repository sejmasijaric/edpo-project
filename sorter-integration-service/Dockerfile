FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY mqtt-kafka-bridge/pom.xml mqtt-kafka-bridge/pom.xml
COPY mqtt-kafka-bridge/src mqtt-kafka-bridge/src
COPY sorter-integration-service/pom.xml sorter-integration-service/pom.xml
COPY sorter-integration-service/src sorter-integration-service/src

RUN mvn -f mqtt-kafka-bridge/pom.xml -DskipTests install
RUN mvn -f sorter-integration-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/sorter-integration-service/target/sorter-integration-service.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
