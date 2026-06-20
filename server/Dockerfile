FROM docker.1ms.run/library/eclipse-temurin:21-jre

WORKDIR /app

COPY adapter/target/*.jar ./app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=dev \
    DB_HOST=localhost \
    DB_PORT=3306 \
    DB_NAME=agent_ops \
    DB_USER=root \
    DB_PASS=123456 \
    REDIS_HOST=localhost \
    REDIS_PORT=6379

ENTRYPOINT ["java", "-jar", "app.jar"]