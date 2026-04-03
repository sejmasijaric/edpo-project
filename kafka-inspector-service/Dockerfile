FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY shared-kafka-support/pom.xml shared-kafka-support/pom.xml
COPY shared-kafka-support/src shared-kafka-support/src
COPY kafka-inspector-service/pom.xml kafka-inspector-service/pom.xml
COPY kafka-inspector-service/src kafka-inspector-service/src

RUN mvn -f shared-kafka-support/pom.xml -DskipTests install
RUN mvn -f kafka-inspector-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/kafka-inspector-service/target/kafka-inspector-service.jar app.jar

EXPOSE 8102

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
