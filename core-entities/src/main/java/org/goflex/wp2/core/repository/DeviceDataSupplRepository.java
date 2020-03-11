package org.goflex.wp2.core.repository;

import org.goflex.wp2.core.models.DeviceDataSuppl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author muhaftab
 * created: 2/8/19
 */
public interface DeviceDataSupplRepository extends JpaRepository<DeviceDataSuppl, Integer> {

    @Query(value = "SELECT * from device_data_suppl WHERE timeseries_id = :timeseriesId order by date desc limit 1", nativeQuery = true)
    DeviceDataSuppl findLatestSupplData(@Param("timeseriesId") long timeseriesId);

    // returns same result as 'findLatestSupplData'
    DeviceDataSuppl findTopByConsumptionTsEntity_TimeseriesIdOrderByDateDesc(@Param("timeseriesId") long timeseriesId);

}
