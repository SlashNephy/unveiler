FROM gradle:8.3.0-jdk17@sha256:5f4ab273b15961c5f22969136ea884ca0343f1d8b2df5c4c6fe0ca8939b401b1 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:8.3.0-jdk17@sha256:5f4ab273b15961c5f22969136ea884ca0343f1d8b2df5c4c6fe0ca8939b401b1 AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/main/ /app/src/main/
RUN gradle shadowJar --parallel --console=verbose

FROM amazoncorretto:18.0.2@sha256:1128cff77f7fb4512215a4ded2bf0a6ec3cd2bf0f414a72136b1bb1d5f6b0518 as runtime
WORKDIR /app

COPY --from=build /app/build/libs/unveiler-all.jar /app/unveiler.jar

ENTRYPOINT ["java", "-jar", "/app/unveiler.jar"]
