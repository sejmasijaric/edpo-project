FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY shared-kafka-support/pom.xml shared-kafka-support/pom.xml
COPY shared-kafka-support/src shared-kafka-support/src
COPY manufacturing-service/pom.xml manufacturing-service/pom.xml
COPY manufacturing-service/src manufacturing-service/src

RUN mvn -f shared-kafka-support/pom.xml -DskipTests install
RUN mvn -f manufacturing-service/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/manufacturing-service/target/manufacturing-service.jar app.jar

EXPOSE 8104

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
