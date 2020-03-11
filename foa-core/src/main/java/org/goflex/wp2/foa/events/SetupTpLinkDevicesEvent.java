package org.goflex.wp2.foa.events;

import org.goflex.wp2.core.entities.DeviceParameters;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.foa.wrapper.DeviceDetailDataWrapper;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * @author muhaftab
 * created: 6/17/19
 */
public class SetupTpLinkDevicesEvent extends ApplicationEvent {

    private String eventName;
    private UserT userT;
    private DeviceParameters deviceParameters;

    public SetupTpLinkDevicesEvent(Object source, String eventName, UserT userT, DeviceParameters deviceParameters) {
        super(source);
        this.eventName = eventName;
        this.userT = userT;
        this.deviceParameters = deviceParameters;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public UserT getUserT() {
        return userT;
    }

    public void setUserT(UserT userT) {
        this.userT = userT;
    }

    public DeviceParameters getDeviceParameters() {
        return deviceParameters;
    }

    public void setDeviceParameters(DeviceParameters deviceParameters) {
        this.deviceParameters = deviceParameters;
    }
}
