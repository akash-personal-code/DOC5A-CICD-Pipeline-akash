FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradlew gradlew.bat settings.gradle build.gradle lombok.config ./
COPY gradle ./gradle
COPY src ./src
RUN chmod +x ./gradlew && ./gradlew clean bootJar -x test -x jacocoTestCoverageVerification --no-daemon

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S novapay && adduser -S novapay -G novapay
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENV SERVER_PORT=8080
EXPOSE 8080
USER novapay:novapay
ENTRYPOINT ["java", "-jar", "app.jar"]
