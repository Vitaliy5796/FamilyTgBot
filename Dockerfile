# ---------- 1. Сборка (JDK для компиляции) ----------
FROM amazoncorretto:17-alpine AS builder

# Устанавливаем Maven (Alpine не имеет его по умолчанию)
RUN apk add --no-cache maven

WORKDIR /app

# Копируем pom.xml для кэширования зависимостей
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходники
COPY src ./src

# Собираем JAR (skip тесты для скорости)
RUN mvn package -DskipTests -B

# ---------- 2. Запуск (тот же JDK, но как runtime) ----------
FROM amazoncorretto:17-alpine

WORKDIR /app

# Копируем готовый JAR
COPY --from=builder /app/target/*.jar app.jar

# Опционально: Настраиваем JVM для контейнеров (важно для VPS)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]