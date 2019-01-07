package net.seesharpsoft.spring.multipart.boot.demo.repository;

import net.seesharpsoft.spring.multipart.boot.demo.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}
