# ---- Сборка: Maven + JDK 17 ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn -B -q package -DskipTests \
 && mvn -B -q dependency:copy-dependencies -DoutputDirectory=target/lib -DincludeScope=runtime

# ---- Рантайм: только JRE ----
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/classes ./classes
COPY --from=build /app/target/lib ./lib

# Приложение поднимает H2 in-memory, создаёт схему, данные и отрабатывает все секции
CMD ["java", "-cp", "classes:lib/*", "org.example.bookshop.Main"]
