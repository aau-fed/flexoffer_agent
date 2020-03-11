package org.goflex.wp2.app.test;


import org.goflex.wp2.app.controllers.UserController;
import org.goflex.wp2.app.fmanintegration.user.FmanUserService;
import org.goflex.wp2.core.entities.DeviceParameters;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.foa.implementation.ImplementationsHandler;
import org.goflex.wp2.foa.implementation.SimulatedDeviceService;
import org.goflex.wp2.foa.implementation.TpLinkDeviceService;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserMessageService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.goflex.wp2.foa.service.EmailService;
import org.goflex.wp2.foa.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by bijay on 12/6/17.
 * updated by aftab on 07/6/18.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private UserService userService;

    @MockBean
    private TpLinkDeviceService tpLinkDeviceService;

    @MockBean
    private DeviceDetailService deviceDetailService;

    @MockBean
    private SimulatedDeviceService simulatedDeviceService;

    @MockBean
    private ImplementationsHandler implementationsHandler;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserMessageService userMessageService;

    @MockBean
    private OrganizationRepository organizationRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private FmanUserService fmanUserService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

    }

    /**
     * Test if valid data is provided, user registration is successful
     */
    @Test
    void whenPostUser_MissingPasswordField_thanStatus400() throws Exception {
        UserT user = new UserT("Rob", "", "", "", "", 10005);
        given(userService.save(user)).willReturn(user);
//        when(userService.save(user)).thenReturn(user);

        mockMvc.perform(post("/prosumer/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(user)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Username and Password fields can not be empty")));
    }


    /**
     * Test if valid data is provided, user registration is successful
     */
    @Test
    void whenPostUser_MissingUserField_thanStatus400() throws Exception {
        UserT user = new UserT("", "password", "", "", "", 10005);
        given(userService.save(user)).willReturn(user);

        mockMvc.perform(post("/prosumer/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Username and Password fields can not be empty")));
    }


    /**
     * Test if valid data is provided, user registration is successful
     */
    @Test
    void whenPostUser_thanStatus201() throws Exception {
        UserT user = new UserT("Rob", "pass", "", "", "", 10005);
        given(userService.save(user)).willReturn(user);
        when(passwordEncoder.encode(user.getPassword())).thenReturn("some-encoded-password");
        // this is for method that return void
        doNothing().when(tpLinkDeviceService).setTpLinkDevices(any(UserT.class), any(DeviceParameters.class));

        mockMvc.perform(post("/prosumer/register")
                .contentType(MediaType.APPLICATION_JSON)
                //.characterEncoding("UTF-8")
                .content(JsonUtil.toJson(user)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message", is("Success")));
        //
        // .andExpect(content().string(containsString("success")));

        verify(userService, VerificationModeFactory.times(1)).save(Mockito.any());
        reset(userService);
    }


    /**
     * if valid username and password return successful
     */
    @Test
    void givenUser_whenGetUser_thanStatus200() throws Exception {

        UserT user = new UserT("rob", "password", "", "", "", 10005);
        given(userService.getUser("rob", "password")).willReturn(user);
        when(passwordEncoder.matches("password", "password")).thenReturn(true);

        mockMvc.perform(post("/prosumer/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(user)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("success")));
    }


    /**
     * if invalid username or password return unauthorized
     */
    @Test
    void whenNoUser_thanReturnStatus401() throws Exception {
        UserT user = new UserT("rob", "pass", "", "", "", 10005);

        given(userService.getUser("bob", "pass")).willReturn(user);

        mockMvc.perform(post("/prosumer/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(user)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid User, either username or password is wrong")));
    }

    @Test
    void whenPostWithMissingUser_thanReturnBadRequest() throws Exception {
        UserT user = new UserT("rob", "pass", "", "", "", 10005);
        given(userService.getUser("", "pass")).willReturn(user);
        mockMvc.perform(post("/prosumer/login")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
