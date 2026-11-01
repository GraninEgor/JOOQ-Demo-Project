package org.example.bookshop.repository;

import org.example.bookshop.entity.pojo.AuthorStats;
import org.example.bookshop.entity.pojo.BookWithAuthor;
import org.example.bookshop.entity.tables.records.BookRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record7;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.util.List;

import static org.example.bookshop.entity.tables.Author.AUTHOR;
import static org.example.bookshop.entity.tables.Book.BOOK;

/**
 * CRUD и «каталожные» выборки книг: сортировка (BookSort), пагинация (limit/offset),
 * JOIN с авторами, фильтр по жанру, агрегаты по авторам.
 */
public class BookRepository {

    private final DSLContext dsl;

    public BookRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /** INSERT новой книги (store() — как у автора). */
    public BookRecord create(long authorId, String title, String genre,
                             BigDecimal price, int rating, Integer publishedYear) {
        BookRecord r = dsl.newRecord(BOOK);
        r.setAuthorId(authorId);
        r.setTitle(title);
        r.setGenre(genre);
        r.setPrice(price);
        r.setRating(rating);
        r.setPublishedYear(publishedYear);
        r.store();
        return r;
    }

    public BookRecord findById(long id) {
        return dsl.selectFrom(BOOK)
                .where(BOOK.ID.eq(id))
                .fetchOne();
    }

    /** UPDATE цены у конкретной книги. */
    public void updatePrice(long id, BigDecimal newPrice) {
        dsl.update(BOOK)
                .set(BOOK.PRICE, newPrice)
                .where(BOOK.ID.eq(id))
                .execute();
    }

    /** UPDATE через сущность: прочли -> изменили -> update(). */
    public void updateRating(long id, int newRating) {
        BookRecord b = findById(id);
        if (b != null) {
            b.setRating(newRating);
            b.update();
        }
    }

    /** DELETE по ключу. */
    public int delete(long id) {
        return dsl.deleteFrom(BOOK)
                .where(BOOK.ID.eq(id))
                .execute();
    }

    /** Сколько книг подходит под фильтр (для расчёта числа страниц). */
    public int count(String genre) {
        Integer total = dsl.selectCount()
                .from(BOOK)
                .where(genreCondition(genre))
                .fetchOne(0, int.class);
        return total != null ? total : 0;
    }

    /**
     * Постраничный каталог книг: фильтр по жанру (может быть null), сортировка,
     * LIMIT/OFFSET. page нумеруется с нуля.
     */
    public List<BookRecord> findPage(String genre, BookSort sort, int page, int size) {
        return dsl.selectFrom(BOOK)
                .where(genreCondition(genre))
                .orderBy(sort.orderFields())
                .limit(size)
                .offset((long) page * size)
                .fetch();
    }

    /**
     * JOIN: книги вместе с именем автора; та же пагинация и сортировка.
     * Результат фасуется в POJO через RecordMapper (fetchInto).
     */
    public List<BookWithAuthor> findWithAuthorPage(String genre, BookSort sort, int page, int size) {
        SelectJoinStep<Record7<Long, String, String, String, BigDecimal, Integer, Integer>> select =
                dsl.select(BOOK.ID,
                           BOOK.TITLE,
                           AUTHOR.NAME.as("AUTHOR_NAME"),
                           BOOK.GENRE,
                           BOOK.PRICE,
                           BOOK.RATING,
                           BOOK.PUBLISHED_YEAR)
                        .from(BOOK)
                        .join(AUTHOR).on(BOOK.AUTHOR_ID.eq(AUTHOR.ID));

        return select
                .where(genreCondition(genre))
                .orderBy(sort.orderFields())
                .limit(size)
                .offset((long) page * size)
                .fetchInto(BookWithAuthor.class);
    }

    /** GROUP BY + COUNT + AVG: по каждому автору — число книг и средняя цена. */
    public List<AuthorStats> authorStats() {
        return dsl.select(AUTHOR.NAME.as("AUTHOR_NAME"),
                          DSL.count(BOOK.ID).as("BOOK_COUNT"),
                          DSL.avg(BOOK.PRICE).as("AVG_PRICE"))
                .from(AUTHOR)
                .leftJoin(BOOK).on(BOOK.AUTHOR_ID.eq(AUTHOR.ID))
                .groupBy(AUTHOR.ID, AUTHOR.NAME)
                .orderBy(DSL.count(BOOK.ID).desc(), AUTHOR.NAME.asc())
                .fetchInto(AuthorStats.class);
    }

    private Condition genreCondition(String genre) {
        return genre != null && !genre.isBlank() ? BOOK.GENRE.eq(genre) : DSL.noCondition();
    }
}
