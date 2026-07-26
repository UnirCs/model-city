FROM eclipse-temurin:25-jre
WORKDIR /app
COPY model-city-service-registry/target/*.jar app.jar
EXPOSE 8761
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
