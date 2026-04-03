FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY shared-kafka-support/pom.xml shared-kafka-support/pom.xml
COPY shared-kafka-support/src shared-kafka-support/src
COPY qc-service/pom.xml qc-service/pom.xml
COPY qc-service/src qc-service/src

RUN mvn -f shared-kafka-support/pom.xml -DskipTests install
RUN mvn -f qc-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/qc-service/target/qc-service.jar app.jar

EXPOSE 8100

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
