package org.example.bookshop;

import org.example.bookshop.config.Database;
import org.example.bookshop.db.SchemaSeeder;
import org.example.bookshop.entity.pojo.AuthorStats;
import org.example.bookshop.entity.pojo.BookWithAuthor;
import org.example.bookshop.entity.tables.records.AuthorRecord;
import org.example.bookshop.entity.tables.records.BookRecord;
import org.example.bookshop.repository.AuthorRepository;
import org.example.bookshop.repository.BookRepository;
import org.example.bookshop.repository.BookSort;

import java.math.BigDecimal;
import java.util.List;

/**
 * Демонстрация jOOQ на встроенной H2: сущности (AuthorRecord/BookRecord), CRUD,
 * выборки с сортировкой, пагинацией (limit/offset) и JOIN'ами.
 *
 * Запуск: mvn -q compile exec:java
 */
public class Main {

    public static void main(String[] args) {
        // Приглушаем собственные INFO-логи jOOQ (баннер и советы), чтобы не мешали выводу.
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");
        java.util.logging.Logger.getLogger("org.jooq").setLevel(java.util.logging.Level.WARNING);

        try (Database db = Database.open()) {
            var dsl = db.dsl();
            var authors = new AuthorRepository(dsl);
            var books = new BookRepository(dsl);

            System.out.println("== 0. Схема и тестовые данные ==");
            SchemaSeeder.init(dsl);
            System.out.printf("авторов: %d, книг: %d%n%n", authors.findPage(AuthorRepository.byNameAsc(), 0, 1000).size(),
                    books.count(null));

            System.out.println("== 1. CRUD: сущности AuthorRecord / BookRecord (store/update/delete) ==");
            AuthorRecord created = authors.create("Новый Автор", "Казахстан");
            System.out.printf("create  -> id=%d, %s (%s)%n", created.getId(), created.getName(), created.getCountry());
            authors.updateCountry(created.getId(), "Германия");
            System.out.printf("update  -> страна теперь: %s%n", authors.findById(created.getId()).getCountry());

            BookRecord createdBook = books.create(created.getId(), "Первая книга", "Fantasy",
                    new BigDecimal("10.00"), 4, 2024);
            System.out.printf("create  -> книга id=%d «%s» у автора id=%d%n",
                    createdBook.getId(), createdBook.getTitle(), createdBook.getAuthorId());

            books.updatePrice(createdBook.getId(), new BigDecimal("12.99"));
            books.updateRating(createdBook.getId(), 5);
            BookRecord after = books.findById(createdBook.getId());
            System.out.printf("update  -> «%s»: цена=%s, рейтинг=%d%n", after.getTitle(), after.getPrice(), after.getRating());

            int removed = books.delete(createdBook.getId());
            System.out.printf("delete  -> удалено книг: %d%n", removed);
            authors.delete(created.getId());
            System.out.printf("delete  -> удалён автор id=%d%n%n", created.getId());

            System.out.println("== 2. Каталог с сортировкой и пагинацией (limit/offset), без JOIN ==");
            int size = 5;
            int total = books.count(null);
            int pages = (total + size - 1) / size;
            System.out.printf("всего книг: %d, страниц по %d: %d, сортировка: цена по убыванию%n", total, size, pages);
            for (int page = 0; page < pages; page++) {
                List<BookRecord> catalogPage = books.findPage(null, BookSort.PRICE_DESC, page, size);
                System.out.printf("  страница %d/%d:%n", page + 1, pages);
                catalogPage.forEach(b -> System.out.printf("    %3d  %-28s %-10s %6.2f  ★%d%n",
                        b.getId(), b.getTitle(), b.getGenre(), b.getPrice(), b.getRating()));
            }
            System.out.println();

            System.out.println("== 3. JOIN: книги + авторы, фильтр по жанру, сортировка по рейтингу, offset ==");
            String genre = "Fantasy";
            int fantasyTotal = books.count(genre);
            int fantasyPages = (fantasyTotal + size - 1) / size;
            System.out.printf("жанр «%s»: всего %d книг, страниц: %d%n", genre, fantasyTotal, fantasyPages);
            for (int page = 0; page < fantasyPages; page++) {
                List<BookWithAuthor> joinPage = books.findWithAuthorPage(genre, BookSort.RATING_DESC, page, size);
                System.out.printf("  страница %d/%d:%n", page + 1, fantasyPages);
                joinPage.forEach(b -> System.out.printf("    «%s» — %s, %d г., %.2f$, ★%d%n",
                        b.getTitle(), b.getAuthorName(), b.getPublishedYear(), b.getPrice(), b.getRating()));
            }
            System.out.println();

            System.out.println("== 4. JOIN + GROUP BY: статистика по авторам (книг и средняя цена) ==");
            List<AuthorStats> stats = books.authorStats();
            stats.forEach(s -> System.out.printf("  %-18s книг: %d, средняя цена: %.2f$%n",
                    s.getAuthorName(), s.getBookCount(), s.getAvgPrice()));

            System.out.println();
            System.out.println("Готово. Приложение отработало на H2 in-memory (данные исчезают вместе с процессом).");
        }
    }
}
