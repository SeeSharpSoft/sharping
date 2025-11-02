package net.seesharpsoft.spring.multipart.boot.demo.entity;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
public class Article {
    @Id
    @GeneratedValue
    private Long id;

    private String title;

    @ManyToMany
    @JoinTable(name = "ArticleTags", joinColumns = @JoinColumn(name = "articleId"), inverseJoinColumns = @JoinColumn(name = "tagId"))
    private Set<Tag> tags;

    @ManyToOne
    private Person author;

//    @Type(type = "net.seesharpsoft.spring.multipart.boot.demo.util.ElementCollectionType")
    private List<String> languages;
}
