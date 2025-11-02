package net.seesharpsoft.spring.test.selectable;

import lombok.AllArgsConstructor;
import net.seesharpsoft.spring.data.jpa.selectable.Select;
import net.seesharpsoft.spring.data.jpa.selectable.Selectable;
import net.seesharpsoft.spring.test.model.Person;

@Selectable(
        from = Person.class
)
@AllArgsConstructor
public class PersonInfo {

    private int id;

    @Select("firstName || ' ' || lastName")
    private String fullName;
}
