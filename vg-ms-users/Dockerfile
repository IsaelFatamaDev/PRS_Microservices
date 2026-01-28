FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY target/vg-ms-users-1.0.0.jar app.jar

EXPOSE 8081

ENV SPRING_PROFILE=prod
ENV PORT=8081

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=${SPRING_PROFILE}"]
