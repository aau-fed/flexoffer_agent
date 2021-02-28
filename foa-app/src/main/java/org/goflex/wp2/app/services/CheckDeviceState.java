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
 *  Last Modified 2/22/18 9:35 PM
 */

package org.goflex.wp2.app.services;

import org.goflex.wp2.app.common.AppRuntimeConfig;
import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.Organization;
import org.goflex.wp2.core.models.PoolDeviceModel;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.core.repository.DeviceRepository;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.events.DeviceStateChangeEvent;
import org.goflex.wp2.foa.events.UpdateDevicesEvent;
import org.goflex.wp2.foa.implementation.ImplementationsHandler;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.goflex.wp2.foa.listeners.FlexOfferScheduleReceivedListener;
import org.goflex.wp2.foa.wrapper.DeviceDetailDataWrapper;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by bijay on 1/5/18.
 */
@Component
public class CheckDeviceState {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckDeviceState.class);

    @Resource(name = "tclDeviceFutureFOs")
    ConcurrentHashMap<String, Date> tclDeviceFutureFOs;

    @Resource(name = "devicePendingControlSignals")
    ConcurrentHashMap<String, Integer> devicePendingControlSignals;

    @Resource(name = "poolDeviceDetail")
    private ConcurrentHashMap<String, Map<String, PoolDeviceModel>> poolDeviceDetail;

    private List<DeviceDetailDataWrapper> deviceDetailDataList;

    private UserService userService;
    private OrganizationRepository organizationRepository;
    private DeviceDetailService deviceDetailService;
    private DeviceRepository deviceRepository;
    private FOAProperties foaProperties;
    private AppRuntimeConfig appRuntimeConfig;
    private ImplementationsHandler implementationsHandler;
    private ApplicationEventPublisher applicationEventPublisher;
    private FlexOfferScheduleReceivedListener flexOfferScheduleReceivedListener;


    public CheckDeviceState() {
    }

    @Autowired
    public CheckDeviceState(UserService userService,
                            FOAProperties foaProperties,
                            DeviceDetailService deviceDetailService,
                            OrganizationRepository organizationRepository,
                            DeviceRepository deviceRepository,
                            AppRuntimeConfig appRuntimeConfig,
                            ImplementationsHandler implementationsHandler,
                            ApplicationEventPublisher applicationEventPublisher,
                            FlexOfferScheduleReceivedListener flexOfferScheduleReceivedListener) {
        this.userService = userService;
        this.foaProperties = foaProperties;
        this.deviceDetailService = deviceDetailService;
        this.organizationRepository = organizationRepository;
        this.deviceRepository = deviceRepository;
        this.appRuntimeConfig = appRuntimeConfig;
        this.implementationsHandler = implementationsHandler;
        this.applicationEventPublisher = applicationEventPublisher;
        this.flexOfferScheduleReceivedListener = flexOfferScheduleReceivedListener;
    }


    //@Scheduled(fixedRate = 60000)
    // second, minute, hour, day of month, month, day of week.
    //e.g. "0 * * * * MON-FRI" means once per minute on weekdays
    //this schedule updates the device state and collect consumption data
    @Scheduled(cron = "0 * * * * *")
    public void updateDevices() {
        if (this.appRuntimeConfig.isMonitorPlugStatus()) {
            this.deviceDetailDataList = new ArrayList<>();
            Date startTime = new Date();
            LOGGER.info("Updating devices state for all users");
            organizationRepository.findAll()
                    .stream().filter(o -> o.getDirectControlMode() != OrganizationLoadControlState.Stopped)
                    .forEach(this::processForOrganization);
            LOGGER.debug("Fetched device data and status update for all users.");
            UpdateDevicesEvent updateDevicesEvent = new UpdateDevicesEvent(
                    this, "Persist state and consumption data update for all devices",
                    this.deviceDetailDataList);
            this.applicationEventPublisher.publishEvent(updateDevicesEvent);
            LOGGER.info("Updated device state for all users. Took {} seconds",
                    (new Date().getTime() - startTime.getTime()) / 1000);
        }
    }


    private void processForOrganization(Organization organization) {
        LOGGER.debug("Updating devices for organization: " + organization.getOrganizationName());
        List<UserT> users = userService.getActiveUsersForOrganization(organization.getOrganizationId());
        Hibernate.initialize(users);
        users.forEach(user -> processForUser(user, organization));
    }


    private void processForUser(UserT user, Organization organization) {
        deviceDetailService.getActiveDeviceByUser(user.getId()).stream()
                .filter(deviceDetail -> deviceDetail.getDeviceType() != null)
                .forEach(deviceDetail -> {
                    DeviceDetailDataWrapper deviceDetailDataWrapper = new DeviceDetailDataWrapper();
                    deviceDetailDataWrapper.setUserName(user.getUserName());
                    deviceDetailDataWrapper.setDeviceDetailId(deviceDetail.getDeviceDetailId());
                    DeviceDetailData data = this.processDevice(user, organization, deviceDetail);
                    if (data != null) {
                        deviceDetailDataWrapper.setDeviceDetailData(data);
                        this.deviceDetailDataList.add(deviceDetailDataWrapper);
                    }
                });
    }


    private DeviceDetailData processDevice(UserT user, Organization organization, DeviceDetail deviceDetail) {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setAPIKey(user.getAPIKey() != null ? user.getAPIKey() : "");
        deviceParameters.setCloudUserName(user.getTpLinkUserName());
        deviceParameters.setCloudPassword(user.getTpLinkPassword());
        deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());

        return this.implementationsHandler.get(deviceDetail.getPlugType())
                .updateDeviceState(user, organization.getOrganizationName(), deviceDetail, deviceParameters);
    }

    //This schedule is to monitor wet device state every 13 seconds,
    // if device is turnedon the loop should stop the device and genarate a flexoffer
    @Scheduled(fixedRate = 13000)
    public void checkWetAndBatteryDevices() {
        if (this.appRuntimeConfig.isMonitorPlugStatus()) {
            LOGGER.debug("Monitoring wet and battery devices state for all users");
            organizationRepository.findAll()
                    .stream().filter(o -> o.getDirectControlMode() != OrganizationLoadControlState.Stopped)
                    .forEach(this::processWetAndBatteryDevicesForOrganization);
            LOGGER.debug("Wet and battery devices monitoring completed for all users");
        }
    }

    private void processWetAndBatteryDevicesForOrganization(Organization organization) {
        LOGGER.info("monitoring wet and battery devices for organization: " + organization.getOrganizationName());
        List<UserT> users = userService.getActiveUsersForOrganization(organization.getOrganizationId());
        Hibernate.initialize(users);
        users.forEach(user -> processWetAndBatteryDevicesForUser(user, organization));
    }

    private void processWetAndBatteryDevicesForUser(UserT user, Organization organization) {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setAPIKey(user.getAPIKey() != null ? user.getAPIKey() : "");
        deviceParameters.setCloudUserName(user.getTpLinkUserName());
        deviceParameters.setCloudPassword(user.getTpLinkPassword());
        deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
        deviceRepository.findWetDevicesByUserId(user.getId()).stream()
                .filter(deviceDetail -> deviceDetail.getDeviceType() != null && deviceDetail.isFlexible())
                .forEach(deviceDetail -> implementationsHandler.get(deviceDetail.getPlugType())
                        .processWetAndBatteryDevice(user, organization.getOrganizationName(), deviceDetail,
                                deviceParameters));
    }

    //This schedule is to monitor wet device state every 13 seconds,
    // if device is turned on the loop should stop the device and generate a flexoffer
    //TODO: get data from database as its already extracted
    @Scheduled(fixedRate = 60000)
    public void checkTCLDevices() {
        if (this.appRuntimeConfig.isMonitorPlugStatus()) {
            LOGGER.debug("Monitoring TCL devices state for all users");
            organizationRepository.findAll().stream()
                    .filter(org -> org.getDirectControlMode() != OrganizationLoadControlState.Stopped && !org.isPoolBasedControl())
                    .collect(Collectors.toList()) .forEach(this::processTCLDevicesForOrganization);
            LOGGER.debug("TCL devices monitoring completed for all users");
        }
    }

    private void processTCLDevicesForOrganization(Organization organization) {
        LOGGER.info("Monitoring TCL devices for FO generation for organization : " + organization.getOrganizationName());
        List<UserT> users = userService.getActiveUsersForOrganization(organization.getOrganizationId());
        Hibernate.initialize(users);
        users.forEach(user -> processTCLDevicesForUser(user, organization));
    }

    private void processTCLDevicesForUser(UserT user, Organization organization) {
        DeviceParameters deviceParameters = new DeviceParameters();
        deviceParameters.setAPIKey(user.getAPIKey() != null ? user.getAPIKey() : "");
        deviceParameters.setCloudUserName(user.getTpLinkUserName());
        deviceParameters.setCloudPassword(user.getTpLinkPassword());
        deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
//        deviceRepository.findTCLDevicesByUserId(user.getId()).stream()
//                .filter(deviceDetail -> deviceDetail.getDeviceType() != null && deviceDetail.isFlexible())
//                .forEach(deviceDetail -> implementationsHandler.get(deviceDetail.getPlugType())
//                        .processTCLDevice(user, organization.getOrganizationName(), deviceDetail, deviceParameters));

        for(DeviceDetail deviceDetail: deviceRepository.findTCLDevicesByUserId(user.getId())){
            if(deviceDetail.getDeviceType() != null && deviceDetail.isFlexible()){
                LOGGER.info("processing device " + deviceDetail.getDeviceId());
                implementationsHandler.get(deviceDetail.getPlugType()).
                        processTCLDevice(user, organization.getOrganizationName(), deviceDetail, deviceParameters);
            }
        }
    }

    // TODO: no need for sww, need better implementation for generalization
    //@Scheduled(cron = "0 0 4 25/5 * ?")
    public void updatePlugTimezone() {
        DeviceParameters deviceParameters = new DeviceParameters();

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date dateBeforeDST = null;
        Date dateAfterDST = null;
        try {
            dateBeforeDST = df.parse("2018-03-25 03:55");
            dateAfterDST = df.parse("2018-03-28 03:55");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cal1.setTime(dateBeforeDST);
        cal2.setTime(dateAfterDST);
        Date now = new Date();
        for (Organization organization : organizationRepository.findAll()) {
            List<UserT> users = userService.getActiveUsersForOrganization(organization.getOrganizationId());
            Hibernate.initialize(users);
            for (UserT user : users) {
                List<DeviceDetail> devices = deviceDetailService.getActiveDeviceByUser(user.getId());
                LOGGER.debug("Updating device for user: " + user.getUserName());
                //if user has devices setup API key is already available
                deviceParameters.setAPIKey(user.getAPIKey() != null ? user.getAPIKey() : "");
                deviceParameters.setCloudUserName(user.getTpLinkUserName());
                deviceParameters.setCloudPassword(user.getTpLinkPassword());
                deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
                for (DeviceDetail device : devices) {
                    if (implementationsHandler.get(device.getPlugType()) != null) {
                        String time_zone = implementationsHandler.get(device.getPlugType())
                                .getTimeZone(user.getUserName(), deviceParameters, device.getDeviceId().split("@")[1]);
                        if (time_zone != null) {
                            if (now.after(cal1.getTime()) && now.before(cal2.getTime())) {
                                int hours = Integer.parseInt(time_zone.substring(1, 3));
                                int minutes = Integer.parseInt(time_zone.substring(4, 6));
                                int offset = Integer.parseInt(time_zone.split("@")[1]);
                                int offsetHrs = Math.floorDiv(offset, 60);
                                int offsetMins = Math.floorMod(offset, 60);


                                if (time_zone.charAt(0) == '+') {
                                    if ((minutes + offsetMins) > 60) {
                                        offsetHrs = offsetHrs + 1;
                                        offsetMins = Math.floorMod((minutes + offsetMins), 60);
                                    } else {
                                        offsetMins = minutes + offsetMins;
                                    }
                                    if (offsetMins < 10) {
                                        time_zone = "+0" + (offsetHrs + hours) + ":" + offsetMins + "0";
                                    } else {
                                        time_zone = "+0" + (offsetHrs + hours) + ":" + offsetMins;
                                    }

                                } else if (time_zone.charAt(0) == '-') {
                                    if ((minutes - offsetMins) < 0) {
                                        offsetHrs = offsetHrs - 1;
                                        offsetMins = 60 + minutes - minutes;
                                    } else {
                                        offsetMins = minutes - offsetMins;
                                    }
                                    if (offsetMins < 10) {
                                        time_zone = "-0" + (hours - offsetHrs) + ":" + offsetMins + "0";
                                    } else {
                                        time_zone = "-0" + (hours - offsetHrs) + ":" + offsetMins;
                                    }
                                }


                            /*try {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(new Date());
                                LOGGER.debug(cal.get(Calendar.ZONE_OFFSET) + ", " +
                                        cal.get(Calendar.DST_OFFSET) + ", ");
                                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                Date dateAfterDST = null;
                                dateAfterDST = df.parse("2018-05-25 01:55");
                                cal.setTime(dateAfterDST);
                                LOGGER.debug(cal.get(Calendar.ZONE_OFFSET) + ", " +
                                        cal.get(Calendar.DST_OFFSET) + ", ");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }*/
                                device.setTimeZone(time_zone);
                                deviceDetailService.updateTimezone(device);
                                LOGGER.debug("ok");
                            } else {
                                device.setTimeZone(time_zone.split("@")[0]);
                                deviceDetailService.updateTimezone(device);
                                LOGGER.debug("ok");
                            }
                        } else {
                            LOGGER.warn("Error updating time zone for device: " + device.getDeviceId());
                        }
                    }
                }
            }
        }

    }

    //check if FO has to be generated for devices
    @Scheduled(cron = "0 * * * * *")
    private void checkTCLFOGeneration() {
        try {
            LOGGER.info("tclDeviceFutureFOs: " + tclDeviceFutureFOs.toString());
            Date currentTime = new Date();
            for (Map.Entry<String, Date> entry : tclDeviceFutureFOs.entrySet()) {
                if (entry.getValue().compareTo(currentTime) < 0) { // if FO generation time delay elapsed

                    String deviceId = entry.getKey();
                    tclDeviceFutureFOs.remove(deviceId); // remove entry from the map
                    DeviceDetail device = this.deviceDetailService.getDevice(deviceId);

                    // make api call to find out if FO can be generated or not
                    String url = foaProperties.getFogConnectionConfig().getShouldGenerateDeviceFOUrl() + "/" + deviceId;
                    ResponseEntity<String> response = this.implementationsHandler.get(device.getPlugType())
                            .makeHttpRequest(url, HttpMethod.POST, null, null);

                    if (response.getStatusCode().value() == 200) {
                        UserT user = this.userService.getUser(deviceId.split("@")[0]);
                        String organizationName =
                                this.organizationRepository.findByOrganizationId(user.getOrganizationId())
                                        .getOrganizationName();
                        // make api call to generate flex offer
                        url = foaProperties.getFogConnectionConfig().getGenerateDeviceFOUrl() + "/" +
                                device.getDeviceId() + "/" + organizationName;
                        this.implementationsHandler.get(device.getPlugType())
                                .makeHttpRequest(url, HttpMethod.POST, null, null);
                    } else {
                        LOGGER.debug(
                                "Not generating FO for device: {} because FO generation requirements not " +
                                        "satisfied at this time.", device.getDeviceId());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //this tigger unexecuted control signal to tp-link plug
    @Scheduled(fixedRate = 300000)
    public void sendPendingDeviceControlSignals() {
        LOGGER.info("devicePendingControlSignals:" + this.devicePendingControlSignals.toString());
        this.devicePendingControlSignals.forEach((key, val) -> {
            LOGGER.info("Retrying to {} device: {}", val==1?"start":"stop", key);
            DeviceDetail deviceDetail = this.deviceDetailService.getDevice(key);
            DeviceState deviceState = deviceDetail.getDeviceState();
            // no point in trying if device is unreachable
            if (deviceDetail.getPlugType() == PlugType.MQTT
                    || !(deviceState == DeviceState.Disconnected || deviceState == DeviceState.Unknown)) {
                DeviceStateChangeEvent stateChangeEvent = new DeviceStateChangeEvent(
                        this, "Device pending control signal event", deviceDetail, val == 1);
                this.applicationEventPublisher.publishEvent(stateChangeEvent);
            }
        });
    }

    //This schedule updates the estimated operation duration for devices
    @Scheduled(cron = "0 47 23 * * *")
    public void updateDevicesOnDuration() {
        /**
         * updates how long a device takes to completes it's operation
         * the information is used during FO generation to count the number of slices
         */
        LOGGER.info("Updating average duration of operation for all devices");
        this.deviceDetailService.getAllDevices() .forEach(this::updateOnDurationForASingleDevice);

        LOGGER.info("Updating average duration of operation for pool devices");
        organizationRepository.findAll().stream().filter(org -> org.isPoolBasedControl())
                .collect(Collectors.toList()).forEach(org -> {
            userService.getDeviceListforOrganization(org.getOrganizationId()).forEach(dd -> {
                this.updateOnDurationForPoolDevice(dd, org.getOrganizationName());
            });
        });
    }

    private void updateOnDurationForPoolDevice(DeviceDetail dd, String orgName) {
            String deviceId = dd.getDeviceId();
            poolDeviceDetail.get(orgName).get(deviceId).setAverageOperationDuration(dd.getConsumptionTs().getAverageOnDuration());
            poolDeviceDetail.get(orgName).get(deviceId)
                    .setAveragePower(this.deviceDetailService.getAvgPowerConsumptionForLastSevenDays(deviceId));
    }

    private void updateOnDurationForASingleDevice(DeviceDetail dd) {
        this.deviceDetailService.getDeviceOnDuration(dd.getConsumptionTs().getId());
    }


    @Scheduled(cron = "50 * * * * *")
    public void executePoolSchedule() {
       this.flexOfferScheduleReceivedListener.executePoolSchedule();
    }


    @Scheduled(cron = "0 */15 * * * *")
    //@Scheduled(cron = "0 0,15,30,45 * * * *")
    public void transferSchedules() {
        this.flexOfferScheduleReceivedListener.transferScheduleFromPrevToCurrSlice();
    }
}
