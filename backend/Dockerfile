# ==============================================================
# Stage 1 - Build: compila el proyecto con Maven
# ==============================================================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar el descriptor de dependencias primero para aprovechar
# la caché de capas de Docker (solo se re-descarga si cambia pom.xml)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar el JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==============================================================
# Stage 2 - Runtime: imagen mínima solo con el JAR compilado
# ==============================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario no-root por seguridad
RUN addgroup -S uniway && adduser -S uniway -G uniway

# Copiar el JAR desde la etapa de build
COPY --from=build /app/target/uniway-backend-0.0.1-SNAPSHOT.jar app.jar

# Cambiar al usuario no-root
USER uniway

# Puerto expuesto por Spring Boot
EXPOSE 8080

# Variables de entorno con valores por defecto (sobreescribir en producción)
ENV SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/uniway_db \
    SPRING_DATASOURCE_USERNAME=root \
    SPRING_DATASOURCE_PASSWORD= \
    MAIL_USERNAME= \
    MAIL_PASSWORD= \
    JWT_SECRET=mySecretKey123456789012345678901234567890

# Opciones de la JVM: límite de memoria y zona horaria
ENTRYPOINT ["java", \
  "-Xms256m", "-Xmx512m", \
  "-Duser.timezone=America/Bogota", \
  "-jar", "app.jar"]
