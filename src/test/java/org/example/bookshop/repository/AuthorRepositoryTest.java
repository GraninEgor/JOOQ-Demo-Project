package org.example.bookshop.repository;

import org.example.bookshop.entity.tables.records.AuthorRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CRUD и пагинация по авторам — каждый метод репозитория проверяется отдельно.
 */
class AuthorRepositoryTest extends AbstractRepositoryTest {

    @Test
    void createAssignsGeneratedIdAndFindByIdReturnsEntity() {
        AuthorRecord created = authors.create("Тестовый Автор", "Россия");

        assertTrue(created.getId() != null && created.getId() > 0, "store() должен подтянуть сгенерированный ID");

        AuthorRecord loaded = authors.findById(created.getId());
        assertEquals("Тестовый Автор", loaded.getName());
        assertEquals("Россия", loaded.getCountry());
    }

    @Test
    void findByIdReturnsEntityForExistingKey() {
        AuthorRecord created = authors.create("Find Me", "Португалия");

        AuthorRecord found = authors.findById(created.getId());

        assertNotNull(found, "по существующему ключу должна вернуться сущность");
        assertEquals(created.getId(), found.getId());
        assertEquals("Find Me", found.getName());
        assertEquals("Португалия", found.getCountry());
    }

    @Test
    void findByIdReturnsEntityCreatedBySeedToo() {
        // сиды созданы тем же SchemaSeeder, что и в приложении: id 1..4 существуют
        AuthorRecord seeded = authors.findById(1L);

        assertNotNull(seeded);
        assertEquals(1L, seeded.getId());
        assertTrue(seeded.getName() != null && !seeded.getName().isBlank());
    }

    @Test
    void findByIdReturnsNullForUnknownKey() {
        assertNull(authors.findById(-1L), "по несуществующему ключу fetchOne() возвращает null");
    }

    @Test
    void findByIdReturnsNullAfterDeletion() {
        AuthorRecord created = authors.create("Будет удалён", "Испания");
        authors.delete(created.getId());

        assertNull(authors.findById(created.getId()));
    }

    @Test
    void updateChangesPersistedField() {
        AuthorRecord created = authors.create("До Обновления", "Франция");

        authors.updateCountry(created.getId(), "Италия");

        assertEquals("Италия", authors.findById(created.getId()).getCountry());
    }

    @Test
    void deleteRemovesRow() {
        AuthorRecord created = authors.create("Временно", "Испания");

        int removed = authors.delete(created.getId());

        assertEquals(1, removed);
        assertNull(authors.findById(created.getId()));
    }

    @Test
    void deleteUnknownIdReturnsZero() {
        assertEquals(0, authors.delete(999_999L));
    }

    @Test
    void findPageSortsAndOffsets() {
        // в сидах 4 автора; страница 0 при size=2 — первые два по алфавиту,
        // страница 1 — следующие (offset=2)
        var page0 = authors.findPage(AuthorRepository.byNameAsc(), 0, 2);
        var page1 = authors.findPage(AuthorRepository.byNameAsc(), 1, 2);

        assertEquals(2, page0.size());
        assertEquals(2, page1.size());
        assertTrue(page0.get(0).getName().compareTo(page0.get(1).getName()) <= 0, "сортировка по имени ASC");
        assertTrue(page0.get(1).getName().compareTo(page1.get(0).getName()) <= 0, "offset не должен пересекаться");
        assertNotEquals(page0.get(0).getId(), page1.get(0).getId());

        var pageDesc = authors.findPage(AuthorRepository.byNameDesc(), 0, 4);
        assertTrue(pageDesc.get(0).getName().compareTo(pageDesc.get(3).getName()) >= 0, "DESC-сортировка");
        assertFalse(pageDesc.isEmpty());
    }
}
