package org.example.bookshop.repository;

import org.example.bookshop.entity.tables.records.AuthorRecord;
import org.jooq.DSLContext;
import org.jooq.SortField;

import java.util.List;

import static org.example.bookshop.entity.tables.Author.AUTHOR;

/**
 * CRUD по авторам на «сущностях» (AuthorRecord): store()/update()/delete() —
 * активная запись jOOQ; пагинация — limit/offset + сортировка.
 */
public class AuthorRepository {

    private final DSLContext dsl;

    public AuthorRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /** INSERT: store() у новой записи; после неё ID подтянут из сгенерированных ключей. */
    public AuthorRecord create(String name, String country) {
        AuthorRecord r = dsl.newRecord(AUTHOR);
        r.setName(name);
        r.setCountry(country);
        r.store();
        return r;
    }

    /** SELECT по ключу -> сущность (или null). */
    public AuthorRecord findById(long id) {
        return dsl.selectFrom(AUTHOR)
                .where(AUTHOR.ID.eq(id))
                .fetchOne();
    }

    /** Все авторы с сортировкой и пагинацией. */
    public List<AuthorRecord> findPage(SortField<String> order, int page, int size) {
        return dsl.selectFrom(AUTHOR)
                .orderBy(order)
                .limit(size)
                .offset((long) page * size)
                .fetch();
    }

    /** UPDATE: изменяем поле у прочитанной сущности и вызываем update(). */
    public void updateCountry(long id, String country) {
        AuthorRecord r = findById(id);
        if (r != null) {
            r.setCountry(country);
            r.update();
        }
    }

    /** DELETE по ключу; возвращает число удалённых строк (0 или 1). */
    public int delete(long id) {
        return dsl.deleteFrom(AUTHOR)
                .where(AUTHOR.ID.eq(id))
                .execute();
    }

    public static SortField<String> byNameAsc() {
        return AUTHOR.NAME.asc();
    }

    public static SortField<String> byNameDesc() {
        return AUTHOR.NAME.desc();
    }
}
