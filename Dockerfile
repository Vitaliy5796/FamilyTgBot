# ---------- Сборка ----------
FROM amazoncorretto:17 AS builder

WORKDIR /app

# Устанавливаем Maven (в Corretto на Debian он не предустановлен)
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Кэшируем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем код и собираем
COPY src ./src
RUN mvn package -DskipTests -B

# ---------- Запуск ----------
FROM amazoncorretto:17

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-Xms256m", "-Xmx1024m", "-jar", "app.jar"]