package net.seesharpsoft.spring.test.selectable;

import lombok.AllArgsConstructor;
import net.seesharpsoft.spring.data.jpa.selectable.Select;
import net.seesharpsoft.spring.data.jpa.selectable.Selectable;
import net.seesharpsoft.spring.test.model.User;

@Selectable(
        from = User.class
)
@AllArgsConstructor
public class UserWithCountryInfo {

    private int id;

    @Select("firstName || ' ' || lastName")
    private String fullName;

    @Select("country/name")
    private String country;

    @Select("COUNT(country/users/id)")
    private long countrySharingUserCount;
}
