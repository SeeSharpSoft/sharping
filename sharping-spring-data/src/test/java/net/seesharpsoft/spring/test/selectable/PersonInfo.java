package net.seesharpsoft.spring.test.selectable;

import net.seesharpsoft.spring.data.jpa.selectable.Select;
import net.seesharpsoft.spring.data.jpa.selectable.Selectable;
import net.seesharpsoft.spring.test.model.Person;

@Selectable(
        from = Person.class
)
public record PersonInfo(
        int id,
        @Select("firstName || ' ' || lastName")
        String fullName,
        String mail
) { }
