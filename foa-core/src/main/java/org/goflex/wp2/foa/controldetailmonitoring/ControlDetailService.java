package org.goflex.wp2.foa.controldetailmonitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ControlDetailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControlDetailService.class);

    @Autowired
    private ControlRepository controlRepository;

    public ControlDetail saveControlDetail(ControlDetail controlDetail) {
        return controlRepository.save(controlDetail);
    }

    public List<ControlDetail> getControlHistoryByDeviceAndDate(String deviceId, Date startTime, Date endTime) {
        try {
            return this.controlRepository.findByDeviceIdAndControlDatetime(deviceId, startTime, endTime);
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
            return null;
        }
    }

    public ControlDetail getLatestControlEventByDevice(String deviceId) {
        try {
            return this.controlRepository.findLatestByDeviceId(deviceId);
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
            return null;
        }
    }

    public ControlDetail getLastOffControlEventByDevice(String deviceId) {
        try {
            return this.controlRepository.findLastOffControlTypeByDeviceId(deviceId);
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
            return null;
        }
    }

    public ControlDetail getLastOnControlEventByDevice(String deviceId) {
        try {
            return this.controlRepository.findLastOnControlTypeByDeviceId(deviceId);
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
            return null;
        }
    }
}
