package net.seesharpsoft.spring.suite.test.model;

import jdk.nashorn.internal.objects.annotations.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {

    @Id
    private int id;

    private String firstName;

    private String lastName;

    private String mail;
}
