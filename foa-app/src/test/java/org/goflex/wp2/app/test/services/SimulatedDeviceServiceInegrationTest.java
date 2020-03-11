package org.goflex.wp2.app.test.services;


import org.goflex.wp2.core.entities.DeviceDetailData;
import org.goflex.wp2.core.entities.PlugType;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.foa.implementation.SimulatedDeviceService;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Parameterized tests: see https://blog.codefx.org/libraries/junit-5-parameterized-tests/
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DisplayName("Integration test for SimulatedDeviceService class")
class SimulatedDeviceServiceInegrationTest {
    private static final long MILLIS = 5000;
    private String userName = "testUser";
    private String loadType = "CoffeeMaker";

    private SimulatedDeviceService simulatedDeviceService;
    private DeviceDetailService deviceDetailService;
    private UserService userService;
    private DeviceDetail simDevice;

    @Autowired
    SimulatedDeviceServiceInegrationTest(SimulatedDeviceService simulatedDeviceService,
                                         DeviceDetailService deviceDetailService,
                                         UserService userService) {
        this.simulatedDeviceService = simulatedDeviceService;
        this.deviceDetailService = deviceDetailService;
        this.userService = userService;
    }


    @BeforeAll
    void setUp() throws InterruptedException {

        List<DeviceDetail> simulatedDevices = deviceDetailService.getDevicesByPlugType(PlugType.Simulated);
        if (simulatedDevices.size() == 0) {
            simulatedDeviceService.createSimulatedDevice(userName, loadType);
            Thread.sleep(5000);
            simulatedDevices = deviceDetailService.getDevicesByPlugType(PlugType.Simulated);
        }
        simDevice = simulatedDevices.get(0);
        assertThat("No simulated devices exists in db.", simulatedDevices.size(), greaterThan(0));
    }

    @AfterAll
    void tearDown() {
        simulatedDeviceService.deleteAllSimulatedDevicesForUser(userName);
    }


    @Test
    void testGetDeviceConsumptionAndState() {
        // arrange
        // act
        DeviceDetailData deviceDetailData = simulatedDeviceService.getDeviceConsumptionAndState(null, simDevice.getDeviceId(), null);

        // assert
        assertNotNull(deviceDetailData);
    }


    @Test
    void testGetDeviceConsumptionAndState_returnNullWhenDeviceInvalid() {
        // arrange
        // act
        DeviceDetailData deviceDetailData = simulatedDeviceService.getDeviceConsumptionAndState(null, "InvalidDevice", null);

        // assert
        assertNull(deviceDetailData);
    }

    @Test
    void testStartDevice_returnSuccessWhenValidRequest() {
        String response = simulatedDeviceService.startDevice(null, simDevice.getDeviceId(), null);
        assertEquals("success", response);
    }

    @Test
    void teststartDevice_returnFailureWhenInvalidRequest() {
        String response = simulatedDeviceService.startDevice(null, "Invalid-device-id", null);
        assertEquals("error", response);
    }


    @Test
    void testStoptDevice_returnSuccessWhenValidRequest() {
        String response = simulatedDeviceService.stopDevice(null, simDevice.getDeviceId(), null);
        assertEquals("success", response);
    }

    @Test
    void testStoptDevice_returnFailureWhenInvalidRequest() {
        String response = simulatedDeviceService.stopDevice(null, "Invalid-device-id", null);
        assertEquals("error", response);
    }


    @Test
    void testUpdateDeviceState_returnSuccessWhenValidParams() {
        // arrange
        UserT user = userService.getUser(userName);

        // act
        DeviceDetailData deviceDetailData = simulatedDeviceService.updateDeviceState(user, "", simDevice, null);

        // assert
        assertNotNull(deviceDetailData);
    }

    @Test
    void testUpdateDeviceState_returnFailureWhenInvalidParams() {
        // arrange
        UserT user = userService.getUser("InvalidUser");

        // act
        DeviceDetailData deviceDetailData = simulatedDeviceService.updateDeviceState(user, "", simDevice, null);

        // assert
        assertNotNull(deviceDetailData);
    }


    @Test
    void testRequestFlsServerCheckConnection() {
        // arrange and act
        ResponseEntity<String> response = simulatedDeviceService.requestFLSServer("/", HttpMethod.GET, null, null);

        // assert
        assertNotNull(response);
        //assertEquals(response.toString(), 200, response.getStatusCodeValue());
        //assertThat(response.getStatusCode().value(), is(equalTo(HttpStatus.OK_200.getStatusCode())));
    }

    @Test
    void testRequestFlsServer_whenNonExistentEndpoint_thenReturn404() {
        ResponseEntity<String> responseEntity = simulatedDeviceService.requestFLSServer("/invalid/endpoint", HttpMethod.GET, null, null);
        //assertEquals("Should return 404", HttpStatus.NOT_FOUND_404.getStatusCode(), responseEntity.getStatusCode().value());
    }


    @Test
    void testGetPowerConsumption_whenInvalidDeviceId_thenReturnMinusOne() {
        double power = simulatedDeviceService.getPowerConsumption("invalidDevice", null);
        assertThat("Should return -1 when deviceId is invalid", power, is(equalTo(-1.0)));
    }

    @Test
    void testGetPowerConsumption_whenNonExistentUserOrDevice_thenReturnMinusOne() {
        String deviceId = "non-existent-user@non-existent-device";
        double power = simulatedDeviceService.getPowerConsumption(deviceId, null);
        assertThat("Should return -1.0 when user and/or device is non-existent", power, is(equalTo(-1.0)));
    }

    @Test
    void testGetPowerConsumption_whenDeviceIsOn_thenReturnPositivePower() throws InterruptedException {
        // arrange
        String deviceId = simDevice.getDeviceId();
        if (simulatedDeviceService.getDeviceState(null, deviceId, null) == 0) {
            simulatedDeviceService.startDevice(null, deviceId, null);
            Thread.sleep(MILLIS);
        }

        // act
        double power = simulatedDeviceService.getPowerConsumption(deviceId, null);

        // assert
        assertThat("Should return positive power", power, greaterThan(0.0));
    }

    @Test
    void testGetPowerConsumption_whenDeviceIsOff_thenReturnZeroPower() throws InterruptedException {
        // arrange
        String deviceId = simDevice.getDeviceId();
        if (simulatedDeviceService.getDeviceState(null, deviceId, null) != 0) {
            simulatedDeviceService.stopDevice(null, deviceId, null);
            Thread.sleep(MILLIS);
        }

        // act
        double power = simulatedDeviceService.getPowerConsumption(deviceId, null);

        // assert
        assertThat("Should return zero power", power, equalTo(0.0));
    }


    @Test
    void testCreateSimulatedDevice_returnDeviceIdWhenValidRequest() {


        // act
        String result = simulatedDeviceService.createSimulatedDevice(userName, loadType);

        // assert
        assertThat(result, not(equalTo("error")));
    }

    @Test
    void testCreateSimulatedDevice_throwExceptionWhenInvalidDeviceType() {
        // arrange
        //String userName = simDevice.getUser().getUserName();
        //String userName = "";
        String loadType = "NonExistentLoad";

        // act
        String result = simulatedDeviceService.createSimulatedDevice(userName, loadType);

        // assert
        assertThat(result, equalTo("error"));
    }

    @Test
    void testDeleteSimulatedDevice_ReturnFailureWhenInvalidDevice() {
        // arrange
        // act
        String response = simulatedDeviceService.deleteSimulatedDevice("invalidLoadId");

        // assert
        assertEquals("error", response);
    }

    @Test
    void testDeleteAllSimulatedDevicesForUser_ReturnFailureWhenInvalidUser() {
        // arrange
        // act
        String response = simulatedDeviceService.deleteSimulatedDevice("invalidUser");

        // assert
        assertEquals("error", response);
    }
}