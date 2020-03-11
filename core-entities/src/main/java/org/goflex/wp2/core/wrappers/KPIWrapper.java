package org.goflex.wp2.core.wrappers;


import java.util.Date;

public class KPIWrapper {
    private Date dateofAggregation;
    private int activeDevice;
    private int activeUser;
    private double flexibilityRatio;
    private int foCount;

    public KPIWrapper(Date dateofAggregation, int activeDevice, int activeUser, double flexibilityRatio, int foCount) {
        this.dateofAggregation = dateofAggregation;
        this.activeDevice = activeDevice;
        this.activeUser = activeUser;
        this.flexibilityRatio = flexibilityRatio;
        this.foCount = foCount;
    }

    public KPIWrapper(Date dateofAggregation, long activeDevice, long activeUser, double flexibilityRatio, long foCount) {
        this.dateofAggregation = dateofAggregation;
        this.activeDevice = (int) activeDevice;
        this.activeUser = (int) activeUser;
        this.flexibilityRatio = flexibilityRatio;
        this.foCount = (int) foCount;
    }

    public Date getDateofAggregation() {
        return dateofAggregation;
    }

    public void setDateofAggregation(Date dateofAggregation) {
        this.dateofAggregation = dateofAggregation;
    }

    public int getActiveDevice() {
        return activeDevice;
    }

    public void setActiveDevice(int activeDevice) {
        this.activeDevice = activeDevice;
    }

    public int getActiveUser() {
        return activeUser;
    }

    public void setActiveUser(int activeUser) {
        this.activeUser = activeUser;
    }

    public double getFlexibilityRatio() {
        return flexibilityRatio;
    }

    public void setFlexibilityRatio(double flexibilityRatio) {
        this.flexibilityRatio = flexibilityRatio;
    }

    public int getFoCount() {
        return foCount;
    }

    public void setFoCount(int foCount) {
        this.foCount = foCount;
    }
}
