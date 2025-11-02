package net.seesharpsoft.spring.multipart.boot.demo.entity;

import jakarta.persistence.*;
import java.util.Set;

@Entity
public class Person {
    @Id
    @GeneratedValue
    private Long id;

    private String firstName;

    @OneToMany(mappedBy = "author")
    private Set<Article> articles;
}

