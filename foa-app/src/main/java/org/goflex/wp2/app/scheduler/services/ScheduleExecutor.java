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
 *  Last Modified 2/8/18 4:17 PM
 */

package org.goflex.wp2.app.scheduler.services;


import org.goflex.wp2.app.common.AppRuntimeConfig;
import org.goflex.wp2.foa.controldetailmonitoring.ControlDetail;
import org.goflex.wp2.foa.controldetailmonitoring.ControlDetailService;
import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.OnOffSchedule;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.foa.events.FlexOfferStatusUpdateEvent;
import org.goflex.wp2.foa.implementation.ImplementationsHandler;
import org.goflex.wp2.foa.implementation.ScheduleServiceImpl;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.FOAService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class ScheduleExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleExecutor.class);


    private ScheduleServiceImpl scheduleService;

    private UserService userService;

    private DeviceDetailService deviceDetailService;

    private ImplementationsHandler implementationsHandler;

    private OrganizationRepository organizationRepository;

    private ControlDetailService controlDetailService;

    private ApplicationEventPublisher applicationEventPublisher;

    private AppRuntimeConfig appRuntimeConfig;

    private FOAService foaService;

    public ScheduleExecutor(ScheduleServiceImpl scheduleService,
                            UserService userService,
                            DeviceDetailService deviceDetailService,
                            ImplementationsHandler implementationsHandler,
                            OrganizationRepository organizationRepository,
                            ControlDetailService controlDetailService,
                            ApplicationEventPublisher applicationEventPublisher,
                            AppRuntimeConfig appRuntimeConfig,
                            FOAService foaService) {
        this.scheduleService = scheduleService;
        this.userService = userService;
        this.deviceDetailService = deviceDetailService;
        this.implementationsHandler = implementationsHandler;
        this.organizationRepository = organizationRepository;
        this.controlDetailService = controlDetailService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.appRuntimeConfig = appRuntimeConfig;
        this.foaService = foaService;
    }

    @Async
    public void processSchedule(OnOffSchedule schedule) {
        logger.info("Executing schedule for {}", schedule.getDeviceID());
        DeviceDetail device = deviceDetailService.getDevice(schedule.getDeviceID());

        // added to avoid sending control signals to production loads
        if (device.getDeviceType() == DeviceType.PV || device.getDeviceType() == DeviceType.Wind) {
            return;
        }

        String tempUserName = schedule.getDeviceID().split("@")[0];
        String tempDeviceID = schedule.getDeviceID().split("@")[1];
        UserT user = userService.getUser(tempUserName);
        if (organizationRepository.findByOrganizationId(user.getOrganizationId()).getDirectControlMode() == OrganizationLoadControlState.Active) {
            //    || (device.getDeviceHierarchy() != null && device.getDeviceHierarchy().getHierarchyName().equals("goflex-dc-001"))) {
            DeviceParameters deviceParameters = new DeviceParameters(user.getTpLinkUserName(), user.getPassword(), user.getAPIKey(), "");
            if (schedule.getScheduleToState() == 1) {
                try {
                    implementationsHandler.get(device.getPlugType())
                            .startDevice(user.getUserName(), schedule.getDeviceID(), deviceParameters);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (schedule.getScheduleToState() == 0) {
                implementationsHandler.get(device.getPlugType())
                        .stopDevice(user.getUserName(), schedule.getDeviceID(), deviceParameters);
            }
            controlDetailService.saveControlDetail(new ControlDetail(user.getOrganizationId(), user.getUserName(),
                    schedule.getDeviceID(), new Date(), schedule.getScheduleToState(),
                    schedule.getFlexOfferId(), schedule.getScheduleTable().getScheduleId(), true));
        } else {
            // Store control detail to database for analysis
            controlDetailService.saveControlDetail(new ControlDetail(user.getOrganizationId(), user.getUserName(),
                    schedule.getDeviceID(), new Date(), schedule.getScheduleToState(),
                    schedule.getFlexOfferId(), schedule.getScheduleTable().getScheduleId(), false));
        }

        // update flex-offer status
        FlexOfferState newState = schedule.isLast() ? FlexOfferState.Executed : FlexOfferState.InAdaptation;
        FlexOffer fo = foaService.getFlexOfferByFoID(UUID.fromString(schedule.getFlexOfferId()));
        if (fo != null && fo.getState() != newState) {
            FlexOfferStatusUpdateEvent flexOfferStatusUpdateEvent = new FlexOfferStatusUpdateEvent(this,
                    String.format("FO Status Updated from '%s' to '%s'", fo.getState().name(), newState.name()),
                    "", UUID.fromString(schedule.getFlexOfferId()), newState);
            applicationEventPublisher.publishEvent(flexOfferStatusUpdateEvent);
        }

        logger.debug("Updating schedule status for flex-offer with FoID=>{} and deviceID=>{}", schedule.getFlexOfferId(), tempDeviceID);
        scheduleService.updateScheduleStatus(schedule);
    }


    //@Scheduled(fixedRate = 6000)
    @Scheduled(cron = "0 * * * * *")
    public void executeSchedule() {
        if (this.appRuntimeConfig.isRunScheduler()) {
            logger.debug("Executing Scheduler");
            Date date = new Date();
            Date date1 = new Date();

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                date1 = formatter.parse(formatter.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<OnOffSchedule> schedules = scheduleService.getAllUnPushedSchedules(date1);
            schedules.forEach(this::processSchedule);
        }
    }


}
