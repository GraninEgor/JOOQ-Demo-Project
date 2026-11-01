package org.example.bookshop.db;

import org.example.bookshop.entity.tables.records.AuthorRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.example.bookshop.entity.tables.Author.AUTHOR;
import static org.example.bookshop.entity.tables.Book.BOOK;

/**
 * Создание схемы и тестовых данных. DDL-запросы — такие же QueryPart'ы, как SELECT:
 * их рендерит и исполняет тот же механизм jOOQ.
 */
public final class SchemaSeeder {

    private SchemaSeeder() {
    }

    public static void init(DSLContext dsl) {
        dsl.createTableIfNotExists(AUTHOR)
                .columns(AUTHOR.fields())
                .constraints(DSL.primaryKey(AUTHOR.ID))
                .execute();

        dsl.createTableIfNotExists(BOOK)
                .columns(BOOK.fields())
                .constraints(
                        DSL.primaryKey(BOOK.ID),
                        DSL.foreignKey(BOOK.AUTHOR_ID).references(AUTHOR))
                .execute();

        seed(dsl);
    }

    private static void seed(DSLContext dsl) {
        if (dsl.fetchCount(AUTHOR) > 0) {
            return; // уже посеяно (на случай повторного запуска в той же БД)
        }

        Map<Long, String> authors = new LinkedHashMap<>();
        authors.put(insertAuthor(dsl, "Алексей Иванов", "Россия"), "Алексей Иванов");
        authors.put(insertAuthor(dsl, "Maria Silva", "Portugal"), "Maria Silva");
        authors.put(insertAuthor(dsl, "John Carpenter", "USA"), "John Carpenter");
        authors.put(insertAuthor(dsl, "Hanna Weber", "Germany"), "Hanna Weber");

        // книги: (authorName, title, genre, price, rating, year)
        insertBook(dsl, "Алексей Иванов", "Тень мертвеца", "Detective", new BigDecimal("12.50"), 4, 2019);
        insertBook(dsl, "Алексей Иванов", "Хребет ночи", "Fantasy", new BigDecimal("18.90"), 5, 2021);
        insertBook(dsl, "Алексей Иванов", "Пять минут до утра", "Detective", new BigDecimal("9.99"), 3, 2016);
        insertBook(dsl, "Maria Silva", "The Salt Road", "Romance", new BigDecimal("14.00"), 4, 2020);
        insertBook(dsl, "Maria Silva", "Lisbon Rain", "Romance", new BigDecimal("11.25"), 3, 2018);
        insertBook(dsl, "John Carpenter", "Orbit of Ashes", "Sci-Fi", new BigDecimal("22.40"), 5, 2022);
        insertBook(dsl, "John Carpenter", "The Last Signal", "Sci-Fi", new BigDecimal("19.75"), 4, 2023);
        insertBook(dsl, "John Carpenter", "Cold Frontier", "Sci-Fi", new BigDecimal("15.10"), 4, 2017);
        insertBook(dsl, "Hanna Weber", "Das blaue Haus", "Detective", new BigDecimal("13.30"), 4, 2021);
        insertBook(dsl, "Hanna Weber", "Winterlicht", "Fantasy", new BigDecimal("16.80"), 5, 2022);
        insertBook(dsl, "Hanna Weber", "Stille Wasser", "Romance", new BigDecimal("8.40"), 3, 2015);
        insertBook(dsl, "Hanna Weber", "Feuervogel", "Fantasy", new BigDecimal("20.00"), 4, 2019);
    }

    private static long insertAuthor(DSLContext dsl, String name, String country) {
        AuthorRecord r = dsl.newRecord(AUTHOR);
        r.setName(name);
        r.setCountry(country);
        r.store();
        return r.getId();
    }

    private static void insertBook(DSLContext dsl, String authorName, String title, String genre,
                                   BigDecimal price, int rating, int year) {
        Long authorId = dsl.select(AUTHOR.ID)
                .from(AUTHOR)
                .where(AUTHOR.NAME.eq(authorName))
                .fetchOne(AUTHOR.ID);
        dsl.insertInto(BOOK,
                       BOOK.AUTHOR_ID, BOOK.TITLE, BOOK.GENRE,
                       BOOK.PRICE, BOOK.RATING, BOOK.PUBLISHED_YEAR)
                .values(authorId, title, genre, price, rating, year)
                .execute();
    }
}
