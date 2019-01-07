package net.seesharpsoft.spring.multipart.boot.demo.entity;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Tag {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(name = "ArticleTags", joinColumns = @JoinColumn(name = "tagId"), inverseJoinColumns = @JoinColumn(name = "articleId"))
    private Set<Article> articles;
}
