package net.seesharpsoft.spring.suite.boot.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    public Person(int id, String firstName, String lastName, String mail) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mail = mail;
    }

    @Id
    private int id;

    private String firstName;

    private String lastName;

    private String mail;

    @ManyToOne
    private Country country;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Team> teams;
}
