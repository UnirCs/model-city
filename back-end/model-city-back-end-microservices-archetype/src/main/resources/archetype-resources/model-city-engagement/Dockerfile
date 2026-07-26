FROM eclipse-temurin:25-jre
WORKDIR /app
COPY model-city-engagement/target/*.jar app.jar
EXPOSE 8091
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
