package org.example.bookshop.repository;

import org.example.bookshop.config.Database;
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
