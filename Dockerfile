# ---- Etapa de Construcción (Build Stage) ----
# Usamos una imagen oficial de Maven con Java 17 para compilar nuestro proyecto.
# Maven es la herramienta que lee tu pom.xml y construye la aplicación.
FROM maven:3.8.5-openjdk-17 AS build

# Establecemos el directorio de trabajo dentro de la imagen de construcción.
# Todos los comandos siguientes se ejecutarán desde /app.
WORKDIR /app

# Copiamos primero el archivo pom.xml.
# Esto es un truco de optimización: si las dependencias no cambian, Docker reutilizará la capa de dependencias descargadas.
COPY pom.xml .

# Copiamos el wrapper de Maven, que asegura que se use la versión correcta de Maven.
COPY .mvn/ .mvn
COPY mvnw .

# Descargamos todas las dependencias definidas en el pom.xml.
# 'go-offline' evita que se intente conectar a internet durante la fase de compilación real.
RUN mvn dependency:go-offline

# Ahora que las dependencias están listas, copiamos el resto del código fuente.
COPY src/ /app/src/

# Compilamos la aplicación. 'package' crea el archivo .jar ejecutable.
# '-DskipTests' omite la ejecución de pruebas para acelerar el despliegue.
RUN mvn package -DskipTests


# ---- Etapa de Ejecución (Run Stage) ----
# Ahora cambiamos a una imagen mucho más ligera que solo contiene Java para ejecutar la aplicación.
# Esto hace que la imagen final sea más pequeña y segura.
FROM eclipse-temurin:17-jre-jammy

# Establecemos el directorio de trabajo para la ejecución.
WORKDIR /app

# Copiamos el archivo .jar que se creó en la etapa de construcción ('build').
# Lo copiamos desde /app/target/ del contenedor 'build' al contenedor actual.
# Le cambiamos el nombre a 'app.jar' para que sea más fácil de referenciar.
# ¡ATENCIÓN! Asegúrate de que el nombre 'mvcDemo-0.0.1-SNAPSHOT.jar' coincide exactamente
# con el que se genera en tu carpeta 'target' local.
COPY --from=build /app/target/mvcDemo-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto en el que Render espera que nuestra aplicación escuche.
# Spring Boot por defecto usa 8080, pero Render lo mapeará a este puerto 10000.
EXPOSE 10000

# Este es el comando final que se ejecutará cuando el contenedor se inicie.
# Es el equivalente a escribir 'java -jar app.jar' en la terminal.
ENTRYPOINT ["java", "-jar", "app.jar"]