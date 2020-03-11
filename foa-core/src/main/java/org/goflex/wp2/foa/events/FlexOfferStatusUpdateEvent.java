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
 *  Last Modified 7/26/17 9:11 AM
 */

package org.goflex.wp2.foa.events;

import org.goflex.wp2.core.entities.FlexOfferState;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Created by Ivan Bizaca on 21/07/2017.
 */

public class FlexOfferStatusUpdateEvent extends ApplicationEvent {
    private String eventName;
    private String securityToken;
    private UUID flexOfferId;
    private FlexOfferState flexOfferState;

    public FlexOfferStatusUpdateEvent(Object source, String eventName, String securityToken, UUID flexOfferId, FlexOfferState flexOfferState) {
        super(source);
        this.eventName = eventName;
        this.securityToken = securityToken;
        this.flexOfferId = flexOfferId;
        this.flexOfferState = flexOfferState;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public UUID getFlexOfferId() {
        return flexOfferId;
    }

    public void setFlexOfferId(UUID flexOfferId) {
        this.flexOfferId = flexOfferId;
    }

    public FlexOfferState getFlexOfferState() {
        return flexOfferState;
    }

    public void setFlexOfferState(FlexOfferState flexOfferState) {
        this.flexOfferState = flexOfferState;
    }
}