
/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
 * Copyright (c) 2019.
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
 *  Last Modified 10/28/19 3:05 AM
 */

package org.goflex.wp2.core.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.goflex.wp2.core.entities.DeviceDetailData;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * This class store latest model for swiss case
 * Created by bijay on 28/10/19.
 */
@JsonDeserialize
public class PoolDeviceModel implements Serializable {

    private static final long serialVersionUID = -7665248816778773034L;

    private long organizationID; // no need to update
    private String deviceId; // no need to update
    private String deviceType; // no need to update

    private Date lastFOGenerated;
    private UUID lastFOId;
    private boolean foInExecution;
    private Boolean hasActiveFO;
    private Date lastIncludedInPool;
    private Date lastReleasedFromPool;

    private double minTempThresh; // updated on demand
    private double maxTempThresh; // updated on demand

    private double averagePower; // updated in CheckDeviceState.updateOnDurationForASingleDevice()
    private double averageOperationDuration; // updated in CheckDeviceState.updateOnDurationForASingleDevice()
    private List<Double> predictedConsumption;

    private int currentState; // updated in UserService.updateDeviceState()
    private double currentPower; // updated in UserService.updateDeviceState()
    private double currentTemperature; // updated in UserService.updateDeviceState()

    public PoolDeviceModel() {
    }

    public PoolDeviceModel(DeviceDetail device, DeviceDetailData detailData) {
        this.organizationID = 10004;
        this.deviceId = device.getDeviceId();
        this.deviceType = device.getDeviceType().toString();
        this.lastFOGenerated = null;
        if (this.deviceType.equals("Boiler")) {
            this.minTempThresh = 30;
            this.maxTempThresh = 50;
            this.currentTemperature = detailData.getDeviceDataSuppl().getBoilerTemperature();
        } else {
            this.minTempThresh = 20;
            this.maxTempThresh = 25;
            this.currentTemperature = detailData.getDeviceDataSuppl().getAmbientTemperature();
        }
        this.averagePower = device.getConsumptionTs().getAveragePowerForPrediction();
        this.lastFOId = null;
        this.currentState = device.getDeviceState().getValue();
        this.predictedConsumption = null;
        this.hasActiveFO = false;
        this.averageOperationDuration = 15; // 1 slice minimum
        this.currentPower = device.getConsumptionTs().getLatestPower();
        this.lastIncludedInPool = null;
        this.lastReleasedFromPool = null;
    }

    public PoolDeviceModel(long organizationID, String deviceId, String deviceType, Date lastFOGenerated,
                           double minTempThresh, double maxTempThresh, double averagePower, UUID lastFOId,
                           int currentState, boolean foInExecution, List<Double> predictedConsumption,
                           Boolean hasActiveFO, double averageOperationDuration, double currentPower,
                           double currentTemperature, Date lastIncludedInPool, Date lastReleasedFromPool) {
        this.organizationID = organizationID;
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.lastFOGenerated = lastFOGenerated;
        this.minTempThresh = minTempThresh;
        this.maxTempThresh = maxTempThresh;
        this.averagePower = averagePower;
        this.lastFOId = lastFOId;
        this.currentState = currentState;
        this.foInExecution = foInExecution;
        this.predictedConsumption = predictedConsumption;
        this.hasActiveFO = hasActiveFO;
        this.averageOperationDuration = averageOperationDuration;
        this.currentPower = currentPower;
        this.currentTemperature = currentTemperature;
        this.lastIncludedInPool = lastIncludedInPool;
        this.lastReleasedFromPool = lastReleasedFromPool;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getOrganizationID() {
        return organizationID;
    }

    public void setOrganizationID(long organizationID) {
        this.organizationID = organizationID;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Date getLastFOGenerated() {
        return lastFOGenerated;
    }

    public void setLastFOGenerated(Date lastFOGenerated) {
        this.lastFOGenerated = lastFOGenerated;
    }

    public double getMinTempThresh() {
        return minTempThresh;
    }

    public void setMinTempThresh(double minTempThresh) {
        this.minTempThresh = minTempThresh;
    }

    public double getMaxTempThresh() {
        return maxTempThresh;
    }

    public void setMaxTempThresh(double maxTempThresh) {
        this.maxTempThresh = maxTempThresh;
    }

    public double getAveragePower() {
        return averagePower;
    }

    public void setAveragePower(double averagePower) {
        this.averagePower = averagePower;
    }

    public UUID getLastFOId() {
        return lastFOId;
    }

    public void setLastFOId(UUID lastFOId) {
        this.lastFOId = lastFOId;
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public boolean isFoInExecution() {
        return foInExecution;
    }

    public void setFoInExecution(boolean foInExecution) {
        this.foInExecution = foInExecution;
    }

    public List<Double> getPredictedConsumption() {
        return predictedConsumption;
    }

    public void setPredictedConsumption(List<Double> predictedConsumption) {
        this.predictedConsumption = predictedConsumption;
    }

    public Boolean getHasActiveFO() {
        return hasActiveFO;
    }

    public void setHasActiveFO(Boolean hasActiveFO) {
        this.hasActiveFO = hasActiveFO;
    }

    public double getAverageOperationDuration() {
        return averageOperationDuration;
    }

    public void setAverageOperationDuration(double averageOperationDuration) {
        this.averageOperationDuration = averageOperationDuration;
    }

    public double getCurrentPower() {
        return currentPower;
    }

    public void setCurrentPower(double currentPower) {
        this.currentPower = currentPower;
    }

    public double getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(double currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public Date getLastIncludedInPool() {
        return lastIncludedInPool;
    }

    public void setLastIncludedInPool(Date lastIncludedInPool) {
        this.lastIncludedInPool = lastIncludedInPool;
    }

    public Date getLastReleasedFromPool() {
        return lastReleasedFromPool;
    }

    public void setLastReleasedFromPool(Date lastReleasedFromPool) {
        this.lastReleasedFromPool = lastReleasedFromPool;
    }
}
