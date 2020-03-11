
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
 *  Last Modified 2/22/18 2:49 AM
 */

package org.goflex.wp2.core.models;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity to store tplink devices associated to a user.
 * Created by bijay on 12/11/17.
 * Updated by aftab on 24/06/19.
 */

@Entity
public class OnOffSchedule implements Serializable {

    private static final long serialVersionUID = 268094100618325365L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long onOffScheduleId;

    private String deviceID;

    private Double energyLevel;

    // 0 - off, 1 - on
    private int scheduleToState;

    private String flexOfferId;

    // valid or overwritten
    private int isValid = 1;

    // check if scheduler has to trigger schedule or the schedule has been pushed to device
    private int pushedToDevice = 0;

    private Date registeredTime;

    private String externalScheduleId = "";

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduleId")
    @JsonBackReference
    private ScheduleT scheduleTable;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isLast = false;

    public OnOffSchedule() {}

    public OnOffSchedule(String deviceID) {
        this.deviceID = deviceID;
    }

    public long getOnOffScheduleId() {
        return onOffScheduleId;
    }

    public void setOnOffScheduleId(long onOffScheduleId) {
        this.onOffScheduleId = onOffScheduleId;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public Double getEnergyLevel() {
        return energyLevel;
    }

    public void setEnergyLevel(Double energyLevel) {
        this.energyLevel = energyLevel;
    }

    public int getScheduleToState() {
        return scheduleToState;
    }

    public void setScheduleToState(int scheduleToState) {
        this.scheduleToState = scheduleToState;
    }

    public ScheduleT getScheduleTable() {
        return scheduleTable;
    }

    public void setScheduleTable(ScheduleT scheduleTable) {
        this.scheduleTable = scheduleTable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OnOffSchedule)) return false;

        OnOffSchedule that = (OnOffSchedule) o;

        if (onOffScheduleId != that.onOffScheduleId) return false;
        if (scheduleToState != that.scheduleToState) return false;
        if (deviceID != null ? !deviceID.equals(that.deviceID) : that.deviceID != null) return false;
        if (energyLevel != null ? !energyLevel.equals(that.energyLevel) : that.energyLevel != null) return false;
        return scheduleTable != null ? scheduleTable.equals(that.scheduleTable) : that.scheduleTable == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (onOffScheduleId ^ (onOffScheduleId >>> 32));
        result = 31 * result + (deviceID != null ? deviceID.hashCode() : 0);
        result = 31 * result + (energyLevel != null ? energyLevel.hashCode() : 0);
        result = 31 * result + scheduleToState;
        result = 31 * result + (scheduleTable != null ? scheduleTable.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OnOffSchedule{" +
                "onOffScheduleId=" + onOffScheduleId +
                ", deviceID='" + deviceID + '\'' +
                ", energyLevel=" + energyLevel +
                ", scheduleToState=" + scheduleToState +
                ", scheduleTable=" + scheduleTable +
                '}';
    }

    public String getFlexOfferId() {
        return flexOfferId;
    }

    public void setFlexOfferId(String flexOfferId) {
        this.flexOfferId = flexOfferId;
    }

    public int getIsValid() {
        return isValid;
    }

    public void setIsValid(int isValid) {
        this.isValid = isValid;
    }

    public int getPushedToDevice() {
        return pushedToDevice;
    }

    public void setPushedToDevice(int pushedToDevice) {
        this.pushedToDevice = pushedToDevice;
    }

    public String getExternalScheduleId() {
        return externalScheduleId;
    }

    public void setExternalScheduleId(String externalScheduleId) {
        this.externalScheduleId = externalScheduleId;
    }

    public Date getRegisteredTime() {
        return registeredTime;
    }

    public void setRegisteredTime(Date registeredTime) {
        this.registeredTime = registeredTime;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }
}
