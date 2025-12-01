FROM openjdk:17-jdk-slim

WORKDIR /app

# Le JAR généré par "mvn clean package"
COPY target/eventsProject-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
