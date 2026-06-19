FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml ./
COPY adapter/pom.xml ./adapter/
COPY application/pom.xml ./application/
COPY client/pom.xml ./client/
COPY domain/pom.xml ./domain/
COPY facade/pom.xml ./facade/
COPY infrastructure/pom.xml ./infrastructure/

RUN mvn dependency:go-offline -B

COPY . .

RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/adapter/target/adapter-*.jar ./app.jar

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