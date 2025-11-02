package net.seesharpsoft.spring.suite.test.selectable;

import net.seesharpsoft.spring.data.jpa.selectable.Select;
import net.seesharpsoft.spring.data.jpa.selectable.Selectable;
import net.seesharpsoft.spring.suite.test.model.Person;

@Selectable(
        from = Person.class
)
public class SelectableUser {

    private int id;

    @Select("firstName")
    private String fullName;
}
