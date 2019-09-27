package net.seesharpsoft.spring.data.domain;

import net.seesharpsoft.spring.data.domain.impl.SelectableRepositoryFactoryImpl;
import net.seesharpsoft.spring.data.domain.impl.SqlParserImpl;
import net.seesharpsoft.spring.data.jpa.OperationSpecification;
import net.seesharpsoft.spring.data.jpa.expression.Dialects;
import net.seesharpsoft.spring.data.jpa.expression.Operands;
import net.seesharpsoft.spring.data.jpa.expression.Operations;
import net.seesharpsoft.spring.test.ObjectMother;
import net.seesharpsoft.spring.test.TestApplication;
import net.seesharpsoft.spring.test.model.Team;
import net.seesharpsoft.spring.test.model.User;
import net.seesharpsoft.spring.test.selectable.CountryInfo;
import net.seesharpsoft.spring.test.selectable.UserWithCountryInfo;
import net.seesharpsoft.spring.test.selectable.UserInfo;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureDataJpa
@Transactional
public class SelectableRepositoryIT {

    @Autowired
    private EntityManager entityManager;

    private SelectableRepository selectableRepository;

    User abby, bob, carla;

    @Before
    public void beforeEach() {
        abby = ObjectMother.getUserAbby();
        bob = ObjectMother.getUserBob();
        carla = ObjectMother.getUserCarla();
        Team teamA = entityManager.merge(ObjectMother.getTeamA());
        Team teamB = entityManager.merge(ObjectMother.getTeamB());
        abby.setCountry(entityManager.merge(ObjectMother.getCountryDE()));
        abby.setTeams(new HashSet<>(Arrays.asList(teamA, teamB)));
        bob.setCountry(entityManager.merge(ObjectMother.getCountryDE()));
        bob.setTeams(new HashSet<>(Arrays.asList(teamA)));
        carla.setCountry(entityManager.merge(ObjectMother.getCountryFR()));
        carla.setTeams(new HashSet<>(Arrays.asList(teamB)));
        abby = entityManager.merge(abby);
        bob = entityManager.merge(bob);
        carla = entityManager.merge(carla);
        entityManager.merge(new User(100, "UNKNOWN", null, null));
        entityManager.flush();
        entityManager.clear();
    }

    protected SelectableRepository getSelectableRepository(Class selectableClass) {
        SelectableRepositoryFactory factory = new SelectableRepositoryFactoryImpl(entityManager, new SqlParserImpl(Dialects.SQL.getParser()));
        return factory.createRepository(selectableClass);
    }

    @Test
    public void should_simple_find_all() {
        SelectableRepository<UserInfo> repo = getSelectableRepository(UserInfo.class);

        List<UserInfo> resultList = repo.findAll();

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
        SelectableRepository<UserInfo> repo = getSelectableRepository(UserInfo.class);

        List<UserInfo> resultList = repo.findAll(
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
        SelectableRepository<UserWithCountryInfo> repo = getSelectableRepository(UserWithCountryInfo.class);

        List<UserWithCountryInfo> resultList = repo.findAll(
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
        SelectableRepository<UserWithCountryInfo> repo = getSelectableRepository(UserWithCountryInfo.class);

        List<UserWithCountryInfo> resultList = repo.findAll(
                Sort.by(Sort.Direction.ASC, "lastName")
        );

        assertThat(resultList)
                .extracting("id", "fullName", "country", "countrySharingUserCount")
                .containsExactly(
                        Tuple.tuple(100, null, null, 0L),
                        Tuple.tuple(3, "Carla X", carla.getCountry().getName(), 1L),
                        Tuple.tuple(2, "Bob Y", bob.getCountry().getName(), 2L),
                        Tuple.tuple(1, "Abby Z", abby.getCountry().getName(), 2L)
                );
    }

    @Test
    public void should_find_one_with_join() {
        SelectableRepository<UserWithCountryInfo> repo = getSelectableRepository(UserWithCountryInfo.class);

        UserWithCountryInfo countryUser = repo.findOne(
                new OperationSpecification<>(Operations.equals(Operands.asReference("mail"), abby.getMail()))
        ).orElse(null);

        assertThat(countryUser)
                .extracting("id", "fullName", "country", "countrySharingUserCount")
                .containsExactly(1, "Abby Z", abby.getCountry().getName(), 2L);
    }

    @Test
    public void should_find_one_with_join_and_correct_count_via_alias() {
        SelectableRepository<CountryInfo> repo = getSelectableRepository(CountryInfo.class);

        CountryInfo countryInfo = repo.findOne(
                new OperationSpecification<>(
                        Operations.equals(Operands.asReference("users.mail"), abby.getMail())
                )
        ).orElse(null);

        assertThat(countryInfo)
                .extracting("id", "name", "userCount", "teamCount")
                .containsExactly(abby.getCountry().getId(), abby.getCountry().getName(), 2L, 2L);
    }
}
