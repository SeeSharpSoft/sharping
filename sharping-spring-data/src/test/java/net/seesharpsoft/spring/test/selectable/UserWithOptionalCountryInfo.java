package net.seesharpsoft.spring.test.selectable;

import lombok.AllArgsConstructor;
import net.seesharpsoft.spring.data.jpa.selectable.Join;
import net.seesharpsoft.spring.data.jpa.selectable.Joins;
import net.seesharpsoft.spring.data.jpa.selectable.Select;
import net.seesharpsoft.spring.data.jpa.selectable.Selectable;
import net.seesharpsoft.spring.test.model.User;

import javax.persistence.criteria.JoinType;

@Selectable(
        from = User.class,
        joins = @Joins({
                @Join(value = "country", type = JoinType.LEFT, alias = "userCountry"),
                @Join(value = "userCountry/users", type = JoinType.LEFT, alias = "userCountryUsers")
        })
)
@AllArgsConstructor
public class UserWithOptionalCountryInfo {

    private int id;

    @Select("firstName || ' ' || lastName")
    private String fullName;

    @Select("userCountry/name")
    private String country;

    @Select("COUNT(userCountryUsers/id)")
    private long countrySharingUserCount;
}
