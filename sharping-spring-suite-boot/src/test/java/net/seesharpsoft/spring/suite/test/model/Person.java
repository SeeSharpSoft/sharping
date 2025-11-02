package net.seesharpsoft.spring.suite.test.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Person {

    @Id
    private int id;

    private String firstName;

    private String lastName;

    private String mail;
}
