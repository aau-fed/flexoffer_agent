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
 *  Last Modified 2/6/18 9:56 PM
 */

package org.goflex.wp2.core.repository;


import org.goflex.wp2.core.models.OnOffSchedule;
import org.goflex.wp2.core.models.ScheduleT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

/**
 * Created by bijay on 12/2/17.
 */
@Repository
@Transactional
public interface ScheduleRepository extends JpaRepository<ScheduleT, Long> {

    /**
     * Find  schedule by Date
     */
    ScheduleT findByScheduleDate(Date scheduleDate);

    @Query("SELECT d FROM ScheduleT s " +
            "inner join s.onOffSchedules d " +
            "WHERE d.flexOfferId = :flexOfferId and d.isValid = 1")
    List<OnOffSchedule> findByFlexOfferId(@Param("flexOfferId") String flexOfferId);

    /**
     * Find  schedule by FODate
     */
    @Query("SELECT s FROM ScheduleT s " +
            "inner join s.onOffSchedules d " +
            "WHERE s.scheduleDate >= :startDate and s.scheduleDate < :endDate and d.scheduleToState = 1 and d.isValid = 1")
    List<ScheduleT> findByScheduleFODate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);


    /**
     * Find  schedule by FODate
     */
    @Query("SELECT s FROM ScheduleT s " +
            "WHERE s.scheduleDate >= :currentFoDate")
    List<ScheduleT> findByFutureFODate(@Param("currentFoDate") Date currentFoDate);

    @Query("SELECT d FROM ScheduleT s " +
            "inner join s.onOffSchedules d " +
            "WHERE s.scheduleDate = :scheduleDate and d.deviceID = :deviceID and d.isValid = 1")
    OnOffSchedule findByScheduleDateAndDeviceId(@Param("scheduleDate") Date scheduleDate, @Param("deviceID") String deviceId);

    /**
     * Find  all schedule by schedule date
     */
    @Query("SELECT d FROM ScheduleT s " +
            "inner join s.onOffSchedules d " +
            "WHERE s.scheduleDate = :scheduleDate and d.isValid = 1 and d.pushedToDevice = 0")
    List<OnOffSchedule> findAllByScheduleDate(@Param("scheduleDate") Date scheduleDate);

    /**
     * findschedule by onoff schedule id
     */
    @Query("SELECT d FROM ScheduleT s " +
            "inner join s.onOffSchedules d " +
            "WHERE d.onOffScheduleId = :onOffScheduleId and d.isValid = 1")
    OnOffSchedule findByOnOffScheduleId(@Param("onOffScheduleId") long onOffScheduleId);

}