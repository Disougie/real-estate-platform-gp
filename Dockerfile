FROM eclipse-temurin:21-jre-alpine
ADD target/app.jar app.jar
ENTRYPOINT ["java","-Xms128m","-Xmx256m","-jar","/app.jar"]