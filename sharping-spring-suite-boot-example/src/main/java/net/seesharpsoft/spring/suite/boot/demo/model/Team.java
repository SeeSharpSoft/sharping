package net.seesharpsoft.spring.suite.boot.demo.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
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
public class Team {

    public Team(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Id
    private int id;

    private String name;

    @ManyToMany(mappedBy = "teams", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private Set<Person> people;
}
