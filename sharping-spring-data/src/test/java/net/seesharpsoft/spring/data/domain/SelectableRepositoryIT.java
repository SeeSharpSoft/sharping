package net.seesharpsoft.spring.data.domain;

import net.seesharpsoft.spring.data.domain.impl.SelectableRepositoryFactoryImpl;
import net.seesharpsoft.spring.data.domain.impl.SqlParserImpl;
import net.seesharpsoft.spring.data.jpa.OperationSpecification;
import net.seesharpsoft.spring.data.jpa.expression.Dialects;
import net.seesharpsoft.spring.data.jpa.expression.Operands;
import net.seesharpsoft.spring.data.jpa.expression.Operations;
import net.seesharpsoft.spring.test.ObjectMother;
import net.seesharpsoft.spring.test.TestApplication;
import net.seesharpsoft.spring.test.model.User;
import net.seesharpsoft.spring.test.selectable.CountryUser;
import net.seesharpsoft.spring.test.selectable.SelectableUser;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= TestApplication.class)
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
        abby.setCountry(entityManager.merge(ObjectMother.getCountryDE()));
        bob.setCountry(entityManager.merge(ObjectMother.getCountryDE()));
        carla.setCountry(entityManager.merge(ObjectMother.getCountryFR()));
        abby = entityManager.merge(abby);
        bob = entityManager.merge(bob);
        carla = entityManager.merge(carla);
        entityManager.merge(new User(100, "UNKNOWN", null, null));
        entityManager.flush();
    }

    protected SelectableRepository getSelectableRepository(Class selectableClass) {
        SelectableRepositoryFactory factory = new SelectableRepositoryFactoryImpl(entityManager,  new SqlParserImpl(Dialects.SQL.getParser()));
        return factory.createRepository(selectableClass);
    }

    @Test
    public void should_simple_find_all() {
        SelectableRepository<SelectableUser> repo = getSelectableRepository(SelectableUser.class);

        List<SelectableUser> resultList = repo.findAll();

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
        SelectableRepository<SelectableUser> repo = getSelectableRepository(SelectableUser.class);

        List<SelectableUser> resultList = repo.findAll(
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
        SelectableRepository<SelectableUser> repo = getSelectableRepository(SelectableUser.class);

        List<SelectableUser> resultList = repo.findAll(
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
        SelectableRepository<SelectableUser> repo = getSelectableRepository(CountryUser.class);

        List<SelectableUser> resultList = repo.findAll(
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
}
