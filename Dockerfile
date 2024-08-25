FROM amazoncorretto:17

WORKDIR /app

COPY target/Oauth2-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java","-jar","/app/app.jar"]