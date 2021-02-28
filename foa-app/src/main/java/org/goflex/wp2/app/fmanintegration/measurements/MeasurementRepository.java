package org.goflex.wp2.app.fmanintegration.measurements;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface MeasurementRepository extends JpaRepository<MeasurementT, Long>, JpaSpecificationExecutor<MeasurementT> {
    @Query("SELECT t.userId FROM UserT t WHERE t.userName = :userName")
    long aggregateEnergyForUserName(@Param("userName") String userName);

    List<MeasurementT> findAllByUserNameAndTimeStampBetween(String userName, Date timeStampStart, Date timeStampEnd);

    List<MeasurementT> findAllByUserNameAndTimeIntervalBetween(String userName, long timeIntervalStart, long timeIntervalEnd);

    @Query(value = "SELECT MAX(m.cumulativeEnergy) FROM MeasurementT m " +
            "WHERE m.timeInterval <= :timeInterval")
    Double getCommulativeEnergyUntil(@Param("timeInterval") long timeInterval);

    @Query(value = "SELECT MAX(m.cumulativeEnergy) FROM MeasurementT m " +
            "WHERE m.userName = :userName AND m.timeInterval <= :timeInterval")
    Double getCommulativeEnergyUntil(@Param("userName") String userName, @Param("timeInterval") long timeInterval);

    @Query(value = "SELECT m.timeInterval AS timeInterval, MAX(m.cumulativeEnergy) AS value FROM MeasurementT m " +
            "WHERE m.userName = :userName AND m.timeInterval >= :timeIntervalStart and m.timeInterval<= :timeIntervalEnd " +
            "GROUP BY m.timeInterval " +
            "ORDER BY m.timeInterval ASC")
    List<TimeIntervalAndValue> aggregateForTimeIntervals(@Param("userName") String userName,
                                                         @Param("timeIntervalStart") long timeIntervalStart,
                                                         @Param("timeIntervalEnd") long timeIntervalEnd);

    @Query(value = "SELECT m.timeInterval AS timeInterval, MAX(m.cumulativeEnergy) AS value FROM MeasurementT m " +
            "WHERE m.timeInterval >= :timeIntervalStart and m.timeInterval<= :timeIntervalEnd " +
            "GROUP BY m.timeInterval " +
            "ORDER BY m.timeInterval ASC")
    List<TimeIntervalAndValue> aggregateForTimeIntervals(
            @Param("timeIntervalStart") long timeIntervalStart,
            @Param("timeIntervalEnd") long timeIntervalEnd);

    interface TimeIntervalAndValue {
        Long getTimeInterval();

        Double getValue();
    }
}