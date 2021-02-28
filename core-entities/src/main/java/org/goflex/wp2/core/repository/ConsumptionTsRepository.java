
/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
 * Copyright (c) 2018.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining  a copy of this software and associated documentation
 *  files (the "Software") to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify, merge,
 *  publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions: The above copyright notice and
 *  this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON
 *  INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Last Modified 2/22/18 12:41 AM
 */

package org.goflex.wp2.core.repository;


import org.goflex.wp2.core.models.ConsumptionTsEntity;
import org.goflex.wp2.core.models.DeviceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

/**
 * Created by bijay on 12/2/17.
 */
@Repository
public interface ConsumptionTsRepository extends JpaRepository<ConsumptionTsEntity, Long> {

    ConsumptionTsEntity findByTimeseriesId(long timeseriesId);


    @Query("SELECT m FROM ConsumptionTsEntity t join t.deviceData m where " +
            "t.timeseriesId = :timeseriesId and (m.date >= :StartTime and m.date < :endTime)" +
            "order by m.date ASC")
    List<DeviceData> findEnergyForDate(@Param("timeseriesId") long timeseriesId,
                                       @Param("StartTime") Date StartTime,
                                       @Param("endTime") Date endTime);


    @Query("SELECT m FROM ConsumptionTsEntity t join t.deviceData m where " +
            "t.timeseriesId = :timeseriesId and m.date >= :StartTime " +
            "order by m.date ASC")
    List<DeviceData> findConsumptionFrom(@Param("timeseriesId") long timeseriesId,
                                    @Param("StartTime") Date StartTime);


    @Query("SELECT sum(m.power) FROM ConsumptionTsEntity t join t.deviceData m where " +
            "t.timeseriesId = :timeseriesId and m.date >= :StartTime " +
            "group by t.timeseriesId")
    Double findAvgConsumptionFrom(@Param("timeseriesId") long timeseriesId,
                                         @Param("StartTime") Date StartTime);

    @Query("SELECT avg(m.power) FROM ConsumptionTsEntity t join t.deviceData m where " +
            "m.power > 10 and t.timeseriesId = :timeseriesId and m.date >= :StartTime " +
            "group by t.timeseriesId")
    Double findAvgAboveThresholdConsumptionFrom(@Param("timeseriesId") long timeseriesId,
                                                @Param("StartTime") Date StartTime);

    @Query("SELECT m FROM ConsumptionTsEntity t join t.deviceData m where " +
            "t.timeseriesId = :timeseriesId " +
            "order by m.date ASC")
    List<DeviceData> findAllConsumptionForDevice(@Param("timeseriesId") long timeseriesId);


    @Query("SELECT sum(m.energy) FROM ConsumptionTsEntity t join t.deviceData m where " +
            "t.timeseriesId = :timeseriesId and (m.date >= :StartTime and m.date < :endTime)")
    Double findConsumptionForDate(@Param("timeseriesId") long timeseriesId,
                                  @Param("StartTime") Date StartTime,
                                  @Param("endTime") Date endTime);

    @Query("SELECT sum(m.energy) FROM ConsumptionTsEntity t join t.deviceData m where " +
            "t.timeseriesId in :timeseriesIds and (m.date >= :StartTime and m.date < :endTime)")
    Double findConsumptionForDate(@Param("timeseriesIds") List<Long> timeseriesIds,
                                  @Param("StartTime") Date StartTime,
                                  @Param("endTime") Date endTime);


    /*@Query("SELECT HOUR(m.date) as hourly, avg(m.energy) as energy FROM ConsumptionTsEntity t join t.deviceData m where " +
            "m.date >= :StartTime and m.date < :endTime " +
            "group by HOUR(m.date) order by hourly")
    List<Map<Integer, Double>> findEnergyConsumption(@Param("StartTime") Date StartTime,
                                                     @Param("endTime") Date endTime);*/

    /*@Query("SELECT t FROM ConsumptionTsEntity t join t.deviceData m WHERE t.timeseriesId = :timeseriesId")
    ConsumptionTsEntity findAllData(@Param("timeseriesId") long timeseriesId);

    @Query("SELECT m FROM ConsumptionTsEntity t join t.deviceData m where " +
            "t.timeseriesId = :timeseriesId and (m.date >= :StartTime and m.date < :endTime)" +
            "order by m.date ASC")
    List<ConsumptionTsEntity> findDataForDate(@Param("timeseriesId") long timeseriesId,
                                              @Param("StartTime") Date StartTime,
                                              @Param("endTime") Date endTime);*/

    @Query("SELECT min(m.power) FROM ConsumptionTsEntity t join t.deviceData m where " +
            "t.timeseriesId = :timeseriesId and m.date >= :StartTime " +
            "group by t.timeseriesId")
    Double findMinConsumptionFrom(@Param("timeseriesId") long timeseriesId,
                                         @Param("StartTime") Date StartTime);

    @Query(value = "select truncate(avg(timestampdiff(MINUTE, start,end)), 0) as duration from (" +
            "select min(start) as start, end from " +
            "(select l.date as start, min(r.date) as end from device_data l " +
            "left join device_data r on l.timeseries_id = r.timeseries_id and r.date > l.date " +
            "where l.power > :threshold and r.power < :threshold and l.timeseries_id = :timeSeriesId and l.date > :startTime " +
            "group by l.date) as sub " +
            "group by end) as sub2 where timestampdiff(MINUTE, start,end) > 30", nativeQuery = true)
    Integer findDeviceOnDuration(@Param("timeSeriesId") long timeSeriesId,
                                 @Param("threshold") int threshold,
                                 @Param("startTime") Date startTime);
}
