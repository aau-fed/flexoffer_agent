package org.goflex.wp2.core.entities;

import java.io.Serializable;

public class DeviceBasicInfoDto implements Serializable {

    private static final long serialVersionUid = 324235253121431L;

    private String deviceId;
    private double voltage;
    private double power;
    private DeviceState deviceState;
    private DeviceType deviceType = DeviceType.Unknown;
    private String alias;
    private String groupName = "Not Assigned";
    private boolean isFlexible = true;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public DeviceState getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isFlexible() {
        return isFlexible;
    }

    public void setFlexible(boolean flexible) {
        isFlexible = flexible;
    }
}
