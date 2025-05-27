# Étape 1 : Image avec Maven + JDK
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copier le projet
COPY . .

# Construire le projet (sans les tests)
RUN mvn clean package -DskipTests

# Étape 2 : Image finale plus légère
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copier le .jar depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port attendu par Render
EXPOSE 8080

# Lancer l'application
CMD ["java", "-jar", "app.jar"]