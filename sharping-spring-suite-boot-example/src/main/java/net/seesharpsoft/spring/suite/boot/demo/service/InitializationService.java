package net.seesharpsoft.spring.suite.boot.demo.service;

import net.seesharpsoft.spring.suite.boot.demo.model.Country;
import net.seesharpsoft.spring.suite.boot.demo.model.Person;
import net.seesharpsoft.spring.suite.boot.demo.model.Team;
import net.seesharpsoft.spring.suite.boot.demo.repository.CountryRepository;
import net.seesharpsoft.spring.suite.boot.demo.repository.PersonRepository;
import net.seesharpsoft.spring.suite.boot.demo.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

@Service
public class InitializationService {

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
    public static final Country getCountryUS2() {
        return new Country(4, "US2", "Un-united States");
    }

    public static final Team getTeamA() {
        return new Team(1, "Team A");
    }
    public static final Team getTeamB() {
        return new Team(2, "Team B");
    }

    @Autowired
    CountryRepository countryRepository;
    @Autowired
    PersonRepository personRepository;
    @Autowired
    TeamRepository teamRepository;

    public void init() {
        Person abby = personRepository.save(getPersonAbby());
        Person bob = personRepository.save(getPersonBob());
        Person carla = personRepository.save(getPersonCarla());
        Team teamA = teamRepository.save(getTeamA());
        Team teamB = teamRepository.save(getTeamB());
        Country germany = countryRepository.save(getCountryDE());
        Country france = countryRepository.save(getCountryFR());
        Country us = countryRepository.save(getCountryUS());
        Country us2 = countryRepository.save(getCountryUS2());
        abby.setCountry(germany);
        abby.setTeams(new HashSet<>(Arrays.asList(teamA, teamB)));
        bob.setCountry(germany);
        bob.setTeams(new HashSet<>(Collections.singletonList(teamA)));
        carla.setCountry(france);
        carla.setTeams(new HashSet<>(Collections.singletonList(teamB)));
        abby = personRepository.save(abby);
        bob =personRepository.save(bob);
        carla = personRepository.save(carla);
        personRepository.save(new Person(100, "UNKNOWN", null, null));
    }
}
