# PostMaker

PostMaker - это полнофункциональное CRUD приложение для управления авторами, постами и метками с использованием консольного интерфейса.

## 📋 Описание

Приложение позволяет управлять следующими сущностями:
- **Writer** - авторы с постами
- **Post** - посты с метками и статусами
- **Label** - метки для категоризации постов
- **Status** - статусы постов (ACTIVE, DELETED)

## 🏗️ Архитектура проекта

```
postmaker/
├── gradle/
├── src/
│   ├── main/
│   │   ├── java/org/nosulkora/postmaker/
│   │   │   ├── AppRunner.java                 # Главный класс приложения
│   │   │   ├── controller/                    # Контроллеры бизнес-логики
│   │   │   │   ├── LabelController.java
│   │   │   │   ├── PostController.java
│   │   │   │   └── WriterController.java
│   │   │   ├── database/                      # Управление базой данных
│   │   │   │   ├── DatabaseManager.java       # Подключение к БД
│   │   │   │   └── LiquibaseManager.java      # Миграции базы данных
│   │   │   ├── model/                         # POJO классы сущностей
│   │   │   │   ├── Label.java
│   │   │   │   ├── Post.java
│   │   │   │   ├── Status.java               # Enum: ACTIVE, UNDER_REVIEW, DELETED
│   │   │   │   └── Writer.java
│   │   │   ├── repository/impl/               # Репозитории для работы с данными
│   │   │   │   ├── GenericRepository.java     # Базовый репозиторий
│   │   │   │   ├── LabelRepository.java       # Интерфейс репозитория меток
│   │   │   │   ├── PostRepository.java        # Интерфейс репозитория постов
│   │   │   │   ├── WriterRepository.java      # Интерфейс репозитория авторов
│   │   │   │   ├── JdbcLabelRepositoryImpl.java    # JDBC реализация для меток
│   │   │   │   ├── JdbcPostRepositoryImpl.java     # JDBC реализация для постов
│   │   │   │   ├── JdbcWriterRepositoryImpl.java   # JDBC реализация для авторов
│   │   │   │   ├── GsonLabelRepositoryImpl.java    # JSON реализация для меток
│   │   │   │   ├── GsonPostRepositoryImpl.java     # JSON реализация для постов
│   │   │   │   └── GsonWriterRepositoryImpl.java   # JSON реализация для авторов
│   │   │   └── view/                          # Консольный интерфейс
│   │   │       ├── LabelView.java
│   │   │       ├── MainView.java             # Главное меню
│   │   │       ├── PostView.java
│   │   │       └── WriterView.java
│   │   └── resources/
│   │       └── db/changelog/                 # Миграции базы данных
│   │           ├── changelog-master.xml      # Главный файл миграций
│   │           ├── 001-initial-schema.xml    # Первоначальная схема
│   │           ├── 002-test-data.xml         # Тестовые данные
│   │           └── test-changelog.xml        # Миграции для тестов
│   └── test/
│       └── java/org/nosulkora/postmaker/
│           ├── controller/                    # Тесты контроллеров
│           │   ├── LabelControllerTest.java
│           │   └── PostControllerTest.java
│           └── repository/
│               └── WriterRepositoryTest.java  # Тесты репозиториев
├── gradlew
├── gradlew.bat
└── build.gradle
```

## 🎯 Сущности

### Writer
- `id` - идентификатор
- `firstName` - имя
- `lastName` - фамилия
- `posts` - список постов

### Post
- `id` - идентификатор
- `content` - содержимое
- `created` - дата создания
- `updated` - дата обновления
- `labels` - список меток
- `status` - статус поста

### Label
- `id` - идентификатор
- `name` - название метки

### Status (Enum)
- `ACTIVE` - активный
- `UNDER_REVIEW` - на рассмотрении
- `DELETED` - удален

## 🛠️ Технологии

- **Java 24**
- **MySQL** - реляционная база данных
- **JDBC** - доступ к данным
- **Gradle** - система сборки
- **Liquibase** - управление миграциями БД
- **JUnit 5** - модульное тестирование
- **Mockito** - мокирование зависимостей
- **Gson** - работа с JSON

## 💾 Реализации репозиториев

### JDBC репозитории
- `JdbcWriterRepositoryImpl` - работа с авторами через MySQL
- `JdbcPostRepositoryImpl` - работа с постами через MySQL
- `JdbcLabelRepositoryImpl` - работа с метками через MySQL

[//]: # (В процессе доработки)
## 📥 Установка и настройка

### Предварительные требования
- Java 24
- MySQL Server 8.0+ (для JDBC реализации)
- Gradle 7.0+

### 1. Клонирование репозитория
```bash
git clone https://github.com/Javasus/postmaker.git
cd postmaker
```


### 2. Настройка базы данных (для JDBC)
Создайте базу данных в MySQL:
```sql
CREATE DATABASE postmaker;
```

### 3. Конфигурация подключения
Создайте файл `src/main/resources/application.properties`:
```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/postmaker
db.username=your_username
db.password=your_password

# Repository Type (jdbc or gson)
repository.type=jdbc

# JSON files path (for gson repository)
json.data.path=./data
```

### 4. Запуск миграций БД
```bash
./gradlew migrateDb
```

## 🚀 Запуск приложения

### Важно: Настройка кодировки
Для корректной работы с кириллицей необходимо настроить кодировку UTF-8.

#### IntelliJ IDEA:
1. **Edit Configurations** → **Modify options** → **Add VM options**
2. Добавьте VM options:
```
-Dfile.encoding=UTF-8
-Dconsole.encoding=UTF-8
-Dsun.stdout.encoding=UTF-8
-Dsun.stderr.encoding=UTF-8
```
3. **Environment variables**:
```
JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8
```

#### Запуск через Gradle:
```bash
./gradlew run
```

#### Сборка JAR:
```bash
./gradlew build
java -Dfile.encoding=UTF-8 -jar build/libs/postmaker.jar
```

## 📊 Команды Gradle

### Основные задачи
```bash
./gradlew run          # Запуск приложения
./gradlew build        # Сборка проекта
./gradlew test         # Запуск тестов
./gradlew clean        # Очистка build директории
```

### Управление базой данных
```bash
./gradlew migrateDb    # Применить миграции
./gradlew rollbackDb   # Откатить миграции
./gradlew migrateTest  # Миграции для тестов
```

## 🧪 Тестирование

Проект включает комплексные тесты:
- Модульные тесты контроллеров (JUnit 5 + Mockito)
- Интеграционные тесты репозиториев
- Тесты для обеих реализаций (JDBC и Gson)

Запуск тестов:
```bash
./gradlew test
```

## 🔧 Конфигурация

### Выбор реализации репозитория
В файле `application.properties` можно выбрать тип репозитория:
```properties
# JDBC реализация (работа с MySQL)
repository.type=jdbc

# Или Gson реализация (работа с JSON файлами)
repository.type=gson
```

### Настройки кодировки
Проект настроен для работы с UTF-8 через `build.gradle`:
- Компиляция в UTF-8
- Запуск с правильными системными свойствами
- Поддержка кириллицы в консоли и базе данных

## 📝 Использование

После запуска приложения доступно консольное меню для:
- ✅ Создания, чтения, обновления и удаления авторов
- 📝 Управления постами и их статусами
- 🏷️ Работы с метками
- 🔗 Связывания сущностей между собой
- 📊 Просмотра связанных данных (посты автора, метки поста)

## 🔄 Миграции базы данных

### Структура миграций
- `001-initial-schema.xml` - создание таблиц
- `002-test-data.xml` - начальные тестовые данные
- `test-changelog.xml` - отдельные миграции для тестов

### Создание новых миграций
1. Добавьте новый файл в `resources/db/changelog/`
2. Обновите `changelog-master.xml`
3. Запустите `./gradlew migrateDb`

## 🤝 Разработка

### Добавление новой функциональности
1. Создайте модель в пакете `model/`
2. Добавьте репозиторий в `repository/impl/`
3. Реализуйте контроллер в `controller/`
4. Добавьте view компонент в `view/`
5. Напишите тесты в `test/`

### Стиль кода
- Следуйте Java Code Conventions
- Комментируйте публичные методы и классы
- Пишите тесты для новой функциональности
- Используйте meaningful имена переменных и методов

## 📄 Лицензия

[Указать лицензию проекта]