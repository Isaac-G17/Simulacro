# ---------- Etapa 1: build (imagen pesada con Maven + JDK) ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copiar primero el pom aprovecha la caché de capas: las dependencias solo se descargan si cambia el pom
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# ---------- Etapa 2: runtime (imagen liviana, solo JRE) ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
USER app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
