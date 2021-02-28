package org.goflex.wp2.foa.listeners;


import org.goflex.wp2.core.entities.DeviceDetailData;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.foa.events.UpdateDevicesEvent;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.goflex.wp2.foa.wrapper.DeviceDetailDataWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author muhaftab
 * created: 6/17/19
 */
@Component
public class UpdateDevicesEventListener implements ApplicationListener<UpdateDevicesEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDevicesEventListener.class);

    private final UserService userService;
    private final DeviceDetailService deviceDetailService;

    @Autowired
    public UpdateDevicesEventListener(UserService userService,
                                      DeviceDetailService deviceDetailService) {
        this.userService = userService;
        this.deviceDetailService = deviceDetailService;
    }

    @Override
    @Transactional
    public void onApplicationEvent(UpdateDevicesEvent updateDevicesEvent) {
        LOGGER.debug("Received event: {}", updateDevicesEvent.getEventName());
        updateDevicesEvent.getDeviceDetailDataWrappers()
                .forEach(this::saveDeviceStateAndConsumption);
    }

    private void saveDeviceStateAndConsumption(DeviceDetailDataWrapper wrapper) {
        try {
            // retrieve device from db
            DeviceDetail deviceDetail = deviceDetailService.getDeviceByDetailId(wrapper.getDeviceDetailId());

            DeviceDetailData deviceDetailData = wrapper.getDeviceDetailData();

            if (deviceDetailData != null) {
                // store consumption data
                userService.storeDeviceConsumption(deviceDetail, deviceDetailData);

                // update device state
                userService.updateDeviceState(deviceDetail, deviceDetailData);
            }
        } catch (Exception e) {
            LOGGER.error("Error in saveDeviceStateAndConsumption. " + e.getLocalizedMessage());
        }
    }
}
