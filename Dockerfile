FROM gradle:7.6.1-jdk17@sha256:80f1f49b69b2d94d1bc2c63a9ad8268c3c06129b224f4b2577fb944ae26e1226 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:7.6.1-jdk17@sha256:80f1f49b69b2d94d1bc2c63a9ad8268c3c06129b224f4b2577fb944ae26e1226 AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/main/ /app/src/main/
RUN gradle shadowJar --parallel --console=verbose

FROM amazoncorretto:18.0.2@sha256:1128cff77f7fb4512215a4ded2bf0a6ec3cd2bf0f414a72136b1bb1d5f6b0518 as runtime
WORKDIR /app

COPY --from=build /app/build/libs/unveiler-all.jar /app/unveiler.jar

ENTRYPOINT ["java", "-jar", "/app/unveiler.jar"]
