package org.goflex.wp2.foa.wrapper;

import org.goflex.wp2.core.entities.DeviceDetailData;

/**
 * @author muhaftab
 * created: 6/17/19
 */
public class DeviceDetailDataWrapper {

    private String userName;
    private long deviceDetailId;
    private DeviceDetailData deviceDetailData;

    public DeviceDetailDataWrapper() {}

    public DeviceDetailDataWrapper(String userName, long deviceDetailId, DeviceDetailData deviceDetailData) {
        this.userName = userName;
        this.deviceDetailId = deviceDetailId;
        this.deviceDetailData = deviceDetailData;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getDeviceDetailId() {
        return deviceDetailId;
    }

    public void setDeviceDetailId(long deviceDetailId) {
        this.deviceDetailId = deviceDetailId;
    }

    public DeviceDetailData getDeviceDetailData() {
        return deviceDetailData;
    }

    public void setDeviceDetailData(DeviceDetailData deviceDetailData) {
        this.deviceDetailData = deviceDetailData;
    }
}
