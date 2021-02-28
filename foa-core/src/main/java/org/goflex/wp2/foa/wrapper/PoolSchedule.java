package org.goflex.wp2.foa.wrapper;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author muhaftab
 * created: 11/2/19
 */
@JsonDeserialize
public class PoolSchedule implements Serializable {

    private static final long serialVersionUID = -7135248217770773034L;

    private double offeredEnergyLower;
    private double offeredEnergyUpper;
    private double marketOrder;
    private double currentPowerConsumption;
    private double cumulativeEnergy;
    private Date startTime;
    private Date endTime;

    public PoolSchedule() {
    }

    public PoolSchedule(double offeredEnergyLower, double offeredEnergyUpper, double marketOrder,
                        double currentPowerConsumption, double cumulativeEnergy,
                        Date startTime, Date endTime) {
        this.offeredEnergyLower = offeredEnergyLower;
        this.offeredEnergyUpper = offeredEnergyUpper;
        this.marketOrder = marketOrder;
        this.currentPowerConsumption = currentPowerConsumption;
        this.cumulativeEnergy = cumulativeEnergy;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public double getOfferedEnergyLower() {
        return offeredEnergyLower;
    }

    public void setOfferedEnergyLower(double offeredEnergyLower) {
        this.offeredEnergyLower = offeredEnergyLower;
    }

    public double getOfferedEnergyUpper() {
        return offeredEnergyUpper;
    }

    public void setOfferedEnergyUpper(double offeredEnergyUpper) {
        this.offeredEnergyUpper = offeredEnergyUpper;
    }

    public double getMarketOrder() {
        return marketOrder;
    }

    public void setMarketOrder(double marketOrder) {
        this.marketOrder = marketOrder;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public double getCurrentPowerConsumption() {
        return currentPowerConsumption;
    }

    public void setCurrentPowerConsumption(double currentPowerConsumption) {
        this.currentPowerConsumption = currentPowerConsumption;
    }

    public double getCumulativeEnergy() {
        return cumulativeEnergy;
    }

    public void setCumulativeEnergy(double cumulativeEnergy) {
        this.cumulativeEnergy = cumulativeEnergy;
    }

    @Override
    public String toString() {
        return "PoolSchedule{" +
                "offeredEnergyLower=" + offeredEnergyLower +
                ", offeredEnergyUpper=" + offeredEnergyUpper +
                ", marketOrder=" + marketOrder +
                ", currentPowerConsumption=" + currentPowerConsumption +
                ", cumulativeEnergy=" + cumulativeEnergy +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
