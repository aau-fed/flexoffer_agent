package org.goflex.wp2.foa.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.core.models.*;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.goflex.wp2.foa.interfaces.xEmsServices;
import org.goflex.wp2.foa.swiss.MqttPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SwissDeviceService implements xEmsServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwissDeviceService.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:00'Z'";
    private static final long BASE_LATENCY = 60; // seconds
    private static Map<String, Double> lastConsumption = new HashMap<>();
    @Resource(name = "deviceLatestAggData")
    LinkedHashMap<String, Map<Date, Double>> deviceLatestAggData;
    @Resource(name = "orgAccEnergyData")
    LinkedHashMap<String, Map<Date, Double>> orgAccEnergyData;
    @Resource(name = "defaultFlexibilitySettings")
    Map<Long, DeviceFlexibilityDetail> defaultFlexibilitySettings;
    @Resource(name = "deviceLatestFO")
    ConcurrentHashMap<String, FlexOfferT> deviceLatestFO;
    @Resource(name = "tclDeviceFutureFOs")
    ConcurrentHashMap<String, Date> tclDeviceFutureFOs;
    @Resource(name = "devicePendingControlSignals")
    ConcurrentHashMap<String, Integer> devicePendingControlSignals;
    @Resource(name = "mqttDataMap")
    private ConcurrentHashMap<String, Map<String, Map<String, Object>>> mqttDataMap;
    private UserService userService;
    private DeviceDetailService deviceDetailService;
    private MqttPublisher mqttPublisher;
    private FOAProperties foaProperties;
    private PasswordEncoder passwordEncoder;
    private OrganizationRepository organizationRepository;
    private RestTemplate restTemplate;
    private DeviceFlexOfferGroup deviceFlexOfferGroup;

    private boolean start = false;

    public SwissDeviceService() {
    }

    @Autowired
    public SwissDeviceService(UserService userService,
                              DeviceDetailService deviceDetailService,
                              MqttPublisher mqttPublisher,
                              FOAProperties foaProperties,
                              PasswordEncoder passwordEncoder,
                              OrganizationRepository organizationRepository,
                              RestTemplate restTemplate,
                              DeviceFlexOfferGroup deviceFlexOfferGroup) {
        this.userService = userService;
        this.deviceDetailService = deviceDetailService;
        this.mqttPublisher = mqttPublisher;
        this.foaProperties = foaProperties;
        this.passwordEncoder = passwordEncoder;
        this.organizationRepository = organizationRepository;
        this.restTemplate = restTemplate;
        this.deviceFlexOfferGroup = deviceFlexOfferGroup;
    }

    @Override
    public String getNewToken(String userName, DeviceParameters deviceParameters) {
        return null;
    }

    @Override
    public void validateDevice() {

    }

    //@Scheduled(fixedRate = 60000*5)
    public void testControlDevice() {
        // boiler of house 40
        String deviceID = "";
        start = !start;
        if (start) {
            startDevice(null, deviceID, null);
        } else {
            stopDevice(null, deviceID, null);
        }
    }


    @Override
    public String startDevice(String userName, String deviceID, DeviceParameters deviceParameters) {
        try {

            DeviceDetail deviceDetail = deviceDetailService.getDevice(deviceID);
            LOGGER.info("Turning on device: " + deviceDetail.getAlias());

            String houseId = deviceDetail.getDeviceHierarchy().getHierarchyName();
            String object = deviceDetail.getAlias().split("_")[1] + "Control";
            String topic = "@set/" + houseId + "/nodes/gateway/objects/" + object + "/attributes/parameter";

            ObjectNode objectNode = new ObjectMapper().createObjectNode();
            objectNode.put("timestamp", System.currentTimeMillis());
            objectNode.put("value", 1);
            byte[] message = objectNode.toString().getBytes(StandardCharsets.UTF_8);

            String result = mqttPublisher.publishMessage(topic, message);
            if (!result.equals("success")) {
                this.devicePendingControlSignals.put(deviceDetail.getDeviceId(), 1);
            } else {
                if (this.devicePendingControlSignals.containsKey(deviceDetail.getDeviceId())) {
                    this.devicePendingControlSignals.remove(deviceDetail.getDeviceId());
                }
            }
            LOGGER.debug("Devices switch on signal Sent");

            return result;
        } catch (Exception e) {
            LOGGER.warn("error! - stackTrace: ", e);
            this.devicePendingControlSignals.put(deviceID, 1);
            return "error";
        }
    }


    @Override
    public String stopDevice(String userName, String deviceID, DeviceParameters deviceParameters) {
        try {

            DeviceDetail deviceDetail = deviceDetailService.getDevice(deviceID);
            LOGGER.info("Turning off device: " + deviceDetail.getAlias());

            String houseId = deviceDetail.getDeviceHierarchy().getHierarchyName();
            String object = deviceDetail.getAlias().split("_")[1] + "Control";
            String topic = "@set/" + houseId + "/nodes/gateway/objects/" + object + "/attributes/parameter";

            ObjectNode objectNode = new ObjectMapper().createObjectNode();
            objectNode.put("timestamp", System.currentTimeMillis());
            objectNode.put("value", 0);
            byte[] message = objectNode.toString().getBytes(StandardCharsets.UTF_8);

            String result = mqttPublisher.publishMessage(topic, message);
            if (!result.equals("success")) {
                this.devicePendingControlSignals.put(deviceDetail.getDeviceId(), 0);
            } else {
                if (this.devicePendingControlSignals.containsKey(deviceDetail.getDeviceId())) {
                    this.devicePendingControlSignals.remove(deviceDetail.getDeviceId());
                }
            }
            LOGGER.debug("Devices switch off signal Sent");

            return result;
        } catch (Exception e) {
            LOGGER.warn("error! - stackTrace: ", e);
            this.devicePendingControlSignals.put(deviceID, 0);
            return "error";
        }
    }


    @Override
    public void toogleDevice(String userName, String deviceID, int currentState, DeviceParameters deviceParameters) {
    }


    @Override
    public double getDeviceConsumption(String userName, String deviceID, DeviceParameters deviceParameters) {
        return 0;
    }

    @Override
    public DeviceDetailData getDeviceConsumptionAndState(String userName, String deviceID,
                                                         DeviceParameters deviceParameters) {
        return null;
    }

    @Override
    public Map<String, String> getDevices(String userName, DeviceParameters deviceParameters) {
        return null;
    }

    @Override
    public int getDeviceState(String userName, String deviceID, DeviceParameters deviceParameters) {
        return deviceDetailService.getDevice(deviceID).getDeviceState().getValue();
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

    @Override
    public void processWetAndBatteryDevice(UserT user, String organizationName, DeviceDetail device,
                                           DeviceParameters deviceParameters) {

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

            DeviceDetailData consumptionData = getDeviceDataFromMqttDataMap(device.getDeviceId());
            if (consumptionData == null) {
                LOGGER.debug("No consumption data found for device: {}", device.getDeviceId());
                return;
            }

            Double currentConsumption = consumptionData.getValue();

            // if device is just turned on, then turn if off and make api call to generate flex offer
            if (lastConsumption.containsKey(device.getDevicePlugId())) {

                Double deviceOffThresholdPower = 200.0;
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
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 100, maxDelay = 500))
    public DeviceDetailData updateDeviceState(UserT user, String organizationName, DeviceDetail device,
                                              DeviceParameters deviceParameters) {
        try {
            DeviceDetailData deviceDetailData = getDeviceDataFromMqttDataMap(device.getDeviceId());
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
            return null;
        }
    }


    /**
     * fetch data for the given device (currently entire house is one device) from {@code mqttDataMap}
     *
     * @param deviceId the device id
     * @return {@link DeviceDetailData} object containing primary data {@link DeviceData} and supplementary data
     * {@link DeviceDataSuppl}.
     */
    private DeviceDetailData getDeviceDataFromMqttDataMap(String deviceId) {

        //LOGGER.info("\n\n\nmqttDataMap");
        //LOGGER.info(mqttDataMap.toString());

        if (mqttDataMap.isEmpty()) {
            LOGGER.debug("mqttDataMap is empty");
            return null;
        }

        DeviceDetail deviceDetail = deviceDetailService.getDevice(deviceId);
        String houseId = deviceDetail.getDeviceHierarchy().getHierarchyName();
        if (!mqttDataMap.containsKey(houseId)) {
            LOGGER.debug("mqttDataMap doesn't contain any data for: " + houseId);
            return null;
        }

        Map<String, Map<String, Object>> houseDataMap = mqttDataMap.get(houseId);
        DeviceDetailData deviceDetailData = new DeviceDetailData();
        DeviceData deviceData = null;
        DeviceDataSuppl deviceDataSuppl = null;
        Date timestamp;
        long secondsSinceLastReceived;

        // ambient temperature
        String ambientTemperatureParam = "ambientSensor-1_temperature";
        if (mqttDataMap.get(houseId).containsKey(ambientTemperatureParam)) {
            timestamp = (Date) houseDataMap.get(ambientTemperatureParam).get("timestamp");
            secondsSinceLastReceived = (new Date().getTime() - timestamp.getTime()) / 1000;
            double ambientTemperature = secondsSinceLastReceived < BASE_LATENCY * 180?
                    (Double) houseDataMap.get(ambientTemperatureParam).get("datapoint") : -1;
            if (deviceDataSuppl == null) {
                deviceDataSuppl = new DeviceDataSuppl();
            }
            deviceDataSuppl.setAmbientTemperature(ambientTemperature);
        } else {
            LOGGER.trace("mqttDataMap doesn't contain '" + ambientTemperatureParam + "' data for: " + deviceId);
        }

        // ambient humidity
        String ambientHumidityParam = "ambientSensor-1_relHumidity";
        if (mqttDataMap.get(houseId).containsKey(ambientHumidityParam)) {
            timestamp = (Date) houseDataMap.get(ambientHumidityParam).get("timestamp");
            secondsSinceLastReceived = (new Date().getTime() - timestamp.getTime()) / 1000;
            // 11 minutes (BASE_LATENCY * 11) max latency
            double ambientHumidity = secondsSinceLastReceived < BASE_LATENCY * 180 ?
                    (Double) houseDataMap.get(ambientHumidityParam).get("datapoint") : -1;
            if (deviceDataSuppl == null) {
                deviceDataSuppl = new DeviceDataSuppl();
            }
            deviceDataSuppl.setAmbientHumidity(ambientHumidity);
        } else {
            LOGGER.trace("mqttDataMap doesn't contain '" + ambientHumidityParam + "' data for: " + deviceId);
        }

        // boiler temperature
        String boilerTemperatureParam = "boilerSensor-1_temperature";
        if (mqttDataMap.get(houseId).containsKey(boilerTemperatureParam)) {
            timestamp = (Date) houseDataMap.get(boilerTemperatureParam).get("timestamp");
            secondsSinceLastReceived = (new Date().getTime() - timestamp.getTime()) / 1000;
            // 31 minutes (BASE_LATENCY * 31) max latency
            double boilerTemperature = secondsSinceLastReceived < BASE_LATENCY * 180 ?
                    (Double) houseDataMap.get(boilerTemperatureParam).get("datapoint") : -1;
            if (deviceDataSuppl == null) {
                deviceDataSuppl = new DeviceDataSuppl();
            }
            deviceDataSuppl.setBoilerTemperature(boilerTemperature);
        } else {
            LOGGER.trace("mqttDataMap doesn't contain '" + boilerTemperatureParam + "' data for: " + deviceId);
        }

        // device current
        String deviceCurrentParam = deviceDetail.getAlias().split("_")[2] + "_ampsTotal";
        if (mqttDataMap.get(houseId).containsKey(deviceCurrentParam)) {
            timestamp = (Date) houseDataMap.get(deviceCurrentParam).get("timestamp");
            secondsSinceLastReceived = (new Date().getTime() - timestamp.getTime()) / 1000;
            // 1 minute max latency
            double deviceCurrent = secondsSinceLastReceived < BASE_LATENCY * 2 ?
                    (Double) houseDataMap.get(deviceCurrentParam).get("datapoint") : -1;
            if (deviceData == null) {
                deviceData = new DeviceData();
            }
            deviceData.setCurrent(deviceCurrent);
        } else {
            LOGGER.trace("mqttDataMap doesn't contain '" + deviceCurrentParam + "' data for: " + deviceId);
        }

        // device voltage
        String deviceVoltageParam = deviceDetail.getAlias().split("_")[2] + "_voltsTotal";
        if (mqttDataMap.get(houseId).containsKey(deviceVoltageParam)) {
            timestamp = (Date) houseDataMap.get(deviceVoltageParam).get("timestamp");
            secondsSinceLastReceived = (new Date().getTime() - timestamp.getTime()) / 1000;
            double deviceVoltage = secondsSinceLastReceived < BASE_LATENCY * 2 ? // 2 minute max latency
                    (Double) houseDataMap.get(deviceVoltageParam).get("datapoint") : -1;
            if (deviceData == null) {
                deviceData = new DeviceData();
            }
            deviceData.setVoltage(deviceVoltage);
        } else {
            LOGGER.trace("mqttDataMap doesn't contain '" + deviceVoltageParam + "' data for: " + deviceId);
        }

        // device power
        String devicePowerParam = deviceDetail.getAlias().split("_")[2] + "_wattsTotal";
        if (mqttDataMap.get(houseId).containsKey(devicePowerParam)) {
            timestamp = (Date) houseDataMap.get(devicePowerParam).get("timestamp");
            secondsSinceLastReceived = (new Date().getTime() - timestamp.getTime()) / 1000;
            double devicePower = secondsSinceLastReceived < BASE_LATENCY * 2 ? // 2 minute max latency
                    (Double) houseDataMap.get(devicePowerParam).get("datapoint") : -1;
            if (deviceData == null) {
                deviceData = new DeviceData();
            }
            deviceData.setPower(devicePower);
        } else {
            LOGGER.debug("mqttDataMap doesn't contain '" + devicePowerParam + "' data for: " + deviceId);
        }

        // device energy
        String deviceEnergyParam = deviceDetail.getAlias().split("_")[2] + "_kWhTotal";
        if (mqttDataMap.get(houseId).containsKey(deviceEnergyParam)) {
            timestamp = (Date) houseDataMap.get(deviceEnergyParam).get("timestamp");
            secondsSinceLastReceived = (new Date().getTime() - timestamp.getTime()) / 1000;

            double deviceEnergy = secondsSinceLastReceived < BASE_LATENCY * 2 ? // 2 minute max latency
                    (Double) houseDataMap.get(deviceEnergyParam).get("datapoint") : -1;
            if (deviceData == null) {
                deviceData = new DeviceData();
            }
            deviceData.setEnergy(deviceEnergy);
        } else {
            LOGGER.trace("mqttDataMap doesn't contain '" + deviceEnergyParam + "' data for: " + deviceId);
        }

        // device state
        String deviceStateParam = "gateway_" + deviceDetail.getAlias().split("_")[1] + "State";
        if (mqttDataMap.get(houseId).containsKey(deviceStateParam)) {
            timestamp = (Date) houseDataMap.get(deviceStateParam).get("timestamp");
            secondsSinceLastReceived = (new Date().getTime() - timestamp.getTime()) / 1000;
            int deviceStatus = secondsSinceLastReceived < BASE_LATENCY * 300 ? // 300 minutes max latency
                    ((Double) houseDataMap.get(deviceStateParam).get("datapoint")).intValue() : -1;
            DeviceState deviceState;
            if (deviceStatus == 0) {
                deviceState = DeviceState.Idle;
            } else if (deviceStatus == 1) {
                deviceState = DeviceState.Operating;
            } else {
                deviceState = DeviceState.Disconnected;
            }
            deviceDetailData.setState(deviceState);
        } else {
            LOGGER.debug("mqttDataMap doesn't contain '" + deviceStateParam + "' data for: " + deviceId +
                    ". Temporarily estimating device state based on power consumption.");
            if (deviceData != null) {
                double threshold = 10.0;
                if (deviceData.getPower() < threshold) {
                    deviceDetailData.setState(DeviceState.Idle);
                } else {
                    deviceDetailData.setState(DeviceState.Operating);
                }
            } else {
                deviceDetailData.setState(DeviceState.Unknown);
            }
        }

        // don't update device status and data if no device data received
        if (deviceData == null) {
            return null;
        }

        Date date = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
            date = df.parse(df.format(new Date()));
        } catch (ParseException e) {
            LOGGER.warn("Date format error. Message: {}", e.getLocalizedMessage());
            return null;
        }
        deviceData.setDate(date);
        deviceDetailData.setDeviceData(deviceData);
        deviceDetailData.setValue(deviceData.getPower());
        deviceDetailData.setTime(date);
        if (deviceDataSuppl != null) {
            deviceDataSuppl.setDate(date);
            deviceDetailData.setDeviceDataSuppl(deviceDataSuppl);
        }

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
                LOGGER.debug("Error making Http request to url: {}, status code: {}", url, e.getStatusCode());
            }
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (ResourceAccessException e) {
            LOGGER.error("Error making Http request to url: {}, message: {}", url, e.getLocalizedMessage());
            return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    public void discoverSwissDevices() {

        try {
            LOGGER.info("Running Swiss Device Discovery Routine...");
            // make get request to get list of houses as json
            String houseListUrl = foaProperties.getMqttServiceConfig().getHouseListUrl();
            ResponseEntity<String> response = makeHttpRequest(houseListUrl, HttpMethod.GET, null, null);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode houseList = mapper.readTree(response.getBody());

            String userName = foaProperties.getMqttServiceConfig().getFoaAccountUser();
            UserT userT = this.userService.getUser(userName);
            if (userT == null) {
                Organization organization = this.organizationRepository.findByOrganizationName("SWISS");
                String userPass = foaProperties.getMqttServiceConfig().getFoaAccountPassword();
                UserT swissUser = new UserT();
                swissUser.setUserName(userName);
                swissUser.setPassword(passwordEncoder.encode(userPass));
                swissUser.setRole(UserRole.ROLE_PROSUMER);
                UserAddress address = new UserAddress();
                swissUser.setUserAddress(address);
                swissUser.setRegistrationDate(new Date());
                swissUser.setOrganization(organization.getOrganizationId());
                swissUser.setEmail(userName + "@please-set.email");
                userT = userService.save(swissUser);
            }

            // remove orphan houses
            List<DeviceHierarchy> existingSwissHouses =
                    this.deviceDetailService.getAllDeviceHierarchiesByUserId(userT.getId());
            List<String> lastestHouseList = new ArrayList<>();
            houseList.forEach(house -> lastestHouseList.add(house.get("house").asText()));
            existingSwissHouses.forEach(h -> {
                if (!lastestHouseList.contains(h.getHierarchyName())) {

                    // 1. delete the devices in the house
                    deviceDetailService.deleteDevicesByHierarchyId(h.getHierarchyId());

                    // 2. delete the house
                    deviceDetailService.deleteDeviceHierarchyByHierarchyName(h.getHierarchyName());

                }
            });


            for (JsonNode house : houseList) {
                String houseId = house.get("house").asText();
                String hotwater_relay = house.get("hotwater_relay").asText();
                String heating_relay = house.get("heating_relay").asText();


                // no devices to add as both relays are disconnected
                if (hotwater_relay.equals("0") && heating_relay.equals("0")) {
                    continue;
                }

                List<DeviceDetail> devices = deviceDetailService.getDevicesByGroupName(houseId);
                UserT user = this.userService.getUser(userName);

                if (heating_relay.equals("0") || hotwater_relay.equals("0") || heating_relay.equals(hotwater_relay)) {
                    // db shouldn't contain more than 1 device for the house since boiler and heatpump are combined
                    // this might have happened due to erroneous house info in swiss house list
                    if (devices.size() != 1) {
                        devices.forEach(
                                device -> deviceDetailService.deleteDevicesByDeviceId(device.getDeviceDetailId()));
                    }
                } else {
                    // db should contain 2 devices for the house since boiler and heatpump aren't combined
                    // this might have happened due to erroneous house info in swiss house list
                    if (devices.size() != 2) {
                        devices.forEach(
                                device -> deviceDetailService.deleteDevicesByDeviceId(device.getDeviceDetailId()));
                    }
                }

                if (hotwater_relay.equals("0")) {
                    // create heatpump device
                    String heatPumpRelay = house.get("heating_relay").asText();
                    String heatPumpPowerMeter = Integer.toString(house.get("heating_powermeter").asInt());
                    String heatPumpAlias = "Heating_relay" + heatPumpRelay + "_powerMeter-" + heatPumpPowerMeter;
                    addDeviceToSwissUser(user, houseId, heatPumpAlias, DeviceType.HeatPump);
                } else if (heating_relay.equals("0")) {
                    // create boiler device
                    String boilerRelay = house.get("hotwater_relay").asText();
                    String boilerPowerMeter = Integer.toString(house.get("hotwater_powermeter").asInt());
                    String boilerAlias = "HotWater_relay" + boilerRelay + "_powerMeter-" + boilerPowerMeter;
                    addDeviceToSwissUser(user, houseId, boilerAlias, DeviceType.Boiler);
                } else {

                    // if heatpump is separate from boiler; create two separate devices
                    if (!hotwater_relay.equals(heating_relay)) {

                        // 1. create heatpump device
                        String heatPumpRelay = house.get("heating_relay").asText();
                        String heatPumpPowerMeter = Integer.toString(house.get("heating_powermeter").asInt());
                        String heatPumpAlias = "Heating_relay" + heatPumpRelay + "_powerMeter-" + heatPumpPowerMeter;
                        addDeviceToSwissUser(user, houseId, heatPumpAlias, DeviceType.HeatPump);

                        // 2. create boiler device
                        String boilerRelay = house.get("hotwater_relay").asText();
                        String boilerPowerMeter = Integer.toString(house.get("hotwater_powermeter").asInt());
                        String boilerAlias = "HotWater_relay" + boilerRelay + "_powerMeter-" + boilerPowerMeter;
                        addDeviceToSwissUser(user, houseId, boilerAlias, DeviceType.Boiler);

                    } else {

                        // boiler is connected to heatpump and shares hot water with it. create one combined device
                        // can also use 'hotwater_relay' and 'hotwater_powermeter' since they are same
                        String combinedRelay = house.get("heating_relay").asText();
                        String combinedPowerMeter = Integer.toString(house.get("heating_powermeter").asInt());
                        String combinedAlias = "Combined_relay" + combinedRelay + "_powerMeter-" + combinedPowerMeter;
                        addDeviceToSwissUser(user, houseId, combinedAlias, DeviceType.HeatPump);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }


    private void addDeviceToSwissUser(UserT user, String houseId, String alias, DeviceType deviceType) {
        try {

            if (deviceExists(houseId, alias)) {
                return;
            }

            LOGGER.info("adding new swiss device '" + alias + "' for house '" + houseId + "'");
            String devicePlugId = UUID.randomUUID().toString().replace("-", "");
            String userName = foaProperties.getMqttServiceConfig().getFoaAccountUser();
            String deviceId = userName + "@" + devicePlugId;
            DeviceDetail deviceDetail = new DeviceDetail(deviceId);
            deviceDetail.setDeviceState(DeviceState.Idle);
            deviceDetail.setDevicePlugId(devicePlugId);
            deviceDetail.setAlias(alias);
            ConsumptionTsEntity consumptionTs = new ConsumptionTsEntity();
            deviceDetail.setConsumptionTs(consumptionTs);
            deviceDetail.setPlugType(PlugType.SwissCase);
            deviceDetail.setDeviceType(deviceType);
            deviceDetail.setTimeZone("+02:00");
            Date lastConnected = new Date();
            deviceDetail.setLastConnectedTime(lastConnected);
            deviceDetail.setRegistrationDate(new Date());
            deviceDetail.setDefaultState(new DeviceDefaultState().getDeviceDefaultState(deviceDetail.getDeviceType()));

            GroupingDetail groupingDetail = deviceDetailService.getGroupingDetailByGroupName(houseId);
            if (groupingDetail == null) {
                groupingDetail = new GroupingDetail();
                groupingDetail.setGroupName(houseId);
                groupingDetail.setLocationId(-1);
                deviceDetailService.addGroupingDetail(groupingDetail);
            }
            deviceDetail.setGroupingDetail(groupingDetail);

            DeviceHierarchy deviceHierarchy = deviceDetailService.getDeviceHierarchyByHierarchyName(houseId);
            if (deviceHierarchy == null) {
                deviceHierarchy = new DeviceHierarchy();
                deviceHierarchy.setHierarchyName(houseId);
                deviceHierarchy.setUserId(userService.getUser(userName).getId());
                deviceDetailService.addDeviceHierarchy(deviceHierarchy);
            }
            deviceDetail.setDeviceHierarchy(deviceHierarchy);

            DeviceFlexibilityDetail dfd = new DeviceFlexibilityDetail();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean deviceExists(String houseId, String deviceName) {
        List<DeviceDetail> devices = deviceDetailService.getDevicesByGroupName(houseId);

        if (devices.size() == 0) {
            return false;
        }

        for (DeviceDetail device : devices) {
            if (device.getAlias().equals(deviceName)) {
                LOGGER.debug(device.getAlias() + " already exists for " + houseId);
                return true;
            }
        }

        return false;
    }

    @Override
    public String getTimeZone(String userName, DeviceParameters deviceParameters, String deviceId) {
        return "+00:00";
    }
}
