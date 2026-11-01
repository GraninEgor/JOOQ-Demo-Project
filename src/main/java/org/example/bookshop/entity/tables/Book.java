package org.example.bookshop.entity.tables;

import org.example.bookshop.entity.tables.records.BookRecord;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.math.BigDecimal;

/**
 * Таблица BOOK: связь N:1 с AUTHOR через BOOK.AUTHOR_ID (FK задаётся в DDL).
 */
public final class Book extends TableImpl<BookRecord> {

    private static final long serialVersionUID = 1L;

    public static final Book BOOK = new Book();

    public final TableField<BookRecord, Long> ID =
            createField(DSL.name("ID"), SQLDataType.BIGINT.nullable(false).identity(true), this);
    public final TableField<BookRecord, Long> AUTHOR_ID =
            createField(DSL.name("AUTHOR_ID"), SQLDataType.BIGINT.nullable(false), this);
    public final TableField<BookRecord, String> TITLE =
            createField(DSL.name("TITLE"), SQLDataType.VARCHAR(200).nullable(false), this);
    public final TableField<BookRecord, String> GENRE =
            createField(DSL.name("GENRE"), SQLDataType.VARCHAR(50).nullable(false), this);
    public final TableField<BookRecord, BigDecimal> PRICE =
            createField(DSL.name("PRICE"), SQLDataType.DECIMAL(10, 2).nullable(false), this);
    public final TableField<BookRecord, Integer> RATING =
            createField(DSL.name("RATING"), SQLDataType.INTEGER.nullable(false), this);
    public final TableField<BookRecord, Integer> PUBLISHED_YEAR =
            createField(DSL.name("PUBLISHED_YEAR"), SQLDataType.INTEGER, this);

    /** Первичный ключ нужен, чтобы сущности (BookRecord) умели store()/update()/delete(). */
    private final UniqueKey<BookRecord> primaryKey = Internal.createUniqueKey(this, this.ID);

    public Book() {
        super(DSL.name("BOOK"), null);
    }

    @Override
    public Class<BookRecord> getRecordType() {
        return BookRecord.class;
    }

    @Override
    public UniqueKey<BookRecord> getPrimaryKey() {
        return primaryKey;
    }
}
