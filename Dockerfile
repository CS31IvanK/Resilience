FROM openjdk:17-jdk-slim

# Робоча директорія
WORKDIR /

# Копіюємо проект
COPY . .

# Запускаємо додаток
CMD ["java", "-jar", "target/diplom-0.0.1-SNAPSHOT.jar"]