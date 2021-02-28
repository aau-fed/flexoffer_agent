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

package org.goflex.wp2.app.fmanintegration.measurements;

/**
 * This class sends heartbeat to FMAN-FMAR at regular interval
 * It capture the schedule FOID update sent by the FMAN
 * Created by bijay on 7/7/17.
 */

import org.goflex.wp2.app.common.AppRuntimeConfig;
import org.goflex.wp2.app.fmanintegration.measurements.entities.MeasurementPayload;
import org.goflex.wp2.app.fmanintegration.user.FmanUserService;
import org.goflex.wp2.core.entities.ConsumptionTuple;
import org.goflex.wp2.core.models.FmanUser;
import org.goflex.wp2.core.wrappers.OperationInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


@Component
public class SendMeasurementToFMAN {

    private static final Logger logger = LoggerFactory.getLogger(SendMeasurementToFMAN.class);
    private final long heartBeatRate = 60000;
    long leftLimit = 10L;
    long rightLimit = 100L;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private FMANMeasurementService fmanMeasurementService;
    @Autowired
    private FmanUserService fmanUserService;
    @Autowired
    private AppRuntimeConfig appRuntimeConfig;

    @Scheduled(fixedRate = heartBeatRate)
    public void sendMeasurement() {
        if (this.appRuntimeConfig.isSendHeartBeatToFMAN()) {
            logger.info("Sending measurement data to FMAN");
            HttpHeaders headers = new HttpHeaders();
            String token = "Bearer ";
            FmanUser usr = fmanUserService.getUserbyUsername("AAU");
            if (usr != null && !usr.getAPIKey().equals("")) {
                token = token.concat(usr.getAPIKey());
                if (!token.equals("Bearer ")) {
                    headers.set("Authorization", token);
                    Map<String, OperationInformation> optInfos = fmanMeasurementService.getOperationInfo();
                    fmanMeasurementService.saveOrganizationalConsumption();


                    Map<String, List<MeasurementPayload>> measurementPayloads = new HashMap<>();
                    if (optInfos != null) {
                        for (Map.Entry<String, OperationInformation> tuple : optInfos.entrySet()) {
                            List<MeasurementPayload> measurementPayload = new ArrayList<>();
                            for (ConsumptionTuple ct : tuple.getValue().getOperationPower()) {
                                if (ct != null) { // TODO: check why there is null value
                                    measurementPayload.add(new MeasurementPayload("sysadmin", ThreadLocalRandom.current().nextLong(1000000),
                                            ct.getTimestamp(), ct.getValue()));
                                }
                            }
                            measurementPayloads.put(tuple.getKey(), measurementPayload);
                        }
                    }
                    HttpEntity<Map<String, List<MeasurementPayload>>> postEntity = new HttpEntity<>(measurementPayloads, headers);
                    try {
                        ResponseEntity<String> response = restTemplate.postForEntity(fmanMeasurementService.getFMANMeasurementURL(), postEntity, String.class);
                        fmanMeasurementService._clearAggData();
                        logger.debug(response.getBody());
                    } catch (Exception ex) {
                        logger.warn(ex.getLocalizedMessage());
                        logger.warn("Could not connect to aggregator, please check FMAN is running on the given address");
                        fmanMeasurementService._clearAggData();
                    }
                }
            }
        }
    }
}
