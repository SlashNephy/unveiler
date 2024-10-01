FROM gradle:8.10.2-jdk17@sha256:33477ba5b65634f1aca6e236bff56c9fd3ff9515baa12773a08ca2f5ccd6805e AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:8.10.2-jdk17@sha256:33477ba5b65634f1aca6e236bff56c9fd3ff9515baa12773a08ca2f5ccd6805e AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/main/ /app/src/main/
RUN gradle shadowJar --parallel --console=verbose

FROM amazoncorretto:18.0.2@sha256:1128cff77f7fb4512215a4ded2bf0a6ec3cd2bf0f414a72136b1bb1d5f6b0518 as runtime
WORKDIR /app

COPY --from=build /app/build/libs/unveiler-all.jar /app/unveiler.jar

ENTRYPOINT ["java", "-jar", "/app/unveiler.jar"]
