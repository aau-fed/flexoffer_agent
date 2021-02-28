package org.goflex.wp2.app.controllers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.goflex.wp2.app.common.AppRuntimeConfig;
import org.goflex.wp2.core.entities.OrganizationLoadControlState;
import org.goflex.wp2.core.entities.ResponseMessage;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/system/config")
public class SystemConfigController {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AppRuntimeConfig appRuntimeConfig;

    private String getSessionUser(Authentication authentication) {
        return authentication.getName();
    }

    private ResponseEntity<ResponseMessage> errorResponse(String msg, HttpStatus status) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(status);
        responseMessage.setMessage(msg);
        return new ResponseEntity<>(responseMessage, status);
    }

    @RequestMapping(value = "/loadControl/{toState}/{organization}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> setLoadControl(@PathVariable(value = "toState") String toState,
                                                          @PathVariable(value = "organization") String org) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }
        try {
            if (org.equalsIgnoreCase("all")) {
                organizationRepository.findAll().forEach(organization -> {
                    organization.setDirectControlMode(OrganizationLoadControlState.valueOf(toState));
                    organizationRepository.save(organization);
                });

            } else {
                organizationRepository.findByOrganizationName(org).setDirectControlMode(OrganizationLoadControlState.valueOf(toState));
            }
        } catch (Exception ex) {
            return errorResponse("Invalid Parameters supported are: tostate(Active, Paused) and Valid organization name or all", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return errorResponse("Load control strategy updated", HttpStatus.ACCEPTED);
    }


    @RequestMapping(value = "/runScheduler/{toState}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> setRunScheduler(@PathVariable(value = "toState") int toState) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }
        if (toState == 1 || toState == 0) {
            this.appRuntimeConfig.setRunScheduler(toState != 0);
            return errorResponse("Scheduler operation state updated to state:" + toState, HttpStatus.ACCEPTED);
        } else {
            return errorResponse("Invalid State", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @RequestMapping(value = "/runDeviceDiscovery/{toState}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> setDevicediscovery(@PathVariable(value = "toState") int toState) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }
        if (toState == 1 || toState == 0) {
            this.appRuntimeConfig.setRunDeviceDiscovery(toState != 0);
            return errorResponse("Device discovery state updated to state:" + toState, HttpStatus.ACCEPTED);
        } else {
            return errorResponse("Invalid State", HttpStatus.UNPROCESSABLE_ENTITY);
        }

    }


    @RequestMapping(value = "/sendFoToFman/{toState}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> sendFOToFMAN(@PathVariable(value = "toState") int toState) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }
        if (toState == 1 || toState == 0) {
            this.appRuntimeConfig.setSendFOToFMAN(toState != 0);
            return errorResponse("FO forwarding to FMAN is updated to state:" + toState, HttpStatus.ACCEPTED);
        } else {
            return errorResponse("Invalid State", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @RequestMapping(value = "/sendHeartbeatToFman/{toState}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> sendHeartBeatToFMAN(@PathVariable(value = "toState") int toState) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }
        if (toState == 1 || toState == 0) {
            this.appRuntimeConfig.setSendHeartBeatToFMAN(toState != 0);
            return errorResponse("Send HeartBeat to FMAN is updated to state:" + toState, HttpStatus.ACCEPTED);
        } else {
            return errorResponse("Invalid State", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @RequestMapping(value = "/monitorPlugStatus/{toState}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> monitorPlugStatus(@PathVariable(value = "toState") int toState) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }
        if (toState == 1 || toState == 0) {
            this.appRuntimeConfig.setMonitorPlugStatus(toState != 0);
            return errorResponse("FO forwarding to FMAN is updated to state:" + toState, HttpStatus.ACCEPTED);
        } else {
            return errorResponse("Invalid State", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @RequestMapping(value = "/updateSystemRuntimeConfig/{toState}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> updateSystemRuntimeConfig(@PathVariable(value = "toState") int toState) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }
        if (toState == 1 || toState == 0) {
            this.appRuntimeConfig.setRunScheduler(toState != 0);
            this.appRuntimeConfig.setRunDeviceDiscovery(toState != 0);
            this.appRuntimeConfig.setSendFOToFMAN(toState != 0);
            this.appRuntimeConfig.setSendHeartBeatToFMAN(toState != 0);
            this.appRuntimeConfig.setMonitorPlugStatus(toState != 0);
            return errorResponse("FOA runtime configuration updated to state:" + toState, HttpStatus.ACCEPTED);
        } else {
            return errorResponse("Invalid State", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @RequestMapping(value = "/runtimeConfig", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> getSystemRuntimeConfig() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setData(this.appRuntimeConfig);
        responseMessage.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    @PostMapping(value = "/logLevel/{loglevel}")
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

}

