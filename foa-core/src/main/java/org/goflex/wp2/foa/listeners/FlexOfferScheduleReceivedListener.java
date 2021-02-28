
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
import org.goflex.wp2.core.repository.FlexOfferRepository;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.foa.devicestate.DeviceStateHistory;
import org.goflex.wp2.foa.devicestate.DeviceStateHistoryRepository;
import org.goflex.wp2.foa.events.DeviceStateChangeEvent;
import org.goflex.wp2.foa.events.FlexOfferScheduleReceivedEvent;
import org.goflex.wp2.foa.events.FlexOfferStatusUpdateEvent;
import org.goflex.wp2.foa.implementation.ImplementationsHandler;
import org.goflex.wp2.foa.interfaces.*;
import org.goflex.wp2.foa.util.TimeZoneUtil;
import org.goflex.wp2.foa.wrapper.PoolSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bijay on 7/22/17.
 */

@Component
public class FlexOfferScheduleReceivedListener implements ApplicationListener<FlexOfferScheduleReceivedEvent> {

    private static final long interval = 900 * 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(FlexOfferScheduleReceivedListener.class);

    @Resource(name = "deviceLatestAggData")
    LinkedHashMap<String, Map<Date, Double>> deviceLatestAggData;

    @Resource(name = "scheduleDetailTable")
    private ConcurrentHashMap<Date, ScheduleDetails> scheduleDetail;

    @Resource(name = "deviceLatestFO")
    private ConcurrentHashMap<String, FlexOfferT> deviceLatestFO;

    @Resource(name = "deviceActiveSchedule")
    private ConcurrentHashMap<String, Map<Date, Integer>> deviceActiveSchedule;

    @Resource(name = "poolScheduleMap")
    private ConcurrentHashMap<String, Map<Long, PoolSchedule>> poolScheduleMap;

    @Resource(name = "poolDeviceDetail")
    private ConcurrentHashMap<String, Map<String, PoolDeviceModel>> poolDeviceDetail;

    @Resource(name = "poolTurnedOffDevices")
    private ConcurrentHashMap<String, Map<String, Date>> poolTurnedOffDevices;

    private final ScheduleService scheduleService;
    private final DeviceDetailService deviceDetailService;
    private final ImplementationsHandler implementationsHandler;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DeviceDefaultState deviceDefaultState;
    private final DeviceFlexOfferGroup deviceFlexOfferGroup;
    private final SmsService smsService;
    private final FOAService foaService;
    private final OrganizationRepository organizationRepository;
    private final UserService userService;
    private final FlexOfferRepository flexOfferRepository;
    private final DeviceStateHistoryRepository deviceStateHistoryRepository;


    public FlexOfferScheduleReceivedListener(ScheduleService scheduleService, DeviceDetailService deviceDetailService,
                                             ImplementationsHandler implementationsHandler,
                                             ApplicationEventPublisher applicationEventPublisher,
                                             DeviceDefaultState deviceDefaultState, UserService userService,
                                             DeviceFlexOfferGroup deviceFlexOfferGroup, SmsService smsService,
                                             FOAService foaService, OrganizationRepository organizationRepository,
                                             FlexOfferRepository flexOfferRepository,
                                             DeviceStateHistoryRepository deviceStateHistoryRepository) {
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
        this.flexOfferRepository = flexOfferRepository;
        this.deviceStateHistoryRepository = deviceStateHistoryRepository;
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

            LOGGER.debug(event.getEventName() + " at " + event.getTimestamp());

            FlexOfferT foT = this.flexOfferRepository.findByFoID(event.getFlexOffer().getId().toString());
            Organization org = this.organizationRepository.findByOrganizationId(foT.getOrganizationId());

            if (org.isPoolBasedControl()) {
                processPoolSchedule(event, org);
            } else {
                processDeviceSchedule(event);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error(ex.getLocalizedMessage());
        }
    }


    private void processDeviceSchedule(FlexOfferScheduleReceivedEvent event) {
        try {
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
                                    String deviceType = device.getDeviceType() == DeviceType.WasherDryer ? "Washing Machine" : device.getDeviceType().name();
//                                    msg = "The " + deviceType + " (" + device.getAlias() + ") will start today at " + stringTime +
//                                            ". Please switch on the device as soon as possible after " + stringTime +
//                                            ". If the device starts automatically, you can ignore this SMS.";

                                    //Η ηλεκτρική παροχή στο συσκευή  θα επανέλθει στις 17:30. Σε περίπτωση που το συσκευή δεν επανεκκινεί αυτόματα, παρακαλώ επανεκκινήστε τη συσκευή το συντομότερο μετά τις 17:30.
                                    msg = "Η ηλεκτρική παροχή στο " + deviceType + " (" + device.getAlias() + ") θα επανέλθει στις " + stringTime +
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


    private void processPoolSchedule(FlexOfferScheduleReceivedEvent event, Organization org) {
        try {
            if (!event.getDefaultSchedule()) {
                LOGGER.info("New pool scheduled received from FMAN_FMAR for flex-offer with ID =>{}",
                        event.getFlexOffer().getId());
                /** update schedule update id by 1 */
                event.getNewSchedule().setUpdateId(event.getFlexOffer().getFlexOfferSchedule().getUpdateId() + 1);
                /**update flex-offer schedule */
                event.getFlexOffer().setFlexOfferSchedule(event.getNewSchedule());
                LOGGER.info("Pool schedule updated for Flex-offer with ID =>{}", event.getFlexOffer().getId());

                // also update FlexOfferT
                FlexOfferT flexOfferT = this.foaService.getFlexOffer(event.getFlexOffer().getId());
                flexOfferT.setFlexoffer(event.getFlexOffer());

            }

            // get new schedule and related data from event
            FlexOfferSchedule newSchedule = event.getNewSchedule();
            FlexOffer flexOffer = event.getFlexOffer();
            double offeredLowerEnergy = flexOffer.getFlexOfferProfile(0).getEnergyLower(0);
            double offeredUpperEnergy = flexOffer.getFlexOfferProfile(0).getEnergyUpper(0);
            double marketOrder = newSchedule.getTotalEnergy();
            Date startTime = newSchedule.getStartTime();
            Date endTime = FlexOffer.toAbsoluteTime(FlexOffer.toFlexOfferTime(startTime) + 1);
            Date currentTime = new Date();

            if (startTime.before(currentTime)) {
                LOGGER.warn("Pool schedule time {} is before Current Time {}. Schedule Ignored", startTime, currentTime);
                return;
            }

            if (marketOrder < offeredLowerEnergy || marketOrder > offeredUpperEnergy) {
                LOGGER.warn("Pool schedule market order is out of bounds");
                return;
            }

            PoolSchedule poolSchedule = new PoolSchedule();
            // assuming there is only one slice
            poolSchedule.setOfferedEnergyLower(offeredLowerEnergy);
            poolSchedule.setOfferedEnergyUpper(offeredUpperEnergy);
            poolSchedule.setMarketOrder(marketOrder); // assuming there is always one slice
            poolSchedule.setCurrentPowerConsumption(-1);
            poolSchedule.setCumulativeEnergy(0);
            poolSchedule.setStartTime(startTime);
            poolSchedule.setEndTime(endTime);

            Long scheduleFoTime = FlexOffer.toFlexOfferTime(startTime);
            Long currentSliceFoTime = FlexOffer.toFlexOfferTime(currentTime);

            if (poolScheduleMap.get(org.getOrganizationName()).containsKey(scheduleFoTime)) {
                // transfer list of turned off devices from prev slice
                poolSchedule.setCurrentPowerConsumption(poolScheduleMap.get(org.getOrganizationName()).get(scheduleFoTime).getCurrentPowerConsumption());
                poolSchedule.setCumulativeEnergy(poolScheduleMap.get(org.getOrganizationName()).get(scheduleFoTime).getCumulativeEnergy());
            }

            poolScheduleMap.get(org.getOrganizationName()).put(scheduleFoTime, poolSchedule);
            printPoolSchedule();
            LOGGER.info("poolScheduleMap map updated for organization: {}", event.getFlexOffer().getOfferedById());
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error(ex.getLocalizedMessage());
        }
    }

    private void printPoolSchedule() {
        LOGGER.info("current status of pool schedule map");
        this.poolScheduleMap.forEach((key, val) -> {
            val.forEach((k, v) -> {
                LOGGER.info("org: {} slice: {}, map: {}", key, FlexOffer.toAbsoluteTime(k), v.toString());
            });
        });
    }

    private List<String> getDevicesToTurnOff(Double power, String orgName, double deviceCoolingPeriod) {
        TreeMap<Double, String> devices = new TreeMap<>(Collections.reverseOrder());

        List<String> devicesToTurnOff = new ArrayList<>();
        Double powerToReduce = power;

        for (String key : poolDeviceDetail.get(orgName).keySet()) {
            PoolDeviceModel sdm = poolDeviceDetail.get(orgName).get(key);
            if (sdm.getCurrentState() == 1 && !this.poolScheduleMap.get(orgName).containsKey(key)) {
                devices.put(sdm.getCurrentTemperature() + new Random().nextDouble() * 0.001, sdm.getDeviceId());
            }
        }

        for (Map.Entry<Double, String> entry : devices.entrySet()) {

            if (powerToReduce < 500) {
               break;
            }

            PoolDeviceModel sdm = poolDeviceDetail.get(orgName).get(devices.get(entry.getKey()));
            if (sdm.getLastReleasedFromPool() != null) {
                long coolingTime = (new Date().getTime() - sdm.getLastReleasedFromPool().getTime()) / 60000;
                if (coolingTime < deviceCoolingPeriod) {
                    continue;
                }
            }

            // just in case it was recently turned on externally
            DeviceStateHistory deviceStateHistory = deviceStateHistoryRepository.getLatestDeviceHistory(entry.getValue());
            if (deviceStateHistory != null && deviceStateHistory.getDeviceState() == DeviceState.Operating) {
                long coolingTime = (new Date().getTime() - deviceStateHistory.getTimestamp().getTime()) / 60000;
                if (coolingTime < 60) {
                    continue;
                }
            }

            Double temperature = entry.getKey();
            if (temperature > sdm.getMinTempThresh() && sdm.getCurrentState() == 1) {
                devicesToTurnOff.add(poolDeviceDetail.get(orgName).get(devices.get(temperature)).getDeviceId());
                powerToReduce = powerToReduce - poolDeviceDetail.get(orgName).get(devices.get(temperature)).getCurrentPower();
            }
        }
        return devicesToTurnOff;
    }


    private List<String> getDevicesToTurnOn(Double power, String orgName) {
        TreeMap<Double, String> devices = new TreeMap<>();
        List<String> devicesToTurnOn = new ArrayList<>();
        Double powerToIncrease = power;
        for (String deviceId : poolDeviceDetail.get(orgName).keySet()) {
            PoolDeviceModel sdm = poolDeviceDetail.get(orgName).get(deviceId);
            long coolingTime = 16;
            if (sdm.getLastIncludedInPool() != null) {
                coolingTime = (new Date().getTime() - sdm.getLastIncludedInPool().getTime()) / 60000;
            }
            if ( (this.poolTurnedOffDevices.get(orgName).containsKey(deviceId) || orgName.equals("TEST") )
                    && sdm.getCurrentState() == 0 && coolingTime > 15) {
                devices.put(sdm.getCurrentTemperature() + new Random().nextDouble() * 0.001, sdm.getDeviceId());
            }
        }

        for (Map.Entry<Double, String> entry : devices.entrySet()) {

            Double temperature = entry.getKey();

            if (powerToIncrease < 500) {
                break;
            }

            // just in case it was recently turned on externally
            DeviceStateHistory deviceStateHistory = deviceStateHistoryRepository.getLatestDeviceHistory(entry.getValue());
            if (deviceStateHistory != null && deviceStateHistory.getDeviceState() == DeviceState.Idle) {
                long coolingTime = (new Date().getTime() - deviceStateHistory.getTimestamp().getTime()) / 60000;
                if (coolingTime < 5) {
                    continue;
                }
            }

            String deviceId = devices.get(temperature);
            if (temperature < poolDeviceDetail.get(orgName).get(deviceId).getMaxTempThresh()) {
                    devicesToTurnOn.add(poolDeviceDetail.get(orgName).get(devices.get(temperature)).getDeviceId());
                    powerToIncrease = powerToIncrease - poolDeviceDetail.get(orgName).get(devices.get(temperature)).getCurrentPower();
            }

        }
        return devicesToTurnOn;
    }

    private void releaseDevicesFromPool(String orgName) {

        Set<String> deviceIds = this.poolTurnedOffDevices.get(orgName).keySet();
        if (deviceIds.size() == 0) {
            return;
        }

        LOGGER.info("Releasing necessary devices from the pool");

        for (String deviceId : deviceIds) {
            Date turnedOffTime = this.poolTurnedOffDevices.get(orgName).get(deviceId);
            long minutes = (new Date().getTime() - turnedOffTime.getTime()) / (1000 * 60);
            if (minutes > 30) {
                DeviceDetail deviceDetail = this.deviceDetailService.getDevice(deviceId);
                DeviceStateChangeEvent stateChangeEvent = new DeviceStateChangeEvent(
                        this, String.format("releasing device: '%s' which was turned off at: '%s' from the pool",
                        deviceDetail.getDeviceId(), turnedOffTime),
                        deviceDetail, true);
                this.applicationEventPublisher.publishEvent(stateChangeEvent);
                this.poolDeviceDetail.get(orgName).get(deviceId).setLastReleasedFromPool(new Date());
                this.poolTurnedOffDevices.get(orgName).remove(deviceId);
                LOGGER.info("Released org: {}, device: {} from the pool", orgName, deviceId);
            }
        }
    }


    public void executePoolSchedule() {
        long foTime = FlexOffer.toFlexOfferTime(new Date());
        this.poolScheduleMap.forEach((orgName, orgPoolMap) -> {

            // first release those devices that are in OFF state longer than threshold
            releaseDevicesFromPool(orgName);

            // update pool power consumption
            updatePoolScheduleMapPower(orgName);

            // execute pool schedule for the organization
            executeOrgSchedule(orgName, foTime, orgPoolMap);

        });
    }


    private void executeOrgSchedule(String orgName, long foTime, Map<Long, PoolSchedule> orgPoolMap) {

        if (orgPoolMap.size() == 0) {
            return;
        }

        if (!orgPoolMap.containsKey(foTime)) {
            return;
        }

        double lowerEnergy = orgPoolMap.get(foTime).getOfferedEnergyLower()*4000;
        double upperEnergy = orgPoolMap.get(foTime).getOfferedEnergyUpper()*4000;
        if ( Math.abs(upperEnergy-lowerEnergy) < 1) {
            LOGGER.info("Org: {} is in cooling period", orgName);
            return;
        }

        PoolSchedule poolSchedule = orgPoolMap.get(foTime);
        if (poolSchedule == null) {
            LOGGER.warn("No pool schedule found for the current slice for org: {}", orgName);
            return;
        }

        double marketOrder = poolSchedule.getMarketOrder() * 1000 * 4;

        double currentPowerConsumption = poolSchedule.getCurrentPowerConsumption();
        if (currentPowerConsumption < 0) {
            return;
        }

        double cumulativeEnergy = poolSchedule.getCumulativeEnergy(); // Wh for now

        Organization org = this.organizationRepository.findByOrganizationName(orgName);
        LOGGER.info("Org: {}, Market order: {}, current consumption: {}", org.getOrganizationName(),
                marketOrder, currentPowerConsumption);

        if (marketOrder < currentPowerConsumption) {
            List<String> devicesToTurnOff = getDevicesToTurnOff(currentPowerConsumption-marketOrder, orgName,
                    org.getPoolDeviceCoolingPeriod());

            if (devicesToTurnOff.size() > 0 && org.getDirectControlMode() != OrganizationLoadControlState.Active) {
                LOGGER.warn("Not turning off devices because organization control disabled for: {}", orgName);
                return;
            }

            devicesToTurnOff.forEach(deviceId -> {
                DeviceDetail device = this.deviceDetailService.getDevice(deviceId);
                DeviceStateChangeEvent stateChangeEvent = new DeviceStateChangeEvent(
                        this, String.format("Turning off device: '%s' due to inclusion in the pool",
                        device.getDeviceId()),
                        device, false);
                this.applicationEventPublisher.publishEvent(stateChangeEvent);
                this.poolDeviceDetail.get(orgName).get(deviceId).setLastIncludedInPool(new Date());
                this.poolTurnedOffDevices.get(orgName).put(deviceId, new Date());
            });
        } else {
            List<String> devicesToTurnOn = getDevicesToTurnOn(marketOrder-currentPowerConsumption, orgName);

            if (devicesToTurnOn.size() > 0 && org.getDirectControlMode() != OrganizationLoadControlState.Active) {
                LOGGER.warn("Not turning on devices because organization control disabled for: {}", orgName);
                return;
            }

            devicesToTurnOn.forEach(deviceId -> {
                DeviceDetail device = this.deviceDetailService.getDevice(deviceId);
                DeviceStateChangeEvent stateChangeEvent = new DeviceStateChangeEvent(
                        this, String.format("Turning on device: '%s' due to inclusion in the pool",
                        device.getDeviceId()),
                        device, true);
                this.applicationEventPublisher.publishEvent(stateChangeEvent);
            });
        }

        // update cumulative energy
        this.poolScheduleMap.get(orgName).get(foTime).setCumulativeEnergy(cumulativeEnergy + currentPowerConsumption);
    }

    public void transferScheduleFromPrevToCurrSlice() {
        LOGGER.info("executing routine to transfer necessary schedule data from prev to curr slice");
        long currentSliceFoTime = FlexOffer.toFlexOfferTime(new Date());
        this.poolScheduleMap.forEach((orgName, orgPoolMap) -> {
            if (orgPoolMap.containsKey(currentSliceFoTime - 1)) {
                // delete prev slice from map
                this.poolScheduleMap.get(orgName).remove(currentSliceFoTime - 1);
            }
        });
    }

    private void updatePoolScheduleMapPower(String orgName) {

        double latestAggPower = 0.0;
        for (Map.Entry<String, PoolDeviceModel> entry : this.poolDeviceDetail.get(orgName).entrySet()) {
            latestAggPower += entry.getValue().getCurrentPower();
        }

        long foTime = FlexOffer.toFlexOfferTime(new Date());
        if (this.poolScheduleMap.get(orgName).containsKey(foTime)) {
            this.poolScheduleMap.get(orgName).get(foTime).setCurrentPowerConsumption(latestAggPower);
        }
    }

}
