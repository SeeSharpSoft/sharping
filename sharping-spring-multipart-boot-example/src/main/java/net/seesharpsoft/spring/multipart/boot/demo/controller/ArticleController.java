package net.seesharpsoft.spring.multipart.boot.demo.controller;

import net.seesharpsoft.spring.multipart.boot.demo.entity.Article;
import net.seesharpsoft.spring.multipart.boot.demo.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @RequestMapping("/articles")
    public List<Article> getArticles() {
        return articleService.getArticles();
    }

}
