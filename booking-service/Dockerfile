FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY ./booking-service/pom.xml ./booking-service/pom.xml
COPY ./booking-service/src ./booking-service/src
WORKDIR /app/booking-service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=build /app/booking-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
