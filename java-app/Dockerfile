FROM eclipse-temurin:17-jdk as build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw package -DskipTests

FROM eclipse-temurin:17-jre
VOLUME /tmp
COPY --from=build /workspace/app/target/*.jar app.jar
EXPOSE 3000
ENTRYPOINT ["java","-jar","/app.jar"]
