FROM eclipse-temurin:25-jre
WORKDIR /app
COPY model-city-gateway/target/*.jar app.jar
EXPOSE 8762
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
