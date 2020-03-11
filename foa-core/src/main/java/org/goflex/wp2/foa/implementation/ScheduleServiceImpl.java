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
 *  Last Modified 2/8/18 4:17 PM
 */

package org.goflex.wp2.foa.implementation;


import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexOfferSchedule;
import org.goflex.wp2.core.entities.FlexOfferState;
import org.goflex.wp2.core.entities.ScheduleValidationStatus;
import org.goflex.wp2.core.models.OnOffSchedule;
import org.goflex.wp2.core.models.ScheduleEventLog;
import org.goflex.wp2.core.models.ScheduleT;
import org.goflex.wp2.core.repository.OnOffScheduleRepository;
import org.goflex.wp2.core.repository.ScheduleEventLogRepository;
import org.goflex.wp2.core.repository.ScheduleRepository;
import org.goflex.wp2.foa.events.FlexOfferScheduleReceivedEvent;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.FOAService;
import org.goflex.wp2.foa.interfaces.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bijay on 12/2/17.
 * this class implements the userservice methods
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleServiceImpl.class);

    private ScheduleRepository scheduleRepository;

    private OnOffScheduleRepository onOffScheduleRepository;

    private DeviceDetailService deviceDetailService;

    private FOAService foaService;

    private ApplicationEventPublisher applicationEventPublisher;

    private ScheduleEventLogRepository scheduleEventLogRepository;

    public ScheduleServiceImpl(ScheduleRepository scheduleRepository,
                               OnOffScheduleRepository onOffScheduleRepository,
                               DeviceDetailService deviceDetailService,
                               FOAService foaService,
                               ApplicationEventPublisher applicationEventPublisher,
                               ScheduleEventLogRepository scheduleEventLogRepository) {
        this.scheduleRepository = scheduleRepository;
        this.onOffScheduleRepository = onOffScheduleRepository;
        this.deviceDetailService = deviceDetailService;
        this.foaService = foaService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.scheduleEventLogRepository = scheduleEventLogRepository;
    }

    /**
     * Handle schedule date in UTC, schedule time  is in UTC need to convert to local time
     */
    private Date getLocalTime(Date utcDate, String deviceID) {
        String time_zone = deviceDetailService.getDevice(deviceID).getTimeZone();
        int addMin = Integer.parseInt(time_zone.substring(1, 3)) * 60 + Integer.parseInt(time_zone.substring(4, 6));
        if (time_zone.substring(0, 1).equals("-")) {
            addMin = -addMin;
        }
        return new Date(utcDate.getTime() + (addMin * 60000));//milliseconds in a minute

    }

    /**
     * Check if there is flex offer with same ID as in schedule ID
     */
    private FlexOffer _flexOfferExists(FlexOfferSchedule flexOfferSchedule, UUID foaKey) {

        return foaService.getFlexOfferByFoID(foaKey);
    }

    private void processSchedule(FlexOfferSchedule schedule, UUID foaKey) {

        // get the flexoffer in the temp value
        FlexOffer tempFO = this._flexOfferExists(schedule, foaKey);
        ScheduleValidationStatus scheduleValidationStatus;

        if (tempFO != null) {
            if (tempFO.getId().equals(foaKey)) {
                // If schedule time is in UTC format, and get local time
                Calendar cal = Calendar.getInstance();
                cal.setTime(schedule.getStartTime());
                if (cal.getTimeZone() == TimeZone.getTimeZone("UTC")) {
                    schedule.setStartTime(getLocalTime(schedule.getStartTime(), tempFO.getOfferedById()));
                }
                // check if new schedule complies with flex-offer constraints
                if (schedule.isCorrect(tempFO)) {

                    //TODO: check if flexoffer is already in execution mode,
                    // this can be done by checking FO schedule data,
                    // which has to be updated during schedule execution,, for e.g, tempFO.getState != execution
                    if (tempFO.getState() == FlexOfferState.InAdaptation) {
                        logger.warn("Schedule is rejected as FO: {} is in adaptation mode.", tempFO.getId());
                        scheduleValidationStatus = ScheduleValidationStatus.RejectedDueToAdaptation;
                    } else if (tempFO.getState() == FlexOfferState.Executed) {
                        logger.warn("Schedule is rejected as FO: {} is already executed.", tempFO.getId());
                        scheduleValidationStatus = ScheduleValidationStatus.RejectedDueToExecution;
                    } else {
                        // if valid flex offer schedule receive event
                        FlexOfferScheduleReceivedEvent scheduleReceivedEvent = new FlexOfferScheduleReceivedEvent(this,
                                "Schedule Received", tempFO, schedule, false);
                        applicationEventPublisher.publishEvent(scheduleReceivedEvent);
                        logger.debug("Schedule for FO: {} is accepted and published.", tempFO.getId());
                        scheduleValidationStatus = ScheduleValidationStatus.ValidAndPublished;
                    }
                } else {
                    logger.warn("Schedule is rejected as FO: {} is invalid.", tempFO.getId());
                    scheduleValidationStatus = ScheduleValidationStatus.InvalidSchedule;
                }
            } else {
                logger.warn("Flex-offer with the ID=>{} doesn't exists", tempFO.getId());
                scheduleValidationStatus = ScheduleValidationStatus.InvalidFlexOfferId;
            }
        } else {
            logger.warn("Flex-offer with the key=>{} doesn't exists", foaKey);
            scheduleValidationStatus = ScheduleValidationStatus.InvalidFlexOfferId;
        }

        ScheduleEventLog scheduleEventLog = new ScheduleEventLog();
        scheduleEventLog.setFlexOfferId(tempFO.getId().toString());
        scheduleEventLog.setReceivedScheduleId(schedule.getScheduleId());
        scheduleEventLog.setStatus(scheduleValidationStatus);
        scheduleEventLog.setSchedule(schedule.toString());
        scheduleEventLog.setTimestamp(new Date());
        scheduleEventLogRepository.save(scheduleEventLog);
    }

    @Override
    public void handleScheduleFromFMAN(ConcurrentHashMap<UUID, FlexOfferSchedule> schedules) {
        if (schedules != null) {
            Set<UUID> keys = schedules.keySet();
            Iterator<UUID> iter = keys.iterator();
            // Loop through all the schedules received
            while (iter.hasNext()) {
                try {
                    UUID foaKey = iter.next();//iter.next();
                    // Process schedule
                    this.processSchedule(schedules.get(foaKey), foaKey);
                } catch (Exception ex) {
                    logger.warn("Fo schedule could not be triggered to TPlink Plug");
                }
            }
        }
    }

    @Override
    public ScheduleT getByDate(Date schDate) {
        return scheduleRepository.findByScheduleDate(schDate);
    }

    @Override
    public List<ScheduleT> getByFODate(Date startDate, Date endDate) {
        return scheduleRepository.findByScheduleFODate(startDate, endDate);
    }

    @Override
    public List<OnOffSchedule> getByFlexOfferId(String flexOfferId) {
        try {
            return scheduleRepository.findByFlexOfferId(flexOfferId);
        } catch (Exception ex) {
            logger.warn(ex.toString());
        }

        return null;
    }

    @Override
    public List<ScheduleT> getFutureSchedule(Date currentFoDate) {
        return scheduleRepository.findByFutureFODate(currentFoDate);
    }

    @Override
    public OnOffSchedule getByDateAndDeviceId(Date schDate, String deviceId) {
        return scheduleRepository.findByScheduleDateAndDeviceId(schDate, deviceId);
    }

    @Override
    public List<OnOffSchedule> getAllUnPushedSchedules(Date schDate) {
        return scheduleRepository.findAllByScheduleDate(schDate);
    }

    @Override
    public ScheduleT save(ScheduleT scheduleT) {
        return scheduleRepository.saveAndFlush(scheduleT);

    }

    @Override
    @Transactional
    public void updateScheduleStatus(OnOffSchedule onOffSchedule) {
        OnOffSchedule schedule = scheduleRepository.findByOnOffScheduleId(onOffSchedule.getOnOffScheduleId());
        schedule.setIsValid(0);
    }


    @Override
    @Transactional
    public ScheduleT addSchedulesForKey(Date schDate, OnOffSchedule onOffSchedule) {

        if (schDate == null) {
            return null;
        }

        ScheduleT scheduleRepo = scheduleRepository.findByScheduleDate(schDate);

        if (scheduleRepo == null) {
            return null;
        }

        // check if schedule with the given deviceId already exists
        if (scheduleRepository.findByScheduleDateAndDeviceId(schDate, onOffSchedule.getDeviceID()) != null) {
            return null;
        }

        // add schedule to the date key
        scheduleRepo.addOnOffSchedule(onOffSchedule);
        return scheduleRepo;
    }

    /**
     * remove schedule
     */
    @Override
    @Transactional
    public Boolean removeSchedule(Date schDate, String deviceId) {
        ScheduleT scheduleList = scheduleRepository.findByScheduleDate(schDate);
        OnOffSchedule schedule = scheduleRepository.findByScheduleDateAndDeviceId(schDate, deviceId);

        if (scheduleList == null) {
            return false;
        }
        // get schedule with given Id

        if (schedule != null) {
            scheduleList.removeSchedule(schedule);
            return true;
        }
        return false;
    }


    /**
     * remove schedule
     */
    @Override
    @Transactional
    public Boolean inValidateSchedule(Date schDate, String deviceId) {

        OnOffSchedule schedule = scheduleRepository.findByScheduleDateAndDeviceId(schDate, deviceId);

        if (schedule == null) {
            return false;
        }
        // get schedule with given Id

        if (schedule != null) {
            schedule.setIsValid(0);
            return true;
        }
        return false;
    }

    /**
     * remove schedule
     */
    @Override
    @Transactional
    public Boolean inValidateSchedule(String flexOfferId) {

        onOffScheduleRepository.updateScheduleStatus(flexOfferId);
        return true;

    }

    /**
     * remove schedule
     */
    @Override
    @Transactional
    public Boolean inValidateSchedule(Long onOffScheduleId) {
        onOffScheduleRepository.updateScheduleStatus(onOffScheduleId);
        return true;
    }

    /**
     * remove schedule
     */
    @Override
    public List<OnOffSchedule> getOnOffSchedules(String flexOfferId) {
        return onOffScheduleRepository.findAllByFlexOfferId(flexOfferId);
    }


    @Override
    public void handleScheduleReceivedFromFMAN() {

    }

}
