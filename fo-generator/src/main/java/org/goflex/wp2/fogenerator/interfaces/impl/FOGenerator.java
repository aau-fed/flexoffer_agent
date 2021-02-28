package org.goflex.wp2.fogenerator.interfaces.impl;

import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.core.models.*;
import org.goflex.wp2.core.repository.DevicePrognosisRepository;
import org.goflex.wp2.core.repository.DeviceRepository;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.core.repository.UserRepository;
import org.goflex.wp2.foa.events.FlexOfferGeneratedEvent;
import org.goflex.wp2.foa.events.FlexOfferScheduleReceivedEvent;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.FOAService;
import org.goflex.wp2.foa.prediction.OrganizationPrediction;
import org.goflex.wp2.foa.wrapper.PoolFOWrapper;
import org.goflex.wp2.fogenerator.interfaces.FlexOfferGenerator;
import org.goflex.wp2.fogenerator.model.PredictedDemand;
import org.goflex.wp2.fogenerator.model.PredictionTs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FOGenerator implements FlexOfferGenerator {

    public static final long HOUR = 3600 * 1000; // in milli-seconds.
    private static final long interval = 900 * 1000;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * store daily details number of fo generated and
     * timestamp of the last slice of the FO for each device
     */
    @Resource(name = "deviceGeneratedFOCount")
    ConcurrentHashMap<String, Map<String, List<Double>>> deviceGeneratedFOCount = new ConcurrentHashMap<>();

    @Resource(name = "startGeneratingFo")
    private ConcurrentHashMap<String, Integer> startGeneratingFo;

    @Resource(name = "deviceLatestFO")
    private ConcurrentHashMap<String, FlexOfferT> deviceLatestFO;

    @Value("${organization.fo.num.slices}")
    private int numOrgFOSlices;

    private final DeviceFlexOfferGroup deviceFlexOfferGroup;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final FOAService foaService;
    private final OrganizationRepository organizationRepository;
    private final DeviceDetailService deviceDetailService;
    private final DevicePrognosisRepository devicePrognosisRepository;

    private String foaId;
    private final int latency = 2;

    @Value(value = "${foa.status.url}")
    private String foaUrl;


    @Autowired
    public FOGenerator(
            DeviceFlexOfferGroup deviceFlexOfferGroup,
            ApplicationEventPublisher applicationEventPublisher,
            UserRepository userRepository,
            DeviceRepository deviceRepository,
            FOAService foaService,
            OrganizationRepository organizationRepository,
            DeviceDetailService deviceDetailService,
            DevicePrognosisRepository devicePrognosisRepository
    ) {
        this.deviceFlexOfferGroup = deviceFlexOfferGroup;
        this.applicationEventPublisher = applicationEventPublisher;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.foaService = foaService;
        this.organizationRepository = organizationRepository;
        this.deviceDetailService = deviceDetailService;
        this.devicePrognosisRepository = devicePrognosisRepository;
    }


    /**
     * check if the count of fo generated for device on a particular day is within max no of
     * interruption acceptable for the device user.
     */
    @Override
    public boolean shouldGenerateFO(String dt, String prevDt, double currentHour, DeviceDetail deviceDetail) {
        boolean isValid = true;
        double duration;
        try {

            // 1. first check if flexibility generation is off or not for the current device
            if (!deviceDetail.isFlexible()) {
                LOGGER.warn("FO generation paused for device: {} because device flexibility is turned off by user.",
                        deviceDetail.getDeviceId());
                return false;
            }


            DeviceFlexibilityDetail deviceFlexibilityDetail = deviceDetail.getDeviceFlexibilityDetail();
            String deviceId = deviceDetail.getDeviceId();

            if (deviceFlexibilityDetail == null) {
                return false;
            }

            // 2. check if current time is within daily control window
            //Calendar cal = Calendar.getInstance();
            int hour = (int) currentHour;// cal.get(Calendar.HOUR_OF_DAY);
            if (!(hour >= deviceFlexibilityDetail.getDailyControlStart() &&
                    hour < deviceFlexibilityDetail.getDailyControlEnd())) {
                LOGGER.warn("FO generation paused for device: {} because current time is outside daily control window.",
                        deviceDetail.getDeviceId());
                return false;
            }

            /*
            // 3. check if device prediction exists and foType != 0 (0 means don't generate FO)
            long nextSliceTimeStamp = FlexOffer.toFlexOfferTime(new Date())+1;
            DevicePrognosis prediction =  devicePrognosisRepository.findTop1ByDeviceIdAndTimestamp( deviceId,
            nextSliceTimeStamp);
            FlexibilityGroupType flexibilityGroupType = deviceFlexOfferGroup.getDeviceFOGroupType(deviceDetail
            .getDeviceType());
            if (flexibilityGroupType != FlexibilityGroupType.WetLoad && (prediction == null || prediction.getFoType()
             == 0)) {
                LOGGER.warn("FO generation paused for device: {} because either device prediction not available or " +
                        "foType=0 based on prediction.", deviceDetail.getDeviceId());
                return false;
            }
            */

            // 4. temperature guards for swiss heat pumps and boilers
            if (deviceDetail.getPlugType().equals(PlugType.MQTT)) {
                // TODO: replace with per device threshold as each house may have different comfort prefs.
                Double heatPumpTempThreshold = 20.0;
                Double boilerTempThreshold = 30.0;

                DeviceDataSuppl deviceDataSuppl =
                        deviceDetailService.getLatestSupplData(deviceDetail.getConsumptionTs().getId());

                if (deviceDataSuppl == null) {
                    LOGGER.warn("FO generation paused for Swiss device: {} because no sensor data is available.",
                            deviceDetail.getDeviceId());
                    return false;
                }

                if (deviceDetail.getDeviceType().equals(DeviceType.HeatPump)) {
                    if (deviceDataSuppl.getAmbientTemperature() < heatPumpTempThreshold) {
                        LOGGER.warn("FO generation paused for heat pump as room temperature went below threshold");
                        LOGGER.info(String.format("DeviceId: %s, Room Temperature: %.2f, Threshold: %.2f",
                                deviceDetail.getDeviceId(), deviceDataSuppl.getAmbientTemperature(),
                                heatPumpTempThreshold));
                        return false;
                    }
                } else if (deviceDetail.getDeviceType().equals(DeviceType.Boiler)) {
                    if (deviceDataSuppl.getBoilerTemperature() < boilerTempThreshold) {
                        LOGGER.info("FO generation paused for boiler as boiler temperature went below threshold");
                        LOGGER.info(String.format("DeviceId: %s, Boiler Temperature: %.2f, Threshold: %.2f",
                                deviceDetail.getDeviceId(), deviceDataSuppl.getBoilerTemperature(),
                                boilerTempThreshold));
                        return false;
                    }
                }
            }

            // 5. remaining checks
            // check if flexoffer generated on that day
            if (!deviceGeneratedFOCount.containsKey(dt)) {
                if (deviceFlexibilityDetail.getNoOfInterruptionInADay() == 0) {
                    isValid = false;
                }
                // check if flexoffer generated for the device on that day
            } else if (!deviceGeneratedFOCount.get(dt).containsKey(deviceId)) {

                if (deviceFlexibilityDetail.getNoOfInterruptionInADay() == 0) {
                    isValid = false;
                }

                // validate all flexibility parameter
            } else {
                int foCount = deviceGeneratedFOCount.get(dt).get(deviceId).size();

                if (foCount > 0) {
                    // check distance between two consecutive interruptions
                    duration = currentHour - deviceGeneratedFOCount.get(dt).get(deviceId)
                            .get(foCount - 1);//TODO: get current time in hours
                    if (duration < deviceFlexibilityDetail.getMinInterruptionInterval() * 1.0 / 4.0) {
                        isValid = false;
                    }
                    if (foCount >= deviceFlexibilityDetail.getNoOfInterruptionInADay()) {
                        isValid = false;
                    }
                }
            }
            // this is to check for previous day generation in case the day has just started
            if (isValid) {
                if (currentHour < (deviceFlexibilityDetail.getMinInterruptionInterval() * 1.0 / 4.0)) {
                    if (deviceGeneratedFOCount.containsKey(prevDt)) {
                        if (deviceGeneratedFOCount.get(prevDt).containsKey(deviceId)) {
                            int foCount = deviceGeneratedFOCount.get(prevDt).get(deviceId).size();
                            duration = currentHour + 24 -
                                    deviceGeneratedFOCount.get(prevDt).get(deviceId).get(foCount - 1);
                            if (duration < deviceFlexibilityDetail.getMinInterruptionInterval() * 1.0 / 4.0) {
                                isValid = false;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("Some error occurred during FO generation check.");
            ex.printStackTrace();
            return false;
        }
        return isValid;
    }

    /**
     * update the FO generation count for device
     */
    @Override
    public void updateFoGenerationCount(String dt, double currentHour, String deviceId) {
        if (deviceGeneratedFOCount.containsKey(dt)) {
            if (deviceGeneratedFOCount.get(dt).containsKey(deviceId)) {
                if (deviceGeneratedFOCount.get(dt).get(deviceId) == null) {
                    List<Double> generatedTime = new ArrayList<>();
                    generatedTime.add(currentHour);
                    deviceGeneratedFOCount.get(dt).put(deviceId, generatedTime);
                } else {
                    deviceGeneratedFOCount.get(dt).get(deviceId).add(currentHour);
                }
            } else {
                List<Double> generatedTime = new ArrayList<>();
                generatedTime.add(currentHour);
                deviceGeneratedFOCount.get(dt).put(deviceId, generatedTime);
            }
        } else {
            Map<String, List<Double>> val = new HashMap<>();
            List<Double> generatedTime = new ArrayList<>();
            generatedTime.add(currentHour);
            val.put(deviceId, generatedTime);
            deviceGeneratedFOCount.put(dt, val);
        }
    }


    @Override
    public FlexOffer generateFO(DeviceDetail deviceDetail, PredictionTs predictedValues, Organization organization) {
        // TODO: in future add support for other types of FlexOffers (e.g., time-dependent flexoffers or flexoffer
        //  with multiple energy levels
        FlexOffer flexOffer = this.createOnOffFlexOffer(deviceDetail, predictedValues);

        if (flexOffer == null) {
            LOGGER.warn("FO is null. Device: {}", deviceDetail.getDeviceId());
            return null;
        }

        FlexOfferT fo =
                new FlexOfferT(deviceDetail.getDeviceId().split("@")[0],
                        deviceDetail.getDeviceId().split("@")[1],
                        //flexOffer.getCreationTime(),
                        new Date(), // store exact time
                        flexOffer.getFlexOfferSchedule().getStartTime(),
                        flexOffer.getState(), flexOffer.getId().toString(), flexOffer,
                        organization.getOrganizationId(), predictedValues.getFoType());


        // save FO to DB
        foaService.save(fo);

        // also put in memory map
        this.deviceLatestFO.put(deviceDetail.getDeviceId(), fo);
        LOGGER.info("deviceLatestFO map updated. Device Id: {}, FlexOffer ID: {}", flexOffer.getOfferedById(), flexOffer.getId());

        FlexOfferGeneratedEvent foGeneratedEvent =
                new FlexOfferGeneratedEvent(this, "FlexOffer generation request",
                        "", flexOffer, organization); //Create new event
        applicationEventPublisher.publishEvent(foGeneratedEvent); //Publish an event

        // if FO generated update count for device
        Calendar cal = Calendar.getInstance();
        cal.setTime(flexOffer.getFlexOfferSchedule().getStartTime());

        // TODO: update this based on (latest start time + (num_slices*15min))
        double foLastSliceTimestamp = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) * 1.0 / 60.0 +
                flexOffer.getFlexOfferSchedule().getScheduleSlices().length * 1.0 / 4.0;
        // verify current hour is not the hour for next day
        cal.setTime(flexOffer.getCreationTime());
        if (cal.get(Calendar.HOUR_OF_DAY) > foLastSliceTimestamp) {
            foLastSliceTimestamp = 24;
        }

        // update FO generation map
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        String date = format.format(flexOffer.getCreationTime());
        this.updateFoGenerationCount(date, foLastSliceTimestamp, deviceDetail.getDeviceId());

        // create event to execute default schedule
        FlexOfferScheduleReceivedEvent scheduleReceivedEvent = new FlexOfferScheduleReceivedEvent(this,
                "Schedule Received", flexOffer, flexOffer.getDefaultSchedule(), true);
        applicationEventPublisher.publishEvent(scheduleReceivedEvent);
        return flexOffer;
    }

    @Override
    public FlexOffer createOnOffFlexOffer(DeviceDetail device, PredictionTs predictedData) {
        try {
            FlexOffer flexOffer = new FlexOffer();
            flexOffer.setId(UUID.randomUUID());
            flexOffer.setState(FlexOfferState.Initial);
            flexOffer.setStateReason("FlexOffer Initialized");

            //server datetime
            Date date = new Date();

            //flex-offer should be accepted within 15 mins
            Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
            calendar.setTime(date);

            // check passed time
            // TODO:
            //int remainingMins = Math.floorMod(calendar.get(Calendar.MINUTE), 15);
            int remainingMins = Math.floorMod(calendar.get(Calendar.MINUTE), (int) interval / (1000 * 60));


            int generateForInterval = 1;  //  1 means generate FO for the next interval
            // 2 means the interval after that


            // The time to handle the latency of the system. For now we have set 2 min latency
            if (remainingMins > 12 || remainingMins == 0) {
                generateForInterval = generateForInterval + 1;
            }

            flexOffer.setCreationTime(date);
            flexOffer.setOfferedById(device.getDeviceId());

            // For now we only set user location, later this attribute can handle multiple location
            Map<String, GeoLocation> location = new HashMap<>();
            location.put("userLocation", new GeoLocation(device.getLongitude(), device.getLatitude()));
            flexOffer.setLocationId(location);

            Date latestAcceptanceTime = new Date(flexOffer.getCreationTime().getTime() +
                    (generateForInterval + device.getDeviceFlexibilityDetail().getLatestAcceptanceTime()) * interval);
            flexOffer.setAcceptanceBeforeTime(latestAcceptanceTime);
            flexOffer.setAssignmentBeforeTime(latestAcceptanceTime);

            // set Time-flexibility parameters in 15 mins
            int timeFlexibility = device.getDeviceFlexibilityDetail().getMaxInterruptionDelay();

            Date earliestStartTime = new Date(flexOffer.getCreationTime().getTime() + (generateForInterval) * interval);
            flexOffer.setStartAfterTime(earliestStartTime);

            Date latestStartTime = new Date(
                    flexOffer.getCreationTime().getTime() + (generateForInterval + timeFlexibility) * interval);
            flexOffer.setStartBeforeTime(latestStartTime);


            // The cost constraint is random value and is set by FMAN/Aggregator
            flexOffer.setTotalCostConstraint(new FlexOfferConstraint(0, 3));

            //int numSlices = 1; // we are creating one slice for now
            //int numSlices = predictedData.getPredictedDemands().size();
            int numSlices = predictedData.getNumSlices();
            int sliceDuration = 1;

            //better get map from prediction service
            Map<Date, FlexOfferConstraint> predicted_val = new HashMap<>();
            for (PredictedDemand pd : predictedData.getPredictedDemands()) {
                predicted_val.put(pd.getDate(), pd.getFlexOfferConstraint());
            }

            FlexibilityGroupType flexibilityGroupType =
                    deviceFlexOfferGroup.getDeviceFOGroupType(device.getDeviceType());
            FlexOfferSlice[] flexOfferProfileConstraints = new FlexOfferSlice[numSlices];
            for (int i = 0; i < flexOfferProfileConstraints.length; i++) {
                flexOfferProfileConstraints[i] = new FlexOfferSlice();
                flexOfferProfileConstraints[i].setMinDuration(sliceDuration);
                flexOfferProfileConstraints[i].setMaxDuration(sliceDuration);

                FlexOfferConstraint[] energyConstraintList = new FlexOfferConstraint[sliceDuration];
                for (int j = 0; j < energyConstraintList.length; j++) {

                    // For devices other than TCL predicted demand i always same. So no need to verify
                    // whether prediction exists for certain Timestamp or not
                    FlexOfferConstraint foc = predictedData.getPredictedDemands().get(i).getFlexOfferConstraint();

                    // For TCL, check whether prediction exist for the desired time period or not
                    if (flexibilityGroupType == FlexibilityGroupType.ThermostaticControlLoad) {
                        if (!predicted_val.containsKey(earliestStartTime)) {
                            //no predicted value so do not generate FO
                            LOGGER.warn("Failed to create FlexOffer. predicted_val doesn't contain value for earliest start time: {}",
                                    earliestStartTime);
                            return null;
                        } else {
                            foc = predicted_val.get(earliestStartTime);
                        }
                    }

                    energyConstraintList[j] = new FlexOfferConstraint();
                    energyConstraintList[j].setLower(foc.getLower());
                    energyConstraintList[j].setUpper(foc.getUpper());
                }
                flexOfferProfileConstraints[i].setEnergyConstraintList(energyConstraintList);

                // Tariff price here is random value and is updated by FMAN
                flexOfferProfileConstraints[i].setTariffConstraint(new FlexOfferTariffConstraint(0.03, 0.15));
            }

            flexOffer.setFlexOfferProfileConstraints(flexOfferProfileConstraints);

            flexOffer.setTotalEnergyConstraint(flexOffer.getSumEnergyConstraints());

            Date scheduleStartTime = new Date(
                    flexOffer.getCreationTime().getTime() + (generateForInterval + timeFlexibility) * interval);
            TariffConstraintProfile tariffConstraintProfile = new TariffConstraintProfile();
            tariffConstraintProfile.setStartTime(scheduleStartTime);

            TariffSlice[] tariffConstraintSlices = new TariffSlice[numSlices];
            for (int i = 0; i < tariffConstraintSlices.length; i++) {
                tariffConstraintSlices[i] = new TariffSlice();
                tariffConstraintSlices[i] = new TariffSlice(1, 0.03, 0.15);
            }
            tariffConstraintProfile.setTariffSlices(tariffConstraintSlices);
            flexOffer.setFlexOfferTariffConstraint(tariffConstraintProfile);

            FlexOfferSchedule defaultSchedule = new FlexOfferSchedule();
            FlexOfferScheduleSlice[] flexOfferScheduleSlices = new FlexOfferScheduleSlice[numSlices];
            for (int i = 0; i < flexOfferScheduleSlices.length; i++) {
                flexOfferScheduleSlices[i] = new FlexOfferScheduleSlice();

                int foType = predictedData.getFoType();
                double energyAmount;
                FlexOfferConstraint flexOfferConstraint =
                        flexOffer.getFlexOfferProfileConstraints()[i].getEnergyConstraint(0);

                if (flexibilityGroupType == FlexibilityGroupType.Production) {
                    flexOfferScheduleSlices[i] = new FlexOfferScheduleSlice(
                            flexOffer.getFlexOfferProfileConstraints()[i].getEnergyConstraint(0).getLower(), 1);
                } else if (flexibilityGroupType == FlexibilityGroupType.ThermostaticControlLoad) {
                    if (foType == 1) {
                        // TCL device will be turned off like Wet/Battery device, so always turn on by default
                        energyAmount = flexOfferConstraint.getUpper();
                    } else if (foType == 2) {
                        // TCL device will not be turned off. Randomly choose whether to turn the device on or
                        // off in the default schedule
                        Random random = new Random();
                        energyAmount = random.nextBoolean() ? flexOfferConstraint.getUpper() :
                                flexOfferConstraint.getLower();
                    } else {
                        // Device is not turned off. Default schedule is to do nothing
                        energyAmount = flexOfferConstraint.getUpper();
                    }
                    flexOfferScheduleSlices[i] = new FlexOfferScheduleSlice(energyAmount, 1);
                } else if (flexibilityGroupType == FlexibilityGroupType.WetLoad) {
                    // Wet device always have demand for energy so we use getUpper
                    flexOfferScheduleSlices[i] = new FlexOfferScheduleSlice(flexOfferConstraint.getUpper(), 1);
                } else if (flexibilityGroupType == FlexibilityGroupType.BatterySystem) {
                    // Battery systems also have demand for energy so we use getUpper
                    flexOfferScheduleSlices[i] = new FlexOfferScheduleSlice(flexOfferConstraint.getUpper(), 1);
                } else {
                    // for all other devices, use getUpper as we don't have enough details
                    flexOfferScheduleSlices[i] = new FlexOfferScheduleSlice(flexOfferConstraint.getUpper(), 1);
                }

                flexOfferScheduleSlices[i].setDuration(sliceDuration);
            }
            defaultSchedule.setScheduleSlices(flexOfferScheduleSlices);


            if (flexibilityGroupType == FlexibilityGroupType.WetLoad ||
                    flexibilityGroupType == FlexibilityGroupType.BatterySystem) {
                defaultSchedule.setStartTime(latestAcceptanceTime);
            } else {
                defaultSchedule.setStartTime(earliestStartTime);
            }

            flexOffer.setDefaultSchedule(defaultSchedule);
            FlexOfferSchedule flexOfferSchedule = new FlexOfferSchedule(defaultSchedule);
            flexOfferSchedule.setScheduleId(12345); // random
            flexOfferSchedule.setUpdateId(1); // this should be incremented every time a new schedule is received

            flexOffer.setInitialFlexOfferSchedule(flexOfferSchedule);

            return flexOffer;
        } catch (Exception e) {
            LOGGER.error("Error creating onOffFlexOffer. Error message: {}", e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    public String getFoaId() {
        return foaId;
    }


    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getConfigurationURL() {
        return null;
    }

    //@Override
    //@Scheduled(fixedRate = 60000)
    public void generateRandomFO() {
        LOGGER.info("Flex-Offer generation started");
        List<UserT> users = userRepository.findAll();
        List<DeviceDetail> deviceDetails;
        if (users != null && users.size() > 0 && startGeneratingFo.containsKey("start")) {
            for (UserT user : users) {
                deviceDetails = deviceRepository.findByUserId(user.getId());
                if (deviceDetails != null && deviceDetails.size() > 0) {
                    for (DeviceDetail deviceDetail : deviceDetails) {
                        if (deviceDetail.getDeviceState() != DeviceState.Disconnected &&
                                deviceDetail.getDeviceState() != DeviceState.Unknown) {
                            if (deviceDetail.getConsumptionTs().getDefaultValue() > 0) {
                                FlexOffer flexOffer = this.createOnOffFlexOffer(deviceDetail, null);
                                FlexOfferT fo = new FlexOfferT(deviceDetail.getUser().getUserName(),
                                        deviceDetail.getDevicePlugId(),
                                        flexOffer.getCreationTime(), flexOffer.getFlexOfferSchedule().getStartTime(),
                                        flexOffer.getState(),
                                        flexOffer.getId().toString(), flexOffer, user.getOrganizationId());
                                foaService.save(fo);
                                FlexOfferGeneratedEvent foGeneratedEvent =
                                        new FlexOfferGeneratedEvent(this, "FlexOffer Generation Request Received", "",
                                                flexOffer, organizationRepository
                                                .findByOrganizationId(user.getOrganizationId())); //Create new event
                                applicationEventPublisher.publishEvent(foGeneratedEvent); //Publish an event

                            }
                        }
                    }

                }
            }
        }
        LOGGER.info("Flex-Offer generation finished");
    }


    @Override
    public FlexOffer[] createFlexOffer(String deviceID, String foaId,
                                       int number) { //Hashtable will be replaced by userid and plugid
        return new FlexOffer[number];
    }

    @Override
    public FlexOffer generateOrganizationFO(Organization organization, List<OrganizationPrediction> organizationPrediction) {
        FlexOffer flexOffer = this.createOrganizationFlexOffer(organization, organizationPrediction);

        if (flexOffer == null) {
            LOGGER.warn("FO is null. Organization: {}", organization.getOrganizationName());
            return null;
        }

        FlexOfferT fo =
                new FlexOfferT(organization.getOrganizationName(),
                        "",
                        new Date(), // store exact time
                        flexOffer.getFlexOfferSchedule().getStartTime(),
                        flexOffer.getState(), flexOffer.getId().toString(), flexOffer,
                        organization.getOrganizationId(), 1);


        // save FO to DB
        foaService.save(fo);

        FlexOfferGeneratedEvent foGeneratedEvent =
                new FlexOfferGeneratedEvent(this, "FlexOffer generation request",
                        "", flexOffer, organization); //Create new event
        applicationEventPublisher.publishEvent(foGeneratedEvent); //Publish an event

        return flexOffer;
    }


    @Override
    public FlexOffer createOrganizationFlexOffer(Organization organization, List<OrganizationPrediction> organizationPrediction) {
        try {
            FlexOffer flexOffer = new FlexOffer();
            flexOffer.setId(UUID.randomUUID());
            flexOffer.setState(FlexOfferState.Initial);
            flexOffer.setStateReason("FlexOffer Initialized");

            //server datetime
            Date date = new Date();

            //flex-offer should be accepted within 15 mins
            Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
            calendar.setTime(date);

            int generateForInterval = 1;  //  1 means generate FO for the next interval

            flexOffer.setCreationTime(date);
            flexOffer.setOfferedById(organization.getOrganizationName());

            // For now we only set user location, later this attribute can handle multiple location
            Map<String, GeoLocation> location = new HashMap<>();
            location.put("userLocation", new GeoLocation());
            flexOffer.setLocationId(location);

            Date latestAcceptanceTime = new Date(organizationPrediction.get(0).getTimestamp() * interval);
            // Date latestAcceptanceTime = new Date(flexOffer.getCreationTime().getTime() + (generateForInterval) * interval);
            flexOffer.setAcceptanceBeforeTime(latestAcceptanceTime);
            flexOffer.setAssignmentBeforeTime(latestAcceptanceTime);
            flexOffer.setStartAfterTime(latestAcceptanceTime);
            flexOffer.setStartBeforeTime(latestAcceptanceTime);

            // The cost constraint is random value and is set by FMAN/Aggregator
            flexOffer.setTotalCostConstraint(new FlexOfferConstraint(0, 3));

            int numSlices = numOrgFOSlices;
            int sliceDuration = 1;

            FlexOfferSlice[] flexOfferProfileConstraints = new FlexOfferSlice[numSlices];
            for (int i = 0; i < flexOfferProfileConstraints.length; i++) {
                flexOfferProfileConstraints[i] = new FlexOfferSlice();
                flexOfferProfileConstraints[i].setMinDuration(sliceDuration);
                flexOfferProfileConstraints[i].setMaxDuration(sliceDuration);

                FlexOfferConstraint[] energyConstraintList = new FlexOfferConstraint[sliceDuration];
                for (int j = 0; j < energyConstraintList.length; j++) {

                    double nextSliceOrgPred = organizationPrediction.get(i).getPower()/1000D;
                    FlexOfferConstraint foc = new FlexOfferConstraint(nextSliceOrgPred, nextSliceOrgPred);

                    energyConstraintList[j] = new FlexOfferConstraint();
                    energyConstraintList[j].setLower(foc.getLower());
                    energyConstraintList[j].setUpper(foc.getUpper());
                }
                flexOfferProfileConstraints[i].setEnergyConstraintList(energyConstraintList);

                // Tariff price here is random value and is updated by FMAN
                flexOfferProfileConstraints[i].setTariffConstraint(new FlexOfferTariffConstraint(0.03, 0.15));
            }

            flexOffer.setFlexOfferProfileConstraints(flexOfferProfileConstraints);

            flexOffer.setTotalEnergyConstraint(flexOffer.getSumEnergyConstraints());

            Date scheduleStartTime = latestAcceptanceTime;
            TariffConstraintProfile tariffConstraintProfile = new TariffConstraintProfile();
            tariffConstraintProfile.setStartTime(scheduleStartTime);

            TariffSlice[] tariffConstraintSlices = new TariffSlice[numSlices];
            for (int i = 0; i < tariffConstraintSlices.length; i++) {
                tariffConstraintSlices[i] = new TariffSlice();
                tariffConstraintSlices[i] = new TariffSlice(1, 0.03, 0.15);
            }
            tariffConstraintProfile.setTariffSlices(tariffConstraintSlices);
            flexOffer.setFlexOfferTariffConstraint(tariffConstraintProfile);

            FlexOfferSchedule defaultSchedule = new FlexOfferSchedule();
            FlexOfferScheduleSlice[] flexOfferScheduleSlices = new FlexOfferScheduleSlice[numSlices];
            for (int i = 0; i < flexOfferScheduleSlices.length; i++) {
                flexOfferScheduleSlices[i] = new FlexOfferScheduleSlice();

                FlexOfferConstraint flexOfferConstraint =
                        flexOffer.getFlexOfferProfileConstraints()[i].getEnergyConstraint(0);

                flexOfferScheduleSlices[i] = new FlexOfferScheduleSlice(flexOfferConstraint.getUpper(), 1);
                flexOfferScheduleSlices[i].setDuration(sliceDuration);
            }
            defaultSchedule.setScheduleSlices(flexOfferScheduleSlices);
            defaultSchedule.setStartTime(latestAcceptanceTime);

            flexOffer.setDefaultSchedule(defaultSchedule);
            FlexOfferSchedule flexOfferSchedule = new FlexOfferSchedule(defaultSchedule);
            flexOfferSchedule.setScheduleId(12345); // random
            flexOfferSchedule.setUpdateId(1); // this should be incremented every time a new schedule is received

            flexOffer.setInitialFlexOfferSchedule(flexOfferSchedule);

            return flexOffer;
        } catch (Exception e) {
            LOGGER.error("Error creating onOffFlexOffer. Error message: {}", e.getLocalizedMessage());
            return null;
        }
    }

    private void sendFoTFOA(PoolFOWrapper poolFOWrapper){
        RestTemplate restTemplate = new RestTemplate();
        try {
            String _uri = this.foaUrl+"/organization/flexOffers";
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<PoolFOWrapper> postEntity = new HttpEntity<>(poolFOWrapper, headers);
            Object response = restTemplate.postForEntity(_uri, postEntity, String.class);
        } catch (Exception ex) {

        }
    }
    @Override
    public FlexOffer generatePoolFO(Organization organization,
                                    List<OrganizationPrediction> organizationPrediction,
                                    List<PoolDeviceModel> deviceModels, boolean isInCoolingPeriod) {
        List<String> flexDetails= getMinMaxEnergy(deviceModels, organization.getPoolDeviceCoolingPeriod());
        System.out.println(flexDetails.size());
        List<String> min_max = Arrays.asList(flexDetails.get(flexDetails.size() - 1).split(", "));
        double baseEnergy = Double.parseDouble(min_max.get(0));
        double flexEnergy = Double.parseDouble(min_max.get(1));
        if(isInCoolingPeriod){
            flexEnergy = 0;
        }
        flexDetails.remove(flexDetails.get(flexDetails.size() - 1));
        FlexOffer flexOffer = this.createPoolFlexOffer(organization, organizationPrediction,
                deviceModels, baseEnergy, flexEnergy);

        if (flexOffer == null) {
            LOGGER.warn("FO is null. Organization: {}", organization.getOrganizationName());
            return null;
        }
        PoolFOWrapper foWrapper = new PoolFOWrapper();
        foWrapper.setFoId(flexOffer.getId());
        foWrapper.setDeviceIds(flexDetails);
        foWrapper.setCoolingPeriod(isInCoolingPeriod);
        FlexOfferT fo =
                new FlexOfferT(organization.getOrganizationName(),
                        "",
                        new Date(), // store exact time
                        flexOffer.getFlexOfferSchedule().getStartTime(),
                        flexOffer.getState(), flexOffer.getId().toString(), flexOffer,
                        organization.getOrganizationId(), 1);


        // save FO to DB
        foaService.save(fo);
        //send FO details to FOA
        sendFoTFOA(foWrapper);
        FlexOfferGeneratedEvent foGeneratedEvent =
                new FlexOfferGeneratedEvent(this, "FlexOffer generation request",
                        "", flexOffer, organization); //Create new event
        applicationEventPublisher.publishEvent(foGeneratedEvent); //Publish an event

        return flexOffer;
    }


    @Override
    public FlexOffer createPoolFlexOffer(Organization organization,
                                         List<OrganizationPrediction> organizationPrediction,
                                         List<PoolDeviceModel> deviceModel, Double baseEnergy,
                                         Double flexEnergy) {
        try {
            FlexOffer flexOffer = new FlexOffer();
            flexOffer.setId(UUID.randomUUID());
            flexOffer.setState(FlexOfferState.Initial);
            flexOffer.setStateReason("FlexOffer Initialized");

            //server datetime
            Date date = new Date();

            //flex-offer should be accepted within 15 mins
            Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
            calendar.setTime(date);

            int generateForInterval = 1;  //  1 means generate FO for the next interval

            flexOffer.setCreationTime(date);
            flexOffer.setOfferedById(organization.getOrganizationName());

            // For now we only set user location, later this attribute can handle multiple location
            Map<String, GeoLocation> location = new HashMap<>();
            location.put("userLocation", new GeoLocation());
            flexOffer.setLocationId(location);

            //Date latestAcceptanceTime = new Date(organizationPrediction.get(0).getTimestamp() * interval)
            Date latestAcceptanceTime = new Date(flexOffer.getCreationTime().getTime() + (generateForInterval) * interval);
            flexOffer.setAcceptanceBeforeTime(latestAcceptanceTime);
            flexOffer.setAssignmentBeforeTime(latestAcceptanceTime);
            flexOffer.setStartAfterTime(latestAcceptanceTime);
            flexOffer.setStartBeforeTime(latestAcceptanceTime);

            // The cost constraint is random value and is set by FMAN/Aggregator
            flexOffer.setTotalCostConstraint(new FlexOfferConstraint(0, 3));

            int numSlices = numOrgFOSlices;
            int sliceDuration = 1;

            FlexOfferSlice[] flexOfferProfileConstraints = new FlexOfferSlice[numSlices];
            for (int i = 0; i < flexOfferProfileConstraints.length; i++) {
                flexOfferProfileConstraints[i] = new FlexOfferSlice();
                flexOfferProfileConstraints[i].setMinDuration(sliceDuration);
                flexOfferProfileConstraints[i].setMaxDuration(sliceDuration);

                FlexOfferConstraint[] energyConstraintList = new FlexOfferConstraint[sliceDuration];
                for (int j = 0; j < energyConstraintList.length; j++) {

                    //double nextSliceOrgPred = organizationPrediction.get(i).getPower()/1000D;
                    double lowerLimit = Math.max((baseEnergy-flexEnergy), 0.0); //.25 is tolerance
                    FlexOfferConstraint foc = new FlexOfferConstraint(Math.max((baseEnergy-flexEnergy), 0.0), baseEnergy);

                    energyConstraintList[j] = new FlexOfferConstraint();
                    energyConstraintList[j].setLower(foc.getLower());
                    energyConstraintList[j].setUpper(foc.getUpper());
                }
                flexOfferProfileConstraints[i].setEnergyConstraintList(energyConstraintList);

                // Tariff price here is random value and is updated by FMAN
                flexOfferProfileConstraints[i].setTariffConstraint(new FlexOfferTariffConstraint(0.03, 0.15));
            }

            flexOffer.setFlexOfferProfileConstraints(flexOfferProfileConstraints);

            flexOffer.setTotalEnergyConstraint(flexOffer.getSumEnergyConstraints());

            Date scheduleStartTime = latestAcceptanceTime;
            TariffConstraintProfile tariffConstraintProfile = new TariffConstraintProfile();
            tariffConstraintProfile.setStartTime(scheduleStartTime);

            TariffSlice[] tariffConstraintSlices = new TariffSlice[numSlices];
            for (int i = 0; i < tariffConstraintSlices.length; i++) {
                tariffConstraintSlices[i] = new TariffSlice();
                tariffConstraintSlices[i] = new TariffSlice(1, 0.03, 0.15);
            }
            tariffConstraintProfile.setTariffSlices(tariffConstraintSlices);
            flexOffer.setFlexOfferTariffConstraint(tariffConstraintProfile);

            FlexOfferSchedule defaultSchedule = new FlexOfferSchedule();
            FlexOfferScheduleSlice[] flexOfferScheduleSlices = new FlexOfferScheduleSlice[numSlices];
            for (int i = 0; i < flexOfferScheduleSlices.length; i++) {
                flexOfferScheduleSlices[i] = new FlexOfferScheduleSlice();

                FlexOfferConstraint flexOfferConstraint =
                        flexOffer.getFlexOfferProfileConstraints()[i].getEnergyConstraint(0);

                flexOfferScheduleSlices[i] = new FlexOfferScheduleSlice(flexOfferConstraint.getUpper(), 1);
                flexOfferScheduleSlices[i].setDuration(sliceDuration);
            }
            defaultSchedule.setScheduleSlices(flexOfferScheduleSlices);
            defaultSchedule.setStartTime(latestAcceptanceTime);

            flexOffer.setDefaultSchedule(defaultSchedule);
            FlexOfferSchedule flexOfferSchedule = new FlexOfferSchedule(defaultSchedule);
            flexOfferSchedule.setScheduleId(12345); // random
            flexOfferSchedule.setUpdateId(1); // this should be incremented every time a new schedule is received

            flexOffer.setInitialFlexOfferSchedule(flexOfferSchedule);

            return flexOffer;
        } catch (Exception e) {
            LOGGER.error("Error creating onOffFlexOffer. Error message: {}", e.getLocalizedMessage());
            return null;
        }
    }

    private List<String> getMinMaxEnergy(List<PoolDeviceModel> deviceModels, double deviceCoolingPeriod) {
        List<String> flexDevices = new ArrayList<>();
        double baseLoad = 0.0;
        double flexLoad = 0.0;
        List<Double> flexDemands = new ArrayList<>();
        int maxDeviceToInclude = 100;
        int i = 0;
        for(PoolDeviceModel poolDeviceModel :deviceModels) {
            Date dt = new Date();
            long timeDiff = 61;
            if(poolDeviceModel.getLastIncludedInPool() != null){
                timeDiff = (dt.getTime() - poolDeviceModel.getLastIncludedInPool().getTime())/60000;
            }

            //TODO: See how to implement this
            deviceCoolingPeriod = 0; // now it's minutes not hours
            if(poolDeviceModel.getCurrentState() == 1 && timeDiff > deviceCoolingPeriod &&
                    poolDeviceModel.getCurrentPower() > 300 && i <= maxDeviceToInclude){
                //flexLoad =  flexLoad + poolDeviceModel.getCurrentPower();
                flexDemands.add(poolDeviceModel.getCurrentPower());
                baseLoad =  baseLoad + poolDeviceModel.getCurrentPower();
                flexDevices.add(poolDeviceModel.getDeviceId());
                i += 1;
            }else if (poolDeviceModel.getCurrentPower() != -1){
                baseLoad =  baseLoad + poolDeviceModel.getCurrentPower();
            }
        }
        //only use 80% of the demand from currently flexible load as flexible demand
        //int flexLoadSize =  (int) Math.floor(flexDemands.size()*0.8);
        int flexLoadSize =  (int) Math.floor(flexDemands.size()*1.0);
        //for(int j=1;j<=flexLoadSize;j++){
        for (int j = 0; j < flexLoadSize; j++) {
            flexLoad = flexLoad + flexDemands.get(j);
        }

        baseLoad = baseLoad/4000;
        flexLoad = flexLoad/4000;
        flexDevices.add(baseLoad + ", "+ flexLoad);
        return flexDevices;
    }
}
