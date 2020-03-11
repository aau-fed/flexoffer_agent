package org.goflex.wp2.fogenerator.interfaces.impl;

import org.goflex.wp2.core.models.DeviceFlexibilityDetail;
import org.goflex.wp2.foa.implementation.DeviceDetailServiceImpl;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.fogenerator.interfaces.FlexibilityDetails;
import org.springframework.beans.factory.annotation.Autowired;

public class FlexibilityDetailsService implements FlexibilityDetails {


    @Autowired
    private DeviceDetailService deviceDetailService;

    public int getMaxInterruptionDelay(String deviceID) {
        return this.getDeviceFlexibilityDetail(deviceID).getMaxInterruptionDelay();
    }

    @Override
    public int getMinInterruptionInterval(String deviceID) {
        return this.getDeviceFlexibilityDetail(deviceID).getMinInterruptionInterval();
    }

    @Override
    public int getNoOfInterruptionInADay(String deviceID) {
        return this.getDeviceFlexibilityDetail(deviceID).getNoOfInterruptionInADay();
    }

    @Override
    public int getMaxInterruptionLength(String deviceID) {
        return this.getDeviceFlexibilityDetail(deviceID).getMaxInterruptionLength();
    }

    @Override
    public DeviceFlexibilityDetail getDeviceFlexibilityDetail(String deviceID) {
        return deviceDetailService.getDevice(deviceID).getDeviceFlexibilityDetail();
    }
}
