package net.seesharpsoft.spring.suite.boot.demo.dto;

import net.seesharpsoft.spring.data.jpa.selectable.*;
import net.seesharpsoft.spring.suite.boot.demo.model.Country;

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
