FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY backend/pom.xml backend/pom.xml
RUN mvn -B -pl backend dependency:go-offline
COPY backend/src backend/src
RUN mvn -B -pl backend package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S finance && adduser -S finance -G finance
COPY --from=build --chown=finance:finance /workspace/backend/target/personal-finance-backend-0.1.0-SNAPSHOT.jar app.jar
USER finance
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
