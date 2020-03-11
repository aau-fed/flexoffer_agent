package org.goflex.wp2.core.wrappers;

import org.goflex.wp2.core.entities.DeviceState;
import org.goflex.wp2.core.models.ConsumptionTsEntity;
import org.goflex.wp2.core.models.DeviceHierarchy;

public class LocationWrapper {
    private String deviceId;
    private double latitude;
    private double longitude;
    private DeviceState deviceState;
    private ConsumptionTsEntity consumptionTs;
    private DeviceHierarchy deviceHierarchy;

    public LocationWrapper(String deviceId, double latitude, double longitude, ConsumptionTsEntity consumptionTs, DeviceState deviceState) {
        this.deviceId = deviceId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.consumptionTs = consumptionTs;
        this.deviceState = deviceState;
        /*
        this.deviceHierarchy = deviceHierarchy;
        if (deviceHierarchy != null) {
            this.deviceHierarchy = deviceHierarchy;
        } else {
            this.deviceHierarchy = new DeviceHierarchy("Not Assigned");
        }
        */
    }

    public LocationWrapper() {

    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public ConsumptionTsEntity getConsumptionTs() {
        return consumptionTs;
    }

    public void setConsumptionTs(ConsumptionTsEntity consumptionTs) {
        this.consumptionTs = consumptionTs;
    }

    public DeviceState getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
    }

    public DeviceHierarchy getDeviceHierarchy() {
        return deviceHierarchy;
    }

    public void setDeviceHierarchy(DeviceHierarchy deviceHierarchy) {
        this.deviceHierarchy = deviceHierarchy;
    }
}
