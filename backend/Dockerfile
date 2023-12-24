FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . /app/.
RUN ./gradlew bootJar
EXPOSE 8081
ENTRYPOINT ["./gradlew", "bootRun"]
