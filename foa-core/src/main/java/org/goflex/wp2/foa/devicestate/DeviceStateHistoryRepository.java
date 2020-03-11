package org.goflex.wp2.foa.devicestate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author muhaftab
 * created: 4/24/19
 */
@Repository
public interface DeviceStateHistoryRepository extends JpaRepository<DeviceStateHistory, Long> {

    @Query("select dsh from DeviceStateHistory dsh where dsh.deviceId = :deviceId and " +
            "dsh.timestamp = (select max(dsh2.timestamp) from DeviceStateHistory dsh2 where " +
            "dsh2.deviceId = :deviceId)")
    DeviceStateHistory getLatestDeviceHistory(@Param("deviceId") String deviceId);

    @Query("select dsh from DeviceStateHistory dsh where dsh.deviceId = :deviceId and " +
            "dsh.timestamp = (select max(dsh2.timestamp) from DeviceStateHistory dsh2 where " +
            "dsh2.deviceId = :deviceId and dsh2.timestamp >= :historyDate)")
    DeviceStateHistory getLatestDeviceHistoryForDate(@Param("deviceId") String deviceId,
                                                      @Param("historyDate") Date historyDate);

    DeviceStateHistory findTopByDeviceIdAndTimestamp(String deviceId, Date timestamp);

    @Query("select dsh from DeviceStateHistory dsh where dsh.deviceId = :deviceId and dsh.timestamp >= :startTime " +
            "and dsh.timestamp <= :endTime")
    List<DeviceStateHistory> findDeviceHistoryByDeviceIdAndDuration(@Param("deviceId") String deviceId,
                                                                    @Param("startTime") Date startTime,
                                                                    @Param("endTime") Date endTime);
}
