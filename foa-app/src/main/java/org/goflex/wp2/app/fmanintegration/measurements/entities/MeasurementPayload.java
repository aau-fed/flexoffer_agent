package org.goflex.wp2.app.fmanintegration.measurements.entities;

import org.goflex.wp2.app.fmanintegration.measurements.MeasurementT;

import java.util.Date;

public class MeasurementPayload {
    private String userName;
    private Long measurementId;
    private Date timeStamp;
    private Double cumulativeEnergy;

    public MeasurementPayload(String userName, Long measurementId, Date timeStamp, Double cumulativeEnergy) {
        this.userName = userName;
        this.measurementId = measurementId;
        this.timeStamp = timeStamp;
        this.cumulativeEnergy = cumulativeEnergy;
    }

    static public MeasurementPayload mapper(MeasurementT m) {
        return new MeasurementPayload(m.getUserName(), m.getId(), m.getTimeStamp(), m.getCumulativeEnergy());
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(Long measurementId) {
        this.measurementId = measurementId;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Double getCumulativeEnergy() {
        return cumulativeEnergy;
    }

    public void setCumulativeEnergy(Double cumulativeEnergy) {
        this.cumulativeEnergy = cumulativeEnergy;
    }
}
