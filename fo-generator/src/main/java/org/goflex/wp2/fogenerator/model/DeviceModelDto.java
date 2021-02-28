package org.goflex.wp2.fogenerator.model;

import org.goflex.wp2.core.models.PoolDeviceModel;

import java.util.ArrayList;
import java.util.List;

public class DeviceModelDto{

    private List<PoolDeviceModel> deviceModels;

    public DeviceModelDto(){
        deviceModels = new ArrayList<>();
    }

    public DeviceModelDto(List<PoolDeviceModel> deviceModels) {
        this.deviceModels = deviceModels;
    }

    public List<PoolDeviceModel> getDeviceModels() {
        return deviceModels;
    }

    public void setDeviceModels(List<PoolDeviceModel> deviceModels) {
        this.deviceModels = deviceModels;
    }
}
