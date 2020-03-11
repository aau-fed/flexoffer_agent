package org.goflex.wp2.core.repository;

import org.goflex.wp2.core.models.DevicePrognosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author muhaftab
 * created: 3/31/19
 */
public interface DevicePrognosisRepository extends JpaRepository<DevicePrognosis, Long> {

    // @Query("SELECT new org.goflex.wp2.core.models.DevicePrognosis(d.deviceId, d.timeSeriesId, d.power, d.timestamp)
    // FROM DevicePrognosis d WHERE d.deviceId = :deviceId AND d.timestamp = :timestamp")
    @Query("SELECT d FROM DevicePrognosis d WHERE d.deviceId = :deviceId AND d.timestamp = :timestamp")
    DevicePrognosis findByDeviceIdAndTimestamp(@Param("deviceId") String deviceId, @Param("timestamp") long timestamp);
}
