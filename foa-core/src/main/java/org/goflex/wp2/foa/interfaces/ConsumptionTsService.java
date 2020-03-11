package org.goflex.wp2.foa.interfaces;

import org.goflex.wp2.core.models.ConsumptionTsEntity;
import org.goflex.wp2.core.models.DeviceData;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ConsumptionTsService {

    List<DeviceData> getCtsForDate(long id, Date startDate, Date endTime);

    List<DeviceData> getCtsFromDate(long id, Date startDate);

    Double getAvgCtsFromDate(long id, Date startDate);

    List<DeviceData> getCtsForDevice(long consumptionId);

    Double getConsumptionForDate(long id, Date startDate, Date endTime);

    Double getConsumptionForDate(List<Long> ids, Date startDate, Date endTime);

    ConsumptionTsEntity getCtsById(long id);

    Map<Date, Double> getTs(List<DeviceData> dat);

    Map<Date, Double> createEnergyTs(List<DeviceData> dat);

    Map<Date, Double> fillMissingValues(List<DeviceData> dat);

    Map<Date, Double> extrapolate(List<DeviceData> dat);

    Double getMinCtsFromDate(Long id, Date sinceTime);

    Integer getDeviceOnDuration(Long id, Date sinceTime);
}
