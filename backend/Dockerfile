# syntax=docker/dockerfile:1
# Multi-stage so the runtime image carries a JRE and one application, nothing else.
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

# Start fast on a starved CPU, because the host stops waiting.
#
# Render's free instance is 0.1 CPU, and on 2026-09-24 a deploy failed with no
# error at all: the application took 147 s to start and Render gave up about
# thirty seconds before Tomcat bound its port, keeping the old instance. The
# previous deploy had fitted inside the same window by a margin nobody had
# measured. Measured afterwards at --cpus 0.1 --memory 512m against a live
# schema: the fat jar 175.7 s; the jar extracted to a plain classpath 133.6 s;
# extracted plus a class-data-sharing archive 65.4 s.
#
# So the jar is extracted — Boot's own `extract`, which leaves a thin jar whose
# manifest names lib/ — and a training run records every class the application
# loads into /app/storm-almanac.jsa, which the JVM then maps instead of loading.
# Nothing about the running code changes; only how its classes reach memory.
#
# The training run refreshes the context and exits before anything is served.
# `--network=none` is on purpose: it proves the archive needs no database, so
# the build can never come to depend on one. Flyway and Hibernate's schema
# validation are the only things that reach for it at refresh, and they are
# turned off *for this run only* — they still run at real startup, from classes
# that simply are not in the archive. If the archive is ever unusable (a
# different JDK, a moved path), the JVM prints a warning and starts the slow
# way; it does not fail.
#
# Not done, deliberately: -XX:TieredStopAtLevel=1 took startup to 29.4 s with
# the archive, but it trades away peak speed, and the solver works to a
# two-second budget (ADR 0010). Measure the solver under it before adding it.
COPY --from=build /src/app/build/libs/storm-almanac.jar /tmp/storm-almanac.jar
RUN java -Djarmode=tools -jar /tmp/storm-almanac.jar extract --destination /app/application \
    && rm /tmp/storm-almanac.jar
RUN --network=none java -XX:ArchiveClassesAtExit=/app/storm-almanac.jsa \
      -Dspring.context.exit=onRefresh \
      -Dspring.flyway.enabled=false \
      -Dspring.jpa.hibernate.ddl-auto=none \
      -Dspring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false \
      -Dspring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect \
      -jar /app/application/storm-almanac.jar

USER almanac
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:SharedArchiveFile=/app/storm-almanac.jsa", "-jar", "/app/application/storm-almanac.jar"]
