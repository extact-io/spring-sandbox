package sample.spring;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

import ch.qos.logback.access.tomcat.LogbackValve;
import sample.spring.health.SimpleCompositeHealthConfiguration;

@SpringBootApplication
@EnableJpaRepositories(
        basePackageClasses = Message.class,
        entityManagerFactoryRef = "test1EntityManagerFactory",
        transactionManagerRef = "test1TransactionManager"
    )
@EnableAsync
@Import(SimpleCompositeHealthConfiguration.class)
public class HelloApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloApplication.class, args);
    }

    @Bean
    @ConditionalOnMissingBean
    HelloService helloService() {
        return new HelloServiceByConfig();
    }

    @Bean
    TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcatServletWebServerFactory = new TomcatServletWebServerFactory();
        LogbackValve valve = new LogbackValve();
        valve.setFilename(LogbackValve.DEFAULT_FILENAME);
        tomcatServletWebServerFactory.addContextValves(valve);
        return tomcatServletWebServerFactory;
    }

    @Bean
    HttpExchangeRepository httpTraceRepository() {
        InMemoryHttpExchangeRepository repository = new InMemoryHttpExchangeRepository();
        repository.setCapacity(1000);
        return repository;
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.test1")
    DataSourceProperties test1DataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.test1.hikari")
    HikariDataSource test1DataSource(@Qualifier("test1DataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.test1.jpa")
    public JpaProperties test1JpaProperties() {
        return new JpaProperties();
    }

    @Bean
    @Primary
    LocalContainerEntityManagerFactoryBean test1EntityManagerFactory(
            @Qualifier("test1DataSource") DataSource dataSource,
            @Qualifier("test1JpaProperties") JpaProperties jpaProperties) {
        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(),
                jpaProperties.getProperties(), null);
        return builder
                .dataSource(dataSource)
                .packages("sample.spring")
                .persistenceUnit("test1")
                .properties(Map.of("hibernate.hbm2ddl.auto", "create"))
                .build();
    }

    @Bean
    @Primary
    PlatformTransactionManager test1TransactionManager(
            @Qualifier("test1EntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }

    @Bean
    DataSourceInitializer test1DataSourceInitializer(@Qualifier("test1DataSource") DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("init-data.sql")));
        return initializer;
    }

    // -------------- test2
    @Bean
    @ConfigurationProperties("spring.datasource.test2")
    DataSourceProperties test2DataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.test2.hikari")
    DataSource test2DataSource(@Qualifier("test2DataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.test2.jpa")
    public JpaProperties test2JpaProperties() {
        return new JpaProperties();
    }

    @Bean
    LocalContainerEntityManagerFactoryBean test2EntityManagerFactory(
            @Qualifier("test2DataSource") DataSource dataSource,
            @Qualifier("test2JpaProperties") JpaProperties jpaProperties) {
        EntityManagerFactoryBuilder builder = new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(),
                jpaProperties.getProperties(), null);
        return builder
                .dataSource(dataSource)
                .packages("sample.spring")
                .persistenceUnit("test2")
                .properties(Map.of("hibernate.hbm2ddl.auto", "create"))
                .build();
    }

    @Bean
    PlatformTransactionManager test2TransactionManager(
            @Qualifier("test2EntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }


}
