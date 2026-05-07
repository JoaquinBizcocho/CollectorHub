# Etapa 1: Construcción (Build)
# Usamos una imagen que ya tiene Maven y Java 21 instalados
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos tu pom.xml y el código fuente
COPY pom.xml .
COPY src ./src

# Compilamos el proyecto y generamos el archivo .jar
# Usamos -DskipTests para evitar que falle intentando conectarse a la base de datos durante la compilación
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Producción)
# Usamos una imagen mucho más ligera que solo tiene Java 21 (sin Maven)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiamos el .jar generado en la etapa 1 a esta nueva imagen
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto 8080 (el estándar de Spring Boot)
EXPOSE 8080

# Este es el equivalente al botón "Play" de IntelliJ
ENTRYPOINT ["java", "-jar", "app.jar"]