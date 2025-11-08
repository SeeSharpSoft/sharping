package net.seesharpsoft.spring.test.selectable;

import net.seesharpsoft.spring.data.jpa.selectable.Join;
import net.seesharpsoft.spring.data.jpa.selectable.Joins;
import net.seesharpsoft.spring.data.jpa.selectable.Select;
import net.seesharpsoft.spring.data.jpa.selectable.Selectable;
import net.seesharpsoft.spring.test.model.Country;

@Selectable(
        from = Country.class,
        joins = @Joins(
                @Join(value = "people", alias = "allPeople")
        )
)
public record CountryInfo(
    int id,

    String name,

    @Select("COUNT_DISTINCT(allPeople.id)")
    long peopleCount,

    @Select("COUNT_DISTINCT(allPeople.teams.id)")
    long teamCount
) {}
