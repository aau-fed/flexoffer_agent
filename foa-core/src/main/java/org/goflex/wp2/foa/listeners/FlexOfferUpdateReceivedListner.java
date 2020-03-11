
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
 *  Last Modified 11/29/17 1:31 AM
 */

package org.goflex.wp2.foa.listeners;


import org.goflex.wp2.foa.events.FlexOfferUpdateReceivedEvent;
import org.goflex.wp2.foa.interfaces.FOAService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Created by bijay on 7/22/17.
 */

@Component
public class FlexOfferUpdateReceivedListner implements ApplicationListener<FlexOfferUpdateReceivedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(FlexOfferUpdateReceivedListner.class);

    @Autowired
    private FOAService foaService;

    @Override
    public void onApplicationEvent(FlexOfferUpdateReceivedEvent event) {
        logger.info(event.getEvent() + " at " + event.getTimestamp() + " for flex-offer with ID =>{}", event.getFlexOffer().getId());

        //update flex-offer id if new ID is received
        if (event.getNewUuid() != null && !event.getFlexOffer().getId().equals(event.getNewUuid())) {
            //delete old flex-offer

            //foaMemoryDOA.remove(event.getFlexOffer().getId());

            //save new flex-offer
            //foaMemoryDOA.put(event.getNewUuid(),event.getFlexOffer());
            //update new id
            //foaMemoryDOA.get(event.getNewUuid()).setId(event.getNewUuid());


        } else {
            //replace old flex-offer
            //foaMemoryDOA.replace(event.getFlexOffer().getId(), event.getFlexOffer());
        }
        //event.getFlexOffer().setFlexOfferSchedule(event.getNewSchedule());
        logger.info("Flex-offer Updated for ID =>{}", event.getFlexOffer().getId());
    }
}
