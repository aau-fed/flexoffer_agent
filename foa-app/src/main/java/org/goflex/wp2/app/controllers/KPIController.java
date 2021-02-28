package org.goflex.wp2.app.controllers;

import org.goflex.wp2.app.fmanintegration.user.FmanUserService;
import org.goflex.wp2.app.services.KPIServices;
import org.goflex.wp2.core.entities.KpiData;
import org.goflex.wp2.core.entities.ResponseMessage;
import org.goflex.wp2.core.entities.UserRole;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.core.wrappers.KPIWrapper;
import org.goflex.wp2.foa.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/v1.0/kpi")
public class KPIController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KPIController.class);

    @Autowired
    private KPIServices kpiServices;

    @Autowired
    private UserService userService;

    @Autowired
    private FmanUserService fmanUserService;

    private final List<Integer> activeUsers = Arrays.asList(0, 0);
    private final List<Integer> activeDevices = Arrays.asList(0, 0);
    private final List<Integer> flexOfferCount = Arrays.asList(0, 0);
    private final List<Double> flexibilityRatio = Arrays.asList(0.0, 0.0);
    private final List<Double> rewards = Arrays.asList(0.0, 0.0);


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


    private void setResponseKPIData(KPIWrapper kpi) {
        activeUsers.set(0, kpi.getActiveUser());
        activeDevices.set(0, kpi.getActiveDevice());
        flexOfferCount.set(0, kpi.getFoCount());
        flexibilityRatio.set(0, kpi.getFlexibilityRatio());
    }

    private int getChangeRatio(int current, int previous) {
        double delta = 0.0;

        if (current == 0 && previous == 0) {
            delta = 0.0;
        } else if (current > 0 && previous > 0) {
            delta = (((current - previous) * 1.0) / previous) * 100;
        } else if (current == 0) {
            delta = -100.0;
        } else if (previous == 0) {
            delta = 100.0;
        }

        return (int) delta;
    }

    private double getChangeRatio(double current, double previous) {
        double delta = 0.0;

        if (current == 0.0 && previous == 0.0) {
            delta = 0.0;
        } else if (current > 0.0 && previous > 0.0) {
            delta = (((current - previous) * 1.0) / previous) * 100;
        } else if (current == 0.0) {
            delta = -100.0;
        } else if (previous == 0.0) {
            delta = 100.0;
        }

        return delta;
    }


    @RequestMapping(value = "/foaKpi", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getFoaKpi() {

        String sessionUser;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getName() == null) {
            return null;
        }
        sessionUser = authentication.getName();

        ResponseMessage statusMsg = new ResponseMessage();
        try {
            statusMsg.setStatus(HttpStatus.OK);
            statusMsg.setMessage("Success");
            UserT usr = userService.getUser(sessionUser);
            if (usr == null) {
                statusMsg.setMessage("Unauthorized");
                return new ResponseEntity<>(statusMsg, HttpStatus.UNAUTHORIZED);
            }

            //get today statistic
            //getYesterday from Kpi

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date today = new Date();
            try {
                //today = formatter.parse("2018-06-27");
                today = formatter.parse(formatter.format(today.getTime()));
            } catch (Exception ex) {
            }
            Calendar c = Calendar.getInstance();
            c.setTime(today);
            //to get data for yesterday
            c.add(Calendar.DATE, -1);


            //Get KPI for today
            KPIWrapper kpiToday = kpiServices.getKPI(usr.getId(), usr.getOrganizationId(),
                    (Collection<GrantedAuthority>) authentication.getAuthorities(), today);

            //Get KPI for yesterday
            KPIWrapper kpiYesterday = kpiServices.getKPI(usr.getId(), usr.getOrganizationId(),
                    (Collection<GrantedAuthority>) authentication.getAuthorities(), c.getTime());

            //set kpi response

            //Check if kpi for today is null or not
            if (kpiToday == null && kpiYesterday == null) {
                //do nothing return 0 for all
            } else if (kpiToday == null) {
                this.setResponseKPIData(kpiYesterday);
            } else if (kpiYesterday == null) {
                this.setResponseKPIData(kpiToday);
            } else {
                this.setResponseKPIData(kpiToday);

                //Calculate the stats between two KPIS

                //Calculate change in active user
                activeUsers.set(1, this.getChangeRatio(kpiToday.getActiveUser(),
                        kpiYesterday.getActiveUser()));

                //calculate the change in active devices
                activeDevices.set(1, this.getChangeRatio(kpiToday.getActiveDevice(),
                        kpiYesterday.getActiveDevice()));

                //calculate flex-offer generation ratio
                flexOfferCount.set(1, this.getChangeRatio(kpiToday.getFoCount(),
                        kpiYesterday.getFoCount()));

                //calculate flex-offer generation ratio
                flexibilityRatio.set(1, this.getChangeRatio(kpiToday.getFlexibilityRatio(),
                        kpiYesterday.getFlexibilityRatio()));
            }

            // calculate rewards if requesting user is prosumer
            // todo: make a function out of it
            if (usr.getRole() == UserRole.ROLE_PROSUMER) {
                try {
                    String year = new SimpleDateFormat("yyyy").format(today);
                    String month = new SimpleDateFormat("MM").format(today);
                    ResponseEntity<Object> response;

                    response = fmanUserService.getUserBill(usr, year, month);

                    Map<String, Map<String, Double>> responseMap = new HashMap<>();
                    responseMap = (Map<String, Map<String, Double>>) response.getBody();
                    Double thisMonthReward = responseMap.get("data").get("rewardTotal");

                    Calendar c2 = Calendar.getInstance();
                    c2.setTime(today);
                    c2.add(Calendar.MONTH, -1);
                    year = new SimpleDateFormat("yyyy").format(c2.getTime());
                    month = new SimpleDateFormat("MM").format(c2.getTime());
                    response = fmanUserService.getUserBill(usr, year, month);
                    responseMap = (Map<String, Map<String, Double>>) response.getBody();
                    Double lastMonthReward = responseMap.get("data").get("rewardTotal");

                    rewards.set(0, thisMonthReward);
                    rewards.set(1, getChangeRatio(thisMonthReward, lastMonthReward));

                } catch (Exception ex) {
                    rewards.set(0, 0.0);
                    rewards.set(1, 0.0);
                    LOGGER.error(ex.getLocalizedMessage());
                    LOGGER.error("Error getting rewards from FMAN");
                }
            }

            KpiData kpiData = new KpiData(activeDevices, activeUsers,
                    flexibilityRatio, flexOfferCount, rewards);
            statusMsg.setData(kpiData);
            return new ResponseEntity<>(statusMsg, HttpStatus.OK);
        } catch (Exception ex) {
            statusMsg.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            statusMsg.setMessage("Error");
            return new ResponseEntity<>(statusMsg, HttpStatus.INSUFFICIENT_STORAGE);
        }
    }

    @RequestMapping(value = "/activeProsumer", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getActiveProsumers() {
        String sessionUser;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getName() == null) {
            return null;
        }
        sessionUser = authentication.getName();
        ResponseMessage statusMsg = new ResponseMessage();
        statusMsg.setStatus(HttpStatus.OK);
        statusMsg.setMessage("Success");
        LOGGER.debug(sessionUser);
        UserT usr = userService.getUser(sessionUser);
        if (usr == null) {
            statusMsg.setMessage("Unauthorized");
            return new ResponseEntity<>(statusMsg, HttpStatus.UNAUTHORIZED);

        }
        statusMsg.setData(kpiServices.getActiveProsumers(sessionUser, usr.getOrganizationId(), (Collection<GrantedAuthority>) authentication.getAuthorities()));
        return new ResponseEntity<>(statusMsg, HttpStatus.OK);
    }

    @RequestMapping(value = "/activeDevices", method = RequestMethod.GET)
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> getActiveDevices() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getName() == null) {
            return null;
        }
        ResponseMessage statusMsg = new ResponseMessage();
        statusMsg.setStatus(HttpStatus.OK);
        statusMsg.setMessage("Success");
        UserT usr = userService.getUser(authentication.getName());
        if (usr == null) {
            statusMsg.setMessage("Unauthorized");
            return new ResponseEntity<>(statusMsg, HttpStatus.UNAUTHORIZED);

        }
        statusMsg.setData(kpiServices.getActiveDevices(authentication.getName(), usr.getOrganizationId(),
                usr.getId(), (Collection<GrantedAuthority>) authentication.getAuthorities()));
        return new ResponseEntity<>(statusMsg, HttpStatus.OK);
    }

    @RequestMapping(value = "/getFlexibilityRatio", method = RequestMethod.GET)
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> getFlexibilityRatio() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getName() == null) {
            return null;
        }
        ResponseMessage statusMsg = new ResponseMessage();
        statusMsg.setStatus(HttpStatus.OK);
        statusMsg.setMessage("Success");
        UserT usr = userService.getUser(authentication.getName());
        Map<String, Double> data = kpiServices.getFlexibilityRatio(usr,
                usr.getOrganizationId(), (Collection<GrantedAuthority>) authentication.getAuthorities());
        /*double flexRatio = 0.0;
        if (data != null) {
            flexRatio = (data.get("flexibleDemand") / (data.get("totalConsumption"))) * 100.0;
        }
        if (Double.isNaN(flexRatio)) {
            flexRatio = 0.0;
        }*/

        statusMsg.setData(data.values());
        return new ResponseEntity<>(statusMsg, HttpStatus.OK);
    }


    //date is current date, if resolution is day get FOs for the given date or given month
    @RequestMapping(value = "/getFoCount/{date}/{resolution}", method = RequestMethod.GET)
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> getFOCounts(@PathVariable(value = "date") String date,
                                                       @PathVariable(value = "resolution") String resolution) {
        ResponseMessage statusMsg = new ResponseMessage();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getName() == null) {
            return null;
        }
        Date dateToExtract;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        if (resolution.equals("month")) {
            format = new SimpleDateFormat("yyyy-MM");
        }

        try {
            dateToExtract = format.parse(date);
        } catch (Exception ex) {
            return errorResponse("Invalid Date format, supported type \"yyyy-MM-dd\"", HttpStatus.BAD_REQUEST);
        }
        statusMsg.setStatus(HttpStatus.OK);
        statusMsg.setMessage("Success");
        //TODO: Filter ratio by usertype
        statusMsg.setData(kpiServices.getFOCount(authentication.getName(), dateToExtract, resolution,
                (Collection<GrantedAuthority>) authentication.getAuthorities()));
        return new ResponseEntity<>(statusMsg, HttpStatus.OK);
    }
}
