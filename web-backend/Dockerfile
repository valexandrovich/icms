FROM openjdk:17-jdk-slim as build
VOLUME /tmp
ARG JAR_FILE=target/web-backend*.jar
ADD ${JAR_FILE} web-backend.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/web-backend.jar"]
