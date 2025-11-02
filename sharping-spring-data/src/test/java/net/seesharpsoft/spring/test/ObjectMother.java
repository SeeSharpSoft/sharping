package net.seesharpsoft.spring.test;

import net.seesharpsoft.spring.test.model.Country;
import net.seesharpsoft.spring.test.model.Team;
import net.seesharpsoft.spring.test.model.Person;

public class ObjectMother {

    private ObjectMother() {
        //static
    }

    public static final Person getPersonAbby() {
        return new Person(1, "Abby", "Z", "abby@mail.com");
    }
    public static final Person getPersonBob() {
        return new Person(2, "Bob", "Y", "bob@mail.com");
    }
    public static final Person getPersonCarla() {
        return new Person(3, "Carla", "X", "carla@mail.com");
    }

    public static final Country getCountryDE() {
        return new Country(1, "DE", "Germany");
    }
    public static final Country getCountryFR() {
        return new Country(2, "FR", "France");
    }
    public static final Country getCountryUS() {
        return new Country(3, "US", "United States");
    }

    public static final Team getTeamA() {
        return new Team(1, "Team A");
    }
    public static final Team getTeamB() {
        return new Team(2, "Team B");
    }
}
