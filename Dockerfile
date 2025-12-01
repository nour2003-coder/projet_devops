FROM openjdk:17-jdk-slim

WORKDIR /app

# Le JAR généré par "mvn clean package"
# (d’après ton pom.xml: artifactId=eventsProject, version=1.0.0)
COPY target/eventsProject-1.0.0.jar app.jar

EXPOSE 8089

ENTRYPOINT ["java", "-jar", "app.jar"]
