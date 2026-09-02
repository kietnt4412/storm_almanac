# Multi-stage so the runtime image carries a JRE and one jar, nothing else.
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /src
COPY backend/gradle ./gradle
COPY backend/gradlew backend/settings.gradle.kts backend/build.gradle.kts backend/gradle.properties ./
COPY backend/modules ./modules
COPY backend/substrate ./substrate
COPY backend/app ./app
RUN ./gradlew --no-daemon :app:bootJar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S almanac && adduser -S almanac -G almanac
COPY --from=build /src/app/build/libs/storm-almanac.jar ./storm-almanac.jar
USER almanac
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/storm-almanac.jar"]
