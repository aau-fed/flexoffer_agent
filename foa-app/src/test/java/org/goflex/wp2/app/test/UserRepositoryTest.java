package org.goflex.wp2.app.test;

import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bijay on 12/18/17.
 */
//@RunWith(SpringRunner.class)
//@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;


    //@Test
    public void whenFindByName_thenReturnUser() { //given
        UserT alex = new UserT("alex", "alexPass", "test@email.com", "tpLinkUser", "tpLinkPass", 10005);
        entityManager.persist(alex);
        entityManager.flush();
        //when
        UserT found = userRepository.findByUserName(alex.getUserName());

        //then
        assertThat(found.getUserName()).isEqualTo(alex.getUserName());
    }


}
