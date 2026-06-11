FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY gradlew gradlew.bat settings.gradle build.gradle lombok.config ./
COPY gradle ./gradle
COPY src ./src
RUN chmod +x ./gradlew && ./gradlew clean bootJar -x test -x jacocoTestCoverageVerification --no-daemon

# Distroless runtime reduces OS package surface area and avoids Alpine/JDK package CVE noise.
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app
COPY --from=builder --chown=nonroot:nonroot /app/build/libs/*.jar /app/app.jar
ENV SERVER_PORT=8080
EXPOSE 8080
USER nonroot:nonroot
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
