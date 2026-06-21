FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copiar todos los archivos del proyecto al contenedor
COPY . .

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Construir el proyecto omitiendo los tests para mayor velocidad
RUN ./mvnw clean package -DskipTests

# Exponer el puerto que usa Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]
