FROM openjdk:17-jdk-slim as build
VOLUME /tmp
ARG JAR_FILE=target/scheduler*.jar
ADD ${JAR_FILE} scheduler.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/scheduler.jar"]
