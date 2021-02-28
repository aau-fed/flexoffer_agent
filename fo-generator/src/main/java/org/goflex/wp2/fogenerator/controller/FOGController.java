package org.goflex.wp2.fogenerator.controller;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.DeviceFlexibilityDetail;
import org.goflex.wp2.core.models.Organization;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.foa.events.DeviceStateChangeEvent;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.FOAService;
import org.goflex.wp2.fogenerator.event.NonTCLEVFOGeneration;
import org.goflex.wp2.fogenerator.event.NonTCLWetDeviceFOGeneration;
import org.goflex.wp2.fogenerator.event.TCLFOGeneration;
import org.goflex.wp2.fogenerator.interfaces.FlexOfferGenerator;
import org.goflex.wp2.fogenerator.interfaces.impl.NonTCLWetDeviceForecastService;
import org.goflex.wp2.fogenerator.services.FOGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/fog")
public class FOGController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FOGController.class);
    @Resource(name = "deviceGeneratedFOCount")
    ConcurrentHashMap<String, Map<String, List<Double>>> deviceGeneratedFOCount = new ConcurrentHashMap<>();
    @Resource(name = "startGeneratingFo")
    private ConcurrentHashMap<String, Integer> startGeneratingFo;

    private final FlexOfferGenerator foGenerator;
    private final FOGenerationService generatorService;
    private final FOAService foaService;
    private final DeviceDetailService deviceDetailService;
    private final OrganizationRepository organizationRepository;
    private final DeviceFlexOfferGroup deviceFlexOfferGroup;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final NonTCLWetDeviceForecastService nonTCLWetDeviceForecastService;


    @Autowired
    public FOGController(FlexOfferGenerator foGenerator,
                         FOGenerationService generatorService, FOAService foaService,
                         DeviceDetailService deviceDetailService, OrganizationRepository organizationRepository,
                         DeviceFlexOfferGroup deviceFlexOfferGroup, ApplicationEventPublisher applicationEventPublisher,
                         NonTCLWetDeviceForecastService nonTCLWetDeviceForecastService) {
        this.foGenerator = foGenerator;
        this.generatorService = generatorService;
        this.foaService = foaService;
        this.deviceDetailService = deviceDetailService;
        this.organizationRepository = organizationRepository;
        this.deviceFlexOfferGroup = deviceFlexOfferGroup;
        this.applicationEventPublisher = applicationEventPublisher;
        this.nonTCLWetDeviceForecastService = nonTCLWetDeviceForecastService;
    }

    /*Test Generate FO*/
    @RequestMapping(value = ControllerConstant.Generate_FO_Type1, method = RequestMethod.POST)
    public ResponseEntity<String> generateFO() {
        foGenerator.generateRandomFO();
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    /*Test Generate FO*/
    @RequestMapping(value = ControllerConstant.Generate_FO_Type2, method = RequestMethod.POST)
    public ResponseEntity<String> generateFOs() {
        foGenerator.generateRandomFO();
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    @RequestMapping(value = "getFOGStatus", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getFOGStatus() {
        ResponseMessage responseMessage = new ResponseMessage();
        if (startGeneratingFo.containsKey("start")) {
            responseMessage.setStatus(HttpStatus.OK);
            responseMessage.setData(startGeneratingFo);
            responseMessage.setMessage("Success");
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } else {
            responseMessage.setStatus(HttpStatus.BAD_REQUEST);
            responseMessage.setMessage("Error");
            return new ResponseEntity<>(responseMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }


    @RequestMapping(value = ControllerConstant.Start_Generator, method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> startFogeneration() {
        ResponseMessage responseMessage = new ResponseMessage();

        if (!startGeneratingFo.containsKey("start")) {
            responseMessage.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            responseMessage.setMessage("Error determining FOG status");
            return new ResponseEntity<>(responseMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (startGeneratingFo.get("start") == 1) {
            responseMessage.setStatus(HttpStatus.OK);
            responseMessage.setMessage("FOG already running");
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } else {
            generatorService.populateFOCount();
            startGeneratingFo.put("start", 1);
            responseMessage.setMessage("FOG started");
            responseMessage.setStatus(HttpStatus.OK);
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        }
    }

    @RequestMapping(value = ControllerConstant.Stop_Generator, method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> stopFoGeneration() {
        ResponseMessage responseMessage = new ResponseMessage();

        if (!startGeneratingFo.containsKey("start")) {
            responseMessage.setStatus(HttpStatus.UNPROCESSABLE_ENTITY);
            responseMessage.setMessage("Error determining FOG status");
            return new ResponseEntity<>(responseMessage, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (startGeneratingFo.get("start") == 0) {
            responseMessage.setStatus(HttpStatus.OK);
            responseMessage.setMessage("FOG already stopped");
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } else {
            deviceGeneratedFOCount.clear();
            startGeneratingFo.put("start", 0);
            responseMessage.setMessage("FOG stopped");
            responseMessage.setStatus(HttpStatus.OK);
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/getAllFlexoffer", method = RequestMethod.GET)
    public ResponseEntity<List<FlexOffer>> retrieveActiveFlexOffers() {
        List<Object> params = new ArrayList<Object>();
        params.add(FlexOfferState.Initial);
        return new ResponseEntity<>(foaService.getFlexOfferByStatus(params), HttpStatus.OK);
    }

    @PostMapping(value = "generateDeviceFO/{deviceId}/{organizationName}")
    public ResponseEntity<ResponseMessage> generateDeviceFO(@PathVariable(value = "deviceId") String deviceId,
                                                            @PathVariable(value = "organizationName")
                                                                    String organizationName) {

        ResponseMessage responseMessage = new ResponseMessage();
        try {
            String msg;
            HttpStatus httpStatus;
            if (startGeneratingFo.get("start") == 0) {
                msg = "FO Generator is disabled";
                httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
                LOGGER.warn(msg);
                responseMessage.setMessage(msg);
                responseMessage.setStatus(httpStatus);
                return new ResponseEntity<>(responseMessage, httpStatus);
            }

            if (deviceId == null) {
                msg = "deviceId can not be null";
                httpStatus = HttpStatus.BAD_REQUEST;
                LOGGER.warn(msg);
                responseMessage.setMessage(msg);
                responseMessage.setStatus(httpStatus);
                return new ResponseEntity<>(responseMessage, httpStatus);
            }

            if (!this.shouldGenerateFO(deviceId, null)) {
                msg = String.format("shouldGenerateFO() returned false for device: %s", deviceId);
                httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
                LOGGER.warn(msg);
                responseMessage.setMessage(msg);
                responseMessage.setStatus(httpStatus);
                return new ResponseEntity<>(responseMessage, httpStatus);
            }

            DeviceDetail deviceDetail = deviceDetailService.getDevice(deviceId);
            if (deviceDetail.getDeviceState() == DeviceState.Disconnected ||
                    deviceDetail.getDeviceState() == DeviceState.Unknown) {
                msg = String.format("device: %s has incorrect state %s", deviceId,
                        deviceDetail.getDeviceState().name());
                httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
                LOGGER.warn(msg);
                responseMessage.setMessage(msg);
                responseMessage.setStatus(httpStatus);
                return new ResponseEntity<>(responseMessage, httpStatus);
            }

            httpStatus = HttpStatus.OK;
            Organization organization = organizationRepository.findByOrganizationName(organizationName);
            FlexibilityGroupType flexibilityGroupType =
                    deviceFlexOfferGroup.getDeviceFOGroupType(deviceDetail.getDeviceType());
            String eventName = "No flex offer generation";
            if (flexibilityGroupType == FlexibilityGroupType.WetLoad) {
                eventName = "NonTCL wet device FlexOffer generation";
                NonTCLWetDeviceFOGeneration foGeneratedEvent =
                        new NonTCLWetDeviceFOGeneration(this, eventName, "", deviceDetail, organization);
                applicationEventPublisher.publishEvent(foGeneratedEvent);
            } else if (flexibilityGroupType == FlexibilityGroupType.ThermostaticControlLoad) {
                eventName = "TCL device FlexOffer generation";
                TCLFOGeneration foGeneratedEvent = new TCLFOGeneration(this, eventName, "", deviceDetail, organization);
                applicationEventPublisher.publishEvent(foGeneratedEvent);
            } else if (flexibilityGroupType == FlexibilityGroupType.BatterySystem) {
                eventName = "NonTCL EV device FlexOffer generation";
                NonTCLEVFOGeneration foGeneratedEvent =
                        new NonTCLEVFOGeneration(this, eventName, "", deviceDetail, organization);
                applicationEventPublisher.publishEvent(foGeneratedEvent);
            } else {
                httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
            }

            msg = String.format("%s for deviceId: %s and type: %s", eventName, deviceId, deviceDetail.getDeviceType());
            LOGGER.info(msg);
            responseMessage.setMessage(msg);
            responseMessage.setStatus(httpStatus);
            return new ResponseEntity<>(responseMessage, httpStatus);
        } catch (Exception ex) {
            responseMessage
                    .setMessage(String.format("Error generating flex offer for deviceId: %s", deviceId));
            responseMessage.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/shouldGenerateDeviceFO/{deviceId}", method = RequestMethod.POST)
    public ResponseEntity<Boolean> shouldGenerateDeviceFO(@PathVariable(value = "deviceId") String deviceId) {
        try {

            if (startGeneratingFo.get("start") == 0) {
                LOGGER.warn("FO Generator is disabled");
                return new ResponseEntity<>(false, HttpStatus.UNPROCESSABLE_ENTITY);
            }

            Boolean res = this.shouldGenerateFO(deviceId, null);
            return res == true ? new ResponseEntity<>(res, HttpStatus.OK) :
                    new ResponseEntity<>(res, HttpStatus.CONFLICT);
        } catch (Exception ex) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/willgeneratefo/{deviceId}/{date}", method = RequestMethod.POST)
    public ResponseEntity<Boolean> willGenerateFO(@PathVariable(value = "deviceId") String deviceId,
                                                  @PathVariable(value = "date") String date,
                                                  @RequestBody DeviceFlexibilityDetail deviceFlexibilityDetail) {
        return new ResponseEntity<>(this.shouldGenerateFO(deviceId, date), HttpStatus.OK);
    }

    private Boolean shouldGenerateFO(String deviceId, String currentDate) {
        Date dt = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        double currentHour = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) * 1.0 / 60.0;

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        cal.add(Calendar.DATE, -1);
        if (currentDate == null || currentDate.equals("")) {
            currentDate = format.format(dt);
        }
        return foGenerator.shouldGenerateFO(currentDate, format.format(cal.getTime()), currentHour,
                deviceDetailService.getDevice(deviceId));
    }

    @PostMapping(value = "/loglevel/{loglevel}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity setLoggingLevel(@PathVariable(value = "loglevel") String loglevel) {
        String msg;
        String packageName = "org.goflex.wp2";
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        if (loglevel.equalsIgnoreCase("ALL")) {
            loggerContext.getLogger(packageName).setLevel(Level.ALL);
            msg = "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("TRACE")) {
            loggerContext.getLogger(packageName).setLevel(Level.TRACE);
            msg = "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("DEBUG")) {
            loggerContext.getLogger(packageName).setLevel(Level.DEBUG);
            msg = "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("INFO")) {
            loggerContext.getLogger(packageName).setLevel(Level.INFO);
            msg = "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("WARN")) {
            loggerContext.getLogger(packageName).setLevel(Level.WARN);
            msg = "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("ERROR")) {
            loggerContext.getLogger(packageName).setLevel(Level.ERROR);
            msg = "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("OFF")) {
            loggerContext.getLogger(packageName).setLevel(Level.OFF);
            msg = "Logging level successfully set to " + loglevel;
        } else {
            msg = "Error, not a known loglevel: " + loglevel;
        }

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage(msg);
        responseMessage.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/testMqttPublisher/{deviceId}/{state}", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> testMqttPublisher(@PathVariable(value = "deviceId") String deviceId,
                                                     @PathVariable(value = "state") String state) {
        ResponseMessage responseMessage = new ResponseMessage();
        try {
            DeviceDetail deviceDetail = this.deviceDetailService.getDevice(deviceId);
            boolean newState = state.equals("1");
            DeviceStateChangeEvent stateChangeEvent = new DeviceStateChangeEvent(
                    this, "testing mqtt publisher", deviceDetail, newState);
            this.applicationEventPublisher.publishEvent(stateChangeEvent);
            responseMessage.setMessage("Published mqtt command");
            responseMessage.setStatus(HttpStatus.OK);
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getLocalizedMessage());
            responseMessage.setMessage("Error publishing mqtt command");
            responseMessage.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
