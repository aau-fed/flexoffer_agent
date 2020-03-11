package org.goflex.wp2.foa.implementation;

import org.goflex.wp2.core.models.ConsumptionTsEntity;
import org.goflex.wp2.core.models.DeviceData;
import org.goflex.wp2.core.models.DeviceDataSuppl;
import org.goflex.wp2.core.repository.ConsumptionTsRepository;
import org.goflex.wp2.core.repository.DeviceDataSupplRepository;
import org.goflex.wp2.foa.interfaces.ConsumptionTsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConsumptionTsServiceImpl implements ConsumptionTsService {

    private ConsumptionTsRepository consumptionTsRepository;
    private DeviceDataSupplRepository deviceDataSupplRepository;

    @Autowired
    public ConsumptionTsServiceImpl(
            ConsumptionTsRepository consumptionTsRepository, DeviceDataSupplRepository deviceDataSupplRepository) {
        this.consumptionTsRepository = consumptionTsRepository;
        this.deviceDataSupplRepository = deviceDataSupplRepository;
    }

    @Override
    public ConsumptionTsEntity getCtsById(long id) {
        return this.consumptionTsRepository.findByTimeseriesId(id);
    }

    /**
     * Get Consumption data for date
     */
    @Override
    public List<DeviceData> getCtsForDate(long id, Date startDate, Date endDate) {

        List<DeviceData> ctsList;
        ctsList = consumptionTsRepository.findEnergyForDate(id, startDate, endDate);

        return ctsList;
    }

    /**
     * Get consumption data after a given datetime
     */
    @Override
    public List<DeviceData> getCtsFromDate(long id, Date startDate) {

        List<DeviceData> ctsList;

        ctsList = consumptionTsRepository.findConsumptionFrom(id, startDate);

        return ctsList;
    }

    /**
     * Get consumption data after a given datetime
     */
    @Override
    public Double getAvgCtsFromDate(long id, Date startDate) {
        //return consumptionTsRepository.findAvgConsumptionFrom(id, startDate);
        return consumptionTsRepository.findAvgAboveThresholdConsumptionFrom(id, startDate);
    }


    /**
     * Get consumption data for a particular device given by deviceId
     */
    @Override
    public List<DeviceData> getCtsForDevice(long consumptionId) {

        List<DeviceData> ctsList;

        ctsList = consumptionTsRepository.findAllConsumptionForDevice(consumptionId);

        return ctsList;
    }


    /**
     * Used for KPIs
     */
    @Override
    public Double getConsumptionForDate(long id, Date startDate, Date endTime) {
        Double consumption = consumptionTsRepository.findConsumptionForDate(id, startDate, endTime);
        if (consumption == null) {
            return 0.0;
        }
        return consumption;
    }

    @Override
    public Double getConsumptionForDate(List<Long> ids, Date startDate, Date endTime) {
        Double consumption = consumptionTsRepository.findConsumptionForDate(ids, startDate, endTime);
        if (consumption == null) {
            return 0.0;
        }
        return consumption;
    }


    public DeviceDataSuppl getLatestSupplData(long id) {
        try {
            DeviceDataSuppl deviceDataSuppl = deviceDataSupplRepository.findLatestSupplData(id);
            //DeviceDataSuppl deviceDataSuppl = deviceDataSupplRepository
            // .findTopByConsumptionTsEntity_TimeseriesIdOrderByDateDesc(id);
            return deviceDataSuppl;
        } catch (Exception ex) {
            return null;
        }
    }


    @Override
    public Map<Date, Double> getTs(List<DeviceData> dat) {
        return dat.stream().collect(
                Collectors.toMap(DeviceData::getDate, DeviceData::getPower, (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new)
        );

    }

    @Override
    public Map<Date, Double> createEnergyTs(List<DeviceData> dat) {
        //TODO: Need to implement
        return null;
    }

    @Override
    public Map<Date, Double> fillMissingValues(List<DeviceData> dat) {
        //TODO: Need to implement
        return null;
    }

    @Override
    public Map<Date, Double> extrapolate(List<DeviceData> dat) {
        //TODO: Need to implement
        return null;
    }

    @Override
    public Double getMinCtsFromDate(Long id, Date sinceTime) {
        return consumptionTsRepository.findMinConsumptionFrom(id, sinceTime);
    }

    @Override
    public Integer getDeviceOnDuration(Long id, Date sinceTime) {
        /** commented this in favor of below code because SQL based calculation is very slow */
        //Integer avgDuration = consumptionTsRepository.findDeviceOnDuration(id, 5, sinceTime);

        List<DeviceData> deviceData = consumptionTsRepository.findConsumptionFrom(id, sinceTime);
        List<Integer> durations = new ArrayList<>();
        int threshold = 5;
        double prevPower = 0.0;
        Date startDate = null;
        for (DeviceData dd : deviceData) {
            if (dd.getPower() > threshold && prevPower < threshold) {
                startDate = dd.getDate();
            }
            if (dd.getPower() < threshold && prevPower > threshold && startDate != null) {
                long diff = dd.getDate().getTime() - startDate.getTime();
                int duration = (int) diff/(1000*60);
                if (duration >= 30) {
                    durations.add(duration);
                }
                startDate = null;
            }
            prevPower = dd.getPower();
        }

        Integer avgDuration = 0;
        for (Integer duration : durations) {
                avgDuration += duration;
        }
        avgDuration = durations.size() > 0 ? avgDuration / durations.size() : avgDuration;

        // smaller multiple of 15
        int a = (avgDuration/15)*15;
        // larger multiple of 15
        int b = a + 15;

        // get the closest multiple of 15
        avgDuration = avgDuration - a > b - avgDuration ? b : a;

        // finally make sure that duration is never less than 30 or greater than 120 (i.e. min slices = 2, max slices = 8)
        return Math.max(30,Math.min(avgDuration, 120));
    }
}
