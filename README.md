# JOOQ_Demo_Project

Учебное приложение на **jOOQ 3.18** с встроенной БД **H2 (in-memory)**:
сущности, CRUD, выборки с **сортировкой**, **пагинацией (limit/offset)** и **JOIN'ами**.
Всё работает сразу после запуска — никакой внешней БД не нужно.

## Запуск

```bash
mvn -q compile exec:java
```

или просто запустите `main()` класса `org.example.bookshop.Main` в IDEA.

Требования: JDK 17+, Maven 3.8+ (при первом запуске скачает jOOQ и H2).

## Тесты

Каждый метод репозиториев можно дёргать отдельно — тесты вызывают их по одному
на **изолированной** in-memory БД (у каждого теста своя база, данные не пересекаются):

```bash
mvn test            # или в IDEA: ПКМ на src/test → Run all tests
```

* `AuthorRepositoryTest` — CRUD (`store/update/delete`), поиск по ключу, сортировка ASC/DESC и offset;
* `BookRepositoryTest` — CRUD книг, `count` с фильтром по жанру, пагинация `PRICE_DESC`
  (страницы не пересекаются и упорядочены), JOIN-выборка с фильтром и offset, статистика по авторам.

База для теста собирается в `AbstractRepositoryTest` через `Database.open("test_<uuid>")` —
схема и сиды создаются тем же `SchemaSeeder`, что и в приложении.

## Домен

Книжный магазин:

* `AUTHOR` — авторы (`ID`, `NAME`, `COUNTRY`);
* `BOOK` — книги (`ID`, `AUTHOR_ID` FK → `AUTHOR`, `TITLE`, `GENRE`, `PRICE`, `RATING`, `PUBLISHED_YEAR`).

Схема создаётся jOOQ-DDL при старте, затем засеиваются 4 автора и 12 книг (`db/SchemaSeeder`).

## Что демонстрирует `Main`

| Секция вывода | Что внутри |
|---|---|
| 0. Схема и данные | `createTableIfNotExists` + PK/FK через jOOQ-DDL, сиды |
| 1. CRUD | «активные записи» jOOQ: `record.store()` (INSERT, ID из generated keys), `findById`, изменение поля + `update()`, `deleteFrom` |
| 2. Каталог | сортировка (`PRICE_DESC` — см. `BookSort`), `limit/offset`, подсчёт страниц через `selectCount` |
| 3. JOIN-выборка | `BOOK JOIN AUTHOR`, фильтр по жанру, сортировка по рейтингу, постранично; результат фасуется в POJO (`fetchInto`) через `RecordMapper` |
| 4. Статистика | `LEFT JOIN + GROUP BY + COUNT + AVG` → POJO `AuthorStats` |

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
├─ repository/
│  ├─ AuthorRepository.java          # CRUD авторов
│  ├─ BookRepository.java            # CRUD книг + findPage/findWithAuthorPage/authorStats
│  └─ BookSort.java                  # варианты сортировки (SortField для orderBy)
└─ src/test/java/org/example/bookshop/repository/
   ├─ AbstractRepositoryTest.java    # изолированная БД на каждый тест + сиды
   ├─ AuthorRepositoryTest.java      # CRUD + сортировка/offset
   └─ BookRepositoryTest.java        # CRUD + count + пагинация + JOIN + агрегаты
```

## Почему так

* `entity/tables/*` — это руками написанный аналог того, что генерирует
  [jOOQ codegen](https://www.jooq.org/doc/latest/manual/code-generation/) по схеме БД
  (`TableImpl` с полями через `createField`, `UpdatableRecordImpl`-сущности с первичным ключом).
  В боевом проекте эти классы генерируются автоматически из вашей базы.
* `store()` у сущности сама решает: `INSERT` (новая) или `UPDATE` (изменённая),
  а сгенерированный ключ после вставки возвращается в `getId()`.
* `Configuration` (`config/Database.java`) — центральная настройка jOOQ: диалект,
  источник соединений (`DataSourceConnectionProvider` над H2-пулом), `Settings`.
  Компоненты взаимозаменяемы: поменять H2 на PostgreSQL — это одна строка диалекта и URL.

## Зависимости

`org.jooq:jooq:3.18.22`, `com.h2database:h2:2.5.252`.
`reactive-streams` подтягивается транзитивно (нужен, т.к. `ResultQuery` расширяет `Publisher`).
