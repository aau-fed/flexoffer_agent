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
 *  Last Modified 2/8/18 12:07 PM
 */

package org.goflex.wp2.foa.interfaces;

import org.goflex.wp2.core.entities.FlexOfferSchedule;
import org.goflex.wp2.core.models.OnOffSchedule;
import org.goflex.wp2.core.models.ScheduleT;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * All Flex-Offer generators and external parties will talk to this service to publish Flex-Offers
 */
public interface ScheduleService {

    void handleScheduleFromFMAN(ConcurrentHashMap<UUID, FlexOfferSchedule> schedules);

    ScheduleT save(ScheduleT scheduleT);

    ScheduleT getByDate(Date date);

    List<ScheduleT> getByFODate(Date startDate, Date endDate);

    List<ScheduleT> getFutureSchedule(Date currentFoDate);

    OnOffSchedule getByDateAndDeviceId(Date date, String deviceId);

    List<OnOffSchedule> getByFlexOfferId(String flexOfferId);

    List<OnOffSchedule> getAllUnPushedSchedules(Date schDate);

    ScheduleT addSchedulesForKey(Date schDate, OnOffSchedule onOffSchedule);

    Boolean removeSchedule(Date schDate, String deviceId);

    Boolean inValidateSchedule(Date schDate, String deviceId);

    Boolean inValidateSchedule(String flexOfferId);

    Boolean inValidateSchedule(Long onOffScheduleId);

    List<OnOffSchedule> getOnOffSchedules(String flexOfferId);

    void updateScheduleStatus(OnOffSchedule onOffSchedule);

    void handleScheduleReceivedFromFMAN();
}
