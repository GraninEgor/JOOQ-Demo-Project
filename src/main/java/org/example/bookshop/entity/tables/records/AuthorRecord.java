package org.example.bookshop.entity.tables.records;

import org.example.bookshop.entity.tables.Author;
import org.jooq.impl.UpdatableRecordImpl;

/**
 * «Сущность» автора: jOOQ-запись (UpdatableRecordImpl) с активным поведением —
 * store() сама решает INSERT это или UPDATE, delete() удаляет, refresh() перечитывает.
 * Такая же структура получается на выходе jOOQ-codegen.
 */
public class AuthorRecord extends UpdatableRecordImpl<AuthorRecord> {

    private static final long serialVersionUID = 1L;

    public AuthorRecord() {
        super(Author.AUTHOR);
    }

    public AuthorRecord(Long id, String name, String country) {
        super(Author.AUTHOR);
        set(0, id);
        set(1, name);
        set(2, country);
    }

    public Long getId() { return get(Author.AUTHOR.ID); }
    public void setId(Long id) { set(Author.AUTHOR.ID, id); }
    public String getName() { return get(Author.AUTHOR.NAME); }
    public void setName(String name) { set(Author.AUTHOR.NAME, name); }
    public String getCountry() { return get(Author.AUTHOR.COUNTRY); }
    public void setCountry(String country) { set(Author.AUTHOR.COUNTRY, country); }
}
