FROM eclipse-temurin:17-jdk as build
WORKDIR /workspace/app

# Install Maven directly instead of using wrapper
RUN apt-get update && apt-get install -y maven

# Copy pom.xml and source code
COPY pom.xml .
COPY src src

# Build the application using installed Maven
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre
VOLUME /tmp
COPY --from=build /workspace/app/target/*.jar app.jar
EXPOSE 3000
ENTRYPOINT ["java","-jar","/app.jar"]
