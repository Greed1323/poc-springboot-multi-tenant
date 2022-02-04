package com.example.demo.config.datasource;

import com.example.demo.config.connexion.TenantConnectionProvider;
import com.example.demo.config.connexion.TenantSchemaResolver;
import com.example.demo.dao.common.WorkspaceCommonRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import liquibase.integration.spring.MultiTenantSpringLiquibase;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.demo.dao.tenant", entityManagerFactoryRef = "productEntityManager", transactionManagerRef = "productTransactionManager")
public class ProductDataSourceConfiguration {

    @Autowired
    private DataSourceProperties properties;

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    private WorkspaceCommonRepository wsRepo;

    private final static Map<Object, Object> resolvedDataSources = new HashMap<>();

    public static Map<Object, Object> getDataSources() {
        return resolvedDataSources;
    }

    @Bean
    @ConfigurationProperties(prefix = "app.datasource.product")
    public DataSourceProperties productDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public MultitenantDataSource productDataSource() {

        DataSource defaultDataSource = defaultDataSource();

        List<String> tenants = wsRepo.findAll().stream().map(e -> e.getName()).collect(Collectors.toList());
        for (String tenant : tenants) {
            Properties tenantProperties = new Properties();
            DataSourceBuilder dataSourceBuilder = DataSourceBuilder.derivedFrom(defaultDataSource);

            dataSourceBuilder.url(properties.getUrl() + "?currentSchema=" + tenant);

            resolvedDataSources.put(tenant, dataSourceBuilder.build());
        }

        // Create the final multi-tenant source.
        // It needs a default database to connect to.
        // Make sure that the default database is actually an empty tenant database.
        // Don't use that for a regular tenant if you want things to be safe!
        MultitenantDataSource dataSource = new MultitenantDataSource();
        dataSource.setDefaultTargetDataSource(defaultDataSource);
        dataSource.setTargetDataSources(resolvedDataSources);

        // Call this to finalize the initialization of the data source.
        dataSource.afterPropertiesSet();

        MultiTenantSpringLiquibase msl = new MultiTenantSpringLiquibase();
        msl.setDataSource(dataSource);
        msl.setChangeLog("classpath:/db/tenant/db.changelog-master.xml");
        msl.setResourceLoader(resourceLoader);
        msl.setSchemas(tenants);
        msl.setShouldRun(true);
        try {
            msl.afterPropertiesSet();
        } catch (Exception e1) {
            System.err.println("La migration de la BD des tenants a planté");
            e1.printStackTrace();
        }

        return dataSource;
    }

    /**
     * Creates the default data source for the application
     * 
     * @return
     */
    private DataSource defaultDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create(this.getClass().getClassLoader())
                .driverClassName(properties.getDriverClassName())
                .url(properties.getUrl())
                .username(properties.getUsername())
                .password(properties.getPassword());

        if (properties.getType() != null) {
            dataSourceBuilder.type(properties.getType());
        }

        return dataSourceBuilder.build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean productEntityManager(DataSource dataSource,
        TenantConnectionProvider multiTenantConnectionProviderImpl, TenantSchemaResolver currentTenantIdentifierResolverImpl) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.demo.dao.tenant");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        HashMap<String, Object> properties = new HashMap<>();
        properties.put(Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
        properties.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProviderImpl);
        properties.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolverImpl);

        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    public PlatformTransactionManager productTransactionManager(TenantConnectionProvider multiTenantConnectionProviderImpl,
        TenantSchemaResolver currentTenantIdentifierResolverImpl) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
                productEntityManager(productDataSource(), multiTenantConnectionProviderImpl, currentTenantIdentifierResolverImpl)
                        .getObject());
        return transactionManager;
    }
}
