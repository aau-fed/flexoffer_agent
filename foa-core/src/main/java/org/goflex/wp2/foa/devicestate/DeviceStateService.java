package org.goflex.wp2.foa.devicestate;

import org.goflex.wp2.core.entities.DeviceState;
import org.goflex.wp2.core.models.DeviceDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author muhaftab
 * created: 4/24/19
 */
@Service
public class DeviceStateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceStateService.class);
    //private static final int THRESHOLD = 120; // minutes
    private static final int THRESHOLD = 60; // minutes

    private DeviceCurrentStateRepository deviceCurrentStateRepository;
    private DeviceStateHistoryRepository deviceStateHistoryRepository;

    @Autowired
    public DeviceStateService(DeviceCurrentStateRepository deviceCurrentStateRepository,
                              DeviceStateHistoryRepository deviceStateHistoryRepository) {
        this.deviceCurrentStateRepository = deviceCurrentStateRepository;
        this.deviceStateHistoryRepository = deviceStateHistoryRepository;
    }


    public List<DeviceStateHistory> getDeviceStateHistoryByDeviceAndDate(String deviceId, Date startTime, Date endTime) {
        try {
            return this.deviceStateHistoryRepository.findDeviceHistoryByDeviceIdAndDuration(deviceId, startTime, endTime);
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
            return null;
        }
    }

    public DeviceStateHistory storeDeviceStateHistory(String deviceId, DeviceState state) {
        try {
            DeviceStateHistory deviceStateHistory = new DeviceStateHistory(deviceId, state, new Date());
            return this.deviceStateHistoryRepository.save(deviceStateHistory);
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
            return null;
        }
    }

    public DeviceCurrentState getDeviceCurrentState(String deviceId) {
        try {
            return this.deviceCurrentStateRepository.findByDeviceId(deviceId);
        } catch (Exception ex) {
            return null;
        }
    }

    public void storeDeviceCurrentState(String deviceId, boolean state) {
        try {
            DeviceCurrentState deviceCurrentState = null;
            deviceCurrentState = this.getDeviceCurrentState(deviceId);
            if (deviceCurrentState == null) {
                LOGGER.info("Last state not found for device: {}. Storing state for the first time", deviceId);
                deviceCurrentState = new DeviceCurrentState(deviceId, state, new Date());
                this.deviceCurrentStateRepository.save(deviceCurrentState);
                return;
            }

            // last state found. Only update if new state different from stored state
            if (deviceCurrentState.isState() != state) {
                LOGGER.info("Device state changed for device: {}. Old sate: {}, new state: {}",
                        deviceId, deviceCurrentState.isState(), state);
                deviceCurrentState.setState(state);
                deviceCurrentState.setTimestamp(new Date());
                this.deviceCurrentStateRepository.save(deviceCurrentState);
                LOGGER.info("Device state history updated for device: {}", deviceId);
            }

        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
        }
    }

    public boolean deviceOperatingNormally(DeviceDetail device) {
        try {
            String deviceId = device.getDeviceId();
            boolean state = device.getDeviceState() == DeviceState.Operating;
            DeviceCurrentState deviceCurrentState = this.getDeviceCurrentState(deviceId);
            boolean defaultState = device.getDefaultState() == 1;
            if (state != defaultState) {
                Date now = new Date();
                long millis = now.getTime() - deviceCurrentState.getTimestamp().getTime();
                int minutes = (int) (millis / (1000 * 60));
                if (minutes > THRESHOLD) {
                    LOGGER.warn("Device: {} not in default state for {} hours, {} minutes",
                            device.getDeviceId(), minutes / 60, minutes % 60);
                    return false;
                }
            }

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public DeviceStateHistory getLatestDeviceHistory(String deviceId) {
        try {
            return this.deviceStateHistoryRepository.getLatestDeviceHistory(deviceId);
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
            return null;
        }
    }

    public DeviceStateHistory getLatestDeviceHistoryByDate(String deviceId, Date date) {
        try {
            return this.deviceStateHistoryRepository.getLatestDeviceHistoryForDate(deviceId, date);
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
            return null;
        }
    }
}
