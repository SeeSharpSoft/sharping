package net.seesharpsoft.spring.suite.test.selectable;

import net.seesharpsoft.spring.data.jpa.selectable.Select;
import net.seesharpsoft.spring.data.jpa.selectable.Selectable;
import net.seesharpsoft.spring.suite.test.model.User;

@Selectable(
        from = User.class
)
public class SelectableUser {

    private int id;

    @Select("firstName")
    private String fullName;
}
