FROM docker.io/library/maven:3.9-eclipse-temurin-21 as build

COPY . /
RUN mvn clean package

FROM docker.io/library/eclipse-temurin:21-jre

COPY --from=build target/java-connect-proxy-0-jar-with-dependencies.jar java-connect-proxy-0-jar-with-dependencies.jar

ENTRYPOINT ["/opt/java/openjdk/bin/java", "-jar", "java-connect-proxy-0-jar-with-dependencies.jar", "8080"]



