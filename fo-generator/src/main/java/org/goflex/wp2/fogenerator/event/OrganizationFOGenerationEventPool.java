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
 *  Last Modified 9/12/2019
 */

package org.goflex.wp2.fogenerator.event;

import org.goflex.wp2.core.models.Organization;
import org.goflex.wp2.core.models.PoolDeviceModel;
import org.goflex.wp2.foa.prediction.OrganizationPrediction;
import org.springframework.context.ApplicationEvent;

import java.util.List;


public class OrganizationFOGenerationEventPool extends ApplicationEvent {
    private String eventName;
    private String securityToken;
    private List<OrganizationPrediction> organizationPredictions;
    private Organization organization;
    private List<PoolDeviceModel> deviceModel;
    private boolean inCoolingPeriod;

    public OrganizationFOGenerationEventPool(Object source, String eventName, String securityToken,
                                             List<OrganizationPrediction> organizationPredictions,
                                             Organization organization,
                                             List<PoolDeviceModel> deviceModel, boolean inCoolingPeriod) {
        super(source);
        this.eventName = eventName;
        this.securityToken = securityToken;
        this.organizationPredictions = organizationPredictions;
        this.organization = organization;
        this.deviceModel = deviceModel;
        this.inCoolingPeriod = inCoolingPeriod;

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

    public List<OrganizationPrediction> getOrganizationPrediction() {
        return organizationPredictions;
    }

    public void setOrganizationPrediction(List<OrganizationPrediction> organizationPredictions) {
        this.organizationPredictions = organizationPredictions;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public List<PoolDeviceModel> getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(List<PoolDeviceModel> deviceModel) {
        this.deviceModel = deviceModel;
    }

    public boolean isInCoolingPeriod() {
        return inCoolingPeriod;
    }

    public void setInCoolingPeriod(boolean inCoolingPeriod) {
        this.inCoolingPeriod = inCoolingPeriod;
    }
}
