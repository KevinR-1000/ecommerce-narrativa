# ---- Etapa de Construcción (Build Stage) ----
# Usamos una imagen oficial de Maven con Java 17 para compilar nuestro proyecto.
FROM maven:3.8.5-openjdk-17 AS build

# Establecemos el directorio de trabajo dentro de la imagen.
WORKDIR /app

# Copiamos primero el archivo pom.xml para optimizar el caché de dependencias.
COPY pom.xml .

# Descargamos todas las dependencias usando el 'mvn' que viene en la imagen.
RUN mvn dependency:go-offline

# Ahora que las dependencias están listas, copiamos el resto del código fuente.
COPY src/ /app/src/

# Compilamos la aplicación usando el 'mvn' de la imagen.
RUN mvn package -DskipTests


# ---- Etapa de Ejecución (Run Stage) ----
# Esta parte se mantiene igual, es correcta.
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/mvcDemo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]