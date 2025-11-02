package net.seesharpsoft.spring.multipart.boot.demo.service;

import net.seesharpsoft.spring.multipart.boot.demo.entity.Article;
import net.seesharpsoft.spring.multipart.boot.demo.repository.ArticleRepository;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import java.util.List;

@Service
public class ArticleService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ArticleRepository repository;

    private Session getSession() {
        return entityManager.unwrap(Session.class);
    }

    public List<Article> getArticles() {
        return repository.findAll();
    }
}
