# JOOQ_Demo_Project

| |                                      |
|---|--------------------------------------|
| **Автор** | Гранин Егор                          |
| **Точная версия** | **3.18.22**          |
| **Репозиторий библиотеки** | https://github.com/jOOQ/jOOQ         |

## Быстрый старт (локально)

```bash
mvn -q compile exec:java     # приложение: схема + данные + все демо-секции
mvn test                     # 18 тестов репозиториев (JUnit 5)
```

Требования: JDK 17+, Maven 3.8+. Внешняя БД не нужна — используется встроенная H2 in-memory.

## Запуск в Docker

```bash
docker build -t jooq-demo .
docker run --rm jooq-demo
```

Что произойдёт: multi-stage сборка (Maven скачивает зависимости и собирает классы) →
запуск на лёгком JRE-образе. Приложение поднимает H2 in-memory, создаёт схему
(`AUTHOR`, `BOOK` + FK), засеивает данные и отрабатывает все демо-секции — вывод виден
в консоли контейнера. Данные живут, пока работает контейнер (`--rm` убирает его после).

Порт не публикуем: серверной части у приложения нет, вся БД — внутри процесса.

## Тесты: запуск всех и конкретного теста

```bash
mvn test                                                    # все 18 тестов

mvn test -Dtest=AuthorRepositoryTest                        # один класс целиком
mvn test -Dtest=BookRepositoryTest                          # один класс целиком

mvn test -Dtest=AuthorRepositoryTest#findByIdReturnsEntityForExistingKey          # один метод
mvn test -Dtest=BookRepositoryTest#findWithAuthorPageJoinsAndFiltersByGenre       # один метод
mvn test -Dtest='BookRepositoryTest#findPage*+countHonorsGenreFilter'             # маска + метод
```

В IDEA — ещё проще: зелёная стрелка слева от метода теста.

Каждый тест работает со **своей** изолированной H2-базой (`Database.open("test_<uuid>")`
в `AbstractRepositoryTest`): схема и сиды создаются тем же `SchemaSeeder`, что и в
приложении, после теста база исчезает — тесты не влияют друг на друга.

### Что смотреть в выводе теста

В тестах включён SQL-лог jOOQ (`LoggerListener`, уровень FINE) — видно, что тесты
реально ходят в БД, и какие запросы породил каждый вызов репозитория:

```text
FINE: Executing query     : select "BOOK"."TITLE", "AUTHOR"."NAME" from "BOOK"
                          join "AUTHOR" on "BOOK"."AUTHOR_ID" = "AUTHOR"."ID"
                          where "BOOK"."GENRE" = cast(? as varchar(50)) ...
FINE: -> with bind values : ... 'Fantasy' ...
FINE: Affected row(s)     : 1
```

На что обратить внимание:

* **`cast(? as varchar(50))`** — jOOQ сам выставляет тип bind-значения, в SQL не подставляется
  никакая склеенная строка;
* **`-> with bind values`** — те же запросы с подставленными значениями (так печатает
  встроенный `LoggerListener`, это дублирующий показ для читаемости);
* **`select ... from final table (insert ...)`** — как jOOQ исполняет `INSERT … RETURNING` на H2.

## Домен

Книжный магазин:

* `AUTHOR` — авторы (`ID`, `NAME`, `COUNTRY`);
* `BOOK` — книги (`ID`, `AUTHOR_ID` FK → `AUTHOR`, `TITLE`, `GENRE`, `PRICE`, `RATING`, `PUBLISHED_YEAR`).

Схема создаётся jOOQ-DDL при старте, затем засеиваются 4 автора и 12 книг (`db/SchemaSeeder`).

## Структура

```
src/main/java/org/example/bookshop/
├─ Main.java                        # сценарий: схема → CRUD → пагинация → JOIN → статистика
├─ config/Database.java             # сборка Configuration: H2-пул + DataSourceConnectionProvider + диалект + Settings
├─ db/SchemaSeeder.java             # DDL (jOOQ-DDL) и тестовые данные
├─ entity/
│  ├─ tables/Author.java, Book.java           # «выход codegen»: TableImpl + createField + PK
│  ├─ tables/records/AuthorRecord.java,
│  │                   BookRecord.java        # сущности (UpdatableRecordImpl): store/update/delete
│  └─ pojo/BookWithAuthor.java, AuthorStats.java  # POJO для fetchInto() (RecordMapper)
└─ repository/
   ├─ AuthorRepository.java          # CRUD авторов
   ├─ BookRepository.java            # CRUD книг + findPage/findWithAuthorPage/authorStats
   └─ BookSort.java                  # варианты сортировки (SortField для orderBy)

src/test/java/org/example/bookshop/repository/
├─ AbstractRepositoryTest.java      # изолированная БД на каждый тест + сиды + SQL-лог
├─ AuthorRepositoryTest.java        # CRUD + сортировка/offset
└─ BookRepositoryTest.java          # CRUD + count + пагинация + JOIN + агрегаты
```