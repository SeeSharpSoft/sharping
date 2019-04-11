package net.seesharpsoft.spring.test;

import net.seesharpsoft.spring.test.model.Country;
import net.seesharpsoft.spring.test.model.User;

public class ObjectMother {

    private ObjectMother() {
        //static
    }

    public static final User getUserAbby() {
        return new User(1, "Abby", "Z", "abby@mail.com");
    }
    public static final User getUserBob() {
        return new User(2, "Bob", "Y", "bob@mail.com");
    }
    public static final User getUserCarla() {
        return new User(3, "Carla", "X", "carla@mail.com");
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
}
