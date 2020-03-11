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
 *  Last Modified 7/26/17 7:22 PM
 */

package org.goflex.wp2.foa.events;

import org.goflex.wp2.core.entities.FlexOffer;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Created by bijay on 7/22/17.
 */
public class FlexOfferUpdateReceivedEvent extends ApplicationEvent {
    String eventName;
    private UUID newUuid;
    private FlexOffer flexOffer;

    public FlexOfferUpdateReceivedEvent(Object source, String eventType, FlexOffer FO, UUID uuid) {
        super(source);
        eventName = eventType;
        newUuid = uuid;
        flexOffer = FO;
    }


    public String getEvent() {
        return eventName;
    }

    public void setEvent(String event) {
        this.eventName = event;
    }

    public UUID getNewUuid() {
        return newUuid;
    }

    public void setNewUuid(UUID newUuid) {
        this.newUuid = newUuid;
    }

    public FlexOffer getFlexOffer() {
        return flexOffer;
    }

    public void setFlexOffer(FlexOffer flexOffer) {
        this.flexOffer = flexOffer;
    }
}
