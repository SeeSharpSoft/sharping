package net.seesharpsoft.spring.suite.boot.demo.dto;

import net.seesharpsoft.spring.data.jpa.selectable.Select;
import net.seesharpsoft.spring.data.jpa.selectable.Selectable;
import net.seesharpsoft.spring.suite.boot.demo.model.Person;

@Selectable(
        from = Person.class
)
public record PersonInfo(
        int id,
        @Select("firstName || ' ' || lastName")
        String fullName,
        String mail
) { }
