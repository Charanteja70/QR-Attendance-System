FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN chmod +x mvnw
RUN ./mvnw -DskipTests package

EXPOSE 8080

CMD ["java", "-jar", "target/qr-attendance-system-0.0.1-SNAPSHOT.jar"]