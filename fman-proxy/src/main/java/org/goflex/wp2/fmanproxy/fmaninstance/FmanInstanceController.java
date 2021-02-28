package org.goflex.wp2.fmanproxy.fmaninstance;


import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.goflex.wp2.fmanproxy.common.exception.CustomException;
import org.goflex.wp2.fmanproxy.user.UserRole;
import org.goflex.wp2.fmanproxy.user.UserT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author muhaftab
 * created: 11/1/18
 */
@RestController
@RequestMapping("/fmanproxy")
public class FmanInstanceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FmanInstanceController.class);

    @Autowired
    FmanInstanceService fmanInstanceService;

    @Resource(name = "scheduleDetail")
    ConcurrentHashMap<UUID, Object> scheduleDetail;

    @PostMapping(value = "register", produces = "application/json")
    @ApiOperation(value = "Registers a news FMAN instance or a user with already existing FMAN instance")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong. Please verify the request."),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")
    })
    public ResponseEntity register(@RequestBody Map<String, String> reqBody, Authentication auth) {

        if (reqBody.get("type").equals("organization")) { // request is to create new FMAN instance

            reqBody.put("brokerId", ((UserT) auth.getPrincipal()).getUserId().toString());
            FmanInstanceT fmanInstanceT = fmanInstanceService.createFmanInstance(prepareFmanInstance(reqBody));
            if (fmanInstanceT != null) {
                return new ResponseEntity(ImmutableMap.of("message", "success", "data", fmanInstanceT), HttpStatus.OK);
            } else {
                throw new CustomException("Failed to register FMAN instance. Please verify your request!", HttpStatus.UNPROCESSABLE_ENTITY);
            }
        } else if (reqBody.get("type").equals("prosumer")) { // request to create new user on a particular FMAN instance
            return this.fmanInstanceService.registerUserWithFman(reqBody);
        } else {
            throw new CustomException(String.format("error: unknown registration type: '%s'", reqBody.get("type")), HttpStatus.UNPROCESSABLE_ENTITY);
        }

    }

    private FmanInstanceT prepareFmanInstance(Map<String, String> instanceDetails) {
        try {
            FmanInstanceT fmanInstanceT = new FmanInstanceT();
            String key;

            key = "organizationName";
            if (instanceDetails.containsKey(key) && !instanceDetails.get(key).equals("")) {
                fmanInstanceT.setInstanceName(instanceDetails.get(key));
            } else {
                throw new CustomException(String.format("'%s' must be provided!", key), HttpStatus.BAD_REQUEST);
            }

            key = "role";
            if (instanceDetails.containsKey(key) && !instanceDetails.get(key).equals("")) {
                String role = instanceDetails.get(key);
                fmanInstanceT.setAuthorizedRole(UserRole.valueOf(role));
            } else {
                throw new CustomException(String.format("'%s' must be provided!", key), HttpStatus.BAD_REQUEST);
            }

            key = "brokerId";
            if (instanceDetails.containsKey(key) && !instanceDetails.get(key).equals("")) {
                fmanInstanceT.setBrokerId(Long.valueOf(instanceDetails.get(key)));
            } else {
                throw new CustomException(String.format("'%s' must be provided!", key), HttpStatus.BAD_REQUEST);
            }

            key = "url";
            if (instanceDetails.containsKey(key) && !instanceDetails.get(key).equals("")) {
                fmanInstanceT.setInstanceUrl(instanceDetails.get(key));
                fmanInstanceT.setActivationDate(new Date());
                fmanInstanceT.setInstanceStatus(InstanceStatus.ACTIVE);
            } else {
                fmanInstanceT.setInstanceUrl(null);
                fmanInstanceT.setActivationDate(null);
                fmanInstanceT.setInstanceStatus(InstanceStatus.REQUESTED);
            }

            fmanInstanceT.setRegistrationDate(new Date());

            return fmanInstanceT;
        } catch (Exception e) {
            throw new CustomException(String.format("%s", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping(value = "update", produces = "application/json")
    @ApiOperation(value = "Updates an existing FMAN instance details")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong. Please verify the request."),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")
    })
    public Map updateInstance(@RequestBody FmanInstanceT fmanInstanceT) {
        FmanInstanceT updatedFmanInstanceT = fmanInstanceService.updateFmanInstance(fmanInstanceT);
        if (updatedFmanInstanceT != null) {
            return ImmutableMap.of("status", HttpStatus.OK, "message", "success", "data", updatedFmanInstanceT);
        } else {
            throw new CustomException("Failed to update FMAN instance. Please verify your request!", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }


    @GetMapping(value = "get/{instanceName}", produces = "application/json")
    @ApiOperation(value = "Gets a single FMAN instance by name. Name must be provided as @PathVariable")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong. Please verify the request."),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")
    })
    public Map getInstance(@PathVariable String instanceName) {
        if (instanceName == null || instanceName.equals("")) {
            throw new CustomException("Must provide 'instanceName'!", HttpStatus.BAD_REQUEST);
        }
        FmanInstanceT fmanInstanceT = fmanInstanceService.getFmanInstanceByName(instanceName);
        if (fmanInstanceT == null) {
            throw new CustomException(String.format("No FMAN instance found with name: '%s'", instanceName), HttpStatus.NOT_FOUND);
        }
        return ImmutableMap.of("status", HttpStatus.OK, "message", "success", "data", fmanInstanceT);
    }


    @DeleteMapping(value = "delete/{instanceName}", produces = "application/json")
    @ApiOperation(value = "Deletes a FMAN instance by name. Name must be provided as @PathVariable")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong. Please verify the request."),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")
    })
    public Map deleteInstance(@PathVariable String instanceName) {
        if (instanceName == null || instanceName.equals("")) {
            throw new CustomException("Must provide 'instanceName'!", HttpStatus.BAD_REQUEST);
        }
        if (fmanInstanceService.getFmanInstanceByName(instanceName) == null) {
            throw new CustomException(String.format("No FMAN instance found with name: '%s'", instanceName), HttpStatus.NOT_FOUND);
        }

        fmanInstanceService.deleteFmanInstanceByName(instanceName);
        return ImmutableMap.of("status", HttpStatus.OK, "message", "success");
    }

    @GetMapping(value = "getAllInstances", produces = "application/json")
    @ApiOperation(value = "Gets all FMAN instances registered with the proxy")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong. Please verify the request."),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")
    })
    public Map getAllInstances() {

        List<FmanInstanceT> fmanInstances = fmanInstanceService.getAllFmanInstances();
        if (fmanInstances == null || fmanInstances.size() == 0) {
            throw new CustomException(String.format("No FMAN instances exist in db"), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return ImmutableMap.of("status", HttpStatus.OK, "message", "success", "data", fmanInstances);
    }

    @PostMapping(value = "flexoffer/{instanceName}", produces = "application/json")
    @ApiOperation(value = "Recieves a new FlexOffer and relays it to the corresponding FMAN instance")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong. Please verify the request."),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")
    })
    public ResponseEntity postFlexOffer(@PathVariable String instanceName, @RequestBody Object flexOffer) {

        FmanInstanceT fmanInstanceT = fmanInstanceService.getFmanInstanceByName(instanceName);
        doInstanceChecks(fmanInstanceT, instanceName);
        // relay the flexoffer to the concerned FMAN instance
        return fmanInstanceService.sendFlexOfferToFMAN(fmanInstanceT, flexOffer);
    }

    @PutMapping(value = "flexoffer/{instanceName}", produces = "application/json")
    @ApiOperation(value = "Recieves a FlexOffer update and relays it to the corresponding FMAN instance")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong. Please verify the request."),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")
    })
    public ResponseEntity postFlexOfferUpdate(@PathVariable String instanceName, @RequestBody Object flexOffers) {

        FmanInstanceT fmanInstanceT = fmanInstanceService.getFmanInstanceByName(instanceName);
        doInstanceChecks(fmanInstanceT, instanceName);
        // relay the updated flexoffer to the concerned FMAN instance
        return fmanInstanceService.updateFlexOfferOnFMAN(fmanInstanceT, flexOffers);
    }


    @PostMapping(value = "schedules", produces = "application/json")
    @ApiOperation(value = "Gets all schedules from all FMAN instances")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong. Please verify the request."),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")
    })
    public ResponseEntity<Object> getSchedules() {
        ConcurrentHashMap<UUID, Object> scheduleDetailCopy = new ConcurrentHashMap<>(scheduleDetail);
        scheduleDetail.keySet().removeAll(scheduleDetailCopy.keySet()); // remove elements of the copy only
        return new ResponseEntity<>(ImmutableMap.of("id", UUID.randomUUID(), "flexOfferSchedule", scheduleDetailCopy), HttpStatus.OK);
    }


    @PostMapping(value = "measurement", produces = "application/json")
    @ApiOperation(value = "Sends measurements to all FMAN instances")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong. Please verify the request."),
            @ApiResponse(code = 422, message = "Invalid username/password supplied")
    })
    public Map postMeasurements(@RequestBody Map<String, Object> reqBody) {
        return this.fmanInstanceService.sendMeasurementsToFmanInstances(reqBody);
    }


    @GetMapping(value = "/contract/{instanceName}/{userName}")
    @ApiOperation(value = "Gets contract for the given user from the corresponding FMAN instance")
    public ResponseEntity getUserContract(@PathVariable String instanceName, @PathVariable String userName) {
        FmanInstanceT fmanInstanceT = fmanInstanceService.getFmanInstanceByName(instanceName);
        doInstanceChecks(fmanInstanceT, instanceName);
        return this.fmanInstanceService.getUserContractFromFmanInstance(fmanInstanceT, userName);
    }


    @GetMapping(value = "/bill/{instanceName}/{userName}/{year}/{month}")
    @ApiOperation(value = "Gets bill for the given user and month from the corresponding FMAN instance")
    public ResponseEntity getUserBill(@PathVariable String instanceName, @PathVariable String userName,
                                      @PathVariable String year, @PathVariable String month) {
        FmanInstanceT fmanInstanceT = fmanInstanceService.getFmanInstanceByName(instanceName);
        doInstanceChecks(fmanInstanceT, instanceName);
        return this.fmanInstanceService.getUserBillFromFmanInstance(fmanInstanceT, userName, year, month);
    }


    @GetMapping(value = "/getAllUsers/{instanceName}")
    @ApiOperation(value = "Gets all users from a given FMAN instance")
    public ResponseEntity getAllUsersFromFmanInstance(@PathVariable(value = "instanceName") String instanceName) {

        FmanInstanceT fmanInstanceT = fmanInstanceService.getFmanInstanceByName(instanceName);
        doInstanceChecks(fmanInstanceT, instanceName);
        return this.fmanInstanceService.getAllUsersFromFmanInstance(fmanInstanceT);
    }


    @GetMapping(value = "/getAllUserNames/{instanceName}")
    @ApiOperation(value = "Gets all userNames from a given FMAN instance")
    public ResponseEntity getAllUserNamesFromFmanInstance(@PathVariable(value = "instanceName") String instanceName) {

        FmanInstanceT fmanInstanceT = fmanInstanceService.getFmanInstanceByName(instanceName);
        doInstanceChecks(fmanInstanceT, instanceName);
        return this.fmanInstanceService.getAllUserNamesFromFmanInstance(fmanInstanceT);
    }


    @PostMapping(value = "registerMultipleUsers/{instanceName}")
    @ApiOperation("Registers multiple users with a given FMAN instance")
    public ResponseEntity registerMultipleUsersWithFmanInstance(
            @PathVariable(value = "instanceName") String instanceName,
            @RequestBody List<Map<String, String>> userList) {

        List<Map<String, String>> msgList = new ArrayList<>();
        userList.forEach(user -> {
            if (!user.containsKey("organizationName")) {
                user.put("organizationName", instanceName);
            }
            ResponseEntity response = this.fmanInstanceService.registerUserWithFman(user);
            msgList.add(ImmutableMap.of(user.get("userName"), String.valueOf(response.getStatusCodeValue())));
        });

        return new ResponseEntity(msgList, HttpStatus.OK);
    }


    /**
     * sanity checks
     * @param fmanInstanceT
     * @param instanceName
     */
    private void doInstanceChecks(FmanInstanceT fmanInstanceT, String instanceName) {
        // first check if instance exists and active
        if (fmanInstanceT == null) {
            throw new CustomException(String.format("FMAN instance '%s' does not exist", instanceName), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (fmanInstanceT.getInstanceStatus() != InstanceStatus.ACTIVE) {
            throw new CustomException(String.format("FMAN instance '%s' not operational", instanceName), HttpStatus.UNPROCESSABLE_ENTITY);
        }

    }
}
