
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
 *  Last Modified 2/22/18 3:04 AM
 */

package org.goflex.wp2.core.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import javafx.util.Pair;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by bijay on 12/2/17.
 * The table to store grouping details for device
 */
@Entity
@Table(name = "flexibility_detail")
public class DeviceFlexibilityDetail implements Serializable {

    private static final long serialVersionUID = 6410946071190455958L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @OneToOne(mappedBy = "deviceFlexibilityDetail")
    @JsonBackReference
    private DeviceDetail deviceDetail;
    /**
     * max no. of FOs to generate
     */
    private int noOfInterruptionInADay = 6;
    /**
     * the duration of FO, i.e., max no. of slices in a flex-offer
     */
    private int maxInterruptionLength = 1;
    /**
     * minimum time between two FOs earliest time
     */
    private int minInterruptionInterval = 4;
    /**
     * max time a demand can be delayed
     */
    private int maxInterruptionDelay = 1;
    /**
     * latest time an schedule should be received, else default schedule is triggered
     */
    private int latestAcceptanceTime = 1;

    /**
     * Defines a daily time window (consisting of start time and end time) such that the FOA will control the
     * device only within the time window. For example, if the daily control window is (10am-7pm),
     * then any system-forced control will only occur during this time window.
     */
    private int dailyControlStart = 0;
    private int dailyControlEnd = 24;

    //List to store FO generation start and end time
    @Column
    @ElementCollection(targetClass=Date.class)
    private List<Date> flexZone = new ArrayList<>(2);


    public DeviceFlexibilityDetail() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DeviceDetail getDeviceDetail() {
        return deviceDetail;
    }

    public void setDeviceDetail(DeviceDetail deviceDetail) {
        this.deviceDetail = deviceDetail;
    }

    public int getNoOfInterruptionInADay() {
        return noOfInterruptionInADay;
    }

    public void setNoOfInterruptionInADay(int noOfInterruptionInADay) {
        this.noOfInterruptionInADay = noOfInterruptionInADay;
    }

    public int getMaxInterruptionLength() {
        return maxInterruptionLength;
    }

    public void setMaxInterruptionLength(int maxInterruptionLength) {
        this.maxInterruptionLength = maxInterruptionLength;
    }

    public int getMinInterruptionInterval() {
        return minInterruptionInterval;
    }

    public void setMinInterruptionInterval(int minInterruptionInterval) {
        this.minInterruptionInterval = minInterruptionInterval;
    }

    public int getMaxInterruptionDelay() {
        return maxInterruptionDelay;
    }

    public void setMaxInterruptionDelay(int maxInterruptionDelay) {
        this.maxInterruptionDelay = maxInterruptionDelay;
    }

    public int getLatestAcceptanceTime() {
        return latestAcceptanceTime;
    }

    public void setLatestAcceptanceTime(int latestAcceptanceTime) {
        this.latestAcceptanceTime = latestAcceptanceTime;
    }
    public List<Date> getFlexZone() {
        return flexZone;
    }

    public void setFlexZone(List<Date> flexZone) {
        this.flexZone = flexZone;
    }

    public int getDailyControlStart() {
        return dailyControlStart;
    }

    public void setDailyControlStart(int dailyControlStart) {
        this.dailyControlStart = dailyControlStart;
    }

    public int getDailyControlEnd() {
        return dailyControlEnd;
    }

    public void setDailyControlEnd(int dailyControlEnd) {
        this.dailyControlEnd = dailyControlEnd;
    }
}
