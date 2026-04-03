FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY shared-kafka-support/pom.xml shared-kafka-support/pom.xml
COPY shared-kafka-support/src shared-kafka-support/src
COPY order-orchestrator/pom.xml order-orchestrator/pom.xml
COPY order-orchestrator/src order-orchestrator/src

RUN mvn -f shared-kafka-support/pom.xml -DskipTests install
RUN mvn -f order-orchestrator/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/order-orchestrator/target/order-orchestrator.jar app.jar

EXPOSE 8101

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
