package com.example.demo.config.datasource;

import java.util.HashMap;

import javax.sql.DataSource;

import com.example.demo.config.TenantConnectionProvider;
import com.example.demo.config.TenantSchemaResolver;
import com.zaxxer.hikari.HikariDataSource;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.Environment;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.demo.dao.tenant", entityManagerFactoryRef = "productEntityManager", transactionManagerRef = "productTransactionManager")
public class ProductDataSourceConfiguration {

	@Bean
	@ConfigurationProperties(prefix = "app.datasource.product")
	public DataSourceProperties productDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	public DataSource productDataSource() {
		return productDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean productEntityManager(DataSource dataSource,
			TenantConnectionProvider multiTenantConnectionProviderImpl,
			TenantSchemaResolver currentTenantIdentifierResolverImpl) {
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
	public PlatformTransactionManager productTransactionManager(
			TenantConnectionProvider multiTenantConnectionProviderImpl,
			TenantSchemaResolver currentTenantIdentifierResolverImpl) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(productEntityManager(productDataSource(),
				multiTenantConnectionProviderImpl, currentTenantIdentifierResolverImpl).getObject());
		return transactionManager;
	}
}
