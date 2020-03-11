package org.goflex.wp2.app.test.services;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.goflex.wp2.app.test.TestConfig;
import org.goflex.wp2.core.entities.DeviceDetailData;
import org.goflex.wp2.core.entities.DeviceParameters;
import org.goflex.wp2.core.entities.DeviceState;
import org.goflex.wp2.core.entities.PlugType;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.datastructure.FoaMemoryDOA;
import org.goflex.wp2.foa.implementation.TpLinkDeviceService;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserMessageService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Mock external REST API (i.e., FLS server) instead of calling actual REST APi
 */
@DisplayName("Unit Tests for SimulatedDeviceService")
@ExtendWith(SpringExtension.class)
@RestClientTest(TpLinkDeviceService.class)
@Import({FoaMemoryDOA.class, FOAProperties.class, TestConfig.class})
class TpLinkDeviceServiceUnitTest {

    private static final String cloudAPIUrl = "https://eu-wap.tplinkcloud.com";
    private static final String userNameValid = "valid-test-user";
    private static final String userNameInvalid = "invalid-test-user";
    private String devicePlugIdValid = "valid-mock-device-id";
    private String devicePlugIdInvalid = "invalid-mock-device-id";


    @MockBean
    private UserService userService;

    @MockBean
    private DeviceDetailService deviceDetailService;

    @MockBean
    private UserMessageService userMessageService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TpLinkDeviceService tpLinkDeviceService;

    private MockRestServiceServer mockServer;

    private DeviceDetail mockDevice;

    private DeviceParameters deviceParameters;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);

        DeviceDetail mockDeviceDetail = new DeviceDetail(userNameValid + "@" + devicePlugIdValid);
        mockDeviceDetail.setPlugType(PlugType.TPLink_HS110);
        mockDeviceDetail.setAlias("mockDevice");

        UserT testUser = new UserT(userNameValid, "testPass", "test@email.com", "tpLinkUser", "tpLinkPass", 10005);
        testUser.setAPIKey("some-api-key");
        testUser.addDeviceDetail(mockDeviceDetail);
        when(userService.updateDeviceList(userNameValid, mockDevice)).thenReturn(testUser);
        when(userService.getUser(userNameValid)).thenReturn(testUser);

        DeviceDetailData mockDeviceDetailData = new DeviceDetailData();
        mockDeviceDetailData.setState(DeviceState.Operating);
        mockDeviceDetailData.setValue(12.0);
        mockDeviceDetailData.setLatitude(35.0);
        mockDeviceDetailData.setLatitude(22.0);
        when(userService.updateDeviceState(mockDeviceDetail, mockDeviceDetailData)).thenReturn(mockDeviceDetail);

        List<DeviceDetail> mockDevices = new ArrayList<>();
        mockDevices.add(mockDeviceDetail);
        when(deviceDetailService.getDevicesByPlugType(PlugType.TPLink_HS110)).thenReturn(mockDevices);
        mockDevice = deviceDetailService.getDevicesByPlugType(PlugType.TPLink_HS110).get(0);

        deviceParameters = new DeviceParameters();
        deviceParameters.setAPIKey(testUser.getAPIKey() != null ? testUser.getAPIKey() : "");
        deviceParameters.setCloudUserName(testUser.getTpLinkUserName());
        deviceParameters.setCloudPassword(testUser.getTpLinkPassword());
        deviceParameters.setCloudAPIUrl(cloudAPIUrl);
    }


    @Test
    void testGetDeviceConsumptionAndState() throws IOException {
        // arrange
        String deviceID = devicePlugIdValid;
        String requestBody = "{\"method\":\"passthrough\", \n" +
                " \"params\": {\"deviceId\": \"" + deviceID + "\", \n" +
                "            \"requestData\": \"{\\\"system\\\":{\\\"get_sysinfo\\\":" +
                "null}, \\\"emeter\\\":{\\\"get_realtime\\\":null}}\" }}"; //Wrap data and header

        String newURl = deviceParameters.getCloudAPIUrl() + "?token=" + deviceParameters.getAPIKey().replace("\"", "");

        String mockResponse = "{\"error_code\":0," +
                "\"result\":{\"responseData\":" +
                "\"{\\\"system\\\":{\\\"get_sysinfo\\\":{\\\"err_code\\\":0,\\\"sw_ver\\\":\\\"1.1.4 Build 170417 Rel.145118\\\",\\\"hw_ver\\\":\\\"1.0\\\",\\\"type\\\":\\\"IOT.SMARTPLUGSWITCH\\\",\\\"model\\\":\\\"HS110(EU)\\\",\\\"mac\\\":\\\"B0:4E:26:54:3A:37\\\",\\\"deviceId\\\":\\\"80060B5E0FD671D58243CE7162A6054719822955\\\",\\\"hwId\\\":\\\"45E29DA8382494D2E82688B52A0B2EB5\\\",\\\"fwId\\\":\\\"851E8C7225C3220531D5A3AFDACD9098\\\",\\\"oemId\\\":\\\"3D341ECE302C0642C99E31CE2430544B\\\",\\\"alias\\\":\\\"goflex-dev-02\\\",\\\"dev_name\\\":\\\"Wi-Fi Smart Plug With Energy Monitoring\\\",\\\"icon_hash\\\":\\\"\\\",\\\"relay_state\\\":1,\\\"on_time\\\":301946,\\\"active_mode\\\":\\\"schedule\\\",\\\"feature\\\":\\\"TIM:ENE\\\",\\\"updating\\\":0,\\\"rssi\\\":-30,\\\"led_off\\\":0,\\\"latitude\\\":46.083954,\\\"longitude\\\":14.485970}}," +
                "\\\"emeter\\\":{\\\"get_realtime\\\":{\\\"current\\\":0.014141,\\\"voltage\\\":226.028615,\\\"power\\\":0,\\\"total\\\":0.148000,\\\"err_code\\\":0}}}\"}}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(mockResponse);
        JsonNode responseData = mapper.readTree(jsonNode.get("result").get("responseData").textValue());

        mockServer
                .expect(requestTo(newURl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(requestBody))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

        // act
        DeviceDetailData deviceDetailData = tpLinkDeviceService.getDeviceConsumptionAndState(null, mockDevice.getDeviceId(), deviceParameters);

        // assert
        mockServer.verify();
        assertNotNull(deviceDetailData);
        assertEquals(responseData.get("system").get("get_sysinfo").get("relay_state").asInt(), deviceDetailData.getState().getValue(), "should return 1");
        assertEquals(responseData.get("emeter").get("get_realtime").get("voltage").asDouble(), deviceDetailData.getDeviceData().getVoltage(), "should return voltage as double value");
    }
}
