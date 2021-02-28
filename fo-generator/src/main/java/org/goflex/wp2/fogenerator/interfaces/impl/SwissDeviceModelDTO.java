package org.goflex.wp2.fogenerator.interfaces.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.goflex.wp2.core.models.PoolDeviceModel;

import java.util.List;

/**
 * @author muhaftab
 * created: 10/31/19
 */
public class SwissDeviceModelDTO {
    @JsonProperty("data")
    private List<PoolDeviceModel> deviceModels;

    public List<PoolDeviceModel> getDeviceModels() {
        return deviceModels;
    }

    public void setDeviceModels(List<PoolDeviceModel> deviceModels) {
        this.deviceModels = deviceModels;
    }
}
