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
 *  Last Modified 2/21/18 11:48 PM
 */

package org.goflex.wp2.app.fmanintegration.heartbeat;

/**
 * This class sends heartbeat to FMAN-FMAR at regular interval
 * It capture the schedule FOID update sent by the FMAN
 * Created by bijay on 7/7/17.
 */

import org.goflex.wp2.app.common.AppRuntimeConfig;
import org.goflex.wp2.app.fmanintegration.measurements.FMANMeasurementService;
import org.goflex.wp2.app.fmanintegration.user.FmanUserService;
import org.goflex.wp2.core.models.FmanUser;
import org.goflex.wp2.core.wrappers.OperationInformation;
import org.goflex.wp2.core.wrappers.ScheduleWrapper;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.interfaces.FOAHeatBeatService;
import org.goflex.wp2.foa.interfaces.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@Component
public class SendHeartBeatToFMAN implements FOAHeatBeatService {

    private static final Logger logger = LoggerFactory.getLogger(SendHeartBeatToFMAN.class);
    private final long heartBeatRate = 60000;
    @Autowired
    FOAProperties foaProperties;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private FmanUserService fmanUserService;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private AppRuntimeConfig appRuntimeConfig;
    @Autowired
    private FMANMeasurementService fmanMeasurementService;

    @Override
    @Scheduled(fixedRate = heartBeatRate)
    public void sendHeartBeat() {
        if (appRuntimeConfig.isSendHeartBeatToFMAN()) {
            logger.info("Sending Heart Beat to FMAN");
            HttpHeaders headers = new HttpHeaders();
            String token = "Bearer ";
            FmanUser usr = fmanUserService.getUserbyUsername("AAU");
            if (usr != null && !usr.getAPIKey().equals("")) {
                token = token.concat(usr.getAPIKey());
                Map<String, OperationInformation> optInfos = fmanMeasurementService.getOperationInfo();
                headers.set("Authorization", token);
                Map<String, Integer> optStates = new HashMap<>();
                for (Map.Entry<String, OperationInformation> tuple : optInfos.entrySet()) {
                    optStates.put(tuple.getKey(), tuple.getValue().getOperationState());
                }
                HttpEntity<Map<String, Integer>> postEntity = new HttpEntity<>(optStates, headers);
                try {
                    ResponseEntity<ScheduleWrapper> response = restTemplate.postForEntity(
                            foaProperties.getFmanConnectionConfig().getHeartbeatURI(), postEntity, ScheduleWrapper.class);

                    /**Check if the response include schedule*/
                    if (response.getBody().getFlexOfferSchedule() != null) {
                        if (response.getBody().getFlexOfferSchedule().size() > 0) {
                            logger.info("New Schedules Received from FMAN");
                            //TODO: some issue with FMAN need to remove this
                            scheduleService.handleScheduleFromFMAN(response.getBody().getFlexOfferSchedule());
                        }
                    }
                    /**Check if the response include contract*/
                    if (response.getBody().getContract() != null) {
                        logger.info("New Contract Received from FMAN");
                        //scheduleService.handleScheduleFromFMAN(response.getBody().getFlexOfferSchedule());
                    }

                } catch (Exception ex) {
                    logger.warn(ex.getLocalizedMessage());
                    logger.warn("Could not connect to aggregator, please check fman is running on the given address");
                }
            }
        }

    }

    @Override
    public void receiveHeartBeat() {

    }

}
