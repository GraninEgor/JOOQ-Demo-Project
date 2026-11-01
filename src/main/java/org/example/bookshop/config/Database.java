package org.example.bookshop.config;

import org.h2.jdbcx.JdbcConnectionPool;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DSL;

/**
 * Сборка jOOQ-конфигурации: «хаб провайдеров».
 *  - DataSourceConnectionProvider обёрнут вокруг пула H2 (acquire/release соединений);
 *  - SQLDialect.H2 управляет рендером SQL под конкретную СУБД;
 *  - Settings — тонкие настройки рендера/исполнения (здесь — значения по умолчанию).
 *
 * БД в памяти: живёт, пока работает процесс (DB_CLOSE_DELAY=-1).
 */
public final class Database implements AutoCloseable {

    private static final String JDBC_URL = "jdbc:h2:mem:bookshop;DB_CLOSE_DELAY=-1";

    private final JdbcConnectionPool pool;
    private final DSLContext dsl;

    private Database(JdbcConnectionPool pool, DSLContext dsl) {
        this.pool = pool;
        this.dsl = dsl;
    }

    public static Database open() {
        return open("bookshop");
    }

    /**
     * Отдельное имя БД — для тестов: каждый тест получает свою изолированную
     * in-memory базу (данные не пересекаются между тестами).
     */
    public static Database open(String dbName) {
        JdbcConnectionPool pool = JdbcConnectionPool.create(
                "jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1", "sa", "");
        Configuration configuration = new DefaultConfiguration()
                .set(SQLDialect.H2)
                .set(new DataSourceConnectionProvider(pool))
                .set(new Settings());
        return new Database(pool, DSL.using(configuration));
    }

    public DSLContext dsl() {
        return dsl;
    }

    @Override
    public void close() {
        pool.dispose();
    }
}
