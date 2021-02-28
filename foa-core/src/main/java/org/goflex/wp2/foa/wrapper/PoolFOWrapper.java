package org.goflex.wp2.foa.wrapper;


import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PoolFOWrapper {
    private UUID foId;
    private List<String> deviceIds;
    private boolean isCoolingPeriod;

    public PoolFOWrapper() {
    }

    public PoolFOWrapper(UUID foId, List<String> deviceIds, boolean isCoolingPeriod) {
        this.foId = foId;
        this.deviceIds = deviceIds;
        this.isCoolingPeriod = isCoolingPeriod;
    }

    public UUID getFoId() {
        return foId;
    }

    public void setFoId(UUID foId) {
        this.foId = foId;
    }

    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }

    public boolean isCoolingPeriod() {
        return isCoolingPeriod;
    }

    public void setCoolingPeriod(boolean coolingPeriod) {
        isCoolingPeriod = coolingPeriod;
    }
}
