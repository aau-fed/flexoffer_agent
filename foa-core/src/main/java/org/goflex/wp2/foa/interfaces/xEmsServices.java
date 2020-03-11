/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
 * Copyright (c) 2018.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining  a copy of this software and associated documentation
 *  files (the "Software") to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify, merge,
 *  publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions: The above copyright notice and
 *  this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON
 *  INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Last Modified 2/9/18 10:47 AM
 */

package org.goflex.wp2.foa.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import org.goflex.wp2.core.entities.DeviceDetailData;
import org.goflex.wp2.core.entities.DeviceParameters;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.UserT;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;

import java.util.Date;
import java.util.Map;

/**
 * All FOA send heartbeat to other components
 * <p>
 * Created by Bijay on 08/07/2017.
 */
public interface xEmsServices {


    /**
     * get new token for API call
     */
    String getNewToken(String userName, DeviceParameters deviceParameters);

    /**
     * Validate Device - Register device to cloud service
     */
    void validateDevice();


    /**
     * send signal to start device
     */
    String startDevice(String userName, String deviceID, DeviceParameters deviceParameters);

    /**
     * send signal to toogle the current sate of the device
     */
    void toogleDevice(String userName, String deviceID, int currentState, DeviceParameters deviceParameters);


    /**
     * send signal to syop device
     */
    String stopDevice(String userName, String deviceID, DeviceParameters deviceParameters);


    /**
     * get energy consumption of the device
     */
    double getDeviceConsumption(String userName, String deviceID, DeviceParameters deviceParameters);

    /**
     * get energy consumption of the device and state
     */
    @Async
    DeviceDetailData getDeviceConsumptionAndState(String userName, String deviceID, DeviceParameters deviceParameters);

    /**
     * get list of devices foa a user with a given deviceParameters
     */
    Map<String, String> getDevices(String userName, DeviceParameters deviceParameters);

    /**
     * get current state for a device
     */
    int getDeviceState(String userName, String deviceID, DeviceParameters deviceParameters);

    /**
     * add schedule for a device
     */
    String addOnOffSchedule(String deviceID, String userName, Date eventTime, int action);

    /**
     * get schedule for a device
     */
    JsonNode getOnOffSchedules(String deviceID, DeviceParameters deviceParameters);

    /**
     * update schedule for a device
     */
    boolean updateOnOffSchedule(String deviceID, String userName, String scheduleId, int action);

    boolean deleteOnOffSchedule(String deviceID, String userName, String scheduleId, int action);

    DeviceDetailData updateDeviceState(UserT user, String organizationName, DeviceDetail device, DeviceParameters deviceParameters);

    void processWetAndBatteryDevice(UserT user, String organizationName, DeviceDetail device, DeviceParameters deviceParameters);

    String getTimeZone(String userName, DeviceParameters deviceParameters, String deviceId);

    void processTCLDevice(UserT user, String organizationName, DeviceDetail device, DeviceParameters deviceParameters);

    ResponseEntity<String> makeHttpRequest(String url, HttpMethod method, HttpHeaders httpHeaders, String requestBody);
}

