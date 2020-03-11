package org.goflex.wp2.fogenerator.interfaces;

import org.goflex.wp2.core.models.DeviceFlexibilityDetail;

public interface FlexibilityDetails {
    int getNoOfInterruptionInADay(String deviceID);

    int getMaxInterruptionLength(String deviceID);

    int getMinInterruptionInterval(String deviceID);

    int getMaxInterruptionDelay(String deviceID);

    DeviceFlexibilityDetail getDeviceFlexibilityDetail(String deviceID);
}
