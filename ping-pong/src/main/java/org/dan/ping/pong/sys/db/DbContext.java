package org.dan.ping.pong.sys.db;

import static org.jooq.SQLDialect.valueOf;
import static org.jooq.impl.DSL.using;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.tournament.DbUpdaterFactory;
import org.jooq.DSLContext;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Named;
import javax.sql.DataSource;

@Slf4j
@Import({BatchExecutorFactory.class, DbUpdaterFactory.class})
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class DbContext {
    public static final String DSL_CONTEXT = "dslContext";
    public static final String TRANSACTION_MANAGER = "transactionManager";
    public static final String DATA_SOURCE = "dataSource";

    @Bean(name = DATA_SOURCE)
    public DataSource dataSource(
            @Value("${db.data.source}") String driver,
            @Value("${db.username}") String user,
            @Value("${db.password}") String password,
            @Value("${db.jdbc.url}") String url) {
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName(driver);
        //config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.addDataSourceProperty("user", user);
        config.addDataSourceProperty("password", password);
        config.addDataSourceProperty("url", url);
        //config.addDataSourceProperty("characterEncoding", "utf8");
        //config.addDataSourceProperty("useUnicode", "true");
        config.setConnectionTestQuery("select 1");
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
        config.setAutoCommit(false);
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        config.setPoolName("db-pool");
        return new HikariDataSource(config);
    }

    @Bean(name = TRANSACTION_MANAGER)
    public DataSourceTransactionManager txManager(
            @Value("${db.tx.timeout.seconds}") int timeoutSeconds,
            @Named(DATA_SOURCE) DataSource dataSource) {
        DataSourceTransactionManager result = new DataSourceTransactionManager(dataSource);
        result.setDefaultTimeout(timeoutSeconds);
        return result;
    }

    @Bean(name = DSL_CONTEXT)
    public DSLContext dslContext(
            @Value("${db.dialect}") String sqlDialect,
            @Named(DATA_SOURCE) DataSource dataSource) {
        return using(new DefaultConfiguration()
                .set(new DataSourceConnectionProvider(
                        new TransactionAwareDataSourceProxy(dataSource)))
                .set(valueOf(sqlDialect)));
    }
}
