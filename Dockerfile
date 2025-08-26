FROM gradle:8.14.3-jdk17@sha256:fdb8cfa611ab5667b84c450a4d35e142090417d48bb6eae308ec3f45bcc7493b AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:8.14.3-jdk17@sha256:fdb8cfa611ab5667b84c450a4d35e142090417d48bb6eae308ec3f45bcc7493b AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/main/ /app/src/main/
RUN gradle shadowJar --parallel --console=verbose

FROM amazoncorretto:21.0.8@sha256:cb7c5e363aafadf48e6423fa5bcc6d0b66fd2609fc0c578be6166497e86c2701 as runtime
WORKDIR /app

COPY --from=build /app/build/libs/unveiler-all.jar /app/unveiler.jar

ENTRYPOINT ["java", "-jar", "/app/unveiler.jar"]
