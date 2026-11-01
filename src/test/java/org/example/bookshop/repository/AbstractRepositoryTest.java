package org.example.bookshop.repository;

import org.example.bookshop.config.Database;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.example.bookshop.db.SchemaSeeder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;

/**
 * База для тестов репозиториев: каждый тест получает СВОЮ in-memory БД
 * (уникальное имя => полная изоляция), схему и сиды создаёт SchemaSeeder —
 * те же, что и в приложении.
 */
public abstract class AbstractRepositoryTest {

    static {
        // Показываем SQL-лог jOOQ (LoggerListener): каждый запрос и его bind-значения
        // будут видны прямо в выводе тестов — наглядно видно, что тесты ходят в БД.
        Logger jooq = Logger.getLogger("org.jooq");
        jooq.setLevel(Level.FINE);
        jooq.setUseParentHandlers(false);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.FINE);
        ch.setFormatter(new java.util.logging.SimpleFormatter());
        for (Handler h : jooq.getHandlers()) jooq.removeHandler(h);
        jooq.addHandler(ch);
    }

    protected Database db;
    protected AuthorRepository authors;
    protected BookRepository books;

    @BeforeEach
    void setUpDatabase() {
        db = Database.open("test_" + UUID.randomUUID());
        SchemaSeeder.init(db.dsl());
        authors = new AuthorRepository(db.dsl());
        books = new BookRepository(db.dsl());
    }

    @AfterEach
    void tearDownDatabase() {
        db.close();
    }
}
