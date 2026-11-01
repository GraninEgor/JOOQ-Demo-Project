package org.example.bookshop.repository;

import org.jooq.SortField;

import java.util.List;

import static org.example.bookshop.entity.tables.Book.BOOK;

/**
 * Варианты сортировки книг для списков. Каждый вариант — jOOQ SortField,
 * который просто передаётся в .orderBy(...).
 */
public enum BookSort {
    PRICE_DESC {
        @Override public List<SortField<?>> orderFields() {
            return List.of(BOOK.PRICE.desc(), BOOK.ID.asc());
        }
    },
    RATING_DESC {
        @Override public List<SortField<?>> orderFields() {
            return List.of(BOOK.RATING.desc(), BOOK.TITLE.asc());
        }
    },
    TITLE_ASC {
        @Override public List<SortField<?>> orderFields() {
            return List.of(BOOK.TITLE.asc());
        }
    },
    YEAR_DESC {
        @Override public List<SortField<?>> orderFields() {
            return List.of(BOOK.PUBLISHED_YEAR.desc(), BOOK.TITLE.asc());
        }
    };

    public abstract List<SortField<?>> orderFields();
}
