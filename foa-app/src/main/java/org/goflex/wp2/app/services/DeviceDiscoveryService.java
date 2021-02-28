package org.goflex.wp2.app.services;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.goflex.wp2.app.common.AppRuntimeConfig;
import org.goflex.wp2.app.mqtt.MqttSubscriber;
import org.goflex.wp2.core.entities.DeviceParameters;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.foa.implementation.TpLinkDeviceService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.goflex.wp2.foa.implementation.MqttDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceDiscoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceDiscoveryService.class);

    @Autowired(required = false)
    private MqttDeviceService mqttDeviceService;

    @Autowired(required = false)
    private MqttSubscriber mqttSubscriber;

    private final UserService userService;
    private final TpLinkDeviceService tpLinkDeviceService;
    private final AppRuntimeConfig appRuntimeConfig;

    @Autowired
    public DeviceDiscoveryService(UserService userService,
                                  TpLinkDeviceService tpLinkDeviceService,
                                  AppRuntimeConfig appRuntimeConfig) {
        this.userService = userService;
        this.tpLinkDeviceService = tpLinkDeviceService;
        this.appRuntimeConfig = appRuntimeConfig;
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


    // second, minute, hour, day of month, month, day of week.
    // this one runs every hour
    @Scheduled(cron = "0 0 * * * *")
    public void runSwissDeviceDiscoveryRoutine() {
        this.mqttDeviceService.discoverMqttDevices();
        try {
            this.mqttSubscriber.refreshTopicsAndMqttSubscriber();
        } catch (MqttException e) {
           LOGGER.error(e.getMessage());
        }
    }
}
