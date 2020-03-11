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
 *  Last Modified 2/21/18 11:21 PM
 */

package org.goflex.wp2.app.controllers;


import com.fasterxml.jackson.databind.JsonNode;
import org.goflex.wp2.core.models.*;
import org.goflex.wp2.foa.controldetailmonitoring.ControlDetail;
import org.goflex.wp2.foa.controldetailmonitoring.ControlDetailService;
import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.core.repository.DeviceHierarchyRepository;
import org.goflex.wp2.foa.devicestate.DeviceStateHistory;
import org.goflex.wp2.foa.devicestate.DeviceStateService;
import org.goflex.wp2.foa.implementation.ImplementationsHandler;
import org.goflex.wp2.foa.implementation.SimulatedDeviceService;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by bijay on 12/6/17.
 */

@RestController
@RequestMapping("/api/v1.0/devices")
public class DeviceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceController.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Value("${cloud.api.url}")
    private String cloudApiUrl;

    private DeviceHierarchyRepository deviceHierarchyRepository;
    private UserService userService;
    private DeviceDetailService deviceDetailService;
    private ImplementationsHandler implementationsHandler;
    private SimulatedDeviceService simulatedDeviceService;
    private ControlDetailService controlDetailService;
    private DeviceStateService deviceStateService;
    private DeviceFlexOfferGroup deviceFlexOfferGroup;


    public DeviceController() {
    }

    @Autowired
    public DeviceController(
            DeviceHierarchyRepository deviceHierarchyRepository,
            UserService userService,
            DeviceDetailService deviceDetailService,
            ImplementationsHandler implementationsHandler,
            SimulatedDeviceService simulatedDeviceService,
            ControlDetailService controlDetailService,
            DeviceStateService deviceStateService,
            DeviceFlexOfferGroup deviceFlexOfferGroup

    ) {
        this.deviceHierarchyRepository = deviceHierarchyRepository;
        this.userService = userService;
        this.deviceDetailService = deviceDetailService;
        this.implementationsHandler = implementationsHandler;
        this.simulatedDeviceService = simulatedDeviceService;
        this.controlDetailService = controlDetailService;
        this.deviceStateService = deviceStateService;
        this.deviceFlexOfferGroup = deviceFlexOfferGroup;
    }

    @GetMapping(value = "/getControlHistory/{deviceId}/{startDate}/{endDate}")
    public ResponseEntity<ResponseMessage> getDeviceControlHistory(@PathVariable("deviceId") String deviceId,
                                                                   @PathVariable("startDate") String startDate,
                                                                   @PathVariable("endDate") String endDate) {
        String sessionUser;
        ResponseMessage responseMessage = new ResponseMessage();

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (this.getSessionUser(authentication) == null) {
                return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
            }
            sessionUser = authentication.getName();

            if (deviceId == null || deviceId.equals("")) {
                return errorResponse("DeviceId can not be empty or null", HttpStatus.BAD_REQUEST);
            }

            //check if device belongs to current user
            //if (!deviceId.split("@")[0].equals(sessionUser) && !authentication.getAuthorities().contains(UserRole
            // .ROLE_ADMIN)) {
            if (!deviceId.split("@")[0].equals(sessionUser) && authentication.getAuthorities().stream()
                    .noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                return errorResponse("Device not found", HttpStatus.NOT_FOUND);
            }

            Date fromDt;
            Date toDt;
            try {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                //format.setTimeZone(TimeZone.getTimeZone("UTC"));
                fromDt = format.parse(startDate);
                toDt = format.parse(endDate);
            } catch (Exception e) {
                return errorResponse("Invalid Date format, supported type is \"yyyy-MM-dd'T'HH:mm:ss\"",
                        HttpStatus.BAD_REQUEST);
            }

            List<ControlDetail> controlHistory = this.controlDetailService.getControlHistoryByDeviceAndDate(deviceId,
                    fromDt, toDt);
            responseMessage.setStatus(HttpStatus.OK);
            responseMessage.setData(controlHistory);
            responseMessage.setMessage(String.format("found %d records", controlHistory.size()));
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);

        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
            responseMessage.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            responseMessage.setMessage("error getting control history. error msg: " + ex.getLocalizedMessage());
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getStateHistory/{deviceId}/{startDate}/{endDate}")
    public ResponseEntity<ResponseMessage> getDeviceStateHistory(@PathVariable("deviceId") String deviceId,
                                                                   @PathVariable("startDate") String startDate,
                                                                   @PathVariable("endDate") String endDate) {
        String sessionUser;
        ResponseMessage responseMessage = new ResponseMessage();

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (this.getSessionUser(authentication) == null) {
                return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
            }
            sessionUser = authentication.getName();

            if (deviceId == null || deviceId.equals("")) {
                return errorResponse("DeviceId can not be empty or null", HttpStatus.BAD_REQUEST);
            }

            //check if device belongs to current user
            //if (!deviceId.split("@")[0].equals(sessionUser) && !authentication.getAuthorities().contains(UserRole
            // .ROLE_ADMIN)) {
            if (!deviceId.split("@")[0].equals(sessionUser) && authentication.getAuthorities().stream()
                    .noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                return errorResponse("Device not found", HttpStatus.NOT_FOUND);
            }

            Date fromDt;
            Date toDt;
            try {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                //format.setTimeZone(TimeZone.getTimeZone("UTC"));
                fromDt = format.parse(startDate);
                toDt = format.parse(endDate);
            } catch (Exception e) {
                return errorResponse("Invalid Date format, supported type is \"yyyy-MM-dd'T'HH:mm:ss\"",
                        HttpStatus.BAD_REQUEST);
            }

            List<DeviceStateHistory> stateHistory = this.deviceStateService
                    .getDeviceStateHistoryByDeviceAndDate(deviceId, fromDt, toDt);
            responseMessage.setStatus(HttpStatus.OK);
            responseMessage.setData(stateHistory);
            responseMessage.setMessage(String.format("found %d records", stateHistory.size()));
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);

        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
            responseMessage.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            responseMessage.setMessage("error getting state history. error msg: " + ex.getLocalizedMessage());
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getSessionUser(Authentication authentication) {
        return authentication.getName();
    }

    private ResponseEntity<ResponseMessage> errorResponse(String msg, HttpStatus status) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(status);
        responseMessage.setMessage(msg);
        return new ResponseEntity<>(responseMessage, status);
    }

    private ResponseEntity<ResponseMessage> successResponse(String msg) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setMessage(msg);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/updateDeviceLocation", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> updateDeviceLocation(@RequestBody DeviceDetail deviceDetail) {
        try {
            // Authenticate session user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (this.getSessionUser(authentication) == null) {
                return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
            }

            // Check if user exists
            String sessionUser = authentication.getName();

            // Check if device Id exists in request body
            if (deviceDetail.getDeviceId() == null || deviceDetail.getDeviceId().equals("")) {
                return errorResponse("DeviceId cannot be null", HttpStatus.BAD_REQUEST);
            }

            // Check if device location exists in request body
            if (deviceDetail.getLatitude() == 0 || deviceDetail.getLongitude() == 0) {
                return errorResponse("Device location cannot be null", HttpStatus.BAD_REQUEST);
            }

            //check if device exist and belongs to current user or the current user is admin
            DeviceDetail dd = deviceDetailService.getDevice(deviceDetail.getDeviceId());
            //if (dd == null || !dd.getDeviceId().split("@")[0].equals(sessionUser) && !authentication.getAuthorities
            // ().contains(UserRole.ROLE_ADMIN)) {
            if (dd == null || !dd.getDeviceId().split("@")[0].equals(sessionUser) &&
                    authentication.getAuthorities().stream()
                            .noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                return errorResponse(
                        String.format("Either device does not exist or it does not belong to user: %s\"", sessionUser),
                        HttpStatus.NOT_FOUND);
            }


            String message = deviceDetailService
                    .updateDeviceLocation(deviceDetail.getDeviceId(), deviceDetail.getLatitude(),
                            deviceDetail.getLongitude());
            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setStatus(HttpStatus.OK);
            responseMessage.setMessage(message);
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (Exception ex) {
            return errorResponse("failed to update device location", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @RequestMapping(value = "/updateDevice", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> updateDeviceDetail(@RequestBody DeviceDetail deviceDetail) {

        // Authenticate session user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }

        // Check if user exists
        String sessionUser = authentication.getName();

        // Check if device Id exists in request body
        if (deviceDetail.getDeviceId() == null || deviceDetail.getDeviceId().equals("")) {
            return errorResponse("DeviceId cannot be null", HttpStatus.BAD_REQUEST);
        }

        //check if device exist and belongs to current user or the current user is admin
        DeviceDetail dd = deviceDetailService.getDevice(deviceDetail.getDeviceId());
        //if (dd == null || !dd.getDeviceId().split("@")[0].equals(sessionUser) && !authentication.getAuthorities()
        // .contains(UserRole.ROLE_ADMIN)) {
        if (dd == null || !dd.getDeviceId().split("@")[0].equals(sessionUser) &&
                authentication.getAuthorities().stream()
                        .noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            return errorResponse(
                    String.format("Either device does not exist or it does not belong to user: %s\"", sessionUser),
                    HttpStatus.NOT_FOUND);
        }


        Map<String, Object> payload = new HashMap<>();
        if (deviceDetail.getDeviceType() != null && !deviceDetail.getDeviceType().equals("")) {
            payload.put("deviceType", deviceDetail.getDeviceType());
        }
        if (deviceDetail.getDeviceFlexibilityDetail() != null) {
            payload.put("flexibilityDetail", deviceDetail.getDeviceFlexibilityDetail());
        }
        if (deviceDetail.getGroupingDetail() != null) {
            payload.put("grouping", deviceDetail.getGroupingDetail());
        }
        if (deviceDetail.getDeviceHierarchy() != null) {
            payload.put("deviceHierarchy", deviceDetail.getDeviceHierarchy());
        } else {
            // remove hierarchy if exists
            if (dd.getDeviceHierarchy() != null) {
                payload.put("deviceHierarchy", deviceDetail.getDeviceHierarchy());
            }
        }

        payload.put("flexible", deviceDetail.isFlexible());

        String message = userService.updateDeviceDetail(deviceDetail.getDeviceId(), payload);
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(HttpStatus.OK);
        //responseMessage.setMessage(message);
        responseMessage.setMessage("Device successfully updated.");
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);

    }

    @RequestMapping(value = "/updateDeviceGrouping/{deviceid}", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> updateDeviceGrouping(@RequestBody GroupingDetail groupingDetail,
                                                                @PathVariable(value = "deviceid") String deviceId) {

        // Authenticate session user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }

        // Check device Id exists
        if (deviceId == null || deviceId.equals("")) {
            return errorResponse("DeviceId cannot be null", HttpStatus.BAD_REQUEST);
        }

        //check if device belongs to current user or admin
        String sessionUser = authentication.getName();
        //if (!deviceId.split("@")[0].equals(sessionUser) && !authentication.getAuthorities().contains(UserRole
        // .ROLE_ADMIN)) {
        if (!deviceId.split("@")[0].equals(sessionUser) && authentication.getAuthorities().stream()
                .noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {


            return errorResponse("Device not found", HttpStatus.UNAUTHORIZED);
        }

        // Check if user exists
        UserT usr = userService.getUser(sessionUser);
        if (usr == null) {
            return errorResponse(String.format("User: %s not found", sessionUser), HttpStatus.NOT_FOUND);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("grouping", groupingDetail);

        String message = userService.updateDeviceDetail(deviceId, payload);
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setMessage(message);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);

    }

    /**
     * URI to set devicedetail for user FOA user
     */
    @RequestMapping(value = "/setdevice", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> setdevice(@RequestBody DeviceDetail deviceDetail) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }

        // Check if user exists
        String sessionUser = authentication.getName();
        UserT usr = userService.getUser(sessionUser);
        if (usr == null) {
            return errorResponse(String.format("User: %s not found", sessionUser), HttpStatus.NOT_FOUND);
        }

        // check if device with the given deviceId already exists exists
        if (deviceDetail.getDeviceId() == null) {
            return errorResponse("deviceId can not be null", HttpStatus.BAD_REQUEST);
        }

        // deviceDetailId is autogenerated, check it is not preassigned a value
        if (deviceDetail.getDeviceDetailId() > 0) {
            return errorResponse("deviceDetailId is autogenerated, it can not be preassigned a value",
                    HttpStatus.BAD_REQUEST);
        }

        UserT user = userService.updateDeviceList(sessionUser, deviceDetail);
        if (user == null) {
            return errorResponse("The request format is incorrect", HttpStatus.BAD_REQUEST);
        }

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setData(user);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * URI to get a single device's details
     *
     * @return
     */
    @RequestMapping(value = "/getDevice/{deviceId}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getDevice(@PathVariable(value = "deviceId") String deviceId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }

        String sessionUser = authentication.getName();
        UserT user = userService.getUser(sessionUser);
        DeviceDetail device = deviceDetailService.getDevice(deviceId);
        ResponseMessage responseMessage = new ResponseMessage();
        //if (user.getDeviceDetail().contains(device) || authentication.getAuthorities().contains(UserRole
        // .ROLE_ADMIN)) {
        if (user.getDeviceDetail().contains(device) || authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            responseMessage.setMessage("Success");
            responseMessage.setStatus(HttpStatus.OK);
            responseMessage.setData(device);
        } else {
            return errorResponse(
                    String.format("Error: device with id: '%s' not found for user: '%s'", deviceId, sessionUser),
                    HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * URI to get a list containing basic info of devices registered to FOA user
     * used to display device list in client gui
     */
    @RequestMapping(value = "/deviceSummaryList", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getDeviceSummaryList() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (this.getSessionUser(authentication) == null) {
                return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
            }

            String sessionUser = authentication.getName();
            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setMessage("Success");
            responseMessage.setStatus(HttpStatus.OK);
            UserT usr = userService.getUser(sessionUser);
            //if (authentication.getAuthorities().contains(UserRole.ROLE_ADMIN)) {
            if (authentication.getAuthorities().stream()
                    .anyMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                responseMessage.setData(userService.getDeviceSummaryListForOrganization(usr.getOrganizationId()));
            } else {
                Set<DeviceBasicInfoDto> deviceSummaryList = userService.getDeviceBasicInfoList(sessionUser);
                responseMessage.setData(deviceSummaryList);
            }
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (Exception ex) {
            return errorResponse(ex.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * URI to get list of devices registered to FOA user
     */
    @RequestMapping(value = "/devices", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getDevices() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }

        String sessionUser = authentication.getName();
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        //if (authentication.getAuthorities().contains(UserRole.ROLE_ADMIN)) {
        if (authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            UserT usr = userService.getUser(sessionUser);
            responseMessage.setData(userService.getDeviceListforOrganization(usr.getOrganizationId()));
        } else {
            Set<DeviceDetail> deviceDetailSet = userService.getDeviceList(sessionUser);
            responseMessage.setData(deviceDetailSet);
        }
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/getDevicesLocation", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getDeviceLocations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }
        String sessionUser = authentication.getName();
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        UserT user = userService.getUser(sessionUser);
        //responseMessage.setData(deviceDetailService.getDevicesLocation(sessionUser, user.getRole(), user
        // .getOrganizationId()));
        responseMessage.setData(
                deviceDetailService.getDevicesLocation2(sessionUser, user.getRole(), user.getOrganizationId()));
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/getDevicesLocation/{hierarchyId}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getDeviceLocationsByHierarchyId(
            @PathVariable(value = "hierarchyId") long hierarchyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }
        String sessionUser = authentication.getName();
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        UserT user = userService.getUser(sessionUser);
        responseMessage.setData(deviceDetailService.getDevicesLocationByHierarchyId(user, hierarchyId));
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * URI to get all consumption data for all device registered to a user
     */
    @RequestMapping(value = "/getallconsumption", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getAllConsumption() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }

        String sessionUser = authentication.getName();

        Set<DeviceDetail> devices = new HashSet<>();
        //if (authentication.getAuthorities().contains(UserRole.ROLE_ADMIN)) {
        if (authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            UserT usr = userService.getUser(sessionUser);
            devices.addAll(userService.getDeviceListforOrganization(usr.getOrganizationId()));
            //devices = deviceDetailService.getAllDevices().stream().collect(Collectors.toSet());
        } else {
            devices = userService.getDevices(sessionUser);
        }

        //check if device belongs to current user
        if (devices == null) {
            return errorResponse(String.format("No devices found for user: %s", sessionUser), HttpStatus.NOT_FOUND);
        }

        Map<String, Map<Date, Double>> deviceData = new HashMap<>();
        for (DeviceDetail dd : devices) {
            Map<Date, Double> currentDeviceData = deviceDetailService.getAllConsumptionDataForDevice(dd.getDeviceId());
            deviceData.put(dd.getDeviceId(), currentDeviceData);
        }

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setData(deviceData);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * URI to get consumption for all device registered to a user for the current date
     */
    @RequestMapping(value = "/getLatestConsumption", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getLatestConsumption() {
        String sessionUser;
        ResponseMessage responseMessage = new ResponseMessage();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }
        sessionUser = authentication.getName();

        Set<DeviceDetail> devices = new HashSet<>();
        //if (authentication.getAuthorities().contains(UserRole.ROLE_ADMIN)) {
        if (authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            devices.addAll(deviceDetailService.getAllDevices());
            //devices = deviceDetailService.getAllDevices().stream().collect(Collectors.toSet());
        } else {
            devices = userService.getDevices(sessionUser);
        }

        if (devices == null) {
            return errorResponse(String.format("No devices found for user: %s", sessionUser), HttpStatus.NOT_FOUND);
        }

        //check if device belongs to current user
        Iterator<DeviceDetail> iterator = devices.iterator();
        DeviceResponseData[] deviceResponseData = new DeviceResponseData[devices.size()];
        int i = 0;
        Date today = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
            today = df.parse(df.format(new Date()));
        } catch (Exception ex) {
            return errorResponse("Date error: " + Arrays.toString(ex.getStackTrace()), HttpStatus.BAD_REQUEST);
        }
        while (iterator.hasNext()) {
            DeviceDetail dd = iterator.next();
            DeviceResponseData deviceData = new DeviceResponseData();
            deviceData.setDeviceID(dd.getDeviceId());
            deviceData.setDeviceType(dd.getDeviceType());
            Map<Date, Double> latestData = deviceDetailService.getConsumptionDataFromDate(dd.getDeviceId(), today);
            deviceData.setDeviceLatestData(latestData);
            deviceResponseData[i] = deviceData;
            i++;
        }

        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setData(deviceResponseData);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * URI to get latest consumption for the current date for a specific device
     */
    @RequestMapping(value = "/getLatestConsumption/{deviceid}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getDeviceConsumptionForToday(
            @PathVariable(value = "deviceid") String deviceId) {
        String sessionUser;
        ResponseMessage responseMessage = new ResponseMessage();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
        }
        sessionUser = authentication.getName();

        if (deviceId == null || deviceId.equals("")) {
            return errorResponse("DeviceId can not be empty or null", HttpStatus.BAD_REQUEST);
        }

        //check if device belongs to current user
        //if (!deviceId.split("@")[0].equals(sessionUser) && !authentication.getAuthorities().contains(UserRole
        // .ROLE_ADMIN)) {
        if (!deviceId.split("@")[0].equals(sessionUser) && authentication.getAuthorities().stream()
                .noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            return errorResponse("Device not found", HttpStatus.NOT_FOUND);
        }

        Date today = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
            today = df.parse(df.format(new Date()));
        } catch (Exception ex) {
            return errorResponse("Date error: " + Arrays.toString(ex.getStackTrace()), HttpStatus.BAD_REQUEST);
        }

        Map<Date, Double> latestData = deviceDetailService.getConsumptionDataFromDate(deviceId, today);

        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setData(latestData);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * URI to get latest consumption for a device after a given datetime
     */
    @RequestMapping(value = "/getLatestConsumption/{deviceid}/{givendatetime}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getDeviceConsumptionFrom(@PathVariable(value = "deviceid") String deviceId,
                                                                    @PathVariable(value = "givendatetime")
                                                                            String givenDatetime) {

        String sessionUser;
        ResponseMessage responseMessage = new ResponseMessage();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
        }
        sessionUser = authentication.getName();

        if (deviceId == null || deviceId.equals("")) {
            return errorResponse("DeviceId can not be empty or null", HttpStatus.BAD_REQUEST);
        }

        //check if device belongs to current user
        //if (!deviceId.split("@")[0].equals(sessionUser) && !authentication.getAuthorities().contains(UserRole
        // .ROLE_ADMIN)) {
        if (!deviceId.split("@")[0].equals(sessionUser) && authentication.getAuthorities().stream()
                .noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            return errorResponse("Device not found", HttpStatus.NOT_FOUND);
        }

        Date lastDt;
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            //format.setTimeZone(TimeZone.getTimeZone("UTC"));
            lastDt = format.parse(givenDatetime);
        } catch (Exception e) {
            return errorResponse("Invalid Date format, supported type is \"yyyy-MM-dd'T'HH:mm:ss\"",
                    HttpStatus.BAD_REQUEST);
        }

        Map<Date, Double> latestData = deviceDetailService.getConsumptionDataFromDate(deviceId, lastDt);

        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setData(latestData);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * get device power consumption for a device between two dates
     */
    @RequestMapping(value = "/getPowerConsumption/{deviceId}/{fromDate}/{toDate}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getDevicePowerConsumption(@PathVariable(value = "deviceId") String deviceId,
                                                                     @PathVariable(value = "fromDate") String fromDate,
                                                                     @PathVariable(value = "toDate") String toDate) {

        String sessionUser;
        ResponseMessage responseMessage = new ResponseMessage();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
        }
        sessionUser = authentication.getName();

        if (deviceId == null || deviceId.equals("")) {
            return errorResponse("DeviceId can not be empty or null", HttpStatus.BAD_REQUEST);
        }

        //check if device belongs to current user
        //if (!deviceId.split("@")[0].equals(sessionUser) && !authentication.getAuthorities().contains(UserRole
        // .ROLE_ADMIN)) {
        if (!deviceId.split("@")[0].equals(sessionUser) && authentication.getAuthorities().stream()
                .noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            return errorResponse("Device not found", HttpStatus.NOT_FOUND);
        }

        Date fromDt;
        Date toDt;
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            //format.setTimeZone(TimeZone.getTimeZone("UTC"));
            fromDt = format.parse(fromDate);
            toDt = format.parse(toDate);
        } catch (Exception e) {
            return errorResponse("Invalid Date format, supported type is \"yyyy-MM-dd'T'HH:mm:ss\"",
                    HttpStatus.BAD_REQUEST);
        }

        Map<Date, Double> latestData = deviceDetailService.getPowerConsumption(deviceId, fromDt, toDt);

        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setData(latestData);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * URI to get consumption for a device for any given date
     */
    @RequestMapping(value = "/getConsumptionForDate/{deviceid}/{consumptionDate}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getDeviceConsumptionForDate(
            @PathVariable(value = "deviceid") String deviceId,
            @PathVariable(value = "consumptionDate") String consumptionDate) {
        String sessionUser;
        ResponseMessage responseMessage = new ResponseMessage();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
        }
        sessionUser = authentication.getName();

        if (deviceId == null || deviceId.equals("")) {
            return errorResponse("DeviceId can not be empty or null", HttpStatus.BAD_REQUEST);
        }

        //check if device belongs to current user
        //if (!deviceId.split("@")[0].equals(sessionUser) && !authentication.getAuthorities().contains(UserRole
        // .ROLE_ADMIN)) {
        if (!deviceId.split("@")[0].equals(sessionUser) && authentication.getAuthorities().stream()
                .noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            return errorResponse("Device not found", HttpStatus.NOT_FOUND);
        }

        Date consumptionDt;
        try {
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
            consumptionDt = df.parse(consumptionDate);
        } catch (Exception ex) {
            return errorResponse("Invalid Date format, supported type is \"yyyy-MM-dd\"", HttpStatus.BAD_REQUEST);
        }

        Map<Date, Double> latestData = deviceDetailService.getConsumptionDataForDate(deviceId, consumptionDt);

        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setData(latestData);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * URI to get list of devices registered to FOA user
     */
    @RequestMapping(value = "/alldevices", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> getAllDeviceList() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
        }

        String sessionUser = authentication.getName();
        if (!sessionUser.equals("admin")) {
            return errorResponse("Only admin user is allowed to take this action", HttpStatus.UNAUTHORIZED);
        }

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setMessage("Success");
        responseMessage.setData(deviceDetailService.getAllDevices());
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /*Switch on/off device*/
    @RequestMapping(value = "/toggleDevice/{deviceid}/{state}", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> toogleDevice(@PathVariable(value = "deviceid") String deviceId,
                                                        @PathVariable(value = "state") int state) {
        String sessionUser;
        ResponseMessage responseMessage = new ResponseMessage();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
        }
        sessionUser = authentication.getName();

        if (deviceId == null || deviceId.equals("")) {
            return errorResponse("DeviceId can not be empty or null", HttpStatus.BAD_REQUEST);
        }

        //check if device belongs to current user
        //if (!deviceId.split("@")[0].equals(sessionUser) && !authentication.getAuthorities().contains(UserRole
        // .ROLE_ADMIN)) {
        if (!deviceId.split("@")[0].equals(sessionUser) && authentication.getAuthorities().stream()
                .noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            return errorResponse("Device not found", HttpStatus.UNAUTHORIZED);
        }

        //UserT user = userService.getUser(sessionUser);
        UserT user = userService.getUser(deviceId.split("@")[0]);

        DeviceDetail deviceDetail = deviceDetailService.getDevice(deviceId);

        if (deviceDetail == null) {
            return errorResponse("Device does not exist", HttpStatus.NOT_FOUND);
        }

        DeviceParameters deviceParameters = new DeviceParameters(user.getTpLinkUserName(),
                user.getTpLinkPassword(), user.getAPIKey(), cloudApiUrl);
        int deviceCurrentState = implementationsHandler.get(deviceDetail.getPlugType())
                .getDeviceState(user.getUserName(), deviceId, deviceParameters);
        if (state == deviceCurrentState) {
            //return errorResponse("Device already in selected state", HttpStatus.BAD_REQUEST);
        }

        //check if device if offline
        if (deviceCurrentState == -1) {
            return errorResponse("Device is offline", HttpStatus.BAD_REQUEST);
        }

        if (deviceCurrentState == -2) {
            return errorResponse("Please set Plug Username and Password", HttpStatus.BAD_REQUEST);
        }


        if (state == 1) {
            implementationsHandler.get(deviceDetail.getPlugType())
                    .startDevice(user.getUserName(), deviceId, deviceParameters);
            responseMessage.setMessage("Device switched on");
            responseMessage.setStatus(HttpStatus.OK);
        } else if (state == 0) {
            implementationsHandler.get(deviceDetail.getPlugType())
                    .stopDevice(user.getUserName(), deviceId, deviceParameters);
            responseMessage.setMessage("Device switched off");
            responseMessage.setStatus(HttpStatus.OK);
        } else {
            responseMessage.setMessage("Invalid device state");
            responseMessage.setStatus(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
        }
        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);

    }

    /**
     * Switch on/off device
     */
    @RequestMapping(value = "/addschedule/{deviceid}", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> addSchedule(@PathVariable(value = "deviceid") String deviceId) {
        String sessionUser = "";
        ResponseMessage responseMessage = new ResponseMessage();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
        }
        sessionUser = authentication.getName();

        if (deviceId == null || deviceId.equals("")) {
            return errorResponse("DeviceId can not be empty or null", HttpStatus.BAD_REQUEST);
        }

        //check if device belongs to current user
        //if (!deviceId.split("@")[0].equals(sessionUser) && !authentication.getAuthorities().contains(UserRole
        // .ROLE_ADMIN)) {
        if (!deviceId.split("@")[0].equals(sessionUser) && authentication.getAuthorities().stream()
                .noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            return errorResponse("Device not found", HttpStatus.UNAUTHORIZED);
        }


        //UserT user = userService.getUser(sessionUser);
        UserT user = userService.getUser(deviceId.split("@")[0]);

        DeviceDetail deviceDetail = deviceDetailService.getDevice(deviceId);
        if (deviceDetail == null) {
            return errorResponse("Device does not exist", HttpStatus.NOT_FOUND);
        }

        DeviceParameters deviceParameters = new DeviceParameters(user.getTpLinkUserName(),
                user.getTpLinkPassword(), user.getAPIKey(), cloudApiUrl);

        implementationsHandler.get(deviceDetail.getPlugType())
                .addOnOffSchedule(deviceId.split("@")[1], "admin", new Date(), 1);

        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);

    }

    @RequestMapping(value = "/getschedule/{deviceid}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getSchedule(@PathVariable(value = "deviceid") String deviceId) {
        String sessionUser = "";
        ResponseMessage responseMessage = new ResponseMessage();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
        }
        sessionUser = authentication.getName();

        if (deviceId == null || deviceId.equals("")) {
            //throw new CustomException("DeviceId can not be empty or null", HttpStatus.BAD_REQUEST);
            return errorResponse("DeviceId can not be empty or null", HttpStatus.BAD_REQUEST);
        }

        //check if device belongs to current user
        //if (!deviceId.split("@")[0].equals(sessionUser) && !authentication.getAuthorities().contains(UserRole
        // .ROLE_ADMIN)) {
        if (!deviceId.split("@")[0].equals(sessionUser) && authentication.getAuthorities().stream()
                .noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            return errorResponse("Device not found", HttpStatus.UNAUTHORIZED);
        }

        //UserT user = userService.getUser(sessionUser);
        UserT user = userService.getUser(deviceId.split("@")[0]);

        /*if (user.getTpLinkUserName() == "" || user.getTpLinkUserName() == null
                || user.getTpLinkPassword() == "" || user.getTpLinkPassword() == null) {
            return errorResponse("Please set device connection username and password first in the profile page",
            HttpStatus.BAD_REQUEST);
        }*/

        DeviceDetail deviceDetail = deviceDetailService.getDevice(deviceId);
        if (deviceDetail == null) {
            return errorResponse("Device does not exist", HttpStatus.NOT_FOUND);
        }

        DeviceParameters deviceParameters = new DeviceParameters(user.getTpLinkUserName(),
                user.getTpLinkPassword(), user.getAPIKey(), cloudApiUrl);

        JsonNode responseJson = implementationsHandler.get(deviceDetail.getPlugType())
                .getOnOffSchedules(deviceId.split("@")[1], deviceParameters);

        responseMessage.setMessage("Success");
        responseMessage.setData(responseJson);
        responseMessage.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteSchedule/{deviceid}/{username}/{scheduleId}/{action}", method = RequestMethod.POST)
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteSchedule(@PathVariable(value = "deviceid") String deviceId,
                                                 @PathVariable(value = "username") String username,
                                                 @PathVariable(value = "scheduleId") String scheduleId,
                                                 @PathVariable(value = "action") int action) {
        DeviceDetail deviceDetail = deviceDetailService.getDevice(deviceId);
        Boolean status = implementationsHandler.get(deviceDetail.getPlugType())
                .deleteOnOffSchedule(deviceId, username, scheduleId, action);
        String body =
                String.format("Schedule with id: %s for deviceID: %s is successfully deleted", scheduleId, deviceId);
        if (!status) {
            body = String.format("Error in deleting schedule with id: %s for deviceID: %s", scheduleId, deviceId);
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(body, HttpStatus.OK);

    }

    /**
     * Start a new simulated load
     */
    @RequestMapping(value = "/loadSimulatedDevice", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> loadSimulatedDevice(@RequestBody String simDeviceJson) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
        }
        String sessionUser = authentication.getName();

        String deviceId = simulatedDeviceService.createSimulatedDevice(sessionUser, simDeviceJson);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage(deviceId);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * delete a new simulated load
     */
    @RequestMapping(value = "/deleteSimulatedDevice/{deviceId}", method = RequestMethod.DELETE)
    public ResponseEntity<ResponseMessage> deleteSimulatedDevice(@PathVariable(value = "deviceId") String deviceId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
        }

        // todo: also delete from local db
        String sessionUser = authentication.getName();

        String result = simulatedDeviceService.deleteSimulatedDevice(deviceId);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage(result);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * delete all simulated loads
     */
    @RequestMapping(value = "/deleteAllSimulatedDevices", method = RequestMethod.DELETE)
    public ResponseEntity<ResponseMessage> deleteAllSimulatedDevices() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
        }

        // todo: also delete from local db
        String sessionUser = authentication.getName();

        String result = simulatedDeviceService.deleteAllSimulatedDevicesForUser(sessionUser);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage(result);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * get new token from Flexible Load Simulator server
     */
    @RequestMapping(value = "/getNewTokenFromFLS", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getNewTokenFromFLS() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
        }

        String newToken = simulatedDeviceService.getNewToken(null, null);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage(newToken);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * Turn on a simulated load
     */
    @RequestMapping(value = "/startSimulatedDevice/{deviceId}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> startSimulatedDevice(@PathVariable(value = "deviceId") String deviceId) {

        LOGGER.info("starting simulated device: " + deviceId);
        String result = simulatedDeviceService.startDevice(null, deviceId, null);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage(result);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * Turn off a simulated load
     */
    @RequestMapping(value = "/stopSimulatedDevice/{deviceId}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> stopSimulatedDevice(@PathVariable(value = "deviceId") String deviceId) {

        LOGGER.debug("stopping simulated device " + deviceId);
        String result = simulatedDeviceService.stopDevice(null, deviceId, null);
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage(result);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * Get the on/off status of a simulated device
     */
    @RequestMapping(value = "/getSimulatedDeviceState/{deviceId}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getSimulatedDeviceState(@PathVariable String deviceId) {

        LOGGER.debug("getting status of simulated device: " + deviceId);
        int status = simulatedDeviceService.getDeviceState(null, deviceId, null);
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage(String.valueOf(status));
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * Get total energy consumption data (since the very start) from simulated load
     */
    @RequestMapping(value = "/getSimulatedDeviceEnergy/{deviceId}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getSimulatedDeviceEnergy(@PathVariable String deviceId) {

        LOGGER.debug("getting energy data for simulated device: " + deviceId);
        DeviceParameters deviceParameters = new DeviceParameters("", "", "", "");
        ResponseMessage responseMessage = new ResponseMessage();
        double energy = simulatedDeviceService.getEnergyConsumption(deviceId, deviceParameters);
        responseMessage.setMessage(String.valueOf(energy));
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * Get live power consumption data from simulated load
     */
    @RequestMapping(value = "/getSimulatedDevicePower/{deviceId}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getSimulatedDevicePower(@PathVariable String deviceId) {

        LOGGER.debug("getting live power data for simulated device: " + deviceId);
        DeviceParameters deviceParameters = new DeviceParameters("", "", "", "");
        ResponseMessage responseMessage = new ResponseMessage();
        double power = simulatedDeviceService.getPowerConsumption(deviceId, deviceParameters);
        responseMessage.setMessage(String.valueOf(power));
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * deletes a device with a given user id
     *
     * @param deviceId
     * @return
     */
    @RequestMapping(value = "/deleteDeviceById/{deviceId}", method = RequestMethod.DELETE)
    public ResponseEntity<ResponseMessage> deleteDeviceById(@PathVariable(value = "deviceId") String deviceId) {
        String message;
        String sessionUser;
        ResponseMessage responseMessage = new ResponseMessage();

        // Authenticate session user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }
        sessionUser = authentication.getName();

        if (deviceId == null || deviceId.equals("")) {
            return errorResponse("DeviceId can not be empty or null", HttpStatus.BAD_REQUEST);
        }

        //check if device belongs to current user
        //if (!deviceId.split("@")[0].equals(sessionUser) && !authentication.getAuthorities().contains(UserRole
        // .ROLE_ADMIN)) {
        if (!deviceId.split("@")[0].equals(sessionUser) && authentication.getAuthorities().stream()
                .noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            return errorResponse("Device not found", HttpStatus.UNAUTHORIZED);
        }


        //boolean status = userService.removeDeviceID(sessionUser, deviceId);
        boolean status = userService.removeDeviceID(deviceId.split("@")[0], deviceId);

        if (!status) {
            return errorResponse(String.format("No user: %s or device: %s found", sessionUser, deviceId),
                    HttpStatus.NOT_FOUND);
        }

        responseMessage.setMessage(
                String.format("Successfully deleted device with id: %s for user: %s", deviceId, sessionUser));
        responseMessage.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteAllDevices", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> deleteAllDevices() {

        String sessionUser;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }
        sessionUser = authentication.getName();

        UserT userT = userService.getUser(sessionUser);
        boolean status = userService.removeDevices(userT);
        if (!status) {
            return errorResponse(String.format("No devices found for user: %s ", sessionUser), HttpStatus.NOT_FOUND);
        }

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setMessage(String.format("Sucessfully deleted all devices for user: %s", sessionUser));
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /**
     * deletes all devices (for all users).
     */
    @RequestMapping(value = ("/deleteAllDevicesForAllUsers"), method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> deleteAllDevicesForAllUsers() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (getSessionUser(authentication).equals("")) {
            return errorResponse("Invalid user", HttpStatus.UNAUTHORIZED);
        }
        String sessionUser = authentication.getName();

        if (!sessionUser.equals("admin")) {
            return errorResponse("Only admin user is allowed to take this action", HttpStatus.UNAUTHORIZED);
        }

        deviceDetailService.deleteAllDevices();

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setMessage("Successfully deleted all devices for all users");
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/addGroup", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> createNewDeviceHierarchy(@RequestBody DeviceHierarchy deviceHierarchy) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }

        if (StringUtils.isEmpty(deviceHierarchy.getHierarchyName())) {
            return errorResponse("GroupName can't be empty", HttpStatus.BAD_REQUEST);
        }

        try {
            UserT userT = userService.getUser(authentication.getName());

            DeviceHierarchy dh = deviceHierarchyRepository.findByHierarchyName(deviceHierarchy.getHierarchyName());
            if (dh != null) {
                return errorResponse("Group already exists ", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            deviceHierarchy.setUserId(userT.getId());
            deviceHierarchyRepository.save(deviceHierarchy);


            ResponseMessage statusMsg = new ResponseMessage();
            statusMsg.setStatus(HttpStatus.OK);
            statusMsg.setMessage("Successfully created new group: " + deviceHierarchy.getHierarchyName());
            return new ResponseEntity<>(statusMsg, HttpStatus.OK);
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/getgroup/{hierarchyId}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getDeviceHierarchy(@PathVariable(value = "hierarchyId") long hierarchyId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }

        try {
            DeviceHierarchy deviceHierarchy = deviceHierarchyRepository.findByHierarchyId(hierarchyId);

            ResponseMessage statusMsg = new ResponseMessage();
            statusMsg.setStatus(HttpStatus.OK);
            statusMsg.setMessage("Successfully retrieved group: " + deviceHierarchy.getHierarchyName());
            statusMsg.setData(deviceHierarchy);
            return new ResponseEntity<>(statusMsg, HttpStatus.OK);
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/getGroups", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getHierarchy() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }
        String sessionUser = authentication.getName();
        UserT user = userService.getUser(sessionUser);
        try {

            ResponseMessage statusMsg = new ResponseMessage();
            statusMsg.setStatus(HttpStatus.OK);
            statusMsg.setMessage("Successfully retrieved groups for user: " + authentication.getName());

            //if (authentication.getAuthorities().contains(UserRole.ROLE_ADMIN)) {
            if (authentication.getAuthorities().stream()
                    .anyMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                List<DeviceHierarchy> deviceHierarchy =
                        deviceHierarchyRepository.findAllByOrganization(user.getOrganizationId());
                statusMsg.setData(deviceHierarchy);
            } else {
                List<DeviceHierarchy> deviceHierarchy = deviceHierarchyRepository.findAllByUserId(user.getId());
                statusMsg.setData(deviceHierarchy);
            }

            return new ResponseEntity<>(statusMsg, HttpStatus.OK);
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/updateGroup/{hierarchyId}", method = RequestMethod.PUT)
    public ResponseEntity<ResponseMessage> updateDeviceHierarchy(@PathVariable(value = "hierarchyId") long hierarchyId,
                                                                 @RequestBody DeviceHierarchy updatedDeviceHierarchy) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }

        try {
            DeviceHierarchy deviceHierarchy = deviceHierarchyRepository.findByHierarchyId(hierarchyId);
            if (deviceHierarchy == null) {
                return errorResponse("Group could not be found", HttpStatus.NOT_FOUND);
            }

            deviceHierarchy.setHierarchyName(updatedDeviceHierarchy.getHierarchyName());

            deviceHierarchyRepository.save(deviceHierarchy);

            ResponseMessage statusMsg = new ResponseMessage();
            statusMsg.setStatus(HttpStatus.OK);
            statusMsg.setMessage("Successfully updated group: " + deviceHierarchy.getHierarchyName());
            return new ResponseEntity<>(statusMsg, HttpStatus.OK);
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/removeGroup/{hierarchyId}", method = RequestMethod.DELETE)
    public ResponseEntity<ResponseMessage> deleteDeviceHierarchy(
            @PathVariable(value = "hierarchyId") long hierarchyId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }

        try {
            DeviceHierarchy deviceHierarchy = deviceHierarchyRepository.findByHierarchyId(hierarchyId);
            if (deviceHierarchy == null) {
                return errorResponse("Group could not be found", HttpStatus.NOT_FOUND);
            }
            //TODO: need some efficient method to delete ManytoOne relation
            for (DeviceDetail dd : deviceHierarchy.getDeviceDetail()) {
                dd.setDeviceHierarchy(null);
            }


            deviceHierarchyRepository.delete(deviceHierarchy);

            ResponseMessage statusMsg = new ResponseMessage();
            statusMsg.setStatus(HttpStatus.OK);
            statusMsg.setMessage("Successfully deleted group: " + deviceHierarchy.getHierarchyName());
            return new ResponseEntity<>(statusMsg, HttpStatus.OK);
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Switch device flexibility on/off
    @RequestMapping(value = "/toggleDeviceFlexibility/{deviceid}/{state}", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> toggleDeviceFlexibility(@PathVariable(value = "deviceid") String deviceId,
                                                                   @PathVariable(value = "state") boolean state) {
        ResponseMessage responseMessage = new ResponseMessage();
        try {
            String sessionUser;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (this.getSessionUser(authentication) == null) {
                return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
            }
            sessionUser = authentication.getName();

            if (deviceId == null || deviceId.equals("")) {
                return errorResponse("DeviceId can not be empty or null", HttpStatus.BAD_REQUEST);
            }

            //check if device belongs to current user
            //if (!deviceId.split("@")[0].equals(sessionUser) && !authentication.getAuthorities().contains(UserRole
            // .ROLE_ADMIN)) {
            if (!deviceId.split("@")[0].equals(sessionUser) && authentication.getAuthorities().stream()
                    .noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                return errorResponse("Device not found", HttpStatus.UNAUTHORIZED);
            }

            //UserT user = userService.getUser(sessionUser);
            UserT user = userService.getUser(deviceId.split("@")[0]);

            DeviceDetail deviceDetail = deviceDetailService.getDevice(deviceId);

            if (deviceDetail == null) {
                return errorResponse("Device does not exist", HttpStatus.NOT_FOUND);
            }

            deviceDetailService.toggleDeviceFlexibility(deviceDetail.getDeviceId(), state);

            responseMessage.setMessage("Success");
            responseMessage.setStatus(HttpStatus.OK);
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (Exception ex) {
            responseMessage.setMessage("Error updating device flexibility");
            responseMessage.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    // Reset all org devices flexibility to default setting for organization
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/resetDeviceFlexibilityParamsForOrg", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> resetDeviceFlexibilityParams() {
        ResponseMessage responseMessage = new ResponseMessage();
        try {
            String sessionUser;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (this.getSessionUser(authentication) == null) {
                return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
            }
            sessionUser = authentication.getName();
            /*
            if (authentication.getAuthorities().stream().noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN
            .name()))) {
                return errorResponse(String.format("User: %s not an admin user", sessionUser), HttpStatus.NOT_FOUND);
            }
             */

            UserT user = userService.getUser(sessionUser);
            List<DeviceDetail> devices = userService.getDeviceListforOrganization(user.getOrganizationId());

            if (devices.size() == 0) {
                return errorResponse("No devices found for organization", HttpStatus.NOT_FOUND);
            }

            devices.forEach(device -> {
                deviceDetailService.resetDeviceFlexibilityParams(device.getDeviceId(), user.getOrganizationId());
            });

            responseMessage.setMessage("Success");
            responseMessage.setStatus(HttpStatus.OK);
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (Exception ex) {
            responseMessage.setMessage("Error updating organization devices flexibility");
            responseMessage.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    // Reset device flexibility to default setting for organization
    @RequestMapping(value = "/resetDeviceFlexibilityParams/{deviceid}", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> resetDeviceFlexibilityParams(
            @PathVariable(value = "deviceid") String deviceId) {
        ResponseMessage responseMessage = new ResponseMessage();
        try {
            String sessionUser;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (this.getSessionUser(authentication) == null) {
                return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
            }
            sessionUser = authentication.getName();

            if (deviceId == null || deviceId.equals("")) {
                return errorResponse("DeviceId can not be empty or null", HttpStatus.BAD_REQUEST);
            }

            //check if device belongs to current user
            //if (!deviceId.split("@")[0].equals(sessionUser) && !authentication.getAuthorities().contains(UserRole
            // .ROLE_ADMIN)) {
            if (!deviceId.split("@")[0].equals(sessionUser) && authentication.getAuthorities().stream()
                    .noneMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                return errorResponse("Device not found", HttpStatus.UNAUTHORIZED);
            }

            //UserT user = userService.getUser(sessionUser);
            UserT user = userService.getUser(deviceId.split("@")[0]);

            DeviceDetail deviceDetail = deviceDetailService.getDevice(deviceId);

            if (deviceDetail == null) {
                return errorResponse("Device does not exist", HttpStatus.NOT_FOUND);
            }

            deviceDetailService.resetDeviceFlexibilityParams(deviceDetail.getDeviceId(), user.getOrganizationId());

            responseMessage.setMessage("Success");
            responseMessage.setStatus(HttpStatus.OK);
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (Exception ex) {
            responseMessage.setMessage("Error updating device flexibility");
            responseMessage.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Create simulated devices for every user
     */
    @RequestMapping(value = "/createSimulatedUserAndDevices/{numDevices}", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> createSimulatedUserAndDevices(
            @PathVariable(value = "numDevices") int numDevices) {

        LOGGER.info("Received request to create {} simulated devices", numDevices);
        ResponseMessage responseMessage = new ResponseMessage();
        if (simulatedDeviceService.createSimulatedDevices(
                simulatedDeviceService.createSimulatedUser(), numDevices).equals("success")) {
            responseMessage.setStatus(HttpStatus.OK);
            responseMessage.setMessage("success");

        } else {
            responseMessage.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            responseMessage.setMessage("error");
        }
        return new ResponseEntity<>(responseMessage, responseMessage.getStatus());
    }


    // Updates flexibility parameters for all devices of in a given flexibility group (e.g. all wet devices, all tcl devices)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/updateDeviceFlexibilityParams/{flexGroup}", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> resetDeviceFlexibilityParams(@PathVariable("flexGroup") String flexGroup,
                                                                        @RequestBody DeviceFlexibilityDetail newDeviceFlexParams) {

        ResponseMessage responseMessage = new ResponseMessage();
        try {
            String sessionUser;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (this.getSessionUser(authentication) == null) {
                return errorResponse("Unauthorized to access the service", HttpStatus.UNAUTHORIZED);
            }
            sessionUser = authentication.getName();

            UserT user = userService.getUser(sessionUser);
            List<DeviceDetail> devices = userService.getDeviceListforOrganization(user.getOrganizationId())
                    .stream().filter(dd -> deviceFlexOfferGroup.getDeviceFOGroupType(dd.getDeviceType()) ==
                            FlexibilityGroupType.valueOf(flexGroup))
                    .collect(Collectors.toList());

            if (devices.size() == 0) {
                return errorResponse(String.format("No devices of flexibility group %s found for organization", flexGroup)
                        , HttpStatus.NOT_FOUND);
            }

            devices.forEach(device -> {
                deviceDetailService.updateDeviceFlexibilityParams(device.getDeviceId(), newDeviceFlexParams);
            });

            responseMessage.setMessage("Success");
            responseMessage.setStatus(HttpStatus.OK);
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (Exception ex) {
            responseMessage.setMessage("Error updating devices flexibility");
            responseMessage.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
