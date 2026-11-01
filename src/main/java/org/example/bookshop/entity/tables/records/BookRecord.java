package org.example.bookshop.entity.tables.records;

import org.example.bookshop.entity.tables.Book;
import org.jooq.impl.UpdatableRecordImpl;

import java.math.BigDecimal;

/**
 * «Сущность» книги (аналогично AuthorRecord).
 */
public class BookRecord extends UpdatableRecordImpl<BookRecord> {

    private static final long serialVersionUID = 1L;

    public BookRecord() {
        super(Book.BOOK);
    }

    public BookRecord(Long id, Long authorId, String title, String genre,
                      BigDecimal price, Integer rating, Integer publishedYear) {
        super(Book.BOOK);
        set(0, id);
        set(1, authorId);
        set(2, title);
        set(3, genre);
        set(4, price);
        set(5, rating);
        set(6, publishedYear);
    }

    public Long getId() { return get(Book.BOOK.ID); }
    public void setId(Long id) { set(Book.BOOK.ID, id); }
    public Long getAuthorId() { return get(Book.BOOK.AUTHOR_ID); }
    public void setAuthorId(Long authorId) { set(Book.BOOK.AUTHOR_ID, authorId); }
    public String getTitle() { return get(Book.BOOK.TITLE); }
    public void setTitle(String title) { set(Book.BOOK.TITLE, title); }
    public String getGenre() { return get(Book.BOOK.GENRE); }
    public void setGenre(String genre) { set(Book.BOOK.GENRE, genre); }
    public BigDecimal getPrice() { return get(Book.BOOK.PRICE); }
    public void setPrice(BigDecimal price) { set(Book.BOOK.PRICE, price); }
    public Integer getRating() { return get(Book.BOOK.RATING); }
    public void setRating(Integer rating) { set(Book.BOOK.RATING, rating); }
    public Integer getPublishedYear() { return get(Book.BOOK.PUBLISHED_YEAR); }
    public void setPublishedYear(Integer publishedYear) { set(Book.BOOK.PUBLISHED_YEAR, publishedYear); }
}
