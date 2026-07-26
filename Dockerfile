# Etapa build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -e dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# Etapa runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/educore-1.0-SNAPSHOT.jar app.jar
CMD ["java", "-Dapi.port=8080", "-jar", "app.jar"]
