package net.seesharpsoft.spring.test.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    public User(int id, String firstName, String lastName, String mail) {
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
