package org.goflex.wp2.app.services;

import org.goflex.wp2.app.common.AppRuntimeConfig;
import org.goflex.wp2.app.swiss.MqttSubscriber;
import org.goflex.wp2.core.entities.DeviceParameters;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.foa.implementation.TpLinkDeviceService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.goflex.wp2.foa.implementation.SwissDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceDiscoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceDiscoveryService.class);

    private UserService userService;
    private TpLinkDeviceService tpLinkDeviceService;
    private SwissDeviceService swissDeviceService;
    private AppRuntimeConfig appRuntimeConfig;
    private MqttSubscriber mqttSubscriber;

    @Autowired
    public DeviceDiscoveryService(UserService userService,
                                  TpLinkDeviceService tpLinkDeviceService,
                                  SwissDeviceService swissDeviceService,
                                  AppRuntimeConfig appRuntimeConfig,
                                  MqttSubscriber mqttSubscriber) {
        this.userService = userService;
        this.tpLinkDeviceService = tpLinkDeviceService;
        this.swissDeviceService = swissDeviceService;
        this.appRuntimeConfig = appRuntimeConfig;
        this.mqttSubscriber = mqttSubscriber;
    }

    //Run every hour
    @Scheduled(fixedRate = 3600000)
    public void loadDevices() {
        LOGGER.info("Re-Loading TP-Link Devices for all users");
        List<UserT> users = userService.getUsers();
        if (appRuntimeConfig.isRunDeviceDiscovery()) {
            for (UserT user : users) {
                DeviceParameters deviceParameters = new DeviceParameters("", "", "", "");
                this.tpLinkDeviceService.setTpLinkDevices(user, deviceParameters);
            }
        }

    }

    //Run every hour
    @Scheduled(fixedRate = 3600000)
    public void runSwissDeviceDiscoveryRoutine() {
        this.swissDeviceService.discoverSwissDevices();
        this.mqttSubscriber.refreshTopicsAndMqttSubscriber();
    }
}
