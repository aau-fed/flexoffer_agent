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
    public long aggregateEnergyForUserName(@Param("userName") String userName);

    public List<MeasurementT> findAllByUserNameAndTimeStampBetween(String userName, Date timeStampStart, Date timeStampEnd);

    public List<MeasurementT> findAllByUserNameAndTimeIntervalBetween(String userName, long timeIntervalStart, long timeIntervalEnd);

    @Query(value = "SELECT MAX(m.cumulativeEnergy) FROM MeasurementT m " +
            "WHERE m.timeInterval <= :timeInterval")
    public Double getCommulativeEnergyUntil(@Param("timeInterval") long timeInterval);

    @Query(value = "SELECT MAX(m.cumulativeEnergy) FROM MeasurementT m " +
            "WHERE m.userName = :userName AND m.timeInterval <= :timeInterval")
    public Double getCommulativeEnergyUntil(@Param("userName") String userName, @Param("timeInterval") long timeInterval);

    @Query(value = "SELECT m.timeInterval AS timeInterval, MAX(m.cumulativeEnergy) AS value FROM MeasurementT m " +
            "WHERE m.userName = :userName AND m.timeInterval >= :timeIntervalStart and m.timeInterval<= :timeIntervalEnd " +
            "GROUP BY m.timeInterval " +
            "ORDER BY m.timeInterval ASC")
    public List<TimeIntervalAndValue> aggregateForTimeIntervals(@Param("userName") String userName,
                                                                @Param("timeIntervalStart") long timeIntervalStart,
                                                                @Param("timeIntervalEnd") long timeIntervalEnd);

    @Query(value = "SELECT m.timeInterval AS timeInterval, MAX(m.cumulativeEnergy) AS value FROM MeasurementT m " +
            "WHERE m.timeInterval >= :timeIntervalStart and m.timeInterval<= :timeIntervalEnd " +
            "GROUP BY m.timeInterval " +
            "ORDER BY m.timeInterval ASC")
    public List<TimeIntervalAndValue> aggregateForTimeIntervals(
            @Param("timeIntervalStart") long timeIntervalStart,
            @Param("timeIntervalEnd") long timeIntervalEnd);

    interface TimeIntervalAndValue {
        Long getTimeInterval();

        Double getValue();
    }
}