package org.example.bookshop.entity.tables;

import org.example.bookshop.entity.tables.records.AuthorRecord;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

/**
 * Таблица AUTHOR — то, что jOOQ-codegen генерирует по схеме БД (здесь написано руками).
 * Наследует TableImpl<AuthorRecord>, поля создаются через AbstractTable.createField(...),
 * который регистрирует их внутри таблицы.
 */
public final class Author extends TableImpl<AuthorRecord> {

    private static final long serialVersionUID = 1L;

    /** Синглтон таблицы — используется везде в коде: AUTHOR.ID, AUTHOR.NAME, ... */
    public static final Author AUTHOR = new Author();

    public final TableField<AuthorRecord, Long> ID =
            createField(DSL.name("ID"), SQLDataType.BIGINT.nullable(false).identity(true), this);
    public final TableField<AuthorRecord, String> NAME =
            createField(DSL.name("NAME"), SQLDataType.VARCHAR(120).nullable(false), this);
    public final TableField<AuthorRecord, String> COUNTRY =
            createField(DSL.name("COUNTRY"), SQLDataType.VARCHAR(60), this);

    /** Первичный ключ нужен, чтобы сущности (AuthorRecord) умели store()/update()/delete(). */
    private final UniqueKey<AuthorRecord> primaryKey = Internal.createUniqueKey(this, this.ID);

    public Author() {
        super(DSL.name("AUTHOR"), null);
    }

    @Override
    public Class<AuthorRecord> getRecordType() {
        return AuthorRecord.class;
    }

    @Override
    public UniqueKey<AuthorRecord> getPrimaryKey() {
        return primaryKey;
    }
}
