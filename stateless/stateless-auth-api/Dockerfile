FROM openjdk:17-alpine
COPY build/libs/stateful-any-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]