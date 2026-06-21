# ── Build aşaması ────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon 2>/dev/null || true

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ── Runtime aşaması ──────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN addgroup -S bursio && adduser -S bursio -G bursio

COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown bursio:bursio app.jar

USER bursio

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
