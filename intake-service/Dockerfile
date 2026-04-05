FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY shared-kafka-support/pom.xml shared-kafka-support/pom.xml
COPY shared-kafka-support/src shared-kafka-support/src
COPY intake-service/pom.xml intake-service/pom.xml
COPY intake-service/src intake-service/src

RUN mvn -f shared-kafka-support/pom.xml -DskipTests install
RUN mvn -f intake-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/intake-service/target/intake-service.jar app.jar

EXPOSE 8103

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
