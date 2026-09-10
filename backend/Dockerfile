# Build em duas etapas: compila com JDK, roda so com JRE (imagem final menor).
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -q clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render injeta PORT em runtime; application.properties ja le server.port=${PORT:8080}.
ENTRYPOINT ["java", "-jar", "app.jar"]
