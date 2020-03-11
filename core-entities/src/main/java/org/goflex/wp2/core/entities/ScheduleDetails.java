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
 *  Last Modified 2/22/18 2:33 AM
 */

package org.goflex.wp2.core.entities;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@XmlRootElement(name = "flexOfferSchedule")
@XmlAccessorType(XmlAccessType.FIELD)

public class ScheduleDetails implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleDetails.class);
    private static final long serialVersionUID = 3758419102821738826L;


    @XmlElement
    private List<String> devices = new ArrayList<String>();

    @XmlElement
    private List<UUID> flexoffers = new ArrayList<UUID>();

    @XmlElement
    private List<Integer> action = new ArrayList<Integer>();
    ;


    public ScheduleDetails(String deviceID, UUID foID, int action) {

        this.devices.add(deviceID);
        this.flexoffers.add(foID);
        this.action.add(action);
    }

    public void addSchedule(String deviceID, UUID foID, int action) {

        this.devices.add(deviceID);
        this.flexoffers.add(foID);
        this.action.add(action);
    }

    public String getDeivceID(int idx) {
        return devices.get(idx);
    }

    public int getAction(int idx) {
        return action.get(idx);
    }


    public UUID getFleofferID(int idx) {
        return flexoffers.get(idx);
    }


    public void removeDeivceIDs() {
        this.devices.clear();
    }

    public void removeFleofferIDs() {
        this.flexoffers.clear();
    }


    public List<String> getDevices() {
        return devices;
    }

    public void setDevices(List<String> devices) {
        this.devices = devices;
    }

    public void addDevice(String deviceID) {
        this.devices.add(deviceID);
    }

    public List<UUID> getFlexoffers() {
        return flexoffers;
    }

    public void setFlexoffers(List<UUID> flexoffers) {
        this.flexoffers = flexoffers;
    }

    public void addFlexOfferID(UUID foID) {
        this.flexoffers.add(foID);

    }

    public void addAction(int action) {
        this.action.add(action);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduleDetails)) return false;

        ScheduleDetails that = (ScheduleDetails) o;

        if (devices != null ? !devices.equals(that.devices) : that.devices != null) return false;
        return flexoffers != null ? flexoffers.equals(that.flexoffers) : that.flexoffers == null;
    }

    @Override
    public int hashCode() {
        int result = devices != null ? devices.hashCode() : 0;
        result = 31 * result + (flexoffers != null ? flexoffers.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ScheduleDetails{" +
                "devices=" + devices +
                ", flexoffers=" + flexoffers +
                '}';
    }


    public List<Integer> getAction() {
        return action;
    }

    public void setAction(List<Integer> action) {
        this.action = action;
    }
}