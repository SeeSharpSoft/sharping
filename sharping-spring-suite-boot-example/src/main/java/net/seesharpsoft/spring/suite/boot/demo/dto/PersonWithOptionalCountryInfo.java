package net.seesharpsoft.spring.suite.boot.demo.dto;

import jakarta.persistence.criteria.JoinType;
import lombok.AllArgsConstructor;
import net.seesharpsoft.spring.data.jpa.selectable.Join;
import net.seesharpsoft.spring.data.jpa.selectable.Joins;
import net.seesharpsoft.spring.data.jpa.selectable.Select;
import net.seesharpsoft.spring.data.jpa.selectable.Selectable;
import net.seesharpsoft.spring.suite.boot.demo.model.Person;

@Selectable(
        from = Person.class,
        joins = @Joins({
                @Join(value = "country", type = JoinType.LEFT, alias = "personCountry"),
                @Join(value = "personCountry/people", type = JoinType.LEFT, alias = "personCountryPeople")
        })
)
@AllArgsConstructor
public class PersonWithOptionalCountryInfo {

    private int id;

    @Select("firstName || ' ' || lastName")
    private String fullName;

    @Select("personCountry/name")
    private String country;

    @Select("COUNT(personCountryPeople/id)")
    private long countrySharingPeopleCount;
}
