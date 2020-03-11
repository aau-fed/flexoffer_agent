
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

import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.core.models.*;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.foa.events.FlexOfferScheduleReceivedEvent;
import org.goflex.wp2.foa.events.FlexOfferStatusUpdateEvent;
import org.goflex.wp2.foa.implementation.ImplementationsHandler;
import org.goflex.wp2.foa.interfaces.*;
import org.goflex.wp2.foa.util.TimeZoneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bijay on 7/22/17.
 */

@Component
public class FlexOfferScheduleReceivedListener implements ApplicationListener<FlexOfferScheduleReceivedEvent> {

    private static final long interval = 900 * 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlexOfferScheduleReceivedListener.class);

    private ScheduleService scheduleService;

    private DeviceDetailService deviceDetailService;

    private ImplementationsHandler implementationsHandler;

    private ApplicationEventPublisher applicationEventPublisher;

    private DeviceDefaultState deviceDefaultState;

    private DeviceFlexOfferGroup deviceFlexOfferGroup;

    private SmsService smsService;

    private FOAService foaService;

    private OrganizationRepository organizationRepository;

    private UserService userService;

    @Resource(name = "scheduleDetailTable")
    private ConcurrentHashMap<Date, ScheduleDetails> scheduleDetail;

    @Resource(name = "deviceLatestFO")
    private ConcurrentHashMap<String, FlexOfferT> deviceLatestFO;

    @Resource(name = "deviceActiveSchedule")
    private ConcurrentHashMap<String, Map<Date, Integer>> deviceActiveSchedule;

    public FlexOfferScheduleReceivedListener(ScheduleService scheduleService, DeviceDetailService deviceDetailService,
                                             ImplementationsHandler implementationsHandler,
                                             ApplicationEventPublisher applicationEventPublisher,
                                             DeviceDefaultState deviceDefaultState, UserService userService,
                                             DeviceFlexOfferGroup deviceFlexOfferGroup, SmsService smsService,
                                             FOAService foaService, OrganizationRepository organizationRepository) {
        this.scheduleService = scheduleService;
        this.deviceDetailService = deviceDetailService;
        this.implementationsHandler = implementationsHandler;
        this.applicationEventPublisher = applicationEventPublisher;
        this.deviceDefaultState = deviceDefaultState;
        this.userService = userService;
        this.deviceFlexOfferGroup = deviceFlexOfferGroup;
        this.smsService = smsService;
        this.foaService = foaService;
        this.organizationRepository = organizationRepository;
    }

    private void addScheduleToFLS(String offeredById, UUID flexOfferId, Date eventTime, int action) {

        if (scheduleDetail.containsKey(eventTime)) {
            scheduleDetail.get(eventTime).addSchedule(offeredById, flexOfferId, action);
        } else {
            scheduleDetail.put(eventTime, new ScheduleDetails(offeredById, flexOfferId, action));
        }


    }

    private boolean deleteScheduleFromDevice(String offeredById, PlugType plugType, String scheduleID, int action) {
        return implementationsHandler.get(plugType).deleteOnOffSchedule(
                offeredById.split("@")[1], offeredById.split("@")[0], scheduleID, action);
    }


    /**
     * if index -1, return device default
     */
    private int getAction(int index, int lastAction, Double energyAmount, DeviceDetail device) {
        double minEnergyThreshold = 1.0; // min 1Wh in a slice else it's a very small value due to FMAN optimization
        int action;
        int defaultState = deviceDefaultState.getDeviceDefaultState(device.getDeviceType());//
        //device.getDefaultState();
        // if not last slice
        if (index != -1) {
            //This will handle both production and consumption FOs, i.e, for production fo energy is -ve
            //if (energyAmount != 0) {
            if (energyAmount * 1000 > minEnergyThreshold) {
                action = 1;
            } else {
                action = 0;
            }
        } else {
            // This is used to revert back the load to default state, after last slice
            if (lastAction == 1) {
                if (defaultState == 1) {
                    action = -1;
                } else {
                    action = 0;
                }
            } else {
                if (defaultState == 1) {
                    action = 1;
                } else {
                    action = -1;
                }
            }
        }
        return action;
    }

    private OnOffSchedule createOnOffSchedule(String offerById, int action, Double energyAmount, boolean isLast) {
        OnOffSchedule newSchedule = new OnOffSchedule(offerById);
        newSchedule.setRegisteredTime(new Date());
        newSchedule.setEnergyLevel(energyAmount);
        newSchedule.setScheduleToState(action);
        newSchedule.setLast(isLast);
        return newSchedule;
    }

    private String pushScheduleToDevice(DeviceDetail device, String offeredById, UUID flexOfferId, Date startDate,
                                        int state) {

        // push schedule to smart device
        String scheduleId = implementationsHandler.get(device.getPlugType()).addOnOffSchedule(
                offeredById.split("@")[1], offeredById.split("@")[0], startDate, state);

        if (device.getPlugType() == PlugType.Simulated) {
            this.addScheduleToFLS(offeredById, flexOfferId, startDate, state);
        }

        return scheduleId;
    }

    private boolean invalidateOldSchedule(Date startDate, OnOffSchedule newSchedule, OnOffSchedule oldSchedule,
                                          String offeredById, DeviceDetail device) {
        boolean status;
        // invalidate previous schedule from database
        status = scheduleService.inValidateSchedule(startDate, newSchedule.getDeviceID());

        // remove schedule from smart device
        if (oldSchedule.getExternalScheduleId() != null || !oldSchedule.getExternalScheduleId().equals("")) {
            if (this.deleteScheduleFromDevice(offeredById, device.getPlugType(), oldSchedule.getExternalScheduleId(),
                    1) == false) {
                status = false;
                LOGGER.warn("Could not delete existing schedule.");
            } else {
                LOGGER.info("Old Schedule deleted from smartplug");
            }
        }
        return status;
    }


    private boolean invalidateOldSchedule(String flexOfferId, String offeredById, PlugType plugType) {
        boolean status = true;
        /**invalidate previous schedule from database */
        status = scheduleService.inValidateSchedule(flexOfferId);
        /**remove schedule from smart device*/
        for (OnOffSchedule onOffSchedule : scheduleService.getOnOffSchedules(flexOfferId)) {
            if (onOffSchedule.getExternalScheduleId() != null && !onOffSchedule.getExternalScheduleId().equals("")) {
                if (this.deleteScheduleFromDevice(offeredById, plugType, onOffSchedule.getExternalScheduleId(), 1) ==
                        false) {
                    status = false;
                    LOGGER.warn("Could not delete existing schedule.");
                } else {
                    LOGGER.info("Old Schedule deleted from smartplug");
                }
            }
        }
        return status;
    }


    private String addSchedule(int index, Date startDate, OnOffSchedule schedule, UUID flexOfferId, String offeredById,
                               DeviceDetail device) {

        // Check if we already have key for a particular time, if not create a schedule table with the key
        ScheduleT hasKey = scheduleService.getByDate(startDate);
        if (hasKey == null) {
            // Persist the new schedule table
            ScheduleT savedSchedule = scheduleService.save(new ScheduleT(startDate));
        }

        // insert new schedule
        String scheduleId = null;
        try {
            //TODO: handling schedule at TPLink plug is complex need better model. For now we dont push schedule to plug
            scheduleId = "";
            //scheduleId = this.pushScheduleToDevice(device, offeredById, flexOfferId, startDate, schedule
            // .getScheduleToState());

            //if flex-offer schedule Assignment event
            FlexOfferStatusUpdateEvent flexOfferStatusUpdateEvent = new FlexOfferStatusUpdateEvent(this,
                    "FO Status Updated", "", flexOfferId, FlexOfferState.Assigned);
            applicationEventPublisher.publishEvent(flexOfferStatusUpdateEvent);

        } catch (Exception ex) {
            LOGGER.warn("Could not pass schedule to tplink cloud");
        }

        // if successfully pushed to smart device, device would handle schedule,
        // else schedule handler will execute all un-pushed schedules
        if (scheduleId != null && !scheduleId.equals("")) {
            schedule.setPushedToDevice(1);
            schedule.setExternalScheduleId(scheduleId);
        }

        schedule.setFlexOfferId(flexOfferId.toString());
        // add new schedule to the system
        scheduleService.addSchedulesForKey(startDate, schedule);

        return scheduleId;
    }


    @Override
    @Transactional
    public void onApplicationEvent(FlexOfferScheduleReceivedEvent event) {
        try {
            LOGGER.debug(event.getEvent() + " at " + event.getTimestamp());

            /** this will run only when scheduled received from FMAN,
             *  no need for schedule generated during FO generation */
            if (!event.getDefaultSchedule()) {
                LOGGER.info("New scheduled received from FMAN_FMAR for flex-offer with ID =>{}",
                        event.getFlexOffer().getId());
                /** update schedule update id by 1 */
                event.getNewSchedule().setUpdateId(event.getFlexOffer().getFlexOfferSchedule().getUpdateId() + 1);
                /**update flex-offer schedule */
                event.getFlexOffer().setFlexOfferSchedule(event.getNewSchedule());
                LOGGER.info("Scheduled updated for Flex-offer with ID =>{}", event.getFlexOffer().getId());

                // also update FlexOfferT
                FlexOfferT flexOfferT = this.foaService.getFlexOffer(event.getFlexOffer().getId());
                flexOfferT.setFlexoffer(event.getFlexOffer());

                // also update device->FO memory map
                this.deviceLatestFO.put(event.getFlexOffer().getOfferedById(), flexOfferT);
                LOGGER.info("deviceLatestFO map updated for device: {}", event.getFlexOffer().getOfferedById());
            }

            Date startDate = event.getNewSchedule().getStartTime();
            Date currentDate = new Date();
            int lastAction = -1;
            int action;
            // check if current date is after the start time date
            if (startDate.after(currentDate)) {
                String offeredById = event.getFlexOffer().getOfferedById();
                DeviceDetail device = deviceDetailService.getDevice(offeredById);
                String userName = device.getDeviceId().split("@")[0];
                UserT userT = this.userService.getUser(userName);
                Organization org = this.organizationRepository.findByOrganizationId(userT.getOrganizationId());

                //invalidate all old schedule
                if (this.invalidateOldSchedule(event.getFlexOffer().getId().toString(), offeredById,
                        device.getPlugType())) {

                    // get new schedule from event
                    FlexOfferSchedule newSchedule = event.getNewSchedule();

                    FlexibilityGroupType flexibilityGroupType =
                            deviceFlexOfferGroup.getDeviceFOGroupType(device.getDeviceType());

                    // loop through slices in the flex-offer
                    int numSlices = newSchedule.getScheduleSlices().length;
                    boolean alreadySent = false;
                    for (int i = 0;
                         i < numSlices + 1; i++) { // +1 is to put device into default state upon schedule completion
                        LOGGER.debug("Updating new schedule for Flex-offer with ID:" + event.getFlexOffer().getId());

                        // start date is the schedule start time
                        startDate = new Date(startDate.getTime() + i * interval);
                        int idx = i < numSlices ? i : -1;
                        double energyAmount = i < numSlices ? newSchedule.getScheduleSlice(i).getEnergyAmount() : -1.0;
                        action = this.getAction(idx, lastAction, energyAmount, device);

                        if (!this.deviceActiveSchedule.containsKey(device.getDeviceId())) {
                            Map<Date, Integer> val = new HashMap<>();
                            val.put(startDate, action);
                            this.deviceActiveSchedule.put(device.getDeviceId(), val);
                        }

                        // loop through slices and at the end of schedule device should go to default state
                        // create new schedule and set energy level for each slice
                        OnOffSchedule schedule = createOnOffSchedule(offeredById, action, energyAmount, idx == -1);
                        String scheduleId =
                                this.addSchedule(i, startDate, schedule, event.getFlexOffer().getId(), offeredById,
                                        device);

                        //send message of scheduled device operation only for wet devices that needs manual start
                        // for now we assume all wet devices need manual start since we don't have per device info
                        if (scheduleId != null && !event.getDefaultSchedule()
                                && flexibilityGroupType == FlexibilityGroupType.WetLoad
                                && org.getDirectControlMode() == OrganizationLoadControlState.Active) {

                            Map<Date, Integer> val = this.deviceActiveSchedule.get(device.getDeviceId());
                            Date oldStartDate = (Date) val.keySet().toArray()[0];
                            int oldAction = val.get(oldStartDate);
                            if (!newSchedule.getStartTime().equals(oldStartDate) ||
                                    (action == 1 && (action != oldAction || !alreadySent))) {
                                String orgName = org.getOrganizationName();
                                String stringTime = TimeZoneUtil.getTimeZoneAdjustedStringTime(startDate, orgName);
                                String msg;
                                if (orgName.equals("SWW")) {
                                    msg = "Das Gerät " + device.getAlias() + " startet heute um " + stringTime +
                                            " Uhr. Bitte schalten Sie das Gerät so bald wie möglich nach " +
                                            stringTime + " Uhr ein. Startet das Gerät automatisch, können Sie diese SMS ignorieren.";
                                } else if (org.getOrganizationName().equals("CYPRUS")) {
                                    String deviceType = device.getDeviceType() == DeviceType.WasherDryer ? "Washing Machine": device.getDeviceType().name();
//                                    msg = "The " + deviceType + " (" + device.getAlias() + ") will start today at " + stringTime +
//                                            ". Please switch on the device as soon as possible after " + stringTime +
//                                            ". If the device starts automatically, you can ignore this SMS.";

                                //Η ηλεκτρική παροχή στο συσκευή  θα επανέλθει στις 17:30. Σε περίπτωση που το συσκευή δεν επανεκκινεί αυτόματα, παρακαλώ επανεκκινήστε τη συσκευή το συντομότερο μετά τις 17:30.
                                    msg = "Η ηλεκτρική παροχή στο " + deviceType + " (" + device.getAlias() + ") θα επανέλθει στις "  + stringTime +
                                            ". Σε περίπτωση που το συσκευή δεν επανεκκινεί αυτόματα, παρακαλώ επανεκκινήστε τη συσκευή το συντομότερο μετά τις " + stringTime + ".";

                                } else {
                                    msg = "The device " + device.getAlias() + " will start today at " + stringTime +
                                            ". Please switch on the device as soon as possible after " + stringTime +
                                            ". If the device starts automatically, you can ignore this SMS.";
                                }

                                smsService.sendSms(userName, msg);

                                Map<Date, Integer> val2 = new HashMap<>();
                                val2.put(newSchedule.getStartTime(), action);
                                this.deviceActiveSchedule.put(device.getDeviceId(), val2);

                                alreadySent = true;
                            }
                        }

                        lastAction = action;
                    }
                }

            } else {
                LOGGER.info("Schedule is before Current Time. Schedule Ignored");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error(ex.getLocalizedMessage());
        }
    }
}
