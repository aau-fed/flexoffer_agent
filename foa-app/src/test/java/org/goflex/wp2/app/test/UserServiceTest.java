package org.goflex.wp2.app.test;

import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.core.repository.UserRepository;
import org.goflex.wp2.foa.implementation.TpLinkDeviceService;
import org.goflex.wp2.foa.implementation.UserServiceImpl;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


/**
 * Created by bijay on 12/19/17.
 */
@ExtendWith(SpringExtension.class)
class UserServiceTest {
    @Autowired
    private UserService userService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private TpLinkDeviceService tpLinkDeviceService;
    @MockBean
    private DeviceDetailService deviceDetailService;

    @BeforeEach
    void setUp() {
        UserT alex = new UserT("alex", "alexPass", "test@email.com", "tpLinkUser", "tpLinkPass", 10005);
        UserT bob = new UserT("bob", "bobPass", "test@email.com", "tpLinkUser", "tpLinkPass", 10005);

        DeviceDetail device = new DeviceDetail("testDevice");
        device.setDeviceDetailId(Long.parseLong("1"));
        Set devices = new HashSet();
        devices.add(device);

        alex.setTpLinkUserName("alexTp");
        alex.setTpLinkPassword("alexTpPass");
        alex.addDeviceDetail(device);
        //device.setUser(alex);
        //alex.addDevice(device);

        userService.updateDeviceList("alex", device);
        List<UserT> allUser = Arrays.asList(alex, bob);

        Mockito.when(userRepository.findByUserName(alex.getUserName())).thenReturn(alex);
        Mockito.when(userRepository.findByUserNameAndPassword(alex.getUserName(), alex.getPassword())).thenReturn(alex);
        Mockito.when(userRepository.findAll()).thenReturn(allUser);
    }

    @Test
    void whenValidName_thenUserShouldBeFound() {
        String userName = "alex";
        UserT found = userService.getUser(userName);

        assertThat(found.getUserName(), equalTo(userName));
    }

    @Test
    void whenValidNameAndPassword_thenUserShouldBeFound() {
        String userName = "alex";
        String password = "alexPass";
        UserT found = userService.getUser(userName, password);

        assertThat(found.getUserName(), equalTo("alex"));
    }

    @Test
    void whenFindAll_thenAllUserShouldBeReturned() {
        List<UserT> found = userService.getUsers();
        assertThat(found.size(), equalTo(2));
    }

    @Test
    void whenGetDevice_thenReturnDeviceForUser() {
        String userName = "alex";
        Set<DeviceDetail> found = userService.getDevices(userName);
        assertThat(found.size(), equalTo(1));
    }

    @TestConfiguration
    static class UserServiceTextContextConfiguration {
        @Bean
        public UserService userService() {
            return new UserServiceImpl();
        }
    }
}
