package com.example.demo.config.datasource;

import com.zaxxer.hikari.HikariDataSource;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.demo.dao.common", entityManagerFactoryRef = "primeEntityManager", transactionManagerRef = "primeTransactionManager")
public class PrimeDataSourceConfiguration {

    @Autowired
    Environment env;

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "app.datasource.prime")
    public DataSourceProperties primeDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource primeDataSource() {
        HikariDataSource dataSource = primeDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
        SpringLiquibase migrationExecutor = new SpringLiquibase();
        migrationExecutor.setDataSource(dataSource);
        migrationExecutor.setChangeLog("classpath:/db/main/db.changelog-master.xml");
        try {
            migrationExecutor.afterPropertiesSet();
        } catch (LiquibaseException e) {
            System.err.println("La migration de la BD principale a planté");
            e.printStackTrace();
        }

        return dataSource;
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean primeEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(primeDataSource());
        em.setPackagesToScan("com.example.demo.dao.common");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    @Primary
    public PlatformTransactionManager primeTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(primeEntityManager().getObject());
        return transactionManager;
    }
}