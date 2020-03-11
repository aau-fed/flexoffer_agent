
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
 *  Last Modified 2/2/18 2:27 PM
 */

package org.goflex.wp2.fogenerator.event.listener;

import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.OrganizationLoadControlState;
import org.goflex.wp2.foa.events.DeviceStateChangeEvent;
import org.goflex.wp2.fogenerator.event.TCLFOGeneration;
import org.goflex.wp2.fogenerator.interfaces.FlexOfferGenerator;
import org.goflex.wp2.fogenerator.interfaces.impl.TCLForecastService;
import org.goflex.wp2.fogenerator.model.PredictionTs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TCLFOGenerationListener implements ApplicationListener<TCLFOGeneration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TCLFOGenerationListener.class);

    private TCLForecastService tclForecastService;
    private FlexOfferGenerator foGenerator;
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public TCLFOGenerationListener(TCLForecastService tclForecastService,
                                   FlexOfferGenerator foGenerator,
                                   ApplicationEventPublisher applicationEventPublisher) {
        this.tclForecastService = tclForecastService;
        this.foGenerator = foGenerator;
        this.applicationEventPublisher = applicationEventPublisher;
    }


    @Override
    public void onApplicationEvent(TCLFOGeneration event) {
        LOGGER.info(event.getEventName() + " request received at " + new Date(event.getTimestamp()) +
                        ". deviceId: {}, alias: {}",
                event.getDeviceDetail().getDeviceId(), event.getDeviceDetail().getAlias());
        Date dt = new Date();
        // check if forecast model exist for the device  if no forecast model exists, build one
        if (!tclForecastService.hasForecastModel(event.getDeviceDetail())) {
            LOGGER.warn("FO not generated, Prediction model not available");
            // create forecast model for the device
            tclForecastService.buildForecastModel(event.getDeviceDetail(),
                    event.getDeviceDetail().getDeviceType().toString());
        } else {
            // get prediction for the next time
            PredictionTs predictedValues =
                    tclForecastService.getPredictionForNextTs(event.getDeviceDetail(), dt);
            if (predictedValues.isCorrect()) {

                FlexOffer fo = foGenerator.generateFO(event.getDeviceDetail(), predictedValues, event.getOrganization());

                // turn off TCL device if fo type is 1
                if (fo != null && predictedValues.getFoType() == 1) {
                    // turn device off if org control is enabled
                    if (event.getOrganization().getDirectControlMode() == OrganizationLoadControlState.Active) {
                        DeviceStateChangeEvent stateChangeEvent = new DeviceStateChangeEvent(
                                this, "Device turned off upon FO generation", event.getDeviceDetail(), false);
                        this.applicationEventPublisher.publishEvent(stateChangeEvent);
                    }
                }

                LOGGER.info(event.getEventName() + " request executed.");
            } else {
                LOGGER.warn("{} request rejected due to incorrect Predicted values: {}", event.getEventName(),
                        predictedValues.toString());
            }
        }
    }
}
