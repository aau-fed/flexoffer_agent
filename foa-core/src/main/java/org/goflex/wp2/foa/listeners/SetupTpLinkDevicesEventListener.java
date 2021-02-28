package org.goflex.wp2.foa.listeners;


import org.goflex.wp2.core.entities.DeviceParameters;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.foa.events.SetupTpLinkDevicesEvent;
import org.goflex.wp2.foa.implementation.TpLinkDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;


/**
 * @author muhaftab
 * created: 6/17/19
 */
@Component
public class SetupTpLinkDevicesEventListener implements ApplicationListener<SetupTpLinkDevicesEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetupTpLinkDevicesEventListener.class);

    private final TpLinkDeviceService tpLinkDeviceService;

    @Autowired
    public SetupTpLinkDevicesEventListener(TpLinkDeviceService tpLinkDeviceService) {
        this.tpLinkDeviceService = tpLinkDeviceService;
    }

    @Override
    public void onApplicationEvent(SetupTpLinkDevicesEvent event) {
        LOGGER.info("Received event: {}", event.getEventName());
        try {
            UserT userData = event.getUserT();
            DeviceParameters deviceParameters = event.getDeviceParameters();
            tpLinkDeviceService.setTpLinkDevices(userData, deviceParameters);
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
}
