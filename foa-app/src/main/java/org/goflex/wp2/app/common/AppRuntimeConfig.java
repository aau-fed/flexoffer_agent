package org.goflex.wp2.app.common;

import org.springframework.stereotype.Component;

@Component
public class AppRuntimeConfig {

    private boolean runScheduler = true;
    private boolean sendFOToFMAN = true;
    private boolean sendHeartBeatToFMAN = true;
    private boolean monitorPlugStatus = true;
    private boolean runDeviceDiscovery = true;

    public boolean isRunScheduler() {
        return runScheduler;
    }

    public void setRunScheduler(boolean runScheduler) {
        this.runScheduler = runScheduler;
    }

    public boolean isSendFOToFMAN() {
        return sendFOToFMAN;
    }

    public void setSendFOToFMAN(boolean sendFOToFMAN) {
        this.sendFOToFMAN = sendFOToFMAN;
    }

    public boolean isSendHeartBeatToFMAN() {
        return sendHeartBeatToFMAN;
    }

    public void setSendHeartBeatToFMAN(boolean sendHeartBeatToFMAN) {
        this.sendHeartBeatToFMAN = sendHeartBeatToFMAN;
    }

    public boolean isMonitorPlugStatus() {
        return monitorPlugStatus;
    }

    public void setMonitorPlugStatus(boolean monitorPlugStatus) {
        this.monitorPlugStatus = monitorPlugStatus;
    }

    public boolean isRunDeviceDiscovery() {
        return runDeviceDiscovery;
    }

    public void setRunDeviceDiscovery(boolean runDeviceDiscovery) {
        this.runDeviceDiscovery = runDeviceDiscovery;
    }
}
