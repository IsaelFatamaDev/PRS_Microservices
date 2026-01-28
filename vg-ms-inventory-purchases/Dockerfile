FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY ./mvnw ./
COPY ./pom.xml  ./
COPY ./.mvn ./.mvn
RUN chmod +x ./mvnw
RUN ./mvnw clean package -Dmaven.test.skip -Dmaven.main.skip -Dspring-boot.repackage.skip && rm -r ./target/
COPY ./src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -g 1028 devopsc && \
    adduser -D -G devopsc admin
VOLUME /tmp
COPY --from=builder /app/target/*.jar /app/app.jar
RUN chown -R admin:devopsc /app
USER admin

EXPOSE 8088

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8088/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
