
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

package org.goflex.wp2.foa.listeners;

import org.goflex.wp2.core.models.OnOffSchedule;
import org.goflex.wp2.core.models.ScheduleT;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.events.FlexOfferScheduleReceivedEvent;
import org.goflex.wp2.foa.implementation.TpLinkDeviceService;
import org.goflex.wp2.foa.interfaces.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bijay on 7/22/17.
 */

//@Component
public class FlexOfferScheduleUpdateReceivedListner {
    public static final long interval = 900 * 1000;
    private static final Logger logger = LoggerFactory.getLogger(FlexOfferScheduleUpdateReceivedListner.class);
    @Autowired
    private TpLinkDeviceService tpLinkDeviceService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private FOAProperties foaProperties;

    @Resource(name = "tpLinkScheduleIdTable")
    private ConcurrentHashMap<UUID, List<String>> tplinkScheduleID;


    private String addScheduleToTplink(FlexOfferScheduleReceivedEvent event, Date eventTime, int action) {

        Calendar scheduleDate = Calendar.getInstance();
        scheduleDate.setTime(eventTime);
        int schYear = scheduleDate.get(Calendar.YEAR);
        int schMonth = scheduleDate.get(Calendar.MONTH) + 1;
        int schDay = scheduleDate.get(Calendar.DAY_OF_MONTH);
        int schHour = scheduleDate.get(Calendar.HOUR_OF_DAY);
        int schMinute = scheduleDate.get(Calendar.MINUTE);
        int schWeekOfDay = scheduleDate.get(Calendar.DAY_OF_WEEK);

        String ScheduleId = tpLinkDeviceService.addOnOffSchedule(event.getFlexOffer().getOfferedById().split("@")[1], event.getFlexOffer().getOfferedById().split("@")[0],
                eventTime, action);

        return ScheduleId;

    }

    private boolean updateTpLinkSchedule(FlexOfferScheduleReceivedEvent event, String scheduleID, int action) {

        tpLinkDeviceService.deleteOnOffSchedule(event.getFlexOffer().getOfferedById().split("@")[1],
                event.getFlexOffer().getOfferedById().split("@")[0], scheduleID, action);

        return true;

    }

    //@Override
    public void onApplicationEvent(FlexOfferScheduleReceivedEvent event) {
        logger.debug(event.getEventName() + " at " + event.getTimestamp() + " for flex-offer with ID =>{}", event.getFlexOffer().getId());
        //update schedule update id by 1
        event.getNewSchedule().setUpdateId(event.getFlexOffer().getFlexOfferSchedule().getUpdateId() + 1);
        //update flex-offer schedule
        event.getFlexOffer().setFlexOfferSchedule(event.getNewSchedule());
        logger.info("Scheduled updated for Flex-offer with ID =>{}", event.getFlexOffer().getId());

        /**
         * Add schedule to the schedule list Hashtable. The schedule handler will execute all schedule for particular time.
         */

        Date startDate = event.getNewSchedule().getStartTime();
        Date currentDate = new Date();

        /** Check if an schedule has already been created for flex-offer */
        if (tplinkScheduleID.containsKey(event.getFlexOffer().getId()) && 1 == 0) {
            //TODO: call schedule update received listener
            logger.debug("This is not a new schedule, routing to schedule update listener");
        } else {

            /** loop through slices in the flex-offer, + 1 is to toogle device to default sate*/
            List<String> tpLinkIds = new ArrayList<>();
            for (int i = 0; i < event.getNewSchedule().getScheduleSlices().length + 1; i++) {
                logger.info("Schedule for Flex-offer ID:" + event.getFlexOffer().getId());
                /**start date is the schedule start time */
                startDate = new Date(startDate.getTime() + i * interval);
                /**check if current date is after the starttimedate */
                if (startDate.after(currentDate)) {
                    int action;

                    /**check action to perform*/
                    if (i == event.getNewSchedule().getScheduleSlices().length) {
                        action = 1; //default step for the device
                    } else if (event.getNewSchedule().getScheduleSlice(i).getEnergyAmount() > 0) {
                        action = 1;
                    } else {
                        action = 0;
                    }

                    /**create new schedule and set energy level*/
                    OnOffSchedule schedule = new OnOffSchedule(event.getFlexOffer().getOfferedById());
                    if (i == event.getNewSchedule().getScheduleSlices().length) {
                        schedule.setEnergyLevel(0.0);
                    } else {
                        schedule.setEnergyLevel(event.getNewSchedule().getScheduleSlice(i).getEnergyAmount());
                    }

                    /** set device state for the schedule */
                    schedule.setScheduleToState(action);

                    /**Add schedule to scheduleDetail for activation */
                    boolean schExist = false;


                    /**Check if we already key for a particular time*/
                    ScheduleT hasKey = scheduleService.getByDate(startDate);


                    if (hasKey != null) {
                        /**Check if we already have a schedule for the device at a particular time*/
                        OnOffSchedule hasPrevSchedule = scheduleService.getByDateAndDeviceId(startDate, schedule.getDeviceID());

                        /**first remove previous schedule if exists*/
                        if (hasPrevSchedule != null) {
                            scheduleService.removeSchedule(startDate, schedule.getDeviceID());
                            this.updateTpLinkSchedule(event, tplinkScheduleID.get(schedule.getDeviceID()).get(i), action);
                        }

                        /**Add new schedule to the system */
                        scheduleService.addSchedulesForKey(startDate, schedule);
                    } else {

                        /**Persist the new schedule table */
                        ScheduleT savedSchedule = scheduleService.save(new ScheduleT(startDate));

                        /**Add new schedule to the system */
                        scheduleService.addSchedulesForKey(startDate, schedule);

                        logger.info("Adding new schedule for the flex-offer");
                        String scheduleId = this.addScheduleToTplink(event, startDate, action);
                        /**store schedule key received from the tplink plug*/
                        tpLinkIds.add(scheduleId);
                    }


                } else {
                    logger.info("Schedule is before Current Time. Schedule Ignored");
                }
            }


            /**add tplink scheduleIds for the flexoffer, if the key doesnot exist */
            if (!tplinkScheduleID.containsKey(event.getFlexOffer().getId())) {
                tplinkScheduleID.put(event.getFlexOffer().getId(), tpLinkIds);
            }
        }
    }
}
