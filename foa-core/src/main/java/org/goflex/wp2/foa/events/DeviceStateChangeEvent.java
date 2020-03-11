package org.goflex.wp2.foa.events;

import org.goflex.wp2.core.models.DeviceDetail;
import org.springframework.context.ApplicationEvent;

/**
 * @author muhaftab
 * created: 6/25/19
 */
public class DeviceStateChangeEvent extends ApplicationEvent {
    private String eventName;
    private DeviceDetail device;
    private boolean state;

    public DeviceStateChangeEvent(Object source, String eventName, DeviceDetail device, boolean state) {
        super(source);
        this.eventName = eventName;
        this.device = device;
        this.state = state;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public DeviceDetail getDevice() {
        return device;
    }

    public void setDevice(DeviceDetail device) {
        this.device = device;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
