FROM openjdk:17-jdk-slim as build
VOLUME /tmp
ARG JAR_FILE=target/downloader*.jar
ADD ${JAR_FILE} downloader.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/downloader.jar"]
