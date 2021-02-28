package org.goflex.wp2.fogenerator.services;

import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.core.models.*;
import org.goflex.wp2.core.repository.DeviceRepository;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.core.repository.UserRepository;
import org.goflex.wp2.foa.implementation.FOAServiceImpl;
import org.goflex.wp2.foa.prediction.OrganizationPrediction;
import org.goflex.wp2.foa.prediction.OrganizationPredictionRepository;
import org.goflex.wp2.fogenerator.event.*;
import org.goflex.wp2.fogenerator.interfaces.FlexOfferGenerator;
import org.goflex.wp2.fogenerator.interfaces.impl.SwissDeviceModelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FOGenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FOGenerationService.class);

    private final double coolingperiod = 2;

    private Date lastFOStartTime = null;

    @Resource(name = "deviceGeneratedFOCount")
    ConcurrentHashMap<String, Map<String, List<Double>>> deviceGeneratedFOCount = new ConcurrentHashMap<>();

    @Resource(name = "startGeneratingFo")
    private ConcurrentHashMap<String, Integer> startGeneratingFo;

    @Value("1")
    private int numOrgFOSlices;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final FlexOfferGenerator foGenerator;
    private final OrganizationRepository organizationRepository;
    private final DeviceFlexOfferGroup deviceFlexOfferGroup;
    private final FOAServiceImpl foaService;
    private final OrganizationPredictionRepository organizationPredictionRepository;

    private Date dt;
    private String currentDate;
    private final Calendar cal = Calendar.getInstance();
    private double currentHour = 0.0;

    //@Autowired
    //private RestTemplate restTemplate;

    @Value(value = "${foa.status.url}")
    private String foaUrl;

    @Autowired
    public FOGenerationService(
            ApplicationEventPublisher applicationEventPublisher, UserRepository userRepository,
            DeviceRepository deviceRepository, FlexOfferGenerator foGenerator,
            OrganizationRepository organizationRepository, DeviceFlexOfferGroup deviceFlexOfferGroup,
            FOAServiceImpl foaService, OrganizationPredictionRepository organizationPredictionRepository) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.foGenerator = foGenerator;
        this.organizationRepository = organizationRepository;
        this.deviceFlexOfferGroup = deviceFlexOfferGroup;
        this.foaService = foaService;
        this.organizationPredictionRepository = organizationPredictionRepository;
    }

    private List<DeviceDetail> getDevices(Long userId) {

        if (userId != null) {
            return deviceRepository.findByUserIdAndFlexible(userId);
        } else {
            return deviceRepository.findAll();
        }
    }

    @Async
    public void checkAndExecuteGenerator(DeviceDetail deviceDetail, Organization organization) {
        try {

            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            FlexibilityGroupType flexibilityGroupType =
                    deviceFlexOfferGroup.getDeviceFOGroupType(deviceDetail.getDeviceType());

            // check if we can generate more FO for device
            if (foGenerator.shouldGenerateFO(currentDate, format.format(cal.getTime()), currentHour, deviceDetail)) {

                // Generate FO for currently active or idle device
                if (deviceDetail.getDeviceState() != DeviceState.Disconnected &&
                        deviceDetail.getDeviceState() != DeviceState.Unknown) {

                    if (flexibilityGroupType == FlexibilityGroupType.Production) {
                        //Generate FO for production load type, call production  fo generator event
                        ProductionFOGeneration foGeneratedEvent = new ProductionFOGeneration(
                                this, "Production device FlexOffer generation",
                                "", deviceDetail, organization);
                        applicationEventPublisher.publishEvent(foGeneratedEvent);
                    }

                    // FOs for the rest of devices types are generated using http call
                    /*
                    // Generate FO for wet device, call wet fo generator event
                    if (flexibilityGroupType == FlexibilityGroupType.WetLoad) {
                        // commented because wet device FO is generated using http call
                        //NonTCLWetDeviceFOGeneration foGeneratedEvent = new NonTCLWetDeviceFOGeneration(
                        //        this, "NonTCL wet device FlexOffer generation",
                        //        "", deviceDetail, organization);
                        //applicationEventPublisher.publishEvent(foGeneratedEvent);
                    } else if (flexibilityGroupType == FlexibilityGroupType.ThermostaticControlLoad) {
                        // Generate FO for thermostat load type, call thermostat fo generator event
                        TCLFOGeneration foGeneratedEvent = new TCLFOGeneration(
                                this, "TCL device FlexOffer generation",
                                "", deviceDetail, organization);
                        applicationEventPublisher.publishEvent(foGeneratedEvent);
                    } else if (flexibilityGroupType == FlexibilityGroupType.BatterySystem) {
                        //Generate FO for EV/Battery load type, call EV/Battery  fo generator event
                        NonTCLEVFOGeneration foGeneratedEvent = new NonTCLEVFOGeneration(
                                this, "NonTCL EV device FlexOffer generation",
                                "", deviceDetail, organization);
                        applicationEventPublisher.publishEvent(foGeneratedEvent);
                    } else if (flexibilityGroupType == FlexibilityGroupType.Production) {
                        //Generate FO for production load type, call production  fo generator event
                        ProductionFOGeneration foGeneratedEvent = new ProductionFOGeneration(
                                this, "Production device FlexOffer generation",
                                "", deviceDetail, organization);
                        applicationEventPublisher.publishEvent(foGeneratedEvent);
                    }

                     */
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Uncaught Exception Occurred. Detail: {}", ex.getMessage(), ex);
        }
    }


    private void processFOGForOrganization(Organization organization) {
        LOGGER.debug("Processing FOG for organization: {}", organization.getOrganizationName());
        userRepository.findAllByOrganizationIdAndEnabled(organization.getOrganizationId(), true)
                .forEach(user -> processFOGForEachUser(organization, user));
    }

    private void processFOGForEachUser(Organization organization, UserT user) {
        LOGGER.debug("Processing FOG for user: {} of organization: {}", user.getUserName(),
                organization.getOrganizationName());
        this.getDevices(user.getId())
                .stream()
                .filter(deviceDetail -> deviceFlexOfferGroup.getDeviceFOGroupType(deviceDetail.getDeviceType()) ==
                        FlexibilityGroupType.Production)
                .forEach(deviceDetail -> this.checkAndExecuteGenerator(deviceDetail, organization));
    }

    //@Scheduled(fixedRate = 300000)
    //@Scheduled(fixedRate = 60000)
    public void generateFOsForNextInterval() {

        if (startGeneratingFo.get("start") == 0) {
            LOGGER.warn("FO Generator is disabled");
            return;
        }

        // Set date time parameters
        this.dt = new Date();
        this.cal.setTime(this.dt);
        this.currentHour = this.cal.get(Calendar.HOUR_OF_DAY) + this.cal.get(Calendar.MINUTE) * 1.0 / 60.0;

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        currentDate = format.format(this.dt);
        //get previous date
        this.cal.add(Calendar.DATE, -1);


        LOGGER.info("Executing FO-Generation module");
        organizationRepository.findAll().forEach(this::processFOGForOrganization);
        LOGGER.debug("Flex-Offer loop completed");
    }


    public void populateFOCount() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        Date today = new Date();
        try {
            //today = formatter.parse("2018-06-29");
            today = formatter.parse(formatter.format(today.getTime()));
        } catch (Exception ex) {
            LOGGER.error("Date formatter error: {}", ex.getLocalizedMessage());
        }

        DateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy");
        String dt = "";
        try {
            dt = formatter1.format(today.getTime());
        } catch (Exception ex) {
            LOGGER.error("Date formatter error: {}", ex.getLocalizedMessage());
        }

        Calendar c = Calendar.getInstance();
        for (FlexOfferT fo : foaService.getFlexOfferTForDate(today)) {
            String plugID = fo.getFlexoffer().getOfferedById();
            //c.setTime(fo.getCreationTime());
            c.setTime(FlexOffer.toAbsoluteTime(FlexOffer.toFlexOfferTime(fo.getCreationTime())));
            Double creationTime = c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) * 1.0 / 60;
            if (deviceGeneratedFOCount.containsKey(dt)) {
                if (deviceGeneratedFOCount.get(dt).containsKey(plugID)) {
                    if (deviceGeneratedFOCount.get(dt).get(plugID) == null) {
                        List<Double> generatedTime = new ArrayList<>();
                        generatedTime.add(creationTime);
                        deviceGeneratedFOCount.get(dt).put(plugID, generatedTime);
                    } else {
                        deviceGeneratedFOCount.get(dt).get(plugID).add(creationTime);
                    }
                } else {
                    List<Double> generatedTime = new ArrayList<>();
                    generatedTime.add(creationTime);
                    deviceGeneratedFOCount.get(dt).put(plugID, generatedTime);
                }
            } else {
                Map<String, List<Double>> val = new HashMap<>();
                List<Double> generatedTime = new ArrayList<>();
                generatedTime.add(creationTime);
                val.put(plugID, generatedTime);
                deviceGeneratedFOCount.put(dt, val);
            }
        }
    }

    // second, minute, hour, day of month, month, day of week.
    //e.g. "0 * * * * MON-FRI" means once per minute on weekdays
    //@Scheduled(fixedRate = 60000)
    @Scheduled(cron = "0 12,27,42,57 * * * *")
    public void runOrganizationFOGenerationRoutine() {

        LOGGER.info("Generating organization level FOs for each organization");
        organizationRepository.findAll()
                .stream().filter(o -> o.getDirectControlMode() != OrganizationLoadControlState.Stopped)
                .forEach(this::processForOrganization);
        LOGGER.info("Completed generating organization level FOs for each organization");
    }

    private void processForOrganization(Organization organization) {
        int numSlices = numOrgFOSlices;
        boolean generateFO = false;
        if(lastFOStartTime == null){
            lastFOStartTime = new Date();
        }else{
            Date currentTime = new Date();
            double diff = (currentTime.getTime() - lastFOStartTime.getTime())/(60000*60.0);
            if(diff > (coolingperiod + .5)){
                LOGGER.info("Cooling period completed, start time set to current time");
                lastFOStartTime = new Date();
            }else if(diff > coolingperiod){
                LOGGER.info("FOG is now in cooling period");
                generateFO = true;
            }
        }
        this.generateOrganizationalFlexOfferForNextXSlices(organization, numSlices, generateFO);
    }

    private List<PoolDeviceModel> getDeviceModelStateMap(String orgName) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            String _uri = this.foaUrl+"server/getModelStateMap/" + orgName;
            List<PoolDeviceModel> poolDeviceModels = restTemplate.getForObject(_uri, SwissDeviceModelDTO.class).getDeviceModels();
           return poolDeviceModels;
        } catch (Exception ex) {
            return null;
        }
    }

    private void generateOrganizationalFlexOfferForNextXSlices(Organization organization, int numSlices,
                                                               boolean inCoolingPeriod) {

        long timestampFrom = FlexOffer.toFlexOfferTime(new Date())+1;
        long timestampTo = timestampFrom+numSlices;
        List<OrganizationPrediction> organizationPredictions = organizationPredictionRepository
                .findByOrganizationIdAndInterval(organization.getOrganizationId(), timestampFrom, timestampTo);
        if(organization.isPoolBasedControl()) {
            List<PoolDeviceModel> deviceModels = this.getDeviceModelStateMap(organization.getOrganizationName());
            if (organizationPredictions.size() == numSlices || 1==1) {
                LOGGER.info(organizationPredictions.toString());
                OrganizationFOGenerationEventPool oFOGE = new OrganizationFOGenerationEventPool(
                        this, "Organizational level FO for next slice", "",
                        organizationPredictions, organization, deviceModels, inCoolingPeriod);
                this.applicationEventPublisher.publishEvent(oFOGE);
            }

        } else {
            /*
            Object deviceModelState = this.getDeviceModelStateMap();
            if (organizationPredictions.size() == numSlices) {
                LOGGER.info(organizationPredictions.toString());
                OrganizationFOGenerationEvent organizationFOGenerationEvent = new OrganizationFOGenerationEvent(
                        this, "Organizational level FO for next slice", "",
                        organizationPredictions, organization);
                this.applicationEventPublisher.publishEvent(organizationFOGenerationEvent);
            }
             */
        }
    }
}
