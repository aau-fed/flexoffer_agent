package org.goflex.wp2.foa.events;

import org.goflex.wp2.foa.wrapper.DeviceDetailDataWrapper;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * @author muhaftab
 * created: 6/17/19
 */
public class UpdateDevicesEvent extends ApplicationEvent {

    private String eventName;
    private List<DeviceDetailDataWrapper> deviceDetailDataWrappers;

    public UpdateDevicesEvent(Object source, String eventName,
                              List<DeviceDetailDataWrapper> deviceDetailDataWrappers) {
        super(source);
        this.eventName = eventName;
        this.deviceDetailDataWrappers = deviceDetailDataWrappers;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public List<DeviceDetailDataWrapper> getDeviceDetailDataWrappers() {
        return deviceDetailDataWrappers;
    }

    public void setDeviceDetailDataWrappers(List<DeviceDetailDataWrapper> deviceDetailDataWrappers) {
        this.deviceDetailDataWrappers = deviceDetailDataWrappers;
    }
}
