# 1. FÁZIS: Buildelés (Maven és JDK 21)
FROM maven:3.9.8-eclipse-temurin-21 AS builder
LABEL authors="csigi"

WORKDIR /app

# Másoljuk a POM-ot és töltsük le a függőségeket (cache-elés)
COPY pom.xml .
RUN mvn dependency:go-offline

# Másoljuk a forráskódot és buildeljük az alkalmazást
COPY src ./src
RUN mvn package -DskipTests

# 2. FÁZIS: Futtatás (Csak JRE 21)
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Másoljuk át a buildelt .jar fájlt a builder fázisból
COPY --from=builder /app/target/*.jar app.jar

# Exponáljuk a portot
EXPOSE 8080

# Indítsuk az alkalmazást
ENTRYPOINT ["java", "-jar", "app.jar"]