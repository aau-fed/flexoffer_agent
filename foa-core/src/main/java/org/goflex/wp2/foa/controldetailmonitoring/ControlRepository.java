package org.goflex.wp2.foa.controldetailmonitoring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ControlRepository extends JpaRepository<ControlDetail, Long> {
    @Query("SELECT cd from ControlDetail cd WHERE cd.deviceId = :deviceId AND  cd.controlDatetime >= :startTime AND " +
            "cd.controlDatetime <= :endTime")
    List<ControlDetail> findByDeviceIdAndControlDatetime(@Param("deviceId") String deviceId,
                                                         @Param("startTime") Date startTime,
                                                         @Param ("endTime") Date endTime);

    @Query("select cd1 from ControlDetail cd1 WHERE cd1.deviceId = :deviceId " +
            "AND cd1.triggeredToDevice = true " +
            "AND cd1.controlDatetime = (select max(cd2.controlDatetime) " +
            "from ControlDetail cd2 WHERE cd2.deviceId = :deviceId AND cd2.triggeredToDevice = true)")
    ControlDetail findLatestByDeviceId(@Param("deviceId") String deviceId);

    @Query("select cd1 from ControlDetail cd1 WHERE cd1.deviceId = :deviceId " +
            "AND cd1.controlType = 0 AND cd1.triggeredToDevice = true " +
            "AND cd1.controlDatetime = (select max(cd2.controlDatetime) from ControlDetail cd2 " +
            "WHERE cd2.deviceId = :deviceId AND cd2.controlType = 0 AND cd2.triggeredToDevice = true)")
    ControlDetail findLastOffControlTypeByDeviceId(@Param("deviceId") String deviceId);

    @Query("select cd1 from ControlDetail cd1 WHERE cd1.deviceId = :deviceId " +
            "AND cd1.controlType = 1 AND cd1.triggeredToDevice = true " +
            "AND cd1.controlDatetime = (select max(cd2.controlDatetime) from ControlDetail cd2 " +
            "WHERE cd2.deviceId = :deviceId AND cd2.controlType = 1 AND cd2.triggeredToDevice = true)")
    ControlDetail findLastOnControlTypeByDeviceId(@Param("deviceId") String deviceId);
}
