package org.goflex.wp2.foa.listeners;

import org.goflex.wp2.core.entities.DeviceParameters;
import org.goflex.wp2.core.entities.PlugType;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.controldetailmonitoring.ControlDetail;
import org.goflex.wp2.foa.controldetailmonitoring.ControlDetailService;
import org.goflex.wp2.foa.events.DeviceStateChangeEvent;
import org.goflex.wp2.foa.implementation.ImplementationsHandler;
import org.goflex.wp2.foa.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author muhaftab
 * created: 6/25/19
 */
@Component
public class DeviceStateChangeListener implements ApplicationListener<DeviceStateChangeEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceStateChangeListener.class);

    private ImplementationsHandler implementationsHandler;
    private UserService userService;
    private FOAProperties foaProperties;
    private ControlDetailService controlDetailService;

    @Autowired
    public DeviceStateChangeListener(ImplementationsHandler implementationsHandler,
                                     UserService userService, FOAProperties foaProperties,
                                     ControlDetailService controlDetailService) {
        this.implementationsHandler = implementationsHandler;
        this.userService = userService;
        this.foaProperties = foaProperties;
        this.controlDetailService = controlDetailService;
    }

    @Override
    public void onApplicationEvent(DeviceStateChangeEvent event) {
        try {
            LOGGER.info("Received event: {}", event.getEventName());
            DeviceDetail device = event.getDevice();
            String userName = device.getDeviceId().split("@")[0];
            UserT user = this.userService.getUser(userName);

            DeviceParameters deviceParameters = new DeviceParameters();
            //if (user.getOrganizationId() != 10004) { // 10004 is org_id for SWISS
            if (device.getPlugType() == PlugType.TPLink_HS110) {
                deviceParameters.setAPIKey(user.getAPIKey() != null ? user.getAPIKey() : "");
                deviceParameters.setCloudUserName(user.getTpLinkUserName());
                deviceParameters.setCloudPassword(user.getTpLinkPassword());
                deviceParameters.setCloudAPIUrl(foaProperties.getCloudAPIUrl());
            }

            if (event.isState()) {
                this.implementationsHandler.get(event.getDevice().getPlugType())
                        .startDevice(userName, device.getDeviceId(), deviceParameters);
            } else {
                this.implementationsHandler.get(event.getDevice().getPlugType())
                        .stopDevice(userName, device.getDeviceId(), deviceParameters);
            }

            controlDetailService.saveControlDetail(
                    new ControlDetail(user.getOrganizationId(), user.getUserName(),
                            device.getDeviceId(), new Date(), event.isState() ? 1 : 0,
                            event.getEventName(), 0,
                            true));
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
        }
    }
}
