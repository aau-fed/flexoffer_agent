package org.goflex.wp2.app.test;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.goflex.wp2.app.controllers.UserController;
import org.goflex.wp2.app.fmanintegration.user.FmanUserService;
import org.goflex.wp2.core.entities.PlugType;
import org.goflex.wp2.core.models.DeviceDetail;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by bijay on 12/6/17.
 * Updated by aftab on 07/6/18.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTestWithSecurity {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private UserService userService;

    @MockBean
    private TpLinkDeviceService tpLinkDeviceLoader;

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
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()).build();
    }


    @Test
    void testUpdateUser_thenReturnStatus200() throws Exception {
        UserT user = new UserT("rob", "pass", "", "", "", 10005);
        given(userService.getUser("rob")).willReturn(user);

        Map<String, Object> payload = new HashMap();
        payload.put("tpLinkUserName", "bobTplinkUser");
        payload.put("tpLinkPassword", "bobTpLinkPass");


        mockMvc.perform(post("/prosumer/updateUser")
                //.with(user("rob").password("pass"))
                .with(user("rob"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                //.content(JsonUtil.toJson(user)))
                .content(JsonUtil.toJson(payload)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateUser_ifInValidUser_thenStatus401() throws Exception {
        UserT user = new UserT("rob", "pass", "", "", "", 10005);
        given(userService.getUser("rob")).willReturn(user);

        Map<String, Object> payload = new HashMap();
        payload.put("tpLinkUserName", "bobTplinkUser");
        payload.put("tpLinkPassword", "bobTpLinkPass");

        this.mockMvc.perform(post("/prosumer/updateUser")
                .with(user("bob"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                //.content(JsonUtil.toJson(user)))
                .content(JsonUtil.toJson(payload)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetUser_ifValidRequest_thenReturn200() throws Exception {
        // arrange
        String userName = "testUser";
        UserT user = new UserT(userName, "testPassword", "test@eamil.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        when(userService.getUser(userName)).thenReturn(user);

        // act
        mockMvc.perform(get("/prosumer/getUser")
                .with(user(userName))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testGetUser_ifInvalidUser_thenReturnUnauthorizedStatus() throws Exception {
        // arrange
        String userName = "testUser";
        UserT user = new UserT(userName, "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        when(userService.getUser(userName)).thenReturn(user);

        // act
        // assert
        mockMvc.perform(get("/prosumer/getUser")
                .with(user("invalid-user"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
        ).andDo(print())
                .andExpect(status().isUnauthorized());
    }


    @Test
    void testGetUsers_ifValidRequest_thenReturnUserListWithSuccessCode() throws Exception {
        // arrange
        UserT user1 = new UserT("user1", "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        UserT user2 = new UserT("user2", "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        List<UserT> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        when(userService.getUser("user1")).thenReturn(user1);
        when(userService.getUsers()).thenReturn(users);

        // act
        // assert
        mockMvc.perform(
                get("/prosumer/getUsers")
                        .with(user("user1"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
        ).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testGetUsers_ifInvalidRequest_thenReturnUnauthorizedCode() throws Exception {
        // arrange
        UserT user1 = new UserT("user1", "testPassword", "test@email.com",
                "testTpLinkUser", "testTpLinkPassword", 10005);
        UserT user2 = new UserT("user2", "testPassword", "test@email.com",
                "testTpLinkUser", "testTpLinkPassword", 10005);
        List<UserT> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        when(userService.getUser("user1")).thenReturn(user1);
        when(userService.getUsers()).thenReturn(users);

        // act
        // assert
        mockMvc.perform(
                get("/prosumer/getUsers")
                        //            .with(user("user3"))
                        //            .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
        ).andDo(print())
                .andExpect(status().isUnauthorized());
    }


    @Test
    void testUpdateDevice_ifValidRequest_thenUpdateDeviceAndReturnOk() throws Exception {
        // arrange
        String userName = "user1";
        UserT user1 = new UserT(userName, "testPassword", "test@email.com",
                "testTpLinkUser", "testTpLinkPassword", 10005);
        when(userService.getUser(userName)).thenReturn(user1);

        String devicePlugId = "device-plug-id";
        DeviceDetail mockDevice = new DeviceDetail(userName + "@" + devicePlugId);
        mockDevice.setPlugType(PlugType.Simulated);
        mockDevice.setAlias("mockDevice");

        // act
        // assert
        mockMvc.perform(
                post("/prosumer/updateDeviceStateAndConsumption")
                        .with(user(userName))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(JsonUtil.toJson(mockDevice))
        ).andDo(print())
                .andExpect(status().isOk());

    }


    @Test
    void testUpdateDevice_ifDeviceIdIsNull_thenReturnBadRequest() throws Exception {
        // arrange
        String userName = "user1";
        UserT user1 = new UserT(userName, "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        when(userService.getUser(userName)).thenReturn(user1);

        DeviceDetail mockDevice = new DeviceDetail();
        mockDevice.setPlugType(PlugType.Simulated);
        mockDevice.setAlias("mockDevice");
        mockDevice.setDeviceId(null); // device is null for this test

        // act
        // assert
        mockMvc.perform(
                post("/prosumer/updateDeviceStateAndConsumption")
                        .with(user(userName))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(JsonUtil.toJson(mockDevice))
        ).andDo(print())
                .andExpect(status().isBadRequest());

    }


    @Test
    void testUpdateDevice_ifUserNotExists_thenReturnNotFound() throws Exception {
        // arrange
        String userName = "user1";
        UserT user1 = new UserT(userName, "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        when(userService.getUser(userName)).thenReturn(user1);

        String devicePlugId = "device-plug-id";
        DeviceDetail mockDevice = new DeviceDetail(userName + "@" + devicePlugId);
        mockDevice.setAlias("mockDevice");
        mockDevice.setPlugType(PlugType.Simulated);

        // act
        // assert
        mockMvc.perform(
                post("/prosumer/updateDeviceStateAndConsumption")
                        .with(user("non-existent-user")) // user doesn't exist for this test
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(JsonUtil.toJson(mockDevice))
        ).andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    void testUpdateDevice_ifDeviceNotBelongsToCurrentUser_thenReturnUnauthorized() throws Exception {
        // arrange
        String userName = "user1";
        UserT user1 = new UserT(userName, "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        UserT user2 = new UserT("user2", "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        when(userService.getUser(userName)).thenReturn(user1);

        String devicePlugId = "device-plug-id";
        DeviceDetail mockDevice = new DeviceDetail("user2" + "@" + devicePlugId);
        mockDevice.setAlias("mockDevice");
        mockDevice.setPlugType(PlugType.Simulated);

        // act
        // assert
        mockMvc.perform(
                post("/prosumer/updateDeviceStateAndConsumption")
                        .with(user("user1"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(JsonUtil.toJson(mockDevice))
        ).andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSetDevice_whenUnauthorizedUser_thenReturnBadRequest() throws Exception {
        // arrange
        String userName = "user1";
        UserT user1 = new UserT(userName, "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        UserT user2 = new UserT("user2", "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        when(userService.getUser(userName)).thenReturn(user1);

        String devicePlugId = "device-plug-id";
        DeviceDetail mockDevice = new DeviceDetail(userName + "@" + devicePlugId);
        mockDevice.setAlias("mockDevice");
        mockDevice.setPlugType(PlugType.Simulated);

        when(userService.updateDeviceList("user1", mockDevice)).thenReturn(user1);

        // act
        // assert
        mockMvc.perform(
                post("/prosumer/setdevice")
                        .with(user("user2")) // user2 is unauthorized for this test
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(JsonUtil.toJson(mockDevice))
        ).andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    void testSetDevice_whenValidRequest_thenReturnOk() throws Exception {
        // arrange
        String userName = "user1";
        UserT user1 = new UserT(userName, "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        UserT user2 = new UserT("user2", "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        when(userService.getUser(userName)).thenReturn(user1);

        String devicePlugId = "device-plug-id";
        DeviceDetail mockDevice = new DeviceDetail(userName + "@" + devicePlugId);
        mockDevice.setPlugType(PlugType.Simulated);
        mockDevice.setAlias("mockDevice");

        when(userService.updateDeviceList("user1", mockDevice)).thenReturn(user1);

        // act
        // assert
        mockMvc.perform(
                post("/prosumer/setdevice")
                        .with(user("user1"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(JsonUtil.toJson(mockDevice))
        ).andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    void testSetDevice_whenInvalidDeviceId_thenReturnBadRequest() throws Exception {
        // arrange
        String userName = "user1";
        UserT user1 = new UserT(userName, "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        UserT user2 = new UserT("user2", "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        when(userService.getUser(userName)).thenReturn(user1);

        String devicePlugId = "device-plug-id";
        DeviceDetail mockDevice = new DeviceDetail(userName + "@" + devicePlugId);
        mockDevice.setPlugType(PlugType.Simulated);
        mockDevice.setAlias("mockDevice");
        mockDevice.setDeviceId(null); // invalid device id for this test

        when(userService.updateDeviceList("user1", mockDevice)).thenReturn(user1);

        // act
        // assert
        mockMvc.perform(
                post("/prosumer/setdevice")
                        .with(user("user1"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(JsonUtil.toJson(mockDevice))
        ).andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    void testSetDevice_whenDeviceDetailIdIsNoNull_thenReturnBadRequest() throws Exception {
        // arrange
        String userName = "user1";
        UserT user1 = new UserT(userName, "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        UserT user2 = new UserT("user2", "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        when(userService.getUser(userName)).thenReturn(user1);

        String devicePlugId = "device-plug-id";
        DeviceDetail mockDevice = new DeviceDetail(userName + "@" + devicePlugId);
        mockDevice.setPlugType(PlugType.Simulated);
        mockDevice.setAlias("mockDevice");
        mockDevice.setDeviceDetailId(1L); // device detail id pre-assigned for this test

        // set to return null because deviceDetailId is not greater than zero
        when(userService.updateDeviceList("user1", mockDevice)).thenReturn(user1);

        // act
        // assert
        mockMvc.perform(
                post("/prosumer/setdevice")
                        .with(user("user1"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(JsonUtil.toJson(mockDevice))
        ).andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    void testGetAllConsumption_whenDeviceListEmpty_thenReturnNotFound() throws Exception {
        // arrange
        String userName = "user1";
        UserT user1 = new UserT(userName, "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        UserT user2 = new UserT("user2", "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        when(userService.getUser(userName)).thenReturn(user1);

        String devicePlugId1 = "mock-device-plug-id-1";
        DeviceDetail mockDevice1 = new DeviceDetail(userName + "@" + devicePlugId1);
        mockDevice1.setPlugType(PlugType.TPLink_HS110);
        mockDevice1.setAlias("mockDevice1");

        String devicePlugId2 = "mock-device-plug-id-2";
        DeviceDetail mockDevice2 = new DeviceDetail(userName + "@" + devicePlugId2);
        mockDevice2.setPlugType(PlugType.Simulated);
        mockDevice2.setAlias("mockDevice2");

        //List<DeviceDetail> devices = null;
        Set<DeviceDetail> devices = new HashSet<>();
        devices.add(mockDevice1);
        devices.add(mockDevice2);
        when(userService.getDevices(userName)).thenReturn(null); // set to return zero devices for this test

        // act
        // assert
        mockMvc.perform(
                get("/prosumer/getallconsumption")
                        .with(user(userName))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
        ).andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    void testGetAllConsumption_whenDeviceListNotEmpty_thenReturnAllData() throws Exception {
        // arrange
        String userName = "user1";
        UserT user1 = new UserT(userName, "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        UserT user2 = new UserT("user2", "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        when(userService.getUser(userName)).thenReturn(user1);

        String devicePlugId1 = "mock-device-plug-id-1";
        String deviceId1 = userName + "@" + devicePlugId1;
        DeviceDetail mockDevice1 = new DeviceDetail(deviceId1);
        mockDevice1.setPlugType(PlugType.TPLink_HS110);
        mockDevice1.setAlias("mockDevice1");

        String devicePlugId2 = "mock-device-plug-id-2";
        String deviceId2 = userName + "@" + devicePlugId2;
        DeviceDetail mockDevice2 = new DeviceDetail(deviceId2);
        mockDevice2.setPlugType(PlugType.Simulated);
        mockDevice2.setAlias("mockDevice2");

        //List<DeviceDetail> devices = null;
        Set<DeviceDetail> devices = new HashSet<>();
        devices.add(mockDevice1);
        devices.add(mockDevice2);
        when(userService.getDevices(userName)).thenReturn(devices);

        Map<Date, Double> mockData = new HashMap<>();
        Date now = new Date();
        Date now1 = new Date(now.getTime() + 1000); // adds one second
        Date now2 = new Date(now.getTime() + 2000);
        mockData.put(now, 23.9);
        mockData.put(now1, 122.9);
        mockData.put(now2, 132.9);

        when(deviceDetailService.getAllConsumptionDataForDevice(mockDevice1.getDeviceId()))
                .thenReturn(mockData);
        when(deviceDetailService.getAllConsumptionDataForDevice(mockDevice2.getDeviceId()))
                .thenReturn(mockData);

        // act
        // assert
        MvcResult mockResult = mockMvc.perform(
                get("/prosumer/getallconsumption")
                        .with(user(userName))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                // .content(JsonUtil.toJson(devices))
        ).andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(mockResult.getResponse().getContentAsString());

        assertEquals("Success", jsonNode.get("message").asText(), "should return 'Success'");
        assertThat(jsonNode.get("data").get(deviceId1).size(), equalTo(mockData.size()));
    }


    @Test
    void testGetLatestConsumption_whenValidRequest_thenReturnAllDataForTheCurrentDate() throws Exception {
        // arrange
        String userName = "user1";
        UserT user1 = new UserT(userName, "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        UserT user2 = new UserT("user2", "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        when(userService.getUser(userName)).thenReturn(user1);

        String devicePlugId1 = "mock-device-plug-id-3";
        String deviceId1 = userName + "@" + devicePlugId1;
        DeviceDetail mockDevice1 = new DeviceDetail(deviceId1);
        mockDevice1.setPlugType(PlugType.TPLink_HS110);
        mockDevice1.setAlias("mockDevice3");

        String devicePlugId2 = "mock-device-plug-id-4";
        String deviceId2 = userName + "@" + devicePlugId2;
        DeviceDetail mockDevice2 = new DeviceDetail(deviceId2);
        mockDevice2.setPlugType(PlugType.Simulated);
        mockDevice2.setAlias("mockDevice4");

        //List<DeviceDetail> devices = null;
        Set<DeviceDetail> devices = new HashSet<>();
        devices.add(mockDevice1);
        devices.add(mockDevice2);
        when(userService.getDevices(userName)).thenReturn(devices);

        Map<Date, Double> mockData = new HashMap<>();
        Date now = new Date();
        Date now1 = new Date(now.getTime() + 2000); // adds one second
        Date now2 = new Date(now.getTime() + 4000);
        Date now3 = new Date(now.getTime() + 10000);
        mockData.put(now, 123.9);
        mockData.put(now1, 102.9);
        mockData.put(now2, 432.9);
        mockData.put(now3, 32.0);

        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        Date currentDate = df.parse(df.format(now));
        given(deviceDetailService.getConsumptionDataFromDate(mockDevice1.getDeviceId(), currentDate)).willReturn(mockData);
        given(deviceDetailService.getConsumptionDataFromDate(mockDevice2.getDeviceId(), currentDate)).willReturn(mockData);
        //when(deviceDetailService.getConsumptionDataFromDate(mockDevice1.getDeviceId(), currentDate)).thenReturn(mockData);
        //when(deviceDetailService.getConsumptionDataFromDate(mockDevice2.getDeviceId(), currentDate)).thenReturn(mockData);
        //doReturn(mockData).when(deviceDetailService.getConsumptionDataFromDate(mockDevice1.getDeviceId(), currentDate));
        //doReturn(mockData).when(deviceDetailService.getConsumptionDataFromDate(mockDevice2.getDeviceId(), currentDate));

        // act
        // assert
        MvcResult mockResult = mockMvc.perform(
                get("/prosumer/getLatestConsumption")
                        .with(user(userName))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                // .content(JsonUtil.toJson(devices))
        ).andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(mockResult.getResponse().getContentAsString());

        assertEquals("Success", jsonNode.get("message").asText(), "should return 'Success'");
        assertThat(jsonNode.get("data").get(0).get("deviceLatestData").size(), equalTo(mockData.size()));
    }


    @Test
    void testGetLatestConsumptionForSingleDevice_whenValidRequest_thenReturnAllDataForTheCurrentDate() throws Exception {
        // arrange
        String userName = "user1";
        String devicePlugId = "mock-device-plug-id-3";
        String deviceId = userName + "@" + devicePlugId;
        DeviceDetail mockDevice = new DeviceDetail(deviceId);
        mockDevice.setPlugType(PlugType.TPLink_HS110);
        mockDevice.setAlias("mockDevice");

        Map<Date, Double> mockData = new HashMap<>();
        Date now = new Date();
        Date now1 = new Date(now.getTime() + 2000); // adds one second
        Date now2 = new Date(now.getTime() + 4000);
        Date now3 = new Date(now.getTime() + 10000);
        mockData.put(now, 123.9);
        mockData.put(now1, 102.9);
        mockData.put(now2, 432.9);
        mockData.put(now3, 32.0);

        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        Date currentDate = df.parse(df.format(now));
        given(deviceDetailService.getConsumptionDataFromDate(mockDevice.getDeviceId(), currentDate)).willReturn(mockData);

        // act
        // assert
        MvcResult mockResult = mockMvc.perform(
                get("/prosumer/getLatestConsumption" + "/" + deviceId)
                        .with(user(userName))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
        ).andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(mockResult.getResponse().getContentAsString());

        assertEquals("Success", jsonNode.get("message").asText(), "should return 'Success'");
        assertThat(jsonNode.get("data").size(), equalTo(mockData.size()));
    }


    @Test
    void testGetLatestConsumptionForSingleDeviceSinceDate_whenValidRequest_thenReturnAllDataSinceDate() throws Exception {
        // arrange
        String userName = "user1";
        String devicePlugId = "mock-device-plug-id-2";
        String deviceId = userName + "@" + devicePlugId;
        DeviceDetail mockDevice = new DeviceDetail(deviceId);
        mockDevice.setPlugType(PlugType.TPLink_HS110);
        mockDevice.setAlias("mockDevice");

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        String dateStr = "2018-06-19T00:00:00";
        Map<Date, Double> mockData = new HashMap<>();
        Date startDate = format.parse(dateStr);
        Date startDate1 = new Date(startDate.getTime() + 86400 * 1000 * 1); // adds one day
        Date startDate2 = new Date(startDate.getTime() + 86400 * 1000 * 2);
        Date startDate3 = new Date(startDate.getTime() + 86400 * 1000 * 3);
        mockData.put(startDate, 123.9);
        mockData.put(startDate1, 102.9);
        mockData.put(startDate2, 432.9);
        mockData.put(startDate3, 32.0);

        Date givenDate = format.parse(format.format(startDate));
        given(deviceDetailService.getConsumptionDataFromDate(mockDevice.getDeviceId(), givenDate)).willReturn(mockData);

        // act
        // assert
        MvcResult mockResult = mockMvc.perform(
                get("/prosumer/getLatestConsumption" + "/" + deviceId + "/" + dateStr)
                        .with(user(userName))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
        ).andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(mockResult.getResponse().getContentAsString());

        assertEquals("Success", jsonNode.get("message").asText(), "should return 'Success'");
        assertThat(jsonNode.get("data").size(), equalTo(mockData.size()));
    }

    @Test
    void testGetConsumptionForDate_whenValidRequest_thenReturnAllDataForTheCurrentDate() throws Exception {
        // arrange
        String userName = "user1";
        String devicePlugId = "mock-device-plug-id";
        String deviceId = userName + "@" + devicePlugId;
        DeviceDetail mockDevice = new DeviceDetail(deviceId);
        mockDevice.setPlugType(PlugType.TPLink_HS110);
        mockDevice.setAlias("mockDevice");

        String dateStr = "2018-06-19";
        Map<Date, Double> mockData = new HashMap<>();

        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        Date startDate = df.parse(dateStr);
        Date startDate1 = new Date(startDate.getTime() + 1000 * 1); // adds one day
        Date startDate2 = new Date(startDate.getTime() + 1000 * 2);
        Date startDate3 = new Date(startDate.getTime() + 1000 * 3);
        mockData.put(startDate, 123.9);
        mockData.put(startDate1, 102.9);
        mockData.put(startDate2, 432.9);
        mockData.put(startDate3, 32.0);

        Date givenDate = df.parse(df.format(startDate));
        given(deviceDetailService.getConsumptionDataForDate(mockDevice.getDeviceId(), givenDate)).willReturn(mockData);

        // act
        // assert
        MvcResult mockResult = mockMvc.perform(
                get("/prosumer/getConsumptionForDate" + "/" + deviceId + "/" + dateStr)
                        .with(user(userName))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
        ).andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(mockResult.getResponse().getContentAsString());

        assertEquals("Success", jsonNode.get("message").asText(), "should return 'Success'");
        assertThat(jsonNode.get("data").size(), equalTo(mockData.size()));
    }


    @Test
    void testGetAllDevices() throws Exception {
        // arrange
        String userName = "admin";
        UserT user1 = new UserT(userName, "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        UserT user2 = new UserT("user2", "testPassword", "test@email.com", "testTpLinkUser", "testTpLinkPassword", 10005);
        when(userService.getUser(userName)).thenReturn(user1);

        String devicePlugId1 = "mock-device-plug-id-1";
        String deviceId1 = userName + "@" + devicePlugId1;
        DeviceDetail mockDevice1 = new DeviceDetail(deviceId1);
        mockDevice1.setPlugType(PlugType.TPLink_HS110);
        mockDevice1.setAlias("mockDevice1");

        String devicePlugId2 = "mock-device-plug-id-2";
        String deviceId2 = userName + "@" + devicePlugId2;
        DeviceDetail mockDevice2 = new DeviceDetail(deviceId2);
        mockDevice2.setPlugType(PlugType.Simulated);
        mockDevice2.setAlias("mockDevice2");

        List<DeviceDetail> devices = new ArrayList<>();
        devices.add(mockDevice1);
        devices.add(mockDevice2);
        when(deviceDetailService.getAllDevices()).thenReturn(devices);

        // act
        // assert
        MvcResult mockResult = mockMvc.perform(
                get("/prosumer/alldevices")
                        .with(user(userName))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
        ).andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(mockResult.getResponse().getContentAsString());

        assertEquals("Success", jsonNode.get("message").asText(), "should return 'Success'");
        assertThat(jsonNode.get("data").size(), equalTo(devices.size()));
    }
}
