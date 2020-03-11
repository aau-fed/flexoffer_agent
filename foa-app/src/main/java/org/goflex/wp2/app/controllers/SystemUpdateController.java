package org.goflex.wp2.app.controllers;

import org.goflex.wp2.app.fmanintegration.user.FmanUserService;
import org.goflex.wp2.core.entities.ResponseMessage;
import org.goflex.wp2.core.entities.UserRole;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.DeviceFlexibilityDetail;
import org.goflex.wp2.core.models.Organization;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.core.repository.DeviceRepository;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/system/update")
public class SystemUpdateController {

    @Autowired
    private DeviceDetailService deviceDetailService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private FmanUserService fmanUserService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private FOAProperties foaProperties;

    private String getSessionUser(Authentication authentication) {
        return authentication.getName();
    }

    private ResponseEntity<ResponseMessage> httpResponse(String msg, HttpStatus status) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(status);
        responseMessage.setMessage(msg);
        return new ResponseEntity<>(responseMessage, status);
    }

    @RequestMapping(value = "/updateSwissFlexibilityDetail", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> getProsumers() {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (getSessionUser(authentication) == null) {
                return httpResponse("Invalid User", HttpStatus.UNAUTHORIZED);
            }
            UserT user = userService.getUser(foaProperties.getMqttServiceConfig().getFoaAccountUser());
            List<DeviceDetail> deviceDetails = deviceRepository.findByUserId(user.getId());

            for (DeviceDetail deviceDetail : deviceDetails
            ) {
                if (deviceDetail.getDeviceFlexibilityDetail() == null) {
                    DeviceFlexibilityDetail deviceFlexibilityDetail = new DeviceFlexibilityDetail();
                    deviceDetail.setDeviceFlexibilityDetail(deviceFlexibilityDetail);
                    deviceRepository.save(deviceDetail);
                }
            }
            return httpResponse("Swiss User Flexibility Detail Updated", HttpStatus.ACCEPTED);
        } catch (Exception ex) {
            return httpResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "/updateLatestAcceptanceTime", method = RequestMethod.GET)
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> getLatestAcceptanceTime() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (getSessionUser(authentication) == null) {
            return httpResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }
        List<DeviceDetail> deviceDetails = deviceDetailService.getAllDevices();
        deviceDetails.stream().forEach(deviceDetail ->{
                deviceDetail.getDeviceFlexibilityDetail().
                        setLatestAcceptanceTime(deviceDetail.
                                getDeviceFlexibilityDetail().getMaxInterruptionDelay());
                                deviceRepository.save(deviceDetail);});

        return httpResponse("Device Latest acceptance time is updated to time flexibility", HttpStatus.OK);
    }

    @RequestMapping(value = "/registerUserToFman", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> registerAllUsertoFman() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (getSessionUser(authentication) == null) {
            return httpResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }

        List<Organization> org = organizationRepository.findAll();

        for (Organization organization : organizationRepository.findAll()
        ) {
            List<Map<String, String>> unregisteredUser = new ArrayList<>();
            Map<String, String>[] fmanUsers = fmanUserService.getUsersFromFman(organization.getOrganizationName());
            if (fmanUsers != null) {
                List<String> fmanUserList = new ArrayList<>();
                for (Map<String, String> fmanUser : fmanUsers) {
                    for (String key : fmanUser.keySet()) {
                        fmanUserList.add(fmanUser.get(key));
                    }
                }
                for (String userName : userService.getAllUserName(organization.getOrganizationId())
                ) {

                    if (!fmanUserList.contains(userName)) {
                        UserT user = userService.getUser(userName);
                        Map<String, String> userDetails = new HashMap<>();
                        userDetails.put("userName", user.getUserName());
                        userDetails.put("organizationName", organization.getOrganizationName());
                        if (user.getRole() == UserRole.ROLE_ADMIN) {
                            userDetails.put("type", "admin");
                        } else {
                            userDetails.put("type", "prosumer");
                        }
                        userDetails.put("email", user.getEmail());
                        userDetails.put("role", user.getRole().toString());
                        userDetails.put("password", user.getPassword());
                        unregisteredUser.add(userDetails);
                    }

                }
            }
            if (unregisteredUser.size() > 0) {
                fmanUserService._registerUsers(unregisteredUser, organization.getOrganizationName());
            }
        }

        return httpResponse("Completed user registration with FMAN.", HttpStatus.ACCEPTED);
    }

}
