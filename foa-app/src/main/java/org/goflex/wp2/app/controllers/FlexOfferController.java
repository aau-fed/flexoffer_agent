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
 *  Last Modified 8/30/17 9:52 PM
 */

package org.goflex.wp2.app.controllers;

import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexOfferConstraint;
import org.goflex.wp2.core.entities.ResponseMessage;
import org.goflex.wp2.core.models.Contract;
import org.goflex.wp2.core.models.FlexOfferT;
import org.goflex.wp2.core.models.Organization;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.core.wrappers.ProsumerFOWrapper;
import org.goflex.wp2.foa.events.FlexOfferGeneratedEvent;
import org.goflex.wp2.foa.events.FlexOfferUpdatedEvent;
import org.goflex.wp2.foa.interfaces.ContractService;
import org.goflex.wp2.foa.interfaces.FOAService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.goflex.wp2.foa.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by bijay on 8/30/17.
 */
@Controller
@RequestMapping("/api/v1.0/fo")
public class FlexOfferController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlexOfferController.class);

    private UserService userService;
    private ContractService contractService;
    private ApplicationEventPublisher applicationEventPublisher;
    private FOAService foaService;
    private OrganizationRepository organizationRepository;

    @Autowired
    public FlexOfferController(UserService userService,
                               ContractService contractService,
                               ApplicationEventPublisher applicationEventPublisher,
                               FOAService foaService,
                               OrganizationRepository organizationRepository) {
        this.userService = userService;
        this.contractService = contractService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.foaService = foaService;
        this.organizationRepository = organizationRepository;
    }

    private String getSessionUser(Authentication authentication) {
        return authentication.getName();
    }


    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    FlexOfferConstraint getFOConstraint(@RequestParam(value = "min", required = false, defaultValue = "2.0") double min,
                                        @RequestParam(value = "max", required = false, defaultValue = "2.0")
                                                double max) {

        return new FlexOfferConstraint(min, max);
    }

    private ResponseEntity<ResponseMessage> errorResponse(String msg, HttpStatus status) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(status);
        responseMessage.setMessage(msg);
        return new ResponseEntity<>(responseMessage, status);
    }

    private ResponseEntity<ResponseMessage> successResponse(String msg, HttpStatus status, Object data) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(status);
        responseMessage.setMessage(msg);
        responseMessage.setData(data);
        return new ResponseEntity<>(responseMessage, status);
    }

    @RequestMapping(value = "/getFos/{foDate}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getFOforDate(@PathVariable(value = "foDate") String foDate) {

        String sessionUser;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) != null) {
            sessionUser = authentication.getName();
        } else {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }
        Date foDt;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            foDt = format.parse(foDate);
        } catch (Exception ex) {
            return errorResponse("Invalid Date format, supported type \"yyyy-MM-dd\"", HttpStatus.BAD_REQUEST);
        }
        List<Object> params = new ArrayList<>();
        params.add(sessionUser);
        params.add(foDt);
        List<ProsumerFOWrapper> flexOffers = foaService.getFlexOfferByClientIDCreationTime(params).stream()
                .map(flexOffer -> new ProsumerFOWrapper(flexOffer.getFlexOfferSchedule().getStartTime(),
                        Arrays.stream(flexOffer.getFlexOfferSchedule().getScheduleSlices())
                                .map(slice -> slice.getEnergyAmount()).collect(Collectors.toList())))
                .collect(Collectors.toList());
        return successResponse("Success", HttpStatus.OK, flexOffers);
    }

    @RequestMapping(value = "/getFoById/{foID}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getFOforFoID(@PathVariable(value = "foID") UUID foID) {
        String sessionUser;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) != null) {
            sessionUser = authentication.getName();
        } else {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }
        return successResponse("Success", HttpStatus.OK, foaService.getFlexOfferByFoID(foID));
    }

    @RequestMapping(value = "/getFos", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getFO() {

        List<ProsumerFOWrapper> flexOffers = new ArrayList<>();

        for (int i = 0; i <= 2; i++) {
            ProsumerFOWrapper proFo = new ProsumerFOWrapper();
            proFo.setScheduleStartTime(DateUtil.toAbsoluteTime(DateUtil.toFlexOfferTime(new Date()) + i * 2));
            proFo.addAmount(5);
            proFo.setDeviceId("device" + i);
            flexOffers.add(proFo);
        }

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setData(flexOffers);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/getSchedules/{plugID}/{fmDate}/{toDate}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getSchedulesforDate(@PathVariable(value = "plugID") String plugID,
                                                               @PathVariable(value = "fmDate") String fmDate,
                                                               @PathVariable(value = "toDate") String toDate
                                                               ) {

        String sessionUser;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) != null) {
            sessionUser = authentication.getName();
        } else {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }

        Date fmDt;
        Date toDt;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            fmDt = format.parse(fmDate);
            toDt = format.parse(toDate);
        } catch (Exception ex) {
            return errorResponse("Invalid Date format, supported type \"yyyy-MM-dd\"", HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(plugID)) {
            return errorResponse("PlugID cannot be empty", HttpStatus.BAD_REQUEST);
        }
        List<Object> params = new ArrayList<>();
        params.add(plugID.split("@")[1]);
        params.add(fmDt);
        params.add(toDt);

        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        List<FlexOfferT> flexOfferTs = foaService.getFlexOfferTByPlugIdAndStartTimeAndEndTime(params);
        List<ProsumerFOWrapper> prosumerFOWrappers = flexOfferTs
                .stream()
                .map(foT -> new ProsumerFOWrapper(foT.getCreationTime(),
                        //foT.getFlexoffer().getFlexOfferSchedule().getStartTime(),
                        foT.getScheduleStartTime(),
                        Arrays.stream(foT.getFlexoffer().getFlexOfferSchedule().getScheduleSlices())
                                .map(slice -> slice.getEnergyAmount())
                                .collect(Collectors.toList()),
                        foT.getFlexoffer().getOfferedById(), foT.getFlexoffer(), foT.getFoType()))
                .collect(Collectors.toList());

        return successResponse("Success", HttpStatus.OK, prosumerFOWrappers);
    }

    @RequestMapping(value = "/getSchedules/{schDate}/{plugID}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getSchedulesforDate(@PathVariable(value = "schDate") String schDate,
                                                               @PathVariable(value = "plugID") String plugID) {

        String sessionUser;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) != null) {
            sessionUser = authentication.getName();
        } else {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }

        Date schDt;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            schDt = format.parse(schDate);
        } catch (Exception ex) {
            return errorResponse("Invalid Date format, supported type \"yyyy-MM-dd\"", HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(plugID)) {
            return errorResponse("PlugID cannot be empty", HttpStatus.BAD_REQUEST);
        }
        List<Object> params = new ArrayList<>();
        params.add(plugID.split("@")[1]);
        params.add(schDt);

        List<FlexOfferT> flexOfferTs = foaService.getFlexOfferTByPlugIdAndDate(params);
        List<ProsumerFOWrapper> prosumerFOWrappers = flexOfferTs
                .stream()
                .map(foT -> new ProsumerFOWrapper(foT.getCreationTime(),
                        foT.getFlexoffer().getFlexOfferSchedule().getStartTime(),
                        Arrays.stream(foT.getFlexoffer().getFlexOfferSchedule().getScheduleSlices())
                                .map(slice -> slice.getEnergyAmount())
                                .collect(Collectors.toList()),
                        foT.getFlexoffer().getOfferedById(), foT.getFlexoffer(), foT.getFoType()))
                .collect(Collectors.toList());

        return successResponse("Success", HttpStatus.OK, prosumerFOWrappers);
    }


    @RequestMapping(value = "/getReward/{year}/{month}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getRewardforMonth(@PathVariable(value = "year") String year,
                                                             @PathVariable(value = "month") String month) {

        String sessionUser;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) != null) {
            sessionUser = authentication.getName();
        } else {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }
        Date date;
        try {
            date = new SimpleDateFormat("MMMM").parse(month);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int yr = Integer.parseInt(year);
            List<Double> reward =
                    foaService.getRewardForMonth(sessionUser, yr, cal.get(Calendar.MONTH));//cal.get(Calendar.MONTH));
            return successResponse("Success", HttpStatus.OK, reward);
        } catch (ParseException e) {
            return errorResponse("Error in parsing input data", HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/saveContract", method = RequestMethod.POST)
    public ResponseEntity<?> saveContract(@RequestBody Contract contract) {
        if (contract.getContractId() == null) {
            contractService.saveContract(contract);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/getContract", method = RequestMethod.GET)
    public ResponseEntity<?> updateContract() {

        String sessionUser;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) != null) {
            sessionUser = authentication.getName();
        } else {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }

        return successResponse("Success", HttpStatus.OK, contractService.getContract(sessionUser));
    }

    @RequestMapping(value = "/updateContract", method = RequestMethod.POST)
    public ResponseEntity<?> updateContract(@RequestBody Contract contract) {
        if (contract.getContractId() == null) {
            contractService.updateContract(contract);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/fogGeneratedEvent", method = RequestMethod.POST)
    public ResponseEntity<?> saveContract(@RequestBody FlexOffer flexOffer) {
        FlexOfferGeneratedEvent foGeneratedEvent =
                new FlexOfferGeneratedEvent(this, "FlexOffer Generation Request Received", "", flexOffer,
                        null); //Create new event
        applicationEventPublisher.publishEvent(foGeneratedEvent);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/clearOrgFosForDate/{foDate}", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> clearOrgFosforDate(@PathVariable(value = "foDate") String foDate) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserT adminUsr = userService.getUser(authentication.getName());
            if (adminUsr == null) {
                return errorResponse(String.format("Admin user: %s not found", authentication.getName()),
                        HttpStatus.UNAUTHORIZED);
            }

            Organization organization = organizationRepository.findByOrganizationId(adminUsr.getOrganizationId());
            if (organization == null) {
                String msg = String.format("Organization with id: %d does not exist", adminUsr.getOrganizationId());
                return errorResponse(msg, HttpStatus.UNPROCESSABLE_ENTITY);
            }

            Date foDt;
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                foDt = format.parse(foDate);
            } catch (Exception ex) {
                return errorResponse("Invalid Date format, supported type \"yyyy-MM-dd\"", HttpStatus.BAD_REQUEST);
            }

            List<Object> params = new ArrayList<>();
            params.add(organization.getOrganizationId());
            params.add(foDt);
            List<FlexOffer> todaysFos = foaService.getFlexOffersByOrganizationIDCreationTime(params);
            todaysFos.forEach(flexOffer -> foaService.deleteFlexOffer(flexOffer.getId()));

            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setMessage(String.format("Success: deleted %d FlexOffers", todaysFos.size()));
            responseMessage.setStatus(HttpStatus.OK);
            return new ResponseEntity(responseMessage, HttpStatus.OK);
        } catch (Exception ex) {
            return errorResponse(ex.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/updateFosOnFMAN")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    ResponseEntity<ResponseMessage> updateFosOnFMAN() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserT adminUsr = userService.getUser(authentication.getName());
            if (adminUsr == null) {
                return errorResponse(String.format("Admin user: %s not found", authentication.getName()),
                        HttpStatus.UNAUTHORIZED);
            }

            Organization organization = organizationRepository.findByOrganizationId(adminUsr.getOrganizationId());
            if (organization == null) {
                String msg = String.format("Organization with id: %d does not exist", adminUsr.getOrganizationId());
                return errorResponse(msg, HttpStatus.UNPROCESSABLE_ENTITY);
            }

            List<FlexOffer> orgFos = this.foaService.getFlexOffersByOrganizationID(organization.getOrganizationId());


            FlexOfferUpdatedEvent event = new FlexOfferUpdatedEvent(
                    this, "FO Update", "", orgFos, organization);
            this.applicationEventPublisher.publishEvent(event);

            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setStatus(HttpStatus.OK);
            responseMessage.setMessage(String.format("Successfully updated %d FOs", orgFos.size()));
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            responseMessage.setMessage("Error updating FOs");
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
