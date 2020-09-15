FROM maven:3.5-jdk-8-alpine as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn package -DskipTests

FROM adoptopenjdk/openjdk8:jdk8u202-b08-alpine-slim

COPY --from=builder /app/target/gateway-workflow-*.war /gateway-workflow.war

CMD ["java", "-Dspring.profiles.active=${ENV}", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/gateway-workflow.war"]