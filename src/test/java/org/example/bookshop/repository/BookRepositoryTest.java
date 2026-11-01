package org.example.bookshop.repository;

import org.example.bookshop.entity.pojo.AuthorStats;
import org.example.bookshop.entity.pojo.BookWithAuthor;
import org.example.bookshop.entity.tables.records.BookRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CRUD книг и «каталожные» выборки: сортировка, пагинация, JOIN, агрегаты.
 * Сид-данные известны: 4 автора, 12 книг, по 3 книги в каждом из 4 жанров.
 */
class BookRepositoryTest extends AbstractRepositoryTest {

    private long anySeedAuthorId() {
        return authors.findPage(AuthorRepository.byNameAsc(), 0, 1).get(0).getId();
    }

    @Test
    void createAssignsGeneratedIdAndFindByIdReturnsEntity() {
        BookRecord created = books.create(anySeedAuthorId(), "Юнит-тест книга", "Sci-Fi",
                new BigDecimal("5.55"), 4, 2024);

        BookRecord loaded = books.findById(created.getId());
        assertEquals("Юнит-тест книга", loaded.getTitle());
        assertEquals(new BigDecimal("5.55"), loaded.getPrice());
        assertEquals(4, loaded.getRating());
    }

    @Test
    void updatePriceChangesPersistedValue() {
        BookRecord created = books.create(anySeedAuthorId(), "Цена изменится", "Fantasy",
                new BigDecimal("30.00"), 3, 2020);

        books.updatePrice(created.getId(), new BigDecimal("27.50"));

        assertEquals(new BigDecimal("27.50"), books.findById(created.getId()).getPrice());
    }

    @Test
    void updateRatingViaEntity() {
        BookRecord created = books.create(anySeedAuthorId(), "Рейтинг изменится", "Detective",
                new BigDecimal("7.00"), 3, 2021);

        books.updateRating(created.getId(), 5);

        assertEquals(5, books.findById(created.getId()).getRating());
    }

    @Test
    void deleteRemovesRow() {
        BookRecord created = books.create(anySeedAuthorId(), "На удаление", "Romance",
                new BigDecimal("1.00"), 2, 2010);

        assertEquals(1, books.delete(created.getId()));
        assertNull(books.findById(created.getId()));
    }

    @Test
    void countHonorsGenreFilter() {
        assertEquals(12, books.count(null));
        assertEquals(3, books.count("Fantasy"));
        assertEquals(3, books.count("Sci-Fi"));
        assertEquals(0, books.count("Такого жанра нет"));
    }

    @Test
    void findPagePriceDescPaginationPagesDoNotOverlap() {
        int size = 5;
        List<BookRecord> page0 = books.findPage(null, BookSort.PRICE_DESC, 0, size);
        List<BookRecord> page1 = books.findPage(null, BookSort.PRICE_DESC, 1, size);
        List<BookRecord> page2 = books.findPage(null, BookSort.PRICE_DESC, 2, size);

        assertEquals(size, page0.size());
        assertEquals(size, page1.size());
        assertEquals(12 - 2 * size, page2.size());

        // сортировка по цене убывает внутри страницы и между страницами
        assertTrue(page0.get(0).getPrice().compareTo(page0.get(size - 1).getPrice()) >= 0);
        assertTrue(page0.get(size - 1).getPrice().compareTo(page1.get(0).getPrice()) >= 0);
        assertTrue(page1.get(size - 1).getPrice().compareTo(page2.get(0).getPrice()) >= 0);

        // страницы не пересекаются по ID
        Set<Long> ids0 = page0.stream().map(BookRecord::getId).collect(Collectors.toSet());
        Set<Long> ids1 = page1.stream().map(BookRecord::getId).collect(Collectors.toSet());
        ids0.retainAll(ids1);
        assertTrue(ids0.isEmpty(), "offset не должен возвращать те же строки");
    }

    @Test
    void findWithAuthorPageJoinsAndFiltersByGenre() {
        List<BookWithAuthor> page = books.findWithAuthorPage("Fantasy", BookSort.RATING_DESC, 0, 10);

        assertEquals(3, page.size());
        for (BookWithAuthor b : page) {
            assertEquals("Fantasy", b.getGenre());
            assertNotNull(b.getAuthorName(), "JOIN должен подтянуть имя автора");
            assertNotNull(b.getPublishedYear());
        }
        // сортировка по рейтингу убывает
        assertTrue(page.get(0).getRating() >= page.get(page.size() - 1).getRating());
    }

    @Test
    void findWithAuthorPageOffsetChangesContent() {
        var page0 = books.findWithAuthorPage(null, BookSort.TITLE_ASC, 0, 6);
        var page1 = books.findWithAuthorPage(null, BookSort.TITLE_ASC, 1, 6);

        assertEquals(6, page0.size());
        assertEquals(6, page1.size());
        assertTrue(page0.get(0).getTitle().compareTo(page0.get(5).getTitle()) <= 0);
        assertNotEquals(page0.get(5).getId(), page1.get(0).getId());
    }

    @Test
    void authorStatsCountsBooksPerAuthor() {
        List<AuthorStats> stats = books.authorStats();

        assertEquals(4, stats.size());
        assertEquals(12, stats.stream().mapToInt(AuthorStats::getBookCount).sum());

        AuthorStats top = stats.get(0);
        assertEquals("Hanna Weber", top.getAuthorName());
        assertEquals(4, top.getBookCount());
        assertTrue(top.getAvgPrice().compareTo(BigDecimal.ZERO) > 0);
    }
}
