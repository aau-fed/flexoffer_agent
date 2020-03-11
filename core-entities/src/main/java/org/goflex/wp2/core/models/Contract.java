/*
 * Created by bijay
 * GOFLEX :: WP2 :: core-entities
 * Copyright (c) 2018 The GoFlex Consortium
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
 *  Last Modified 4/9/18 8:29 PM
 */

package org.goflex.wp2.core.models;

import javax.persistence.*;

/**
 * Created by bijay on 4/9/18.
 */
@Entity
@Table(name = "contract")
public class Contract {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long contractId;

    private long aggregatorContactId;

    private String userName;

    /**
     * Price are in Euro (€)
     */
    private double energyFlexReward;

    private double fixedReward;

    private double schedulingEnergyReward;

    private double schedulingFixedTimeReward;

    private double schedulingStartTimeReward;

    private double timeFlexReward;

    private boolean isDefault;

    private boolean isValid = true;

    public double getEnergyFlexReward() {
        return energyFlexReward;
    }

    public void setEnergyFlexReward(double energyFlexReward) {
        this.energyFlexReward = energyFlexReward;
    }

    public double getFixedReward() {
        return fixedReward;
    }

    public void setFixedReward(double fixedReward) {
        this.fixedReward = fixedReward;
    }

    public double getSchedulingEnergyReward() {
        return schedulingEnergyReward;
    }

    public void setSchedulingEnergyReward(double schedulingEnergyReward) {
        this.schedulingEnergyReward = schedulingEnergyReward;
    }

    public double getSchedulingFixedTimeReward() {
        return schedulingFixedTimeReward;
    }

    public void setSchedulingFixedTimeReward(double schedulingFixedTimeReward) {
        this.schedulingFixedTimeReward = schedulingFixedTimeReward;
    }

    public double getSchedulingStartTimeReward() {
        return schedulingStartTimeReward;
    }

    public void setSchedulingStartTimeReward(double schedulingStartTimeReward) {
        this.schedulingStartTimeReward = schedulingStartTimeReward;
    }

    public double getTimeFlexReward() {
        return timeFlexReward;
    }

    public void setTimeFlexReward(double timeFlexReward) {
        this.timeFlexReward = timeFlexReward;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public long getAggregatorContactId() {
        return aggregatorContactId;
    }

    public void setAggregatorContactId(long aggregatorContactId) {
        this.aggregatorContactId = aggregatorContactId;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }
}
