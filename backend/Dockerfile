# Multi-stage so the runtime image carries a JRE and one jar, nothing else.
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /src
COPY backend/gradle ./gradle
COPY backend/gradlew backend/settings.gradle.kts backend/build.gradle.kts backend/gradle.properties ./
COPY backend/modules ./modules
# Added long after the rest of this file, and that is the whole story: this
# Dockerfile was written at scaffold time, `adapters/` arrived with phase 1, and
# nothing connected the two. `settings.gradle.kts` includes :adapters:reverse-1999
# and :app compiles against it, so every image build since has failed — while the
# tracker recorded compose as verified, because it was, once, before the
# directory existed. A COPY list is a second copy of the module list and it drifts
# in silence; anything added beside these three needs a line here.
COPY backend/adapters ./adapters
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
