FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY dashboard-service/pom.xml ./dashboard-service/pom.xml
WORKDIR /workspace/dashboard-service
RUN mvn -B dependency:go-offline
WORKDIR /workspace
COPY dashboard-service ./dashboard-service
WORKDIR /workspace/dashboard-service
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/dashboard-service/target/dashboard-service.jar app.jar
EXPOSE 8105
ENTRYPOINT ["java", "-jar", "app.jar"]
