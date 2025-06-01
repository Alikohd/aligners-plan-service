FROM maven:3.8.6-amazoncorretto-17 AS build
COPY pom.xml /build/
WORKDIR /build/
RUN mvn dependency:go-offline
COPY src /build/src/
RUN mvn package -DskipTests

FROM bellsoft/liberica-openjre-alpine-musl:17.0.13-12
ARG JAR_FILE=/build/target/*.jar
COPY --from=build $JAR_FILE /opt/treatment-orchestrator/app.jar
ENTRYPOINT ["java", "-jar", "/opt/treatment-orchestrator/app.jar", "--spring.profiles.active=dev"]
