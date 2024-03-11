FROM openjdk:17-jdk-slim as build
VOLUME /tmp
ARG JAR_FILE=target/migrations*.jar
ADD ${JAR_FILE} migrations.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/migrations.jar"]
