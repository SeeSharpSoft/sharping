package net.seesharpsoft.spring.suite.boot.demo.dto;

import lombok.AllArgsConstructor;
import net.seesharpsoft.spring.data.jpa.selectable.Select;
import net.seesharpsoft.spring.data.jpa.selectable.Selectable;
import net.seesharpsoft.spring.suite.boot.demo.model.Person;

@Selectable(
        from = Person.class
)
@AllArgsConstructor
public class PersonWithCountryInfo {

    private int id;

    @Select("firstName || ' ' || lastName")
    private String fullName;

    @Select("country/name")
    private String country;

    @Select("COUNT(country/people/id)")
    private long countrySharingPeopleCount;
}
