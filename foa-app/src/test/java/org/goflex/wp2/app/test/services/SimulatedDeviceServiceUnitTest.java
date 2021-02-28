package org.goflex.wp2.app.test.services;


import org.goflex.wp2.app.test.TestConfig;
import org.goflex.wp2.core.entities.DeviceDetailData;
import org.goflex.wp2.core.entities.DeviceState;
import org.goflex.wp2.core.entities.PlugType;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.datastructure.FoaMemoryDOA;
import org.goflex.wp2.foa.implementation.SimulatedDeviceService;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Mock external REST API (i.e., FLS server) instead of calling actual REST APi
 */
@DisplayName("Unit Tests for SimulatedDeviceService")
@ExtendWith(SpringExtension.class)
@RestClientTest(SimulatedDeviceService.class)
@Import({FoaMemoryDOA.class, FOAProperties.class, TestConfig.class})
class SimulatedDeviceServiceUnitTest {

    private static final String baseUrl = "http://goflex-atp.cs.aau.dk:5000";
    private static final String userNameValid = "valid-test-user";
    private static final String userNameInvalid = "invalid-test-user";
    private final String devicePlugIdValid = "valid-mock-device-id";
    private final String devicePlugIdInvalid = "invalid-mock-device-id";

    @Autowired
    private SimulatedDeviceService simulatedDeviceService;

    @MockBean
    private UserService userService;

    @MockBean
    private DeviceDetailService deviceDetailService;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    private DeviceDetail mockDevice;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);

        DeviceDetail mockDeviceDetail = new DeviceDetail(userNameValid + "@" + devicePlugIdValid);
        mockDeviceDetail.setPlugType(PlugType.Simulated);
        mockDeviceDetail.setAlias("mockDevice");

        UserT testUser = new UserT(userNameValid, "testPass", "test@email.com", "tpLinkUser", "tpLinkPass", 10005);
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
        when(deviceDetailService.getDevicesByPlugType(PlugType.Simulated)).thenReturn(mockDevices);
        mockDevice = deviceDetailService.getDevicesByPlugType(PlugType.Simulated).get(0);
    }


    @Test
    void testRequestFlsServer_CheckConnection() {
        // arrange
        mockServer
                .expect(requestTo(baseUrl))
                .andRespond(withSuccess("{\"message\": \"Welcome to FLS\"}", MediaType.APPLICATION_JSON));

        // act
        ResponseEntity<String> responseEntity = this.simulatedDeviceService.requestFLSServer("", HttpMethod.GET, null, null);

        // assert
        mockServer.verify();
        assertEquals(responseEntity.getStatusCode().value(), HttpStatus.OK.value());
    }


    @Test
    void testGetDeviceConsumptionAndState() {
        // arrange
        mockServer
                .expect(requestTo(baseUrl + "/simloads/status/" + userNameValid + "/" + devicePlugIdValid))
                .andRespond(withSuccess("{\"message\": \"success\", \"power_state\": 1}", MediaType.APPLICATION_JSON));
        mockServer
                .expect(requestTo(baseUrl + "/simloads/power/" + userNameValid + "/" + devicePlugIdValid))
                .andRespond(withSuccess("{\"message\": \"success\", \"power\": 1}", MediaType.APPLICATION_JSON));

        // act
        DeviceDetailData deviceDetailData = simulatedDeviceService.getDeviceConsumptionAndState(null, mockDevice.getDeviceId(), null);

        // assert
        mockServer.verify();
        assertNotNull(deviceDetailData);
    }

    @Test
    void testGetDeviceConsumptionAndState_returnNullWhenDeviceInvalid() {
        // arrange
        mockServer
                .expect(requestTo(baseUrl + "/simloads/status/" + userNameValid + "/" + devicePlugIdInvalid))
                .andRespond(withBadRequest());
        mockServer
                .expect(requestTo(baseUrl + "/simloads/power/" + userNameValid + "/" + devicePlugIdInvalid))
                .andRespond(withBadRequest());

        String deviceId = userNameValid + "@" + devicePlugIdInvalid;

        // act
        DeviceDetailData deviceDetailData = simulatedDeviceService.getDeviceConsumptionAndState(null, deviceId, null);

        // assert
        mockServer.verify();
        assertNull(deviceDetailData);
    }


    @Test
    void testStartDevice_returnSuccessWhenValidRequest() {
        // arrange
        mockServer
                .expect(requestTo(baseUrl + "/simloads/start/" + userNameValid + "/" + devicePlugIdValid))
                .andRespond(withSuccess("{\"message\": \"success\"}", MediaType.APPLICATION_JSON));

        // act
        String response = simulatedDeviceService.startDevice(null, mockDevice.getDeviceId(), null);

        // assert
        mockServer.verify();
        assertEquals("success", response);
    }

    @Test
    void testStartDevice_returnFailureWhenInvalidRequest() {
        // arrange
        mockServer
                .expect(requestTo(baseUrl + "/simloads/start/" + userNameValid + "/" + devicePlugIdInvalid))
                .andRespond(withSuccess("{\"message\": \"error\"}", MediaType.APPLICATION_JSON));
        String deviceId = userNameValid + "@" + devicePlugIdInvalid;

        // act
        String response = simulatedDeviceService.startDevice(null, deviceId, null);

        // assert
        mockServer.verify();
        assertEquals("error", response);
    }


    @Test
    void testStopDevice_returnSuccessWhenValidRequest() {
        // arrange
        mockServer
                .expect(requestTo(baseUrl + "/simloads/stop/" + userNameValid + "/" + devicePlugIdValid))
                .andRespond(withSuccess("{\"message\": \"success\"}", MediaType.APPLICATION_JSON));

        // act
        String response = simulatedDeviceService.stopDevice(null, mockDevice.getDeviceId(), null);

        // assert
        mockServer.verify();
        assertEquals("success", response);
    }

    @Test
    void testStopDevice_returnFailureWhenInvalidRequest() {
        // arrange
        mockServer
                .expect(requestTo(baseUrl + "/simloads/stop/" + userNameValid + "/" + devicePlugIdInvalid))
                .andRespond(withSuccess("{\"message\": \"error\"}", MediaType.APPLICATION_JSON));
        String deviceId = userNameValid + "@" + devicePlugIdInvalid;

        // act
        String response = simulatedDeviceService.stopDevice(null, deviceId, null);

        // assert
        mockServer.verify();
        assertEquals("error", response);
    }


    @Test
    void testUpdateDeviceState_returnSuccessWhenValidParams() {
        // arrange
        mockServer
                .expect(requestTo(baseUrl + "/simloads/status/" + userNameValid + "/" + devicePlugIdValid))
                .andRespond(withSuccess("{\"message\": \"success\", \"power_state\": 1}", MediaType.APPLICATION_JSON));
        mockServer
                .expect(requestTo(baseUrl + "/simloads/power/" + userNameValid + "/" + devicePlugIdValid))
                .andRespond(withSuccess("{\"message\": \"success\", \"power\": 1}", MediaType.APPLICATION_JSON));

        UserT user = userService.getUser(userNameValid);

        // act
        DeviceDetailData deviceDetailData = simulatedDeviceService.updateDeviceState(user, "", mockDevice, null);

        // assert
        mockServer.verify();
        assertNotNull(deviceDetailData);
    }


    @Test
    void testUpdateDeviceState_returnFailureWhenInvalidParams() {
        // arrange
        mockServer
                .expect(requestTo(baseUrl + "/simloads/status/" + userNameValid + "/" + devicePlugIdValid))
                .andRespond(withSuccess("{\"message\": \"success\", \"power_state\": 1}", MediaType.APPLICATION_JSON));
        mockServer
                .expect(requestTo(baseUrl + "/simloads/power/" + userNameValid + "/" + devicePlugIdValid))
                .andRespond(withSuccess("{\"message\": \"success\", \"power\": 1}", MediaType.APPLICATION_JSON));

        UserT user = userService.getUser("invalidUser");

        // act
        DeviceDetailData deviceDetailData = simulatedDeviceService.updateDeviceState(user, "", mockDevice, null);

        // assert
        mockServer.verify();
        assertNotNull(deviceDetailData);
    }

    @Test
    void testRequestFlsServer_whenNonExistentEndpoint_thenReturn404() {
        // arrange
        mockServer
                .expect(requestTo(baseUrl + "/invalid/endpoint"))
                .andRespond(withBadRequest());

        // act
        ResponseEntity<String> responseEntity = simulatedDeviceService.requestFLSServer("/invalid/endpoint", HttpMethod.GET, null, null);

        // assert
        mockServer.verify();
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(), "Should return 404");
    }


    @Test
    void testGetPowerConsumption_whenInvalidDeviceId_thenReturnMinusOne() {
        // arrange
        mockServer
                .expect(requestTo(baseUrl + "/simloads/power/" + userNameValid + "/" + devicePlugIdInvalid))
                .andRespond(withSuccess("{\"message\": \"success\"}", MediaType.APPLICATION_JSON));
        String deviceId = "invalid-device-id";

        // act
        double power = simulatedDeviceService.getPowerConsumption(deviceId, null);

        // assert
        // verify() makes sure that all expected requests were actually. In this case an exception is thrown
        // due to invalid device id, preventing the request to perform, as per design. Thus no need to run
        // verifiy()
        //mockServer.verify();

        assertThat("Should return -1 when deviceId is invalid", power, is(equalTo(-1.0)));
    }

    @Test
    void testGetPowerConsumption_whenNonExistentUserOrDevice_thenReturnMinusOne() {
        // arrange
        mockServer
                .expect(requestTo(baseUrl + "/simloads/power/" + userNameInvalid + "/" + devicePlugIdInvalid))
                .andRespond(withSuccess("{\"message\": \"success\"}", MediaType.APPLICATION_JSON));
        String deviceId = userNameInvalid + "@" + devicePlugIdInvalid;

        // act
        double power = simulatedDeviceService.getPowerConsumption(deviceId, null);

        // assert
        mockServer.verify();
        assertThat("Should return -1.0 when user and/or device is non-existent", power, is(equalTo(-1.0)));
    }

    @Test
    void testGetPowerConsumption_whenDeviceIsOn_thenReturnPositivePower() throws InterruptedException {
        // arrange
        mockServer
                .expect(requestTo(baseUrl + "/simloads/power/" + userNameValid + "/" + devicePlugIdValid))
                .andRespond(withSuccess("{\"message\": \"success\", \"power\": 2.0}", MediaType.APPLICATION_JSON));
        String deviceId = userNameValid + "@" + devicePlugIdValid;

        // act
        double power = simulatedDeviceService.getPowerConsumption(deviceId, null);

        // assert
        mockServer.verify();
        assertThat("Should return positive power", power, greaterThan(0.0));
    }


    @Test
    void testCreateSimulatedDevice_returnDeviceIdWhenValidRequest() {
        // arrange
        String loadTypeValid = "CoffeeMaker";
        mockServer
                .expect(requestTo(baseUrl + "/simloads/create/" + userNameValid + "/" + loadTypeValid))
                .andRespond(withSuccess("{\"message\": \"success\", \"data\": {\"load_name\": \"CoffeeMaker\"}}", MediaType.APPLICATION_JSON));
        String deviceId = userNameValid + "@" + devicePlugIdValid;

        // act
        String result = simulatedDeviceService.createSimulatedDevice(userNameValid, loadTypeValid);

        // assert
        mockServer.verify();
        assertThat(result, not(equalTo("error")));
    }


    @Test
    void testCreateSimulatedDevice_throwExceptionWhenInvalidDeviceType() {
        // arrange
        String loadTypeInvalid = "NonExistentLoad";
        mockServer
                .expect(requestTo(baseUrl + "/simloads/create/" + userNameValid + "/" + loadTypeInvalid))
                .andRespond(withBadRequest());

        // act
        String result = simulatedDeviceService.createSimulatedDevice(userNameValid, loadTypeInvalid);

        // assert
        mockServer.verify();
        assertThat(result, equalTo("error"));
    }


    @Test
    void testDeleteSimulatedDevice_ReturnFailureWhenInvalidUserOrInvalidDevice() {
        // arrange
        String devicePlugIdInvalid = "some-device-plug-id";
        mockServer
                .expect(requestTo(baseUrl + "/simloads/delete/" + userNameInvalid + "/" + devicePlugIdInvalid))
                .andRespond(withBadRequest());
        // act
        String response = simulatedDeviceService.deleteSimulatedDevice(userNameInvalid + "@" + devicePlugIdInvalid);

        // assert
        mockServer.verify();
        assertEquals("error", response);
    }
}
