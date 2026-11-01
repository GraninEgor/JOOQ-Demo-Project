package org.example.bookshop.entity.pojo;

import java.math.BigDecimal;

/**
 * Строка выборки «книга + имя автора» (JOIN): цель fetchInto(BookWithAuthor.class).
 * DefaultRecordMapper сопоставляет колонки результата со свойствами по алиасам
 * (AUTHOR_NAME -> authorName и т.д.).
 */
public class BookWithAuthor {
    private Long id;
    private String title;
    private String authorName;
    private String genre;
    private BigDecimal price;
    private Integer rating;
    private Integer publishedYear;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public Integer getPublishedYear() { return publishedYear; }
    public void setPublishedYear(Integer publishedYear) { this.publishedYear = publishedYear; }
}
