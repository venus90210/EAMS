# ── Stage 1: Build ─────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Descargar dependencias primero (cache de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Compilar omitiendo pruebas (las pruebas corren en CI, no en el build de imagen)
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Usuario no root por seguridad (RNF04)
RUN addgroup -S eams && adduser -S eams -G eams
USER eams

COPY --from=build /app/target/eams-backend-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
