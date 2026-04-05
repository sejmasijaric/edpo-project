FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY shared-kafka-support/pom.xml shared-kafka-support/pom.xml
COPY shared-kafka-support/src shared-kafka-support/src
COPY mqtt-kafka-bridge/pom.xml mqtt-kafka-bridge/pom.xml
COPY mqtt-kafka-bridge/src mqtt-kafka-bridge/src
COPY vacuum-gripper-integration-service/pom.xml vacuum-gripper-integration-service/pom.xml
COPY vacuum-gripper-integration-service/src vacuum-gripper-integration-service/src

RUN mvn -f shared-kafka-support/pom.xml -DskipTests install
RUN mvn -f mqtt-kafka-bridge/pom.xml -DskipTests install
RUN mvn -f vacuum-gripper-integration-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/vacuum-gripper-integration-service/target/vacuum-gripper-integration-service.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
