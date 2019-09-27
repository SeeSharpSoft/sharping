package net.seesharpsoft.spring.suite.test.selectable;

import net.seesharpsoft.spring.data.jpa.selectable.Select;
import net.seesharpsoft.spring.data.jpa.selectable.Selectable;
import net.seesharpsoft.spring.suite.test.model.User;

@Selectable(
        from = User.class
)
public class SimpleUser {

    private int id;

    @Select("lastName")
    private String fullName;

}
