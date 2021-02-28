/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
 * Copyright (c) 2018 The GoFlex Consortium
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
 *  Last Modified 3/20/18 3:00 PM
 */

package org.goflex.wp2.foa.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.core.models.*;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.controldetailmonitoring.ControlDetailService;
import org.goflex.wp2.foa.events.UpdateDevicesEvent;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.goflex.wp2.foa.interfaces.xEmsServices;
import org.goflex.wp2.foa.wrapper.DeviceDetailDataWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bijay on 1/5/18.
 */
@Service
@ConditionalOnProperty(value="fls.enabled", havingValue = "true")
public class SimulatedDeviceService implements xEmsServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulatedDeviceService.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:00'Z'";
    private static final Map<String, Double> lastConsumption = new HashMap<>();

    @Resource(name = "deviceLatestAggData")
    LinkedHashMap<String, Map<Date, Double>> deviceLatestAggData;

    @Resource(name = "defaultFlexibilitySettings")
    Map<Long, DeviceFlexibilityDetail> defaultFlexibilitySettings;

    @Resource(name = "orgAccEnergyData")
    LinkedHashMap<String, Map<Date, Double>> orgAccEnergyData;

    @Resource(name = "deviceLatestFO")
    ConcurrentHashMap<String, FlexOfferT> deviceLatestFO;

    @Resource(name = "tclDeviceFutureFOs")
    ConcurrentHashMap<String, Date> tclDeviceFutureFOs;

    @Resource(name = "poolDeviceDetail")
    private ConcurrentHashMap<String, Map<String, PoolDeviceModel>> poolDeviceDetail;

    private final RestTemplate restTemplate;
    private final FOAProperties foaProperties;
    private final UserService userService;
    private final DeviceDetailService deviceDetailService;
    private final DeviceFlexOfferGroup deviceFlexOfferGroup;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final ControlDetailService controlDetailService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private Map<String, Map<String, Object>> deviceModels;

    @Autowired
    public SimulatedDeviceService(FOAProperties foaProperties,
                                  UserService userService,
                                  DeviceDetailService deviceDetailService,
                                  DeviceFlexOfferGroup deviceFlexOfferGroup,
                                  OrganizationRepository organizationRepository,
                                  RestTemplate restTemplate,
                                  PasswordEncoder passwordEncoder,
                                  ApplicationEventPublisher applicationEventPublisher,
                                  ControlDetailService controlDetailService) {
        this.foaProperties = foaProperties;
        this.userService = userService;
        this.deviceDetailService = deviceDetailService;
        this.deviceFlexOfferGroup = deviceFlexOfferGroup;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.controlDetailService = controlDetailService;
        this.applicationEventPublisher = applicationEventPublisher;
        prepareSimDeviceModelsMap();
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

            DeviceDetailData consumptionData = getDeviceConsumptionAndState(user.getUserName(),
                    device.getDeviceId(), deviceParameters);
            if (consumptionData == null) {
                LOGGER.warn("No consumption data found for device: {}", device.getDeviceId());
                return;
            }

            Double currentConsumption = consumptionData.getValue();

            // if device is just turned on, then turn if off and make api call to generate flex offer
            if (lastConsumption.containsKey(device.getDevicePlugId())) {

                Double deviceOffThresholdPower = 10.0;
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
                            UpdateDevicesEvent updateDevicesEvent = new UpdateDevicesEvent(this,
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
                        LOGGER.debug(
                                "Not generating FO for device: {} because FO generation requirements not satisfied at" +
                                        " this time.",
                                device.getDeviceId());
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

                Double deviceOffThresholdPower = 10.0;
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


    @Override
    public DeviceDetailData getDeviceConsumptionAndState(String userName, String deviceID,
                                                         DeviceParameters deviceParameters) {
        DeviceDetailData consumptionData = new DeviceDetailData();
        DeviceData deviceData = new DeviceData();

        int deviceStatus = this.getDeviceState(userName, deviceID, deviceParameters);
        double devicePowerConsumption = this.getPowerConsumption(deviceID, deviceParameters);
        if (deviceStatus != -1 && devicePowerConsumption != -1.0) {

            Date currentDate = null;
            try {
                SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
                currentDate = df.parse(df.format(new Date()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            consumptionData.setTime(currentDate);
            if (deviceStatus == 0) {
                consumptionData.setState(DeviceState.Idle);
            } else if (deviceStatus == 1) {
                consumptionData.setState(DeviceState.Operating);
            }
            consumptionData.setValue(devicePowerConsumption);

            deviceData.setDate(currentDate);
            deviceData.setPower(devicePowerConsumption);
            deviceData.setVoltage(230.0 + new Random().nextDouble());
            consumptionData.setDeviceData(deviceData);

            return consumptionData;

        } else {
            LOGGER.warn("error: failed to receive simulated device state and energy consumption data.");
            return null;
        }
    }


    /**
     * todo: this function should fetch all historical consumption data for the given device
     *
     * @param deviceID
     * @param deviceParameters
     * @return
     */
    @Override
    public double getDeviceConsumption(String userName, String deviceID, DeviceParameters deviceParameters) {
        try {
            String deviceId = deviceID.split("@")[1];
            Map<String, String> reqBodyMap = new HashMap<>();
            reqBodyMap.put("device_id", deviceId);
            String reqBody = new Gson().toJson(reqBodyMap);

            String uri = foaProperties.getSimulatedDeviceServiceConfig().getDeviceConsumptionUri();
            LOGGER.debug("Device all historical consumption data request sent for device: {}", deviceID);

            ResponseEntity<String> response = this.requestFLSServer(uri, HttpMethod.POST, null, reqBody);
            ObjectMapper mapper = new ObjectMapper();

            JsonNode responseJsn = mapper.readTree(response.getBody());
            if (responseJsn.get("status").asText().equals("success")
                    && responseJsn.has("data") && !responseJsn.get("data").isNull()) {
                //List<JsonNode> consumptionData = responseJsn.get("data").get("consumption");
                return -1.0;
            } else {
                return -1.0;
            }
        } catch (Exception e) {
            LOGGER.warn("error! - stackTrace: ", e);
            return -1.0;
        }
    }


    @Override
    public int getDeviceState(String userName, String deviceID, DeviceParameters deviceParameters) {
        try {
            String deviceId = deviceID.split("@")[1];
            Map<String, String> reqBodyMap = new HashMap<>();
            reqBodyMap.put("device_id", deviceId);
            String reqBody = new Gson().toJson(reqBodyMap);

            String uri = foaProperties.getSimulatedDeviceServiceConfig().getDeviceStatusUri();
            LOGGER.debug("Device status request sent for device: {}", deviceID);
            ResponseEntity<String> response = this.requestFLSServer(uri, HttpMethod.POST, null, reqBody);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJsn = mapper.readTree(response.getBody());

            if (responseJsn.get("status").asText().equals("success") && responseJsn.has("data")
                    && !responseJsn.get("data").isNull()) {
                String state = responseJsn.get("data").get("power_state").asText();
                return state.equals("on") ? 1 : 0;
            } else {
                return -1;
            }
        } catch (Exception e) {
            //LOGGER.warn("error! - stackTrace: ", e.getLocalizedMessage());
            return -1;
        }
    }


    @Override
    public String startDevice(String userName, String deviceID, DeviceParameters deviceParameters) {
        try {
            String deviceId = deviceID.split("@")[1];
            Map<String, String> reqBodyMap = new HashMap<>();
            reqBodyMap.put("device_id", deviceId);
            String reqBody = new Gson().toJson(reqBodyMap);

            String uri = foaProperties.getSimulatedDeviceServiceConfig().getStartDeviceUri();
            LOGGER.info("Device switched on signal sent for device: {}", deviceID);
            ResponseEntity<String> response = this.requestFLSServer(uri, HttpMethod.POST, null, reqBody);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJsn = mapper.readTree(response.getBody());

            if (responseJsn.get("status").asText().equals("success")) {
                return "success";
            } else {
                return "error";
            }
        } catch (Exception e) {
            LOGGER.warn("error! - stackTrace: ", e);
            return "error";
        }
    }


    @Override
    public String stopDevice(String userName, String deviceID, DeviceParameters deviceParameters) {
        try {
            String deviceId = deviceID.split("@")[1];
            Map<String, String> reqBodyMap = new HashMap<>();
            reqBodyMap.put("device_id", deviceId);
            String reqBody = new Gson().toJson(reqBodyMap);

            String uri = foaProperties.getSimulatedDeviceServiceConfig().getStopDeviceUri();
            LOGGER.info("Device switched off signal sent for device: {}", deviceID);
            ResponseEntity<String> response = this.requestFLSServer(uri, HttpMethod.POST, null, reqBody);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJsn = mapper.readTree(response.getBody());

            if (responseJsn.get("status").asText().equals("success")) {
                return "success";
            } else {
                return "error";
            }
        } catch (Exception e) {
            LOGGER.warn("error! - stackTrace: ", e);
            return "error";
        }
    }


    @Override
    @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
    public DeviceDetailData updateDeviceState(UserT user, String organizationName, DeviceDetail device,
                                              DeviceParameters deviceParameters) {
        try {
            DeviceDetailData deviceDetailData =
                    getDeviceConsumptionAndState(user.getUserName(), device.getDeviceId(), deviceParameters);
            String msg;
            Map<Date, Double> val = new HashMap<>();
            if (deviceDetailData == null || deviceDetailData.getTime() == null) {
                msg = "deviceDetailData is null or its time attribute is not set.";
                LOGGER.debug(msg);
                //return msg;
                return null;
            }

            //userService.updateDeviceState(user.getUserName(), device.getDeviceDetailId(), deviceDetailData);

            // this hold Aggregated recent data collected from the device
            if (deviceLatestAggData.containsKey(organizationName)) {
                if (deviceLatestAggData.get(organizationName).containsKey(deviceDetailData.getTime())) {
                    Double aggConsumption = deviceLatestAggData.get(organizationName).get(deviceDetailData.getTime()) +
                            deviceDetailData.getValue();
                    val.put(deviceDetailData.getTime(), aggConsumption);
                } else {
                    val.put(deviceDetailData.getTime(), deviceDetailData.getValue());
                }
                deviceLatestAggData.put(organizationName, val);
            } else {
                val.put(deviceDetailData.getTime(), deviceDetailData.getValue());
                deviceLatestAggData.put(organizationName, val);
            }

            // store accumulated energy consumption
            /*
            // only include operation power for devices with active flex offers
            Double devicePower = 0.0;
            if (deviceLatestFO.containsKey(device.getDeviceId())) {
                FlexOfferT fo = deviceLatestFO.get(device.getDeviceId());
                Date currentTime = new Date();
                //if (fo.getFlexoffer().getEndBeforeTime().compareTo(currentTime) > 0
                //        && fo.getStatus() != FlexOfferState.Rejected && fo.getStatus() != FlexOfferState.Executed) {
                //    devicePower = deviceDetailData.getValue();
                //}
                if (fo.getFlexoffer().getStartAfterTime().compareTo(currentTime) < 0 &&
                        fo.getFlexoffer().getEndBeforeTime().compareTo(currentTime) > 0 &&
                        fo.getStatus() != FlexOfferState.Rejected) {
                    devicePower = deviceDetailData.getValue();
                }
            }
             */
            Double devicePower = deviceDetailData.getValue();

            if (orgAccEnergyData.containsKey(organizationName)) {
                Set<Date> keys = orgAccEnergyData.get(organizationName).keySet();
                for (Date key : keys) {
                    Double aggConsumption = orgAccEnergyData.get(organizationName).get(key) + devicePower;
                    orgAccEnergyData.get(organizationName).remove(key);
                    orgAccEnergyData.get(organizationName).put(deviceDetailData.getTime(), aggConsumption);
                }

            } else {
                val.put(deviceDetailData.getTime(), devicePower);
                orgAccEnergyData.put(organizationName, val);
            }

            //return "success";
            return deviceDetailData;
        } catch (Exception e) {
            LOGGER.warn("Error updating state for device: {}. Message: {}",
                    device.getDeviceId(), e.getLocalizedMessage());
            //return "error";
            return null;
        }
    }

    @Override
    public String getNewToken(String userName, DeviceParameters deviceParameters) {

        try {
            LOGGER.info("Requesting new token from FLS server");

            String username = foaProperties.getSimulatedDeviceServiceConfig().getUser();
            String password = foaProperties.getSimulatedDeviceServiceConfig().getPassword();

            String auth = username + ":" + password;
            //byte[] encodedAuth = Base64.getEncoder().encode( auth.getBytes(Charset.forName("US-ASCII")) );
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
            String authHeader = "Basic " + new String(encodedAuth);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", authHeader);

            String newTokenUri = foaProperties.getSimulatedDeviceServiceConfig().getNewTokenUri();

            ResponseEntity<String> response = this.requestFLSServer(newTokenUri, HttpMethod.GET, httpHeaders, null);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.getBody());

            String newToken = jsonNode.get("token").textValue();
            foaProperties.getSimulatedDeviceServiceConfig().setApiToken(newToken);

            LOGGER.info(newToken);
            return newToken;

        } catch (Exception e) {
            LOGGER.error("Error getting new token. Message: {}", e.getLocalizedMessage());
            return "error";
        }
    }

    @Override
    public Map<String, String> getDevices(String userName, DeviceParameters deviceParameters) {
        return null;
    }

    @Override
    public void validateDevice() {
    }


    @Override
    public void toogleDevice(String userName, String deviceID, int currentState, DeviceParameters deviceParameters) {
    }

    @Override
    public String addOnOffSchedule(String deviceID, String userName, Date eventTime, int action) {
        return null;
    }

    @Override
    public JsonNode getOnOffSchedules(String deviceID, DeviceParameters deviceParameters) {
        return null;
    }

    @Override
    public boolean updateOnOffSchedule(String deviceID, String userName, String scheduleId, int action) {
        return false;
    }

    @Override
    public boolean deleteOnOffSchedule(String deviceID, String userName, String scheduleId, int action) {
        return false;
    }


    public ResponseEntity<String> requestFLSServer(String uri, HttpMethod method, HttpHeaders httpHeaders,
                                                   String requestBody) {

        if (httpHeaders == null) {
            httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            String apiToken = foaProperties.getSimulatedDeviceServiceConfig().getApiToken();
            if (apiToken.equals("null") || apiToken.equals("")) {
                this.getNewToken(null, null);
                apiToken = foaProperties.getSimulatedDeviceServiceConfig().getApiToken();
            }

            httpHeaders.add("Authorization", "Bearer " + apiToken);
        }

        HttpEntity<String> entity = new HttpEntity<>(requestBody, httpHeaders);

        String newURl = foaProperties.getSimulatedDeviceServiceConfig().getBaseURI() + uri;
        try {
            return restTemplate.exchange(newURl, method, entity, String.class);
        } catch (HttpClientErrorException e) {
            LOGGER.warn("Error making request to FLS server: " + e.getMessage());
            LOGGER.warn("Response body: " + e.getResponseBodyAsString());
            LOGGER.warn("Response status code: " + e.getStatusCode());
            if (handleHttpClientError(e.getResponseBodyAsString())) {
                return requestFLSServer(uri, method, null, requestBody);
            }
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            //LOGGER.warn("Error making request to FLS server: ", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean handleHttpClientError(String errorResponse) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonNode = mapper.readTree(errorResponse);
            if (jsonNode.has("msg") && jsonNode.get("msg").asText().equals("Token has expired")) {
                this.getNewToken(null, null);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public double getPowerConsumption(String deviceID, DeviceParameters deviceParameters) {
        try {
            String userName = deviceID.split("@")[0];
            String deviceId = deviceID.split("@")[1];
            Map<String, String> reqBodyMap = new HashMap<>();
            reqBodyMap.put("device_id", deviceId);
            String reqBody = new Gson().toJson(reqBodyMap);

            String uri = foaProperties.getSimulatedDeviceServiceConfig().getDevicePowerUri();
            ResponseEntity<String> response = this.requestFLSServer(uri, HttpMethod.POST, null, reqBody);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJsn = mapper.readTree(response.getBody());

            if (responseJsn.get("status").asText().equals("success") && responseJsn.has("data")
                    && !responseJsn.get("data").isNull()) {
                return responseJsn.get("data").get("live_power").asDouble();
            } else {
                return -1.0;
            }
        } catch (Exception e) {
            //LOGGER.warn("error! - stackTrace: ", e.getLocalizedMessage());
            return -1.0;
        }
    }


    public double getEnergyConsumption(String deviceID, DeviceParameters deviceParameters) {
        try {
            String userName = deviceID.split("@")[0];
            String deviceId = deviceID.split("@")[1];
            Map<String, String> reqBodyMap = new HashMap<>();
            reqBodyMap.put("device_id", deviceId);
            String reqBody = new Gson().toJson(reqBodyMap);

            String uri = foaProperties.getSimulatedDeviceServiceConfig().getDeviceEnergyUri();
            ResponseEntity<String> response = this.requestFLSServer(uri, HttpMethod.POST, null, reqBody);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJsn = mapper.readTree(response.getBody());

            if (responseJsn.get("status").asText().equals("success") && responseJsn.has("data")
                    && !responseJsn.get("data").isNull()) {
                return responseJsn.get("data").get("energy").asDouble();
            } else {
                return -1.0;
            }
        } catch (Exception e) {
            LOGGER.warn("error! - stackTrace: ", e);
            return -1.0;
        }
    }


    @Transactional
    public void addSimulatedDeviceToUser(String userName, String devicePlugId, String alias) {
        try {

            LOGGER.info("adding new simulated device '" + alias + "' for user '" + userName + "'");
            UserT user = this.userService.getUser(userName);
            DeviceDetail deviceDetail = new DeviceDetail(userName + "@" + devicePlugId);
            deviceDetail.setDeviceState(DeviceState.Idle);
            deviceDetail.setDevicePlugId(devicePlugId);
            deviceDetail.setAlias("Sim" + alias);
            ConsumptionTsEntity consumptionTs = new ConsumptionTsEntity();
            deviceDetail.setConsumptionTs(consumptionTs);
            deviceDetail.setPlugType(PlugType.Simulated);
            deviceDetail.setDeviceType(DeviceType.valueOf(alias));
            deviceDetail.setTimeZone("+00:00");

            Date lastConnected = new Date();
            deviceDetail.setLastConnectedTime(lastConnected);
            deviceDetail.setRegistrationDate(new Date());
            deviceDetail.setDefaultState(new DeviceDefaultState().getDeviceDefaultState(deviceDetail.getDeviceType()));

            String groupName = "Simulated";
            GroupingDetail groupingDetail = deviceDetailService.getGroupingDetailByGroupName(groupName);
            if (groupingDetail == null) {
                groupingDetail = new GroupingDetail();
                groupingDetail.setGroupName(groupName);
                groupingDetail.setLocationId(-1);
                deviceDetailService.addGroupingDetail(groupingDetail);
            }
            deviceDetail.setGroupingDetail(groupingDetail);

            DeviceHierarchy deviceHierarchy = deviceDetailService.getDeviceHierarchyByHierarchyName(groupName);
            if (deviceHierarchy == null) {
                deviceHierarchy = new DeviceHierarchy();
                deviceHierarchy.setHierarchyName(groupName);
                deviceHierarchy.setUserId(userService.getUser(userName).getId());
                deviceDetailService.addDeviceHierarchy(deviceHierarchy);
            }
            deviceDetail.setDeviceHierarchy(deviceHierarchy);

            DeviceFlexibilityDetail dfd = new DeviceFlexibilityDetail();
            Organization org = organizationRepository.findByOrganizationId(user.getOrganizationId());
            Long organizationId = user.getOrganizationId();
            dfd.setDailyControlStart(defaultFlexibilitySettings.get(organizationId).getDailyControlStart());
            dfd.setDailyControlEnd(defaultFlexibilitySettings.get(organizationId).getDailyControlEnd());
            dfd.setNoOfInterruptionInADay(defaultFlexibilitySettings.get(organizationId).getNoOfInterruptionInADay());
            dfd.setMaxInterruptionLength(defaultFlexibilitySettings.get(organizationId).getMaxInterruptionLength());
            dfd.setMinInterruptionInterval(defaultFlexibilitySettings.get(organizationId).getMinInterruptionInterval());
            dfd.setMaxInterruptionDelay(defaultFlexibilitySettings.get(organizationId).getMaxInterruptionDelay());
            dfd.setLatestAcceptanceTime(defaultFlexibilitySettings.get(organizationId).getLatestAcceptanceTime());
            deviceDetail.setDeviceFlexibilityDetail(dfd);

            // new devices are not flexible by default
            deviceDetail.setFlexible(false);

            userService.updateDeviceList(userName, deviceDetail);

            // add device to in memory device models
            String deviceId = userName + "@" + devicePlugId;
            PoolDeviceModel deviceModel = new PoolDeviceModel(organizationId, deviceId, DeviceType.valueOf(alias).toString(),
                    null,  20, 25,
                    -1.0, null, 0, false, null,
                    false, -1, -1, 22.5, null, null);
            this.poolDeviceDetail.get(org.getOrganizationName()).put(deviceId, deviceModel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String createSimulatedDevice(String userName, String simLoadJson) {
        try {
            LOGGER.info("Creating new simulated device:");
            LOGGER.info(simLoadJson);
            String url = foaProperties.getSimulatedDeviceServiceConfig().getDevicesUri();
            ResponseEntity<String> response = this.requestFLSServer(url, HttpMethod.POST, null, simLoadJson);
            LOGGER.info(response.toString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJsn = mapper.readTree(response.getBody());

            if (responseJsn.get("status").asText().equals("success") && responseJsn.has("data")
                    && !responseJsn.get("data").isNull()) {

                String device_id = responseJsn.get("data").get("device_id").asText();
                String deviceName = responseJsn.get("data").get("device_name").asText();

                if (!device_id.equals("")) {
                    this.addSimulatedDeviceToUser(userName, device_id, deviceName);
                }
                return device_id;
            } else {
                return "error creating simulated device";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    public String deleteSimulatedDevice(String deviceID) {
        try {
            LOGGER.info("Deleting simulated device: " + deviceID);

            String userName = deviceID.split("@")[0];
            String deviceId = deviceID.split("@")[1];
            Map<String, String> reqBodyMap = new HashMap<>();
            reqBodyMap.put("device_id", deviceId);
            String reqBody = new Gson().toJson(reqBodyMap);

            String url = foaProperties.getSimulatedDeviceServiceConfig().getDevicesUri();
            ResponseEntity<String> response = this.requestFLSServer(url, HttpMethod.DELETE, null, reqBody);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.getBody());

            if (jsonNode.get("status").asText().equals("success")) {

                //remove from device model map
                poolDeviceDetail.remove(deviceID);

                return "success";
            } else {
                return "error";
            }
        } catch (Exception e) {
            LOGGER.warn("error! - stackTrace: " + e);
            return e.getMessage();
        }
    }


    public String deleteAllSimulatedDevicesForUser(String userName) {
        try {
            LOGGER.info("Deleting all simulated loads for user:" + userName);
            String uri = foaProperties.getSimulatedDeviceServiceConfig().getDevicesUri() + "/deleteAll";
            ResponseEntity<String> response = this.requestFLSServer(uri, HttpMethod.DELETE, null, null);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.getBody());

            if (jsonNode.get("status").asText().equals("success")) {
                return "success";
            } else {
                return "error";
            }

        } catch (Exception e) {
            LOGGER.warn("error! - stackTrace: " + e);
            return e.getMessage();
        }
    }

    @Override
    public String getTimeZone(String userName, DeviceParameters deviceParameters, String deviceId) {
        return "+00:00";
    }

    public void setDeviceModels(Map<String, Map<String, Object>> deviceModels) {
        this.deviceModels = deviceModels;
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
        }
    }

    private void prepareSimDeviceModelsMap() {
        this.deviceModels = new HashMap<>();
        Map<String, Object> params;

        Map<String, Object> PV = new HashMap<>();
        PV.put("device_name", "PV");
        PV.put("model_name", "OnOff");
        params = new HashMap<>();
        params.put("p_on", 2500);
        PV.put("params", params);
        this.deviceModels.put("PV", PV);

        Map<String, Object> wind = new HashMap<>();
        wind.put("device_name", "Wind");
        wind.put("model_name", "OnOff");
        params = new HashMap<>();
        params.put("p_on", 3000);
        wind.put("params", params);
        this.deviceModels.put("Wind", wind);

        Map<String, Object> refrigerator = new HashMap<>();
        refrigerator.put("device_name", "Refrigerator");
        refrigerator.put("model_name", "ExponentialDecay");
        params = new HashMap<>();
        params.put("lambda", 0.27);
        params.put("p_active", 126.19);
        params.put("p_peak", 650.5);
        refrigerator.put("params", params);
        this.deviceModels.put("Refrigerator", refrigerator);


        Map<String, Object> airConditioner = new HashMap<>();
        airConditioner.put("device_name", "AirConditioner");
        airConditioner.put("model_name", "LogarithmicGrowth");
        params = new HashMap<>();
        params.put("lambda", 13.78);
        params.put("p_base", 2120.46 + 126.19);
        airConditioner.put("params", params);
        this.deviceModels.put("AirConditioner", airConditioner);

        Map<String, Object> heatPump = new HashMap<>();
        heatPump.put("device_name", "HeatPump");
        heatPump.put("model_name", "SISOLinearSystem");
        params = new HashMap<>();
        params.put("A", -0.01);
        params.put("B", 0.002);
        params.put("C", 1);
        params.put("D", 0);
        heatPump.put("params", params);
        this.deviceModels.put("HeatPump", heatPump);
    }


    public String createSimulatedDevices(String userName, int num_devices_to_create) {
        try {
            if (num_devices_to_create <= 0) {
                return "success";
            }
            LOGGER.info(String.format("Creating %d random simulated loads for %s", num_devices_to_create, userName));

            List<String> chosenModels = new ArrayList<>(Arrays.asList("AirConditioner", "Refrigerator"));

            for (int i = 0; i < num_devices_to_create; i++) {
                int idx = new Random().nextInt(chosenModels.size());
                String simLoadJson = new Gson().toJson(this.deviceModels.get(chosenModels.get(idx)));
                this.createSimulatedDevice(userName, simLoadJson);
            }
            return "success";

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return "error";
        }
    }


    public String createSimulatedUser() {
        String userName = "simUser";
        if (userService.getUser(userName) != null) {
            LOGGER.info("User: {} already exists", userName);
        } else {
            LOGGER.info("Creating user: " + userName);
            String userPass = "password";
            UserT simUser = new UserT();
            simUser.setUserName(userName);
            simUser.setPassword(passwordEncoder.encode(userPass));
            simUser.setRole(UserRole.ROLE_PROSUMER);
            UserAddress address = new UserAddress();
            simUser.setUserAddress(address);
            simUser.setRegistrationDate(new Date());
            simUser.setOrganization(organizationRepository.findByOrganizationName("AAU").getOrganizationId());
            userService.save(simUser);
            LOGGER.info("Successfully created user: " + userName);
        }
        return userName;
    }
}
