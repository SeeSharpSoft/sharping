package net.seesharpsoft.spring.data.domain;

import net.seesharpsoft.spring.data.domain.impl.SelectableRepositoryFactoryImpl;
import net.seesharpsoft.spring.data.domain.impl.SqlParserImpl;
import net.seesharpsoft.spring.data.jpa.OperationSpecification;
import net.seesharpsoft.spring.data.jpa.expression.Dialects;
import net.seesharpsoft.spring.data.jpa.expression.Operands;
import net.seesharpsoft.spring.data.jpa.expression.Operations;
import net.seesharpsoft.spring.test.ObjectMother;
import net.seesharpsoft.spring.test.TestApplication;
import net.seesharpsoft.spring.test.model.Country;
import net.seesharpsoft.spring.test.model.Team;
import net.seesharpsoft.spring.test.model.Person;
import net.seesharpsoft.spring.test.selectable.CountryInfo;
import net.seesharpsoft.spring.test.selectable.PersonInfo;
import net.seesharpsoft.spring.test.selectable.PersonWithCountryInfo;
import net.seesharpsoft.spring.test.selectable.PersonWithOptionalCountryInfo;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;

import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureDataJpa
@Transactional
public class SelectableRepositoryIT {

    @Autowired
    private EntityManager entityManager;

    Person abby, bob, carla;
    Country france, germany, us;

    @BeforeEach
    public void beforeEach() {
        abby = ObjectMother.getPersonAbby();
        bob = ObjectMother.getPersonBob();
        carla = ObjectMother.getPersonCarla();
        Team teamA = entityManager.merge(ObjectMother.getTeamA());
        Team teamB = entityManager.merge(ObjectMother.getTeamB());
        germany = entityManager.merge(ObjectMother.getCountryDE());
        france = entityManager.merge(ObjectMother.getCountryFR());
        us = entityManager.merge(ObjectMother.getCountryUS());
        abby.setCountry(germany);
        abby.setTeams(new HashSet<>(Arrays.asList(teamA, teamB)));
        bob.setCountry(germany);
        bob.setTeams(new HashSet<>(Collections.singletonList(teamA)));
        carla.setCountry(france);
        carla.setTeams(new HashSet<>(Collections.singletonList(teamB)));
        abby = entityManager.merge(abby);
        bob = entityManager.merge(bob);
        carla = entityManager.merge(carla);
        entityManager.merge(new Person(100, "UNKNOWN", null, null));
        entityManager.flush();
        entityManager.clear();
    }

    protected SelectableRepository getSelectableRepository(Class selectableClass) {
        SelectableRepositoryFactory factory = new SelectableRepositoryFactoryImpl(entityManager, new SqlParserImpl(Dialects.SQL.getParser()));
        return factory.createRepository(selectableClass);
    }

    @Test
    public void should_simple_find_all() {
        SelectableRepository<PersonInfo> repo = getSelectableRepository(PersonInfo.class);

        List<PersonInfo> resultList = repo.findAll();

        assertThat(resultList)
                .extracting("id", "fullName")
                .containsExactlyInAnyOrder(
                        Tuple.tuple(1, "Abby Z"),
                        Tuple.tuple(2, "Bob Y"),
                        Tuple.tuple(3, "Carla X"),
                        Tuple.tuple(100, null)
                );
    }

    @Test
    public void should_find_all_with_mail() {
        SelectableRepository<PersonInfo> repo = getSelectableRepository(PersonInfo.class);

        List<PersonInfo> resultList = repo.findAll(
                new OperationSpecification<>(
                        Operations.not(Operations.equals(Operands.asReference("mail"), null))
                )
        );

        assertThat(resultList)
                .extracting("id", "fullName")
                .containsExactlyInAnyOrder(
                        Tuple.tuple(1, "Abby Z"),
                        Tuple.tuple(2, "Bob Y"),
                        Tuple.tuple(3, "Carla X")
                );
    }

    @Test
    public void should_find_all_sorted() {
        SelectableRepository<PersonWithCountryInfo> repo = getSelectableRepository(PersonWithCountryInfo.class);

        List<PersonWithCountryInfo> resultList = repo.findAll(
                Sort.by(Sort.Direction.ASC, "lastName")
        );

        assertThat(resultList)
                .extracting("id", "fullName")
                .containsExactly(
                        Tuple.tuple(100, null),
                        Tuple.tuple(3, "Carla X"),
                        Tuple.tuple(2, "Bob Y"),
                        Tuple.tuple(1, "Abby Z")

                );
    }

    @Test
    public void should_find_all_with_join() {
        SelectableRepository<PersonWithCountryInfo> repo = getSelectableRepository(PersonWithCountryInfo.class);

        List<PersonWithCountryInfo> resultList = repo.findAll(
                Sort.by(Sort.Direction.ASC, "lastName")
        );

        assertThat(resultList)
                .extracting("id", "fullName", "country", "countrySharingPeopleCount")
                .containsExactly(
                        Tuple.tuple(100, null, null, 0L),
                        Tuple.tuple(3, "Carla X", carla.getCountry().getName(), 1L),
                        Tuple.tuple(2, "Bob Y", bob.getCountry().getName(), 2L),
                        Tuple.tuple(1, "Abby Z", abby.getCountry().getName(), 2L)
                );
    }

    @Test
    public void should_find_all_sorted_with_optional() {
        SelectableRepository<PersonWithOptionalCountryInfo> repo = getSelectableRepository(PersonWithOptionalCountryInfo.class);

        List<PersonWithOptionalCountryInfo> resultList = repo.findAll(
                Sort.by(Sort.Direction.ASC, "lastName")
        );

        assertThat(resultList)
                .extracting("id", "fullName", "country", "countrySharingPeopleCount")
                .containsExactly(
                        Tuple.tuple(100, null, null, 0L),
                        Tuple.tuple(3, "Carla X", carla.getCountry().getName(), 1L),
                        Tuple.tuple(2, "Bob Y", bob.getCountry().getName(), 2L),
                        Tuple.tuple(1, "Abby Z", abby.getCountry().getName(), 2L)
                );
    }

    @Test
    public void should_find_one_with_join() {
        SelectableRepository<PersonWithCountryInfo> repo = getSelectableRepository(PersonWithCountryInfo.class);

        PersonWithCountryInfo countryUser = repo.findOne(
                new OperationSpecification<>(Operations.equals(Operands.asReference("mail"), abby.getMail()))
        ).orElse(null);

        assertThat(countryUser)
                .extracting("id", "fullName", "country", "countrySharingPeopleCount")
                .containsExactly(1, "Abby Z", abby.getCountry().getName(), 2L);
    }

    @Test
    public void should_find_one_with_join_and_correct_count_via_alias() {
        SelectableRepository<CountryInfo> repo = getSelectableRepository(CountryInfo.class);

        CountryInfo countryInfo = repo.findOne(
                new OperationSpecification<>(
                        Operations.equals(Operands.asReference("people.mail"), abby.getMail())
                )
        ).orElse(null);

        assertThat(countryInfo)
                .extracting("id", "name", "peopleCount", "teamCount")
                .containsExactly(abby.getCountry().getId(), abby.getCountry().getName(), 2L, 2L);
    }

    @Test
    public void should_find_all_sorted_by_country_name() {
        SelectableRepository<CountryInfo> repo = getSelectableRepository(CountryInfo.class);

        List<CountryInfo> countryInfos = repo.findAll(
                new OperationSpecification<>(
                        Operations.not(Operations.equals(Operands.asReference("people.mail"), null))
                ),
                Sort.by(Sort.Direction.ASC, "name")
        );

        assertThat(countryInfos)
                .extracting("id", "name", "peopleCount", "teamCount")
                .containsExactly(
                        Tuple.tuple(france.getId(), france.getName(), 1L, 1L),
                        Tuple.tuple(germany.getId(), germany.getName(), 2L, 2L)
                );
    }

    @Test
    public void should_find_all_sorted_desc_by_userCount() {
        SelectableRepository<CountryInfo> repo = getSelectableRepository(CountryInfo.class);

        List<CountryInfo> countryInfos = repo.findAll(
                new OperationSpecification<>(
                        Operations.not(Operations.equals(Operands.asReference("people.mail"), null))
                ),
                Sort.by(Sort.Direction.DESC, "peopleCount")
        );

        assertThat(countryInfos)
                .extracting("id", "name", "peopleCount", "teamCount")
                .containsExactly(
                        Tuple.tuple(germany.getId(), germany.getName(), 2L, 2L),
                        Tuple.tuple(france.getId(), france.getName(), 1L, 1L)
                );
    }

    @Test
    public void should_count() {
        SelectableRepository<CountryInfo> repo = getSelectableRepository(CountryInfo.class);

        long count = repo.count();

        assertThat(count).isEqualTo(3);
    }

    @Test
    public void should_count_with_specification() {
        SelectableRepository<CountryInfo> repo = getSelectableRepository(CountryInfo.class);

        long count = repo.count(
                new OperationSpecification<>(
                        Operations.not(Operations.equals(Operands.asReference("people.mail"), null))
                )
        );

        assertThat(count).isEqualTo(2);
    }
}
