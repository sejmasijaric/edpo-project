FROM eclipse-temurin:25-jdk AS builder

WORKDIR /build

COPY gradle ./gradle
COPY gradlew build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon || true

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
