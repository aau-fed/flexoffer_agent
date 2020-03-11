package org.goflex.wp2.sysmonitor;

import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.Organization;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.foa.controldetailmonitoring.ControlDetail;
import org.goflex.wp2.foa.controldetailmonitoring.ControlDetailService;
import org.goflex.wp2.foa.devicestate.DeviceCurrentState;
import org.goflex.wp2.foa.devicestate.DeviceStateHistory;
import org.goflex.wp2.foa.devicestate.DeviceStateService;
import org.goflex.wp2.foa.events.DeviceStateChangeEvent;
import org.goflex.wp2.foa.implementation.ImplementationsHandler;
import org.goflex.wp2.foa.implementation.TwilioSmsService;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.goflex.wp2.foa.service.EmailService;
import org.goflex.wp2.foa.util.TimeZoneUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author muhaftab
 * created: 4/23/19
 */

@Service
public class SystemMonitorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemMonitorService.class);
    private static final long DEVICE_ALERT_INTERVAL = 60; // minutes
    private static final long FOA_ALERT_INTERVAL = 5; // minutes

    private OrganizationRepository organizationRepository;
    private UserService userService;
    private DeviceDetailService deviceDetailService;
    private DeviceStateService deviceStateService;
    private TwilioSmsService smsService;
    private EmailService emailService;
    private ControlDetailService controlDetailService;
    private ApplicationEventPublisher applicationEventPublisher;
    private DeviceFlexOfferGroup deviceFlexOfferGroup;

    @Autowired
    private RestTemplate restTemplate;

    @Value(value = "${foa.status.url}")
    private String foaUrl;

    private boolean foaAlive;

    private Map<String, Date> lastAlertTime = new HashMap<>();

    @Autowired
    public SystemMonitorService(
            OrganizationRepository organizationRepository,
            UserService userService,
            DeviceDetailService deviceDetailService,
            DeviceStateService deviceStateService,
            TwilioSmsService twilioSmsService,
            EmailService emailService,
            ControlDetailService controlDetailService,
            ApplicationEventPublisher applicationEventPublisher,
            DeviceFlexOfferGroup deviceFlexOfferGroup
    ) {
        this.organizationRepository = organizationRepository;
        this.userService = userService;
        this.deviceDetailService = deviceDetailService;
        this.deviceStateService = deviceStateService;
        this.smsService = twilioSmsService;
        this.emailService = emailService;
        this.controlDetailService = controlDetailService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.deviceFlexOfferGroup = deviceFlexOfferGroup;
    }

    @Scheduled(fixedRate = 60000)
    public void checkDevices() {
        LOGGER.info("running periodic health check and taking necessary action...");

        this.foaAlive = this.isFoaAlive();
        if (!this.foaAlive) {
            this.sendFOAAlert();
            return;
        }

        // FOA down. put all devices into their default state for all organizations with direct control mode = Active
        organizationRepository.findAll().forEach(this::processOrganization);

    }

    private boolean isFoaAlive() {
        try {

            ResponseEntity<ResponseMessage> response = this.restTemplate.exchange(this.foaUrl, HttpMethod.GET, null, ResponseMessage.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody().getMessage().equals("ok")) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    private void processOrganization(Organization organization) {
        LOGGER.info("Processing organization: {}", organization.getOrganizationName());
        userService.getActiveUsersForOrganization(organization.getOrganizationId())
                .forEach(user -> this.processUser(user, organization));
    }

    private void processUser(UserT user, Organization organization) {
        LOGGER.debug("Processing user: {}", user.getUserName());
        deviceDetailService.getActiveDeviceByUser(user.getId())
                .forEach(deviceDetail -> this.processDevice(user, organization, deviceDetail));
    }

    private void processDevice(UserT user, Organization organization, DeviceDetail device) {
        LOGGER.debug("Processing device: {}", device.getAlias());

        // this block of code deals with current on/off state only for flexible devices with control signal enabled
        DeviceState deviceState = device.getDeviceState();
        if (organization.getDirectControlMode() == OrganizationLoadControlState.Active
                //&& (deviceState == DeviceState.Operating || deviceState == DeviceState.Idle)
                && device.isFlexible()) {
            boolean state = deviceState == DeviceState.Operating;
            if (this.deviceStateService.getDeviceCurrentState(device.getDeviceId()) == null) {
                this.deviceStateService.storeDeviceCurrentState(device.getDeviceId(), state);
            } else {
                // see if device state history needs updating
                this.deviceStateService.storeDeviceCurrentState(device.getDeviceId(), state);

                // if foa is down or if device is not in default state for more than x hours, then put in default state
                if (!this.deviceStateService.deviceOperatingNormally(device)) {
                    this.sendDeviceAlert(user, device);
                }
            }
        }

        // store all state changes events regardless of device flexibility status or org control status
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00");
            Date historyDate = format.parse(format.format(new Date()));
            DeviceStateHistory deviceStateHistory =
                    this.deviceStateService.getLatestDeviceHistoryByDate(device.getDeviceId(), historyDate);
            if (deviceStateHistory == null || deviceStateHistory.getDeviceState() != deviceState) {
                this.deviceStateService.storeDeviceStateHistory(device.getDeviceId(), deviceState);
            }
        } catch (Exception e) {
            LOGGER.error("Invalid Date format, supported type is \"yyyy-MM-dd'T'HH:mm:ss\"");
        }
    }

    private void sendFOAAlert() {
        try {
            long minutesSinceLastAlert;
            if (this.lastAlertTime.containsKey("FOA")) {
                minutesSinceLastAlert = (new Date().getTime() - this.lastAlertTime.get("FOA").getTime()) / 60000;
            } else {
                minutesSinceLastAlert = FOA_ALERT_INTERVAL;
                this.lastAlertTime.put("FOA", new Date());
            }

            if (minutesSinceLastAlert < FOA_ALERT_INTERVAL) {
                return;
            }

            String content = "FOA is currently down. All pending device schedules will not execute unless FOA is back online.";
            smsService.sendSms("admin", content); // for now send all alerts to admin user
            emailService.sendSimpleMessage("muhaftabkhan@gmail.com", "Critical FOA Alert", content);
            emailService.sendSimpleMessage("justnilotpal@gmail.com", "Critical FOA Alert", content);

            this.lastAlertTime.put("FOA", new Date());
        } catch (Exception ex) {
            LOGGER.error("Error sending FOA alert");
            LOGGER.error(ex.getLocalizedMessage());
        }
    }

    private void sendDeviceAlert(UserT user, DeviceDetail device) {
        try {
            // don't send alert if device is set to be off by default
            if (device.getDefaultState() == 0) {
                return;
            }

            long minutesSinceLastAlert;
            if (this.lastAlertTime.containsKey(device.getDeviceId())) {
                minutesSinceLastAlert = (new Date().getTime() - this.lastAlertTime.get(device.getDeviceId()).getTime()) / 60000;
            } else {
                minutesSinceLastAlert = DEVICE_ALERT_INTERVAL;
                this.lastAlertTime.put(device.getDeviceId(), new Date());
            }

            if (minutesSinceLastAlert < DEVICE_ALERT_INTERVAL) {
                return;
            }

            DeviceCurrentState deviceCurrentState = deviceStateService.getDeviceCurrentState(device.getDeviceId());
            Date dt = deviceCurrentState.getTimestamp();
            String stringDt = TimeZoneUtil.getTimeZoneAdjustedStringDateTime(dt,
                    organizationRepository.findByOrganizationId(user.getOrganizationId()).getOrganizationName());

            // get time of the last OFF command
            String stringDtLastOffControl = "null";
            long hoursSinceLastOffCmd = 0;
            ControlDetail controlDetailOff = controlDetailService.getLastOffControlEventByDevice(device.getDeviceId());
            if (controlDetailOff != null) {
                stringDtLastOffControl = TimeZoneUtil.getTimeZoneAdjustedStringDateTime(controlDetailOff.getControlDatetime(),
                        organizationRepository.findByOrganizationId(user.getOrganizationId()).getOrganizationName());
                hoursSinceLastOffCmd = (new Date().getTime() - controlDetailOff.getControlDatetime().getTime()) / (1000 * 60 * 60);
            }

            if (stringDtLastOffControl.equals("null") ||
                    (hoursSinceLastOffCmd > 24 && device.getDeviceState() != DeviceState.Idle)) {
                return;
            }


            FlexibilityGroupType flexibilityGroupType =
                    deviceFlexOfferGroup.getDeviceFOGroupType(device.getDeviceType());

            String subject = String.format("[Action Required] %s, %s, Device State Alert",
                    organizationRepository.findByOrganizationId(user.getOrganizationId()).getOrganizationName(),
                    flexibilityGroupType.name());

            // get time of the last ON command
            String stringDtLastOnControl = "null";
            ControlDetail controlDetailOn = controlDetailService.getLastOnControlEventByDevice(device.getDeviceId());
            if (controlDetailOn != null) {
                stringDtLastOnControl = TimeZoneUtil.getTimeZoneAdjustedStringDateTime(controlDetailOn.getControlDatetime(),
                        organizationRepository.findByOrganizationId(user.getOrganizationId()).getOrganizationName());
                if (controlDetailOn.getControlDatetime().before(dt) &&
                        controlDetailOn.getControlDatetime().after(controlDetailOff.getControlDatetime())) {
                    subject = String.format("[No Action Required] %s, %s, Device State Alert",
                            organizationRepository.findByOrganizationId(user.getOrganizationId()).getOrganizationName(),
                            flexibilityGroupType.name());
                    //return;
                }
            }

            String content = String.format("The device: '%s' is turned off since '%s'. " +
                            "The last system generated OFF control signal was sent at '%s'. " +
                            "The last system generated ON control signal was sent at '%s'. " +
                            "Current device state '%s'.",
                    device.getDeviceId(), stringDt, stringDtLastOffControl,
                    stringDtLastOnControl, device.getDeviceState().name());
            //smsService.sendSms(user.getUserName(), content);
            //emailService.sendSimpleMessage(user.getEmail(), "FOA Device State Alert", content);
            //smsService.sendSms("admin", content); // for now send all alerts to admin user
            //emailService.sendSimpleMessage("muhaftabkhan@gmail.com", subject, content);
            emailService.sendSimpleMessage("justnilotpal@gmail.com", subject, content);
            LOGGER.warn(subject);
            LOGGER.warn(content);


            if (flexibilityGroupType == FlexibilityGroupType.ThermostaticControlLoad) {
                this.forceTurnOnDevice(user, device);
            }


            this.lastAlertTime.put(device.getDeviceId(), new Date());
        } catch (Exception ex) {
            LOGGER.error("Failed to alert user: {} regarding device: {}", user.getUserName(), device.getDeviceId());
            LOGGER.error(ex.getLocalizedMessage());
        }
    }


    private void forceTurnOnDevice(UserT user, DeviceDetail device) {
        LOGGER.info("Force turning on device: {}", device.getDeviceId());
        try {
            if (device.getDefaultState() == 1) {
                DeviceStateChangeEvent stateChangeEvent = new DeviceStateChangeEvent(
                        this, String.format("Force turning on device %s",
                        device.getDeviceId()), device, true);
                this.applicationEventPublisher.publishEvent(stateChangeEvent);
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to set default state = 1 for device: {}", device.getDeviceId());
            LOGGER.error(ex.getLocalizedMessage());
        }
    }
}
