package org.example.bookshop.entity.pojo;

import java.math.BigDecimal;

/**
 * Агрегат по автору: сколько его книг в магазине и средняя цена (GROUP BY + COUNT + AVG).
 */
public class AuthorStats {
    private String authorName;
    private Integer bookCount;
    private BigDecimal avgPrice;

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public Integer getBookCount() { return bookCount; }
    public void setBookCount(Integer bookCount) { this.bookCount = bookCount; }
    public BigDecimal getAvgPrice() { return avgPrice; }
    public void setAvgPrice(BigDecimal avgPrice) { this.avgPrice = avgPrice; }
}
