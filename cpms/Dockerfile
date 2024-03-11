FROM openjdk:17-jdk-slim as build
VOLUME /tmp
ARG JAR_FILE=target/cpms*.jar
ADD ${JAR_FILE} cpms.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/cpms.jar"]
