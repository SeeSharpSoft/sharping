package net.seesharpsoft.spring.test.selectable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.seesharpsoft.spring.data.jpa.selectable.Join;
import net.seesharpsoft.spring.data.jpa.selectable.Joins;
import net.seesharpsoft.spring.data.jpa.selectable.Select;
import net.seesharpsoft.spring.data.jpa.selectable.Selectable;
import net.seesharpsoft.spring.test.model.Country;

@Selectable(
        from = Country.class,
        joins = @Joins({
                @Join(value = "users", alias = "allUsers"),
                @Join(value = "allUsers.teams", alias = "allUsersTeams")
        })
)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CountryInfo {

    private int id;

    private String name;

    @Select("COUNT_DISTINCT(allUsers.id)")
    private long userCount;

    @Select("COUNT_DISTINCT(allUsersTeams.id)")
    private long teamCount;
}
