package net.seesharpsoft.spring.suite.boot;

import net.seesharpsoft.spring.data.domain.SelectableRepository;
import net.seesharpsoft.spring.data.domain.SelectableRepositoryFactory;
import net.seesharpsoft.spring.suite.test.TestApplication;
import net.seesharpsoft.spring.suite.test.selectable.SelectableUser;
import net.seesharpsoft.spring.suite.test.selectable.SimpleUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { TestApplication.class })
public class SelectableInterfaceIT {

    @Autowired(required = false)
    private SelectableRepositoryFactory selectableRepositoryFactory;

    @Autowired(required = false)
    private SelectableRepository<SelectableUser> selectableUserRepository;

    @Autowired(required = false)
    private SelectableRepository<SimpleUser> mySimpleUserRepository;

    @Test
    public void setSelectableRepositoryFactory_should_be_autowired() {
        Assertions.assertThat(selectableRepositoryFactory).isNotNull();
    }

    @Test
    public void selectableUserRepository_should_be_autowired() {
        Assertions.assertThat(selectableUserRepository).isNotNull();
        Assertions.assertThat(mySimpleUserRepository).isNotNull();
    }
}
