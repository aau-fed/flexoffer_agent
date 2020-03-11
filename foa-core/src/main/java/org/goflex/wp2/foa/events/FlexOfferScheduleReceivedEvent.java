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

package org.goflex.wp2.foa.events;

import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexOfferSchedule;
import org.springframework.context.ApplicationEvent;

/**
 * Created by bijay on 7/22/17.
 */
public class FlexOfferScheduleReceivedEvent extends ApplicationEvent {
    String eventName;
    private FlexOfferSchedule newSchedule;
    private FlexOffer flexOffer;
    private Boolean defaultSchedule;


    public FlexOfferScheduleReceivedEvent(Object source, String eventType, FlexOffer FO, FlexOfferSchedule schedule, Boolean defSchedule) {
        super(source);
        eventName = eventType;
        newSchedule = schedule;
        flexOffer = FO;
        defaultSchedule = defSchedule;
    }


    public String getEvent() {
        return eventName;
    }

    public void setEvent(String event) {
        this.eventName = event;
    }

    public FlexOfferSchedule getNewSchedule() {
        return newSchedule;
    }

    public void setNewSchedule(FlexOfferSchedule newSchedule) {
        this.newSchedule = newSchedule;
    }

    public FlexOffer getFlexOffer() {
        return flexOffer;
    }

    public void setFlexOffer(FlexOffer flexOffer) {
        this.flexOffer = flexOffer;
    }

    public Boolean getDefaultSchedule() {
        return defaultSchedule;
    }

    public void setDefaultSchedule(Boolean defaultSchedule) {
        this.defaultSchedule = defaultSchedule;
    }
}
