FROM gradle:9.1-jdk25 AS build
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . /home/gradle/project

RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar"]