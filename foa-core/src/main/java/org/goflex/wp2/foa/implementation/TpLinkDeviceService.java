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
 *  Last Modified 2/22/18 1:20 AM
 */

package org.goflex.wp2.foa.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.core.models.*;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.controldetailmonitoring.ControlDetailService;
import org.goflex.wp2.foa.events.UpdateDevicesEvent;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserMessageService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.goflex.wp2.foa.interfaces.xEmsServices;
import org.goflex.wp2.foa.wrapper.DeviceDetailDataWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.abs;

/**
 * Created by bijay on 1/5/18.
 * Update by aftab
 */
@Service
public class TpLinkDeviceService implements xEmsServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(TpLinkDeviceService.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:00'Z'";
    private static final Map<String, Double> lastConsumption = new HashMap<>();
    @Resource(name = "deviceLatestFO")
    ConcurrentHashMap<String, FlexOfferT> deviceLatestFO;

    @Resource(name = "deviceLatestAggData")
    LinkedHashMap<String, Map<Date, Double>> deviceLatestAggData;

    @Resource(name = "orgAccEnergyData")
    LinkedHashMap<String, Map<Date, Double>> orgAccEnergyData;

    @Resource(name = "defaultFlexibilitySettings")
    Map<Long, DeviceFlexibilityDetail> defaultFlexibilitySettings;

    @Resource(name = "tclDeviceFutureFOs")
    ConcurrentHashMap<String, Date> tclDeviceFutureFOs;

    @Resource(name = "devicePendingControlSignals")
    ConcurrentHashMap<String, Integer> devicePendingControlSignals;

    private List<ConsumptionTimeSeries> consumptionTimeSeries = new ArrayList<>();

    private final Map<String, Double> wetDeviceCustomThresholds = new HashMap<>();

    private FOAProperties foaProperties;
    private UserService userService;
    private DeviceDetailService deviceDetailService;
    private UserMessageService userMessageService;
    private OrganizationRepository organizationRepository;
    private DeviceFlexOfferGroup deviceFlexOfferGroup;
    private RestTemplate restTemplate;
    private ControlDetailService controlDetailService;
    private ApplicationEventPublisher applicationEventPublisher;

    private String APIKey = "";
    private boolean tokenInvalid = false;

    private final ArrayList<String> V1_para = new ArrayList<String>() {{
        add("relay_state");
        add("latitude");
        add("longitude");
        add("current");
        add("voltage");
        add("power");
        add("total");
    }};
    private final ArrayList<String> V2_para = new ArrayList<String>() {{
        add("relay_state");
        add("latitude_i");
        add("longitude_i");
        add("current_ma");
        add("voltage_mv");
        add("power_mw");
        add("total_wh");
    }};

    public TpLinkDeviceService() {
        this.wetDeviceCustomThresholds.put("antast@8006FE404856B39803F3CF6A5BD2793518AFDA7E", 50.0);
        this.wetDeviceCustomThresholds.put("bijay@80060B5E0FD671D58243CE7162A6054719822955", 10.0);
    }

    @Autowired
    public TpLinkDeviceService(FOAProperties foaProperties,
                               UserService userService,
                               DeviceDetailService deviceDetailService,
                               UserMessageService userMessageService,
                               OrganizationRepository organizationRepository,
                               DeviceFlexOfferGroup deviceFlexOfferGroup,
                               RestTemplate restTemplate,
                               ApplicationEventPublisher applicationEventPublisher,
                               ControlDetailService controlDetailService
                               ) {

        this.foaProperties = foaProperties;
        this.userService = userService;
        this.deviceDetailService = deviceDetailService;
        this.userMessageService = userMessageService;
        this.organizationRepository = organizationRepository;
        this.deviceFlexOfferGroup = deviceFlexOfferGroup;
        this.restTemplate = restTemplate;
        this.controlDetailService = controlDetailService;
        this.applicationEventPublisher = applicationEventPublisher;
    }


    /**
     * Generic method that makes HTTP requests to TpLink Cloud and return the received data as {@link ResponseEntity}
     *
     * @param requestBody
     * @param deviceParameters
     * @return
     */
    public ResponseEntity<String> requestTpLinkCloud(String requestBody, DeviceParameters deviceParameters) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, httpHeaders);
        try {
            String newURl;
            if (requestBody.contains("login")) {
                newURl = deviceParameters.getCloudAPIUrl();
            } else {
                newURl = deviceParameters.getCloudAPIUrl() + "?token=" + deviceParameters.getAPIKey().replace("\"", "");
            }
            ResponseEntity<String> response =
                    restTemplate.exchange(newURl, HttpMethod.POST, entity, String.class); //Make POST call
            return response;
        } catch (Exception ex) {
            LOGGER.warn(ex.getLocalizedMessage(), ex);
            LOGGER.info("user: {}, requestBody: {}", deviceParameters.getCloudUserName(), requestBody);
            return null;
        }
    }


    /**
     * Stores connection error details as {@link UserMessage}
     *
     * @param responseJsn
     * @param errorCode
     * @param userName
     * @param deviceId
     */
    public void HandleTpLinkConnectionError(JsonNode responseJsn, MessageCode errorCode, String userName,
                                            String deviceId) {
        LOGGER.debug("Error code********************* " + errorCode.getValue() + "\n");

        DeviceDetail device = this.deviceDetailService.getDeviceByPlugId(deviceId);

        if (device == null) {
            return;
        }
        if (device.getPlugType() == PlugType.MQTT) {
            return;
        }

        UserMessage message = new UserMessage();
        if (responseJsn != null) {
            message.setMessage(responseJsn.get("msg").textValue());
        } else {
            message.setMessage("Please verify your Tp-Link username and password is correct");

        }

        if(message.getMessage().contains("Request timeout")
                || message.getMessage().contains("Parameter doesn't exist")
                || message.getMessage().contains("Token expired")
                || message.getMessage().contains("Device is offline")){
            //LOGGER.info("Message Discarded: " + message.getMessage() + "\n");
            return;

        }

        if (userMessageService.similarMessageExists(deviceId, userName, errorCode)) {
            return;
        }

        //UserT user = userService.getUserByLoadId(userName);
        message.setMessageCode(errorCode);
        message.setDeviceID(deviceId);
        message.setUserName(userName);
        /*if(user != null) {
            message.setUserName(user.getUserName());
        }else{
            message.setUserName(userName);
        }*/
        message.setMessageStatus(0);
        message.setMessageDate(new Date());
        userMessageService.save(message);

    }

    private Map<String, Double> extractData_V1(JsonNode jsonNode) {

        try {
            Map<String, Double> data = new HashMap<>();

            double latitude = 1;
            double longitude = 1;
            double current = -1;
            double voltage = -1;
            double power = -1;
            double energy = -1;
            data.put("latitude", latitude);
            data.put("longitude", longitude);
            data.put("current", current);
            data.put("voltage", voltage);
            data.put("power", power);
            data.put("energy", energy);

            if (jsonNode.get("system").get("get_sysinfo").has("latitude")) {
                latitude = jsonNode.get("system").get("get_sysinfo").get("latitude").asDouble();
            }
            data.put("latitude", latitude);

            if (jsonNode.get("system").get("get_sysinfo").has("longitude")) {
                longitude = jsonNode.get("system").get("get_sysinfo").get("longitude").asDouble();
            }
            data.put("longitude", longitude);

            if (jsonNode.get("emeter").has("get_realtime")) {
                if (jsonNode.get("emeter").get("get_realtime").has("current")) {
                    current = jsonNode.get("emeter").get("get_realtime").get("current").asDouble();
                }
                data.put("current", current);

                if (jsonNode.get("emeter").get("get_realtime").has("voltage")) {
                    voltage = jsonNode.get("emeter").get("get_realtime").get("voltage").asDouble();
                }
                data.put("voltage", voltage);

                if (jsonNode.get("emeter").get("get_realtime").has("power")) {
                    power = jsonNode.get("emeter").get("get_realtime").get("power").asDouble();
                }
                data.put("power", power);

                if (jsonNode.get("emeter").get("get_realtime").has("total")) {
                    energy = jsonNode.get("emeter").get("get_realtime").get("total").asDouble();
                }
                data.put("energy", energy);
            }

            return data;
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    private Map<String, Double> extractData_V2(JsonNode jsonNode) {

        try {
            Map<String, Double> data = new HashMap<>();
            double latitude = 1;
            double longitude = 1;
            double current = -1;
            double voltage = -1;
            double power = -1;
            double energy = -1;
            data.put("latitude", latitude);
            data.put("longitude", longitude);
            data.put("current", current);
            data.put("voltage", voltage);
            data.put("power", power);
            data.put("energy", energy);

            if (jsonNode.get("system").get("get_sysinfo").has("latitude_i")) {
                latitude = jsonNode.get("system").get("get_sysinfo").get("latitude_i").asDouble() / 10000;
            }
            data.put("latitude", latitude);

            if (jsonNode.get("system").get("get_sysinfo").has("longitude_i")) {
                longitude = jsonNode.get("system").get("get_sysinfo").get("longitude_i").asDouble() / 10000;
            }
            data.put("longitude", longitude);

            if (jsonNode.get("emeter").has("get_realtime")) {
                if (jsonNode.get("emeter").get("get_realtime").has("current_ma")) {
                    current = jsonNode.get("emeter").get("get_realtime").get("current_ma").asDouble() / 1000;
                }
                data.put("current", current);

                if (jsonNode.get("emeter").get("get_realtime").has("voltage_mv")) {
                    voltage = jsonNode.get("emeter").get("get_realtime").get("voltage_mv").asDouble() / 1000;
                }
                data.put("voltage", voltage);

                if (jsonNode.get("emeter").get("get_realtime").has("power_mw")) {
                    power = jsonNode.get("emeter").get("get_realtime").get("power_mw").asDouble() / 1000;
                }
                data.put("power", power);

                if (jsonNode.get("emeter").get("get_realtime").has("total_wh")) {
                    energy = jsonNode.get("emeter").get("get_realtime").get("total_wh").asDouble() / 1000;
                }
                data.put("energy", energy);
            }

            return data;
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return null;
        }
    }


    /**
     * @param deviceID
     * @param deviceParameters
     * @return
     */
    @Override
    public DeviceDetailData getDeviceConsumptionAndState(String userName, String deviceID,
                                                         DeviceParameters deviceParameters) {

        try {
            deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
            deviceID = this.getDeviceIdFromString(deviceID);
            DeviceDetailData deviceDetailData = new DeviceDetailData();

            if (deviceParameters.getCloudUserName().equals("") || deviceParameters.getCloudUserName() == null
                    || deviceParameters.getCloudPassword().equals("") || deviceParameters.getCloudPassword() == null) {
                this.HandleTpLinkConnectionError(null, MessageCode.MissingCredential, userName, deviceID);
                return deviceDetailData;
            }
            String requestBody = "{\"method\":\"passthrough\", \n" +
                    " \"params\": {\"deviceId\": \"" + deviceID + "\", \n" +
                    "            \"requestData\": \"{\\\"system\\\":{\\\"get_sysinfo\\\":" +
                    "null}, \\\"emeter\\\":{\\\"get_realtime\\\":null}}\" }}"; //Wrap data and header


            ResponseEntity<String> response = this.requestTpLinkCloud(requestBody, deviceParameters);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJsn = null;

            try {
                responseJsn = mapper.readTree(response.getBody());
                LOGGER.trace(responseJsn.toString());
            } catch (IOException e) {
                LOGGER.warn("Error during API call to TP-link cloud server.");
                LOGGER.error(e.getLocalizedMessage(), e);
                return deviceDetailData;
            }

            JsonNode responseJsn1 = null;
            if (responseJsn.has("result")) {
                if (responseJsn.get("result").has("responseData")) {
                    LOGGER.trace("Device consumption data received. deviceId: " + deviceID);

                    try {
                        responseJsn1 = mapper.readTree(responseJsn.get("result").get("responseData").textValue());
                    } catch (IOException e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                    }
                    // get the required parameters from the json response
                    int deviceStatus = responseJsn1.get("system").get("get_sysinfo").get("relay_state").asInt();

                    // prepare a timestamp
                    Date currentDate = null;
                    try {
                        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
                        currentDate = df.parse(df.format(new Date()));
                    } catch (Exception e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                        return null;
                    }

                    Map<String, Double> dt = new HashMap<>();
                    if (responseJsn1.get("system").get("get_sysinfo").has("hw_ver")) {
                        if (responseJsn1.get("system").get("get_sysinfo").get("hw_ver").asDouble() == 1.0) {
                            dt = this.extractData_V1(responseJsn1);
                        } else if (responseJsn1.get("system").get("get_sysinfo").get("hw_ver").asDouble() == 2.0) {
                            dt = this.extractData_V2(responseJsn1);
                        }else{
                            dt = this.extractData_V2(responseJsn1);
                        }
                    }

                    double power = dt.get("power") > 4000 ? -1 : dt.get("power");

                    // prepare DeviceData object
                    DeviceData deviceData = new DeviceData();
                    deviceData.setCurrent(dt.get("current"));
                    deviceData.setVoltage(dt.get("voltage"));
                    deviceData.setPower(power);
                    deviceData.setEnergy(dt.get("energy"));
                    deviceData.setDate(currentDate);

                    // finally prepare the object to be returned
                    deviceDetailData.setTime(currentDate);
                    deviceDetailData.setState(deviceStatus == 0 ? DeviceState.Idle : DeviceState.Operating);
                    deviceDetailData.setValue(power);
                    deviceDetailData.setDeviceData(deviceData);

                    String alias = responseJsn1.get("system").get("get_sysinfo").get("alias").asText();
                    deviceDetailData.setAlias(alias);

                    DeviceDetail deviceDetail = deviceDetailService.getDevice(userName + "@" + deviceID);
                    if (deviceDetail != null && !deviceDetail.isChangedByUser()) {
                        deviceDetailData.setLatitude(dt.get("latitude"));
                        deviceDetailData.setLongitude(dt.get("longitude"));
                    } else {
                        LOGGER.debug(String.format(
                                "Device location manually set by user. Not updating device location from TP-Link " +
                                        "cloud. User: %s, deviceId: %s",
                                userName, deviceID));
                    }

                }
            } else if (responseJsn.has("error_code")) {
                if (responseJsn.get("error_code").intValue() == -20651) {
                    deviceDetailData.setErrorCode(-20651);
                    LOGGER.debug("device: {}. Error -20651 received", deviceID);
                } else if (responseJsn.get("msg").textValue().equals("Token expired")) {
                    deviceDetailData.setErrorCode(-20651);
                    LOGGER.debug("device: {}. Token Expired", deviceID);
                } else if (responseJsn.get("error_code").intValue() == -20104) {
                    deviceDetailData.setErrorCode(-20651);
                    LOGGER.debug("device: {}. Error -20104 received", deviceID);
                } else if (responseJsn.get("error_code").intValue() == -20571 ||
                        responseJsn.get("msg").textValue().equals("Device is offline")) {
                    LOGGER.debug("device: {} is offline", deviceID);
                    deviceDetailData.setState(DeviceState.Disconnected);
                    deviceDetailData.setErrorCode(-20571);
                } else {
                    LOGGER.debug(responseJsn.toString());
                    LOGGER.debug("device: {}. Unknown error received", deviceID);
                    deviceDetailData.setErrorCode(-20651);
                }

                this.HandleTpLinkConnectionError(responseJsn, MessageCode.IncorrectCredential,
                        userName, deviceID);
            } else {
                LOGGER.warn("Error in receiving device list. User: " + userName);
            }

            return deviceDetailData;
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return null;
        }
    }


    @Override
    public double getDeviceConsumption(String userName, String deviceID, DeviceParameters deviceParameters) {
        deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
        deviceID = this.getDeviceIdFromString(deviceID);
        String requestBody = "{\"method\":\"passthrough\", \n" +
                " \"params\": {\"deviceId\": \"" + deviceID + "\", \n" +
                "            \"requestData\": \"{\\\"emeter\\\":{\\\"get_realtime\\\":null}}\" }}"; //Wrap data and
        // header

        ResponseEntity<String> response = this.requestTpLinkCloud(requestBody, deviceParameters);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJsn = null;

        try {
            responseJsn = mapper.readTree(response.getBody());
        } catch (IOException e) {
            LOGGER.warn("Error during API call to TPlink cloud server.");
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        JsonNode responseJsn1 = null;
        if (responseJsn.has("result")) {
            if (responseJsn.get("result").has("responseData")) {
                LOGGER.trace("Device consumption data received for " + deviceID);

                try {
                    responseJsn1 = mapper.readTree(responseJsn.get("result").get("responseData").textValue());
                } catch (IOException e) {
                    LOGGER.error(e.getLocalizedMessage(), e);
                    LOGGER.info("user: {}, device: {}", userName, deviceID);
                }
                Double energyConsum = responseJsn1.get("emeter").get("get_realtime").get("power").asDouble();
                return energyConsum;
            }
        } else if (responseJsn.has("error_code")) {
            this.HandleTpLinkConnectionError(responseJsn, MessageCode.UnknownError,
                    userName, deviceID);
        } else {
            LOGGER.warn("Error in receiving device consumption for device: {}", deviceID);
        }

        return 0.0;
    }


    @Override
    public JsonNode getOnOffSchedules(String deviceID, DeviceParameters deviceParameters) {
        deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
        deviceID = this.getDeviceIdFromString(deviceID);
        String requestBody = "{\"method\":\"passthrough\", \n" +
                " \"params\": {\"deviceId\": \"" + deviceID + "\", \n" +
                "            \"requestData\": \"{\\\"schedule\\\":{\\\"get_rules\\\":null}}\" }}"; //Wrap data and
        // header

        ResponseEntity<String> response = this.requestTpLinkCloud(requestBody, deviceParameters);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJsn = null;

        try {
            responseJsn = mapper.readTree(response.getBody());
        } catch (IOException e) {
            LOGGER.warn("Error during API call to TPlink cloud server for device: {}.", deviceID);
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return responseJsn;
    }


    @Override
    public int getDeviceState(String userName, String deviceID, DeviceParameters deviceParameters) {


        if (StringUtils.isEmpty(deviceParameters.getCloudUserName()) ||
                StringUtils.isEmpty(deviceParameters.getCloudPassword())) {
            return -2;//Device credential not set
        }

        deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
        deviceID = this.getDeviceIdFromString(deviceID);
        String[] deviceDetail = deviceID.split("@");
        if (deviceDetail.length == 2) {
            deviceID = deviceDetail[1];
        }

        String requestBody = "{\"method\":\"passthrough\", \n" +
                " \"params\": {\"deviceId\": \"" + deviceID + "\", \n" +
                "            \"requestData\": \"{\\\"system\\\":{\\\"get_sysinfo\\\":null}," +
                "\\\"emeter\\\":{\\\"get_realtime\\\":null}}\" }}";

        ResponseEntity<String> response = this.requestTpLinkCloud(requestBody, deviceParameters);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJsn = null;

        try {
            responseJsn = mapper.readTree(response.getBody());
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warn("Error getting state of device: {} from TPlink cloud server.", userName + "@" + deviceID);
        }
        JsonNode responseJsn1 = null;
        if (responseJsn.has("result")) {
            if (responseJsn.get("result").has("responseData")) {
                LOGGER.trace("Device consumption data received for " + deviceID);

                try {
                    responseJsn1 = mapper.readTree(responseJsn.get("result").get("responseData").textValue());
                } catch (IOException e) {
                    LOGGER.error(e.getLocalizedMessage(), e);
                    LOGGER.info("user: {}, device: {}", userName, deviceID);
                }
                LOGGER.trace(responseJsn1.get("system").get("get_sysinfo").get("relay_state").toString());
                return Integer.parseInt(responseJsn1.get("system").get("get_sysinfo").get("relay_state").toString());
            }
        } else if (responseJsn.has("error_code")) {

            if (responseJsn.get("error_code").intValue() == -20571 ||
                    responseJsn.get("msg").textValue().equals("Device is offline")) {
                return -1;
            }

            this.HandleTpLinkConnectionError(responseJsn, MessageCode.UnknownError,
                    userName, deviceID);
        } else {

            LOGGER.warn("Error in receiving device list for " + userName);
        }

        return 0;
    }


    @Override
    //@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, maxDelay = 1500))
    public String startDevice(String userName, String deviceID, DeviceParameters deviceParameters) {
        deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
        deviceID = this.getDeviceIdFromString(deviceID);
        String deviceId = userName + "@" + deviceID;
        String requestBody = "{\n" +
                " \"method\":\"passthrough\",\n" +
                " \"params\":{\n" +
                " \"deviceId\":\"" + deviceID + "\",\n" +
                " \"requestData\":\"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":1}}}\"\n" +
                " }\n" + "}"; //Wrap data and header

        LOGGER.info("Device Switched On Signal Sent for " + deviceID);
        ResponseEntity<String> response = this.requestTpLinkCloud(requestBody, deviceParameters);
        LOGGER.trace(response.toString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJsn = null;
        try {
            responseJsn = mapper.readTree(response.getBody());
            if (responseJsn.has("error_code")) {
                if (responseJsn.get("error_code").intValue() > 0) {
                    this.HandleTpLinkConnectionError(responseJsn, MessageCode.LoadUnreachable, userName, deviceID);
                }
                if (responseJsn.get("error_code").intValue() != 0) {
                    this.devicePendingControlSignals.put(deviceId, 1);
                } else {
                    this.devicePendingControlSignals.remove(deviceId);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Error sending start signal to device: {} through TPlink cloud server.",
                    userName + "@" + deviceID);
            LOGGER.error(e.getLocalizedMessage(), e);
            this.devicePendingControlSignals.put(deviceID, 1);
            return "error";
        }

        return "success";
    }


    @Override
    //@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, maxDelay = 1500))
    public String stopDevice(String userName, String deviceID, DeviceParameters deviceParameters) {

        deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
        deviceID = this.getDeviceIdFromString(deviceID);
        String deviceId = userName + "@" + deviceID;
        String requestBody = "{\n" +
                " \"method\":\"passthrough\",\n" +
                " \"params\":{\n" +
                " \"deviceId\":\"" + deviceID + "\",\n" +
                " \"requestData\":\"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":0}}}\"\n" +
                " }\n" + "}";

        LOGGER.info("Device Switched Off Signal Sent for " + deviceID);

        ResponseEntity<String> response = this.requestTpLinkCloud(requestBody, deviceParameters);
        LOGGER.trace(response.toString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJsn = null;
        try {
            responseJsn = mapper.readTree(response.getBody());
            if (responseJsn.has("error_code")) {
                if (responseJsn.get("error_code").intValue() > 0) {
                    this.HandleTpLinkConnectionError(responseJsn, MessageCode.LoadUnreachable,
                            userName, deviceID);
                }
                if (responseJsn.get("error_code").intValue() != 0) {
                    this.devicePendingControlSignals.put(deviceId, 0);
                } else {
                    this.devicePendingControlSignals.remove(deviceId);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Error sending stop signal to device: {} through TPlink cloud server.",
                    userName + "@" + deviceID);
            LOGGER.error(e.getLocalizedMessage(), e);
            this.devicePendingControlSignals.put(deviceId, 0);
            return "error";
        }

        return "success";
    }


    @Override
    public String addOnOffSchedule(String deviceID, String userName, Date eventTime, int action) {

        Calendar scheduleDate = Calendar.getInstance();
        scheduleDate.setTime(eventTime);
        int schYear = scheduleDate.get(Calendar.YEAR);
        int schMonth = scheduleDate.get(Calendar.MONTH) + 1;
        int schDay = scheduleDate.get(Calendar.DAY_OF_MONTH);
        int schHour = scheduleDate.get(Calendar.HOUR_OF_DAY);
        int schMinute = scheduleDate.get(Calendar.MINUTE);
        int schWeekOfDay = scheduleDate.get(Calendar.DAY_OF_WEEK);

        deviceID = this.getDeviceIdFromString(deviceID);
        UserT usr = userService.getUser(userName);
        OrganizationLoadControlState state =
                organizationRepository.findByOrganizationId(usr.getOrganizationId()).getDirectControlMode();
        /**Do not push schedule to smart plug if control state is not Active */
        if (organizationRepository.findByOrganizationId(usr.getOrganizationId()).getDirectControlMode() ==
                OrganizationLoadControlState.Active) {
            DeviceParameters deviceParameters = new DeviceParameters(usr.getTpLinkUserName(),
                    usr.getTpLinkPassword(), usr.getAPIKey(), foaProperties.getCloudAPIUrl());

            List<Integer> wday = Arrays.asList(0, 0, 0, 0, 0, 0, 0);
            wday.set(schWeekOfDay - 1, 1);

            String requestBody = "{\n" +
                    " \"method\":\"passthrough\",\n" +
                    " \"params\":{\n" +
                    " \"deviceId\":\"" + deviceID + "\",\n" +
                    " \"requestData\":\"{\\\"schedule\\\":{\\\"add_rule\\\":{\\\"stime_opt\\\":0, " +
                    "\\\"wday\\\":" + wday + "," +
                    "\\\"smin\\\":" + (schHour * 60 + schMinute) + "," +
                    "\\\"enable\\\":1," +
                    "\\\"repeat\\\":0," +
                    "\\\"etime_opt\\\":-1," +
                    "\\\"name\\\":\\\"name\\\"," +
                    "\\\"eact\\\":-1," +
                    "\\\"month\\\":" + schMonth + "," +
                    "\\\"sact\\\":" + action + "," +
                    "\\\"year\\\":" + schYear + "," +
                    "\\\"longitude\\\":0," +
                    "\\\"day\\\":" + schDay + "," +
                    "\\\"force\\\":0," +
                    "\\\"latitude\\\":0," +
                    "\\\"emin\\\":0" +
                    "}, \\\"set_overall_enable\\\":{ \\\"enable\\\":1" +
                    "}}}\"\n" +
                    " }\n" + "}"; //Wrap data and header


            LOGGER.info("New schedule sent to TpLink Cloud for " + deviceID);
            ResponseEntity<String> response = this.requestTpLinkCloud(requestBody, deviceParameters);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJsn = null;

            try {
                responseJsn = mapper.readTree(response.getBody());
            } catch (IOException e) {
                LOGGER.warn("Error during API call to TPlink cloud server.");
                LOGGER.error(e.getLocalizedMessage(), e);
            }
            JsonNode responseJsn1 = null;
            if (responseJsn.has("result")) {
                if (responseJsn.get("result").has("responseData")) {
                    LOGGER.debug("New schedule Id received for " + deviceID);
                    try {
                        responseJsn1 = mapper.readTree(responseJsn.get("result").get("responseData").textValue());
                    } catch (IOException e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                        LOGGER.info("user: {}, device: {}", userName, deviceID);
                    }
                    return responseJsn1.get("schedule").get("add_rule").get("id").textValue();
                }
            } else if (responseJsn.has("error_code")) {
                this.HandleTpLinkConnectionError(responseJsn, MessageCode.UnknownError,
                        userName, deviceID);
            } else {
                LOGGER.warn("Error in receiving device list for " + userName);
            }
        } else {
            return "";
        }
        return "";
    }


    @Override
    public boolean deleteOnOffSchedule(String deviceID, String userName, String scheduleId, int action) {
        deviceID = this.getDeviceIdFromString(deviceID);
        UserT usr = userService.getUser(userName);
        if (organizationRepository.findByOrganizationId(usr.getOrganizationId()).getDirectControlMode() ==
                OrganizationLoadControlState.Active) {
            DeviceParameters deviceParameters = new DeviceParameters(usr.getTpLinkUserName(),
                    usr.getTpLinkPassword(), usr.getAPIKey(), foaProperties.getCloudAPIUrl());

            String requestBody = "{\n" +
                    " \"method\":\"passthrough\",\n" +
                    " \"params\":{\n" +
                    " \"deviceId\":\"" + deviceID + "\",\n" +
                    " \"requestData\":\"{\\\"schedule\\\":{\\\"delete_rule\\\":{" +
                    "\\\"id\\\":\\\"" + scheduleId + "\\\"" +
                    "}}}\"}}";

            ResponseEntity<String> response = this.requestTpLinkCloud(requestBody, deviceParameters);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJsn = null;
            try {
                responseJsn = mapper.readTree(response.getBody());
            } catch (IOException e) {
                LOGGER.warn("Error during API call to TPlink cloud server.");
                LOGGER.error(e.getLocalizedMessage(), e);
            }
            JsonNode responseJsn1 = null;
            try {
                responseJsn = mapper.readTree(response.getBody());
                if (responseJsn.has("result")) {
                    if (responseJsn.get("result").has("responseData")) {
                        try {
                            responseJsn1 = mapper.readTree(responseJsn.get("result").get("responseData").textValue());
                        } catch (IOException e) {
                            LOGGER.error(e.getLocalizedMessage(), e);
                        }
                        if (responseJsn1.get("schedule").get("delete_rule").get("err_code").intValue() != 0) {
                            return false;
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.warn("Error during API call to TPlink cloud server.");
                LOGGER.error(e.getLocalizedMessage(), e);
                return false;
            }
        } else {
            return true;
        }

        LOGGER.info("Schedule deleted for " + deviceID);
        return true;

    }


    public boolean hasPendingFOGeneration(String deviceID, DeviceFlexibilityDetail deviceFlexibilityDetail) {
        Date dt = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        String date = format.format(dt);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<DeviceFlexibilityDetail> postEntity = new HttpEntity<>(deviceFlexibilityDetail, headers);
        try {

            String url = "http://localhost:8083/fog/willgeneratefo/" + deviceID + "/" + date;
            ResponseEntity<Boolean> response = restTemplate.postForEntity(
                    "http://localhost:8083/fog/willgeneratefo/" + deviceID + "/" + date, postEntity, Boolean.class);
            return response.getBody();
        } catch (Exception e) {
            LOGGER.error("Error connecting to FOG");
            LOGGER.error(e.getLocalizedMessage(), e);
            return false;
        }


    }

    @Override
    public void processWetAndBatteryDevice(UserT user, String organizationName, DeviceDetail device,
                                           DeviceParameters deviceParameters) {
        try {

            FlexibilityGroupType flexibilityGroupType =
                    deviceFlexOfferGroup.getDeviceFOGroupType(device.getDeviceType());
            if (flexibilityGroupType != FlexibilityGroupType.WetLoad &&
                    flexibilityGroupType != FlexibilityGroupType.BatterySystem) {
                LOGGER.warn("device: {} is not a wet or battery charging device", device.getDeviceId());
                return;
            }

            if (!(device.getNoOfUnsuccessfulCon() < foaProperties.getGetNoOfUnsuccessfulCon())) {
                LOGGER.debug("Too many unsuccessful connection attempts for device: {}", device.getDeviceId());
                return;
            }

            if (!(user.getTpLinkUserName() != null && !user.getTpLinkUserName().equals("")
                    && user.getTpLinkPassword() != null && !user.getTpLinkPassword().equals(""))) {
                LOGGER.warn("TP-Link credentials not set for user: {}", user.getUserName());
                return;
            }

            DeviceDetailData consumptionData = getDeviceConsumptionAndState(user.getUserName(),
                    device.getDeviceId().split("@")[1], deviceParameters);
            if (user.getAPIKey().equals("") || (consumptionData != null && consumptionData.getErrorCode() == -20651)) {
                LOGGER.debug("Token Expired, New token requested for " + user.getUserName());
                String newToken = getNewToken(user.getUserName(), deviceParameters);
                user.setAPIKey(newToken);
                deviceParameters.setAPIKey(newToken);
                consumptionData = getDeviceConsumptionAndState(user.getUserName(), device.getDeviceId().split("@")[1],
                        deviceParameters);
            }
            if (consumptionData == null) {
                LOGGER.warn("No consumption data found for device: {}", device.getDeviceId());
                return;
            }

            Double currentConsumption = consumptionData.getValue();

            // if device is just turned on, then turn if off and make api call to generate flex offer
            if (lastConsumption.containsKey(device.getDevicePlugId())) {

                // 25W threshold for Cyprus, 50W for germany
                //Double deviceOffThresholdPower = organizationName.equals("CYPRUS") ? 25.0 : 50.0;
                Double deviceOffThresholdPower;
                if (this.wetDeviceCustomThresholds.containsKey(device.getDeviceId())) {
                    deviceOffThresholdPower = this.wetDeviceCustomThresholds.get(device.getDeviceId());
                } else {
                    deviceOffThresholdPower = 25.0;
                }

                if (lastConsumption.get(device.getDevicePlugId()) <= deviceOffThresholdPower &&
                        currentConsumption > deviceOffThresholdPower) {

                    // make api call to find out if FO can be generated or not
                    String url = foaProperties.getFogConnectionConfig().getShouldGenerateDeviceFOUrl() + "/" +
                            device.getDeviceId();
                    ResponseEntity<String> response = this.makeHttpRequest(url, HttpMethod.POST, null, null);

                    if (response.getStatusCode().value() == 200) {
                        LOGGER.error("Turning off device: {} at power value {}", device.getDeviceId(), currentConsumption);

                        try {
                            // store the data point at which device turned off. store it for the next minute
                            consumptionData.setTime(new Date(consumptionData.getTime().getTime() + 60000));
                            List<DeviceDetailDataWrapper> deviceDetailDataList = new ArrayList<>();
                            DeviceDetailDataWrapper deviceDetailDataWrapper = new DeviceDetailDataWrapper();
                            deviceDetailDataWrapper.setUserName(user.getUserName());
                            deviceDetailDataWrapper.setDeviceDetailId(device.getDeviceDetailId());
                            deviceDetailDataWrapper.setDeviceDetailData(consumptionData);
                            deviceDetailDataList.add(deviceDetailDataWrapper);
                            // store the data point at which device turned off. store it for the next minute
                            UpdateDevicesEvent updateDevicesEvent = new UpdateDevicesEvent( this,
                                    String.format("Storing wet device turn off power value for device: {}", device.getDeviceId()),
                                    deviceDetailDataList);
                            this.applicationEventPublisher.publishEvent(updateDevicesEvent);
                        } catch (Exception ex) {
                            LOGGER.error(ex.getMessage());
                        }

                        // make api call to generate flex offer
                        url = foaProperties.getFogConnectionConfig().getGenerateDeviceFOUrl() + "/" +
                                device.getDeviceId() + "/" + organizationName;
                        this.makeHttpRequest(url, HttpMethod.POST, null, null);
                    } else {
                        LOGGER.debug("Not generating FO for device: {} because FO generation requirements not satisfied " +
                                "at this time.", device.getDeviceId());
                    }
                }

                // save current consumption for next iteration
                lastConsumption.put(device.getDevicePlugId(), currentConsumption);

            } else {
                lastConsumption.put(device.getDevicePlugId(), currentConsumption);
            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            LOGGER.info("user: {}, device: {}", user.getUserName(), device.getDeviceId());
        }
    }

    @Override
    //@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 100, maxDelay = 500))
    public DeviceDetailData updateDeviceState(UserT user, String organizationName, DeviceDetail device,
                                              DeviceParameters deviceParameters) {
        DeviceDetailData consumptionData = null;
        try {
            if (user.getTpLinkUserName() != "" && user.getTpLinkUserName() != null
                    && user.getTpLinkPassword() != "" && user.getTpLinkPassword() != null
                    && user.getAPIKey() != "" && user.getAPIKey() != null) {

                if (device.getNoOfUnsuccessfulCon() < foaProperties.getGetNoOfUnsuccessfulCon()) {
                    consumptionData =
                            getDeviceConsumptionAndState(user.getUserName(), device.getDeviceId().split("@")[1],
                                    deviceParameters);
                    //consumptionData = dummyDeviceDetailData();

                    if (consumptionData.getErrorCode() == -20651 || user.getAPIKey() == "") {
                        LOGGER.debug("Token Expired, New token requested for " + user.getUserName());
                        String newToken = getNewToken(user.getUserName(), deviceParameters);
                        user.setAPIKey(newToken);
                        deviceParameters.setAPIKey(newToken);
                        consumptionData =
                                getDeviceConsumptionAndState(user.getUserName(), device.getDeviceId().split("@")[1],
                                        deviceParameters);
                    }

                    /** this hold Aggregated recent data collected from tplink plug */
                    if (consumptionData.getTime() != null) {
                        Double currentConsumption = consumptionData.getValue();// * 20 / 1000;
                        Map<Date, Double> val = new HashMap<>();
                        if (currentConsumption >= 0) {
                            if (deviceLatestAggData.containsKey(organizationName)) {
                                if (deviceLatestAggData.get(organizationName).containsKey(consumptionData.getTime())) {
                                    Double aggConsumption = deviceLatestAggData.get(organizationName).
                                            get(consumptionData.getTime()) + currentConsumption;

                                    //val.put(consumptionData.getTime(), aggConsumption);
                                    deviceLatestAggData.get(organizationName)
                                            .put(consumptionData.getTime(), aggConsumption);
                                } else {
                                    //val.put(consumptionData.getTime(), currentConsumption);
                                    deviceLatestAggData.get(organizationName)
                                            .put(consumptionData.getTime(), currentConsumption);

                                }
                                //deviceLatestAggData.put(organizationName, val);
                            } else {
                                val.put(consumptionData.getTime(), currentConsumption);
                                deviceLatestAggData.put(organizationName, val);
                            }
                        }

                        // store accumulated energy consumption
                        Double devicePower = 0.0;
                        devicePower = consumptionData.getValue();

                        if (orgAccEnergyData.containsKey(organizationName)) {
                            Set<Date> keys = orgAccEnergyData.get(organizationName).keySet();
                            for (Date key : keys) {
                                Double aggConsumption = orgAccEnergyData.get(organizationName).get(key) + devicePower;
                                orgAccEnergyData.get(organizationName).remove(key);
                                orgAccEnergyData.get(organizationName).put(consumptionData.getTime(), aggConsumption);
                            }

                        } else {
                            val.put(consumptionData.getTime(), devicePower);
                            orgAccEnergyData.put(organizationName, val);
                        }

                    }

                    //userService.updateDeviceState(user.getUserName(), device.getDeviceDetailId(), consumptionData);
                    return consumptionData;

                } else {
                    Date currentDate = new Date();
                    long diff = (currentDate.getTime() - device.getLastConnectedTime().getTime()) / 60000;
                    if (diff > foaProperties.getConnectionHaltDuration()) {
                        userService.resetDeviceLastCon(device.getDeviceDetailId());
                    }
                }
            }

            //return "success";
            return consumptionData;
        } catch (Exception e) {
            LOGGER.warn("Error updating state for device: {}. Message: {}",
                    device.getDeviceId(), e.getLocalizedMessage());
            //return "error";
            return null;
        }
    }


    private DeviceDetailData dummyDeviceDetailData() {
        DeviceDetailData deviceDetailData = new DeviceDetailData();

        // prepare a timestamp
        Date currentDate = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
            currentDate = df.parse(df.format(new Date()));
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            return null;
        }

        // prepare DeviceData object
        DeviceData deviceData = new DeviceData();
        deviceData.setCurrent(2.0);
        deviceData.setVoltage(222.0);
        deviceData.setPower(22.0);
        deviceData.setEnergy(0.2);
        deviceData.setDate(currentDate);

        // finally prepare the object to be returned
        deviceDetailData.setTime(currentDate);
        deviceDetailData.setState(DeviceState.Operating);
        deviceDetailData.setValue(22.2);
        deviceDetailData.setDeviceData(deviceData);

        deviceDetailData.setLatitude(22.2);
        deviceDetailData.setLongitude(33.3);

        return deviceDetailData;
    }


    @Override
    public ResponseEntity<String> makeHttpRequest(String url, HttpMethod method, HttpHeaders httpHeaders,
                                                  String requestBody) {

        if (httpHeaders == null) {
            httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        }

        HttpEntity<String> entity = new HttpEntity<>(requestBody, httpHeaders);

        try {
            return restTemplate.exchange(url, method, entity, String.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                LOGGER.debug("Received Http status {} from url: {}:", e.getStatusCode().value(), url);
            } else {
                LOGGER.debug("Error making Http request to url: {}:", url, e);
            }
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (ResourceAccessException e) {
            LOGGER.error("Error making Http request to url: {}:", url);
            return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean updateOnOffSchedule(String deviceID, String userName, String scheduleId, int action) {
        deviceID = this.getDeviceIdFromString(deviceID);
        UserT usr = userService.getUser(userName);
        DeviceParameters deviceParameters = new DeviceParameters(usr.getTpLinkUserName(),
                usr.getTpLinkPassword(), usr.getAPIKey(), foaProperties.getCloudAPIUrl());


        String requestBody = "{\n" +
                " \"method\":\"passthrough\",\n" +
                " \"params\":{\n" +
                " \"deviceId\":\"" + deviceID + "\",\n" +
                " \"requestData\":\"{\\\"schedule\\\":{\\\"edit_rule\\\":{\\\"stime_opt\\\":0, " +
                "\\\"wday\\\":[0,0,1,0,0,0,0]," +
                "\\\"id\\\":" + scheduleId + "," +
                "\\\"smin\\\":1140," +
                "\\\"enable\\\":1," +
                "\\\"repeat\\\":0," +
                "\\\"etime_opt\\\":-1," +
                "\\\"name\\\":\\\"name\\\"," +
                "\\\"eact\\\":-1," +
                "\\\"month\\\":0," +
                "\\\"sact\\\":" + action + "," +
                "\\\"year\\\":0," +
                "\\\"longitude\\\":0," +
                "\\\"day\\\":0," +
                "\\\"force\\\":0," +
                "\\\"latitude\\\":0," +
                "\\\"emin\\\":0" +
                "}, \\\"set_overall_enable\\\":{ \\\"enable\\\":1" +
                "}}}\"\n" +
                " }\n" + "}"; //Wrap data and header


        LOGGER.debug("Device Switched On Signal Sent for " + deviceID);
        ResponseEntity<String> response = this.requestTpLinkCloud(requestBody, deviceParameters);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJsn = null;
        try {
            responseJsn = mapper.readTree(response.getBody());
            if (responseJsn.has("error_code")) {
                if (responseJsn.get("error_code").intValue() > 0) {
                    this.HandleTpLinkConnectionError(responseJsn, MessageCode.LoadUnreachable,
                            userName, deviceID);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Error during API call to TPlink cloud server.");
            LOGGER.error(e.getLocalizedMessage(), e);
        }

        return true;

    }


    private String getDeviceIdFromString(String deviceID) {
        String[] deviceDetail = deviceID.split("@");
        if (deviceDetail.length == 2) {
            deviceID = deviceDetail[1];
        }
        return deviceID;
    }

    @Override
    public void toogleDevice(String userName, String deviceID, int currentState, DeviceParameters deviceParameters) {

        deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
        deviceID = this.getDeviceIdFromString(deviceID);
        String requestBody = "{\n" +
                " \"method\":\"passthrough\",\n" +
                " \"params\":{\n" +
                " \"deviceId\":\"" + deviceID + "\",\n" +
                " \"requestData\":\"{\\\"system\\\":{\\\"set_relay_state\\\":{\\\"state\\\":" + abs(currentState - 1) +
                "}}}\"\n" +
                " }\n" + "}";

        LOGGER.info("Device Switched {} Signal Sent for {}", abs(currentState - 1), deviceID);
        ResponseEntity<String> response = this.requestTpLinkCloud(requestBody, deviceParameters);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJsn = null;
        try {
            responseJsn = mapper.readTree(response.getBody());
            if (responseJsn.has("error_code")) {
                if (responseJsn.get("error_code").intValue() > 0) {
                    this.HandleTpLinkConnectionError(responseJsn, MessageCode.LoadUnreachable, userName, deviceID);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Error during API call to TPlink cloud server.");
            LOGGER.error(e.getLocalizedMessage(), e);
        }

    }


    @Override
    public String getTimeZone(String userName, DeviceParameters deviceParameters, String deviceId) {
        String requestBody = "{\"method\":\"passthrough\", \n" +
                " \"params\": {\"deviceId\": \"" + deviceId + "\", \n" +
                "            \"requestData\": \"{\\\"time\\\":{\\\"get_timezone\\\":null}}\" }}";

        ResponseEntity<String> response = this.requestTpLinkCloud(requestBody, deviceParameters);
        String time_zone = "00:00";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJsn = null;
        try {
            responseJsn = mapper.readTree(response.getBody());

            if (responseJsn.has("result")) {
                if (responseJsn.get("result").has("responseData")) {
                    LOGGER.debug("Device timezone received for " + deviceId);
                    JsonNode responseJsn1 = mapper.readTree(responseJsn.get("result").get("responseData").textValue());
                    String patternString = "[+#-](\\d)(\\d):(\\d)(\\d)";
                    Pattern pattern = Pattern.compile(patternString);
                    Matcher matcher = pattern.matcher(responseJsn.get("result").get("responseData").textValue());
                    while (matcher.find()) {
                        time_zone = matcher.group(0);
                    }
                    time_zone = time_zone
                            .concat("@" + responseJsn1.get("time").get("get_timezone").get("dst_offset").asInt());
                }
            } else if (responseJsn.has("error_code")) {

                this.HandleTpLinkConnectionError(responseJsn, MessageCode.LoadUnreachable, userName, deviceId);
            }
            return time_zone;
        } catch (Exception e) {
            LOGGER.warn("Error getting timezone for device: {} from TP-Link cloud server. Using default timezone: {}",
                    deviceId, time_zone);
            //LOGGER.error(e.getLocalizedMessage(), e);
            return time_zone;
        }
    }


    /**
     * get a list of smart plugs for the given user form the user's TpLink cloud account
     *
     * @param deviceParameters
     * @return
     */
    @Override
    public Map<String, String> getDevices(String userName, DeviceParameters deviceParameters) {
        deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
        //Device
        Map<String, String> tempDeviceList = new HashMap<>();

        String requestBody = "{\"method\":\"getDeviceList\"}"; //Wrap data and header

        LOGGER.debug("Requesting device list from tpLink cloud API for " + userName);
        ResponseEntity<String> response = this.requestTpLinkCloud(requestBody, deviceParameters);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJsn = null;
        try {
            responseJsn = mapper.readTree(response.getBody());
        } catch (IOException e) {
            LOGGER.warn("Error during API call to TPlink cloud server.");
            LOGGER.error(e.getLocalizedMessage(), e);
        }

        if (responseJsn.has("result")) {
            if (responseJsn.get("result").has("deviceList")) {
                LOGGER.debug("Device List received for " + userName);
                for (int i = 0; i < responseJsn.get("result").get("deviceList").size(); i++) {
                    //LOGGER.debug(responseJsn.get("result").get("deviceList").get(i).get("deviceId").textValue());
                    String alias = responseJsn.get("result").get("deviceList").get(i).get("alias").textValue();
                    String deviceId = responseJsn.get("result").get("deviceList").get(i).get("deviceId").textValue();
                    String deviceType = responseJsn.get("result").get("deviceList").get(i).get("deviceModel").textValue();
                    if(deviceType.equals("HS110(EU)")) {
                        tempDeviceList.put(deviceId, alias);
                    }
                }
            }
        } else if (responseJsn.has("error_code")) {
            if (responseJsn.get("error_code").intValue() == -20651 ||
                    responseJsn.get("msg").textValue().equals("Token expired")) {
                LOGGER.debug("Token Expired for " + userName);
                tempDeviceList.put("Token Expired", "");
            }

            this.HandleTpLinkConnectionError(responseJsn, MessageCode.IncorrectCredential,
                    userName, null);
        } else {
            LOGGER.warn("Error in receiving device list for " + userName);
        }

        return tempDeviceList;

    }

    @Override
    public String getNewToken(String userName, DeviceParameters deviceParameters) {
        deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
        /**
         * TPlink message body to receive token
         */
        String requestBody = "{\n" +
                " \"method\": \"login\",\n" +
                " \"params\": {\n" +
                " \"appType\": \" " + deviceParameters.getAppType() + "\",\n" +
                " \"cloudUserName\": \"" + deviceParameters.getCloudUserName() + "\",\n" +
                " \"cloudPassword\": \"" + deviceParameters.getCloudPassword() + "\",\n" +
                " \"terminalUUID\": \"" + deviceParameters.getTerminalUUID() + "\"\n" +
                " }\n" +
                "}"; //Wrap data and header

        LOGGER.debug("Requesting authorization token from tpLink cloud API for " + userName);
        ResponseEntity<String> response = this.requestTpLinkCloud(requestBody, deviceParameters);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJsn = null;

        try {
            //convert string to json format
            responseJsn = mapper.readTree(response.getBody());
        } catch (IOException e) {
            LOGGER.warn("Error during conversion to JSON format");
            LOGGER.error(e.getLocalizedMessage(), e);
            return "";
        }

        //check if response has token value
        if (responseJsn.has("result")) {
            LOGGER.debug("Authentication Token received for " + userName);
            return responseJsn.get("result").get("token").toString();
        } else if (responseJsn.has("error_code")) {
            this.HandleTpLinkConnectionError(responseJsn, MessageCode.IncorrectCredential,
                    userName, null);
            return "";
        } else {
            LOGGER.debug(response.getBody());
            return "";
        }

    }

    @Override
    public void validateDevice() {

    }

    public boolean deviceAlreadyExists(UserT user, String devicePlugID) {
        boolean deviceExists = false;
        DeviceDetail dd = deviceDetailService.getDevice(devicePlugID);
        /*for(DeviceDetail device:user.getDeviceDetail()){
            if(device.getDeviceId().equals(deviceID)){
                deviceExists = true;
            }
        }*/
        if (dd != null) {
            deviceExists = true;
        }
        return deviceExists;
    }

    public boolean deviceAlreadyRegistered(String devicePlugID) {
        boolean deviceAlreadyRegistered = false;
        DeviceDetail deviceDetail = deviceDetailService.getDeviceByPlugId(devicePlugID);
        if (deviceDetail != null) {
            deviceAlreadyRegistered = true;
        }
        return deviceAlreadyRegistered;
    }


    public boolean isTokenValid(String userName, DeviceParameters deviceParameters) {
        deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
        Map<String, String> availableDevices = this.getDevices(userName, deviceParameters);
        return !availableDevices.containsKey("Token Expired") && !availableDevices.containsKey("Token incorrect");
    }


    /**
     * set up TpLink smart plugs for the current user
     *
     * @param user
     * @param deviceParameters
     */
    @Transactional
    public void setTpLinkDevices(UserT user, DeviceParameters deviceParameters) {

        try {
            deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
            //TODO: Null pointer exception when tplinkusername and password is null
            if (user.getTpLinkUserName() != null && !StringUtils.isEmpty(user.getTpLinkUserName()) &&
                    user.getTpLinkPassword() != null && !StringUtils.isEmpty(user.getTpLinkPassword())) {


                deviceParameters.setCloudUserName(user.getTpLinkUserName());
                deviceParameters.setCloudPassword(user.getTpLinkPassword());
                deviceParameters.setAppType("Kasa_Android");
                deviceParameters.setTerminalUUID(UUID.randomUUID());
                deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
                deviceParameters.setAPIKey(user.getAPIKey());
                /**If tplink key for user is missing, get new key and save */
                if (user.getAPIKey() == null || user.getAPIKey().equals("")) {
                    String cloudAPIToken = this.getNewToken(user.getUserName(), deviceParameters);
                    if (!cloudAPIToken.isEmpty()) {
                        /**
                         * set API token for the given username and password
                         */
                        deviceParameters.setAPIKey(cloudAPIToken);
                        userService.saveTPLinkAPIKey(user.getUserName(), cloudAPIToken);
                    }
                } else if (!isTokenValid(user.getUserName(),
                        deviceParameters)) { /**If tplink key for user is invalid, get new key and save */
                    String cloudAPIToken = this.getNewToken(user.getUserName(), deviceParameters);
                    LOGGER.debug("Token Expired, New token requested for " + user.getUserName());
                    userService.saveTPLinkAPIKey(user.getUserName(), cloudAPIToken);
                    deviceParameters.setAPIKey(cloudAPIToken);
                } else {
                    deviceParameters.setAPIKey(user.getAPIKey());
                }

                if (!deviceParameters.getAPIKey().isEmpty() || !deviceParameters.getAPIKey().equals("")) {
                    Map<String, String> availableDevices = this.getDevices(user.getUserName(), deviceParameters);

                    if (availableDevices.size() > 0) {
                        for (String tpLinkdeviceID : availableDevices.keySet()) {
                            //device id is set as a combination of username and tplink id
                            String deviceID = user.getUserName().concat("@").concat(tpLinkdeviceID);
                            //TODO: || !this.deviceAlreadyRegistered(deviceID)
                            if (!this.deviceAlreadyExists(user, deviceID)) {
                                if (!this.deviceAlreadyRegistered(tpLinkdeviceID)) {
                                    DeviceDetail deviceDetail = new DeviceDetail(deviceID);
                                    DeviceDetailData tpLinkData =
                                            getDeviceConsumptionAndState(user.getUserName(), tpLinkdeviceID,
                                                    deviceParameters);
                                    deviceDetail.setDeviceState(tpLinkData.getState());
                                    deviceDetail.setDevicePlugId(tpLinkdeviceID);
                                    deviceDetail.setAlias(availableDevices.get(tpLinkdeviceID));
                                    ConsumptionTsEntity consumptionTs = new ConsumptionTsEntity();
                                    deviceDetail.setConsumptionTs(consumptionTs);
                                    deviceDetail.setPlugType(PlugType.TPLink_HS110);
                                    String tpLinkGroupName = "tplink-cloud";
                                    GroupingDetail groupingDetail =
                                            deviceDetailService.getGroupingDetailByGroupName(tpLinkGroupName);
                                    if (groupingDetail == null) {
                                        groupingDetail = new GroupingDetail();
                                        groupingDetail.setGroupName(tpLinkGroupName);
                                        deviceDetailService.addGroupingDetail(groupingDetail);
                                    }
                                    deviceDetail.setGroupingDetail(groupingDetail);

                                    DeviceFlexibilityDetail dfd = new DeviceFlexibilityDetail();
                                    Long organizationId = user.getOrganizationId();
                                    dfd.setDailyControlStart(
                                            defaultFlexibilitySettings.get(organizationId).getDailyControlStart());
                                    dfd.setDailyControlEnd(
                                            defaultFlexibilitySettings.get(organizationId).getDailyControlEnd());
                                    dfd.setNoOfInterruptionInADay(
                                            defaultFlexibilitySettings.get(organizationId).getNoOfInterruptionInADay());
                                    dfd.setMaxInterruptionLength(
                                            defaultFlexibilitySettings.get(organizationId).getMaxInterruptionLength());
                                    dfd.setMinInterruptionInterval(defaultFlexibilitySettings.get(organizationId)
                                            .getMinInterruptionInterval());
                                    dfd.setMaxInterruptionDelay(
                                            defaultFlexibilitySettings.get(organizationId).getMaxInterruptionDelay());
                                    dfd.setLatestAcceptanceTime(
                                            defaultFlexibilitySettings.get(organizationId).getLatestAcceptanceTime());
                                    deviceDetail.setDeviceFlexibilityDetail(dfd);

                                    String timeZone =
                                            this.getTimeZone(user.getUserName(), deviceParameters, tpLinkdeviceID)
                                                    .split("@")[0];
                                    deviceDetail.setTimeZone(timeZone);
                                    Date lastConnected = new Date();
                                    deviceDetail.setLastConnectedTime(lastConnected);
                                    deviceDetail.setLatitude(tpLinkData.getLatitude());
                                    deviceDetail.setLongitude(tpLinkData.getLongitude());
                                    deviceDetail.setRegistrationDate(new Date());
                                    deviceDetail.setDefaultState(new DeviceDefaultState()
                                            .getDeviceDefaultState(deviceDetail.getDeviceType()));

                                    // new devices are not flexible by default
                                    deviceDetail.setFlexible(false);

                                    userService.updateDeviceList(user.getUserName(), deviceDetail);
                                    LOGGER.info("Added device with ID: " + deviceID);
                                } else {
                                    LOGGER.debug(
                                            "Device with ID: " + tpLinkdeviceID + " already exits for another user");
                                    this.HandleTpLinkConnectionError(null, MessageCode.DuplicateLoad,
                                            user.getUserName(), tpLinkdeviceID);
                                }
                            } else {
                                LOGGER.debug("Device with ID: " + deviceID + " already exits!");
                            }
                        }

                    }
                }
            } else {
                this.HandleTpLinkConnectionError(null, MessageCode.MissingCredential, user.getUserName(), null);
            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }


    public boolean isTokenInvalid() {
        return tokenInvalid;
    }


    public void setTokenInvalid(boolean tokenInvalid) {
        this.tokenInvalid = tokenInvalid;
    }

    public String getAPIKey() {
        return APIKey;
    }

    public void setAPIKey(String APIKey) {
        this.APIKey = APIKey;
    }

    public List<ConsumptionTimeSeries> getConsumptionTimeSeries() {
        return consumptionTimeSeries;
    }

    public void setConsumptionTimeSeries(List<ConsumptionTimeSeries> consumptionTimeSeries) {
        this.consumptionTimeSeries = consumptionTimeSeries;
    }

    public ConsumptionTimeSeries getConsumptionTimeSeriesIdx(int i) {
        return consumptionTimeSeries.get(i);
    }

    public void addConsumptionTimeSeries() {
        this.consumptionTimeSeries.add(new ConsumptionTimeSeries());
    }

    @Override
    public void processTCLDevice(UserT user, String organizationName, DeviceDetail device,
                                 DeviceParameters deviceParameters) {
        try {

            FlexibilityGroupType flexibilityGroupType =
                    deviceFlexOfferGroup.getDeviceFOGroupType(device.getDeviceType());
            if (flexibilityGroupType != FlexibilityGroupType.ThermostaticControlLoad) {
                LOGGER.warn("device: {} is not a TCL device", device.getDeviceId());
                return;
            }

            if (!(device.getNoOfUnsuccessfulCon() < foaProperties.getGetNoOfUnsuccessfulCon())) {
                LOGGER.debug("Too many unsuccessful connection attempts for device: {}", device.getDeviceId());
                return;
            }

            DeviceDetailData consumptionData = getDeviceConsumptionAndState(user.getUserName(),
                    device.getDeviceId(), deviceParameters);
            if (consumptionData == null) {
                LOGGER.debug("No consumption data found for device: {}", device.getDeviceId());
                return;
            }

            Double currentConsumption = consumptionData.getValue();

            // if device is just turned on, then turn if off and make api call to generate flex offer
            if (lastConsumption.containsKey(device.getDevicePlugId())) {

                Double deviceOffThresholdPower = 25.0;
                int minDelay = 120;
                int maxDelay = 300;
                double minConsumptionInLastHour =
                        deviceDetailService.getMinPowerConsumptionForLastHour(device.getDeviceId());
                if ((lastConsumption.get(device.getDevicePlugId()) <= deviceOffThresholdPower &&
                        currentConsumption > deviceOffThresholdPower) ||
                        (minConsumptionInLastHour > deviceOffThresholdPower)) {

                    // should not immediately turn off TCL devices after it's turned on
                    int delayInMillis = 1000 * (minDelay + new Random().nextInt(maxDelay - minDelay));
                    Date timestamp = new Date(new Date().getTime() + delayInMillis);
                    if (!tclDeviceFutureFOs.containsKey(device.getDeviceId())) {
                        tclDeviceFutureFOs.put(device.getDeviceId(), timestamp);
                    }
                }

                // save current consumption for next iteration
                lastConsumption.put(device.getDevicePlugId(), currentConsumption);

            } else {
                lastConsumption.put(device.getDevicePlugId(), currentConsumption);
            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

}
