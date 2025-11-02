package net.seesharpsoft.spring.suite.test;

import net.seesharpsoft.spring.data.domain.SelectableRepositoryFactory;
import net.seesharpsoft.spring.data.domain.SqlParser;
import net.seesharpsoft.spring.data.domain.impl.SelectableRepositoryFactoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import jakarta.persistence.EntityManager;

@SpringBootApplication

public class TestApplication {

     @Bean
     SelectableRepositoryFactory selectableRepositoryFactory(EntityManager entityManager, SqlParser parser) {
         return new SelectableRepositoryFactoryImpl(entityManager, parser);
     }

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
