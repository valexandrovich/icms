FROM openjdk:17-jdk-slim as build
VOLUME /tmp
ARG JAR_FILE=target/importer*.jar
ADD ${JAR_FILE} importer.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/importer.jar"]
