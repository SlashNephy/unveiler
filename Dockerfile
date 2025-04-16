FROM gradle:8.13.0-jdk17@sha256:10a7534228da8a75c6e77904a5d3c857f92070e842e4bef3f9fe89792d0d3f50 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:8.13.0-jdk17@sha256:10a7534228da8a75c6e77904a5d3c857f92070e842e4bef3f9fe89792d0d3f50 AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/main/ /app/src/main/
RUN gradle shadowJar --parallel --console=verbose

FROM amazoncorretto:21.0.7@sha256:674d24b818b8e889fb530a98c250f8638f14116f34eddbe0191f74e4dbc12aa0 as runtime
WORKDIR /app

COPY --from=build /app/build/libs/unveiler-all.jar /app/unveiler.jar

ENTRYPOINT ["java", "-jar", "/app/unveiler.jar"]
