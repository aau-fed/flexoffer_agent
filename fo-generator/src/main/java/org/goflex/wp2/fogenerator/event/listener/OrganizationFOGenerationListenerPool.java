
/*
 * Created by muhaftab
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
 *  Last Modified 12/9/19
 */

package org.goflex.wp2.fogenerator.event.listener;

import org.goflex.wp2.fogenerator.event.OrganizationFOGenerationEventPool;
import org.goflex.wp2.fogenerator.interfaces.FlexOfferGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class OrganizationFOGenerationListenerPool implements ApplicationListener<OrganizationFOGenerationEventPool> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationFOGenerationListenerPool.class);

    private final FlexOfferGenerator foGenerator;

    @Autowired
    public OrganizationFOGenerationListenerPool(FlexOfferGenerator foGenerator) {
        this.foGenerator = foGenerator;
    }

    @Override
    public void onApplicationEvent(OrganizationFOGenerationEventPool event) {

        LOGGER.info(event.getEventName() + " request received at " + new Date(event.getTimestamp()) +
                        ". organization: {}, prediction: {}",
                event.getOrganization().getOrganizationName(), event.getOrganizationPrediction().toString());
        if (event.getDeviceModel() == null) {
            LOGGER.error("Error executing " + event.getEventName() + " for organization: "
                    + event.getOrganization().getOrganizationName()  + " because no devices supplied.");
            return;
        }
        foGenerator.generatePoolFO(event.getOrganization(), event.getOrganizationPrediction(),
                event.getDeviceModel(), event.isInCoolingPeriod());
        LOGGER.info(event.getEventName() + " request executed.");
    }
}
