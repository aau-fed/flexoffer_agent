package org.goflex.wp2.core.models;

import javax.persistence.*;

/**
 * @author muhaftab
 * created: 3/31/19
 * updated: 6/26/19
 */
@Entity
@Table( name = "device_prognosis",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"time_series_id", "timestamp"})})
public class DevicePrognosis {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private  long id;

    private String deviceId;

    @Column(name = "time_series_id")
    private long timeSeriesId;

    // kWh over a single slice
    private Double energy;

    private long timestamp;

    public DevicePrognosis() {}

    public DevicePrognosis(String deviceId, long timeSeriesId, Double energy, long timestamp) {
        this.deviceId = deviceId;
        this.timeSeriesId = timeSeriesId;
        this.energy = energy;
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getTimeSeriesId() {
        return timeSeriesId;
    }

    public void setTimeSeriesId(long timeSeriesId) {
        this.timeSeriesId = timeSeriesId;
    }

    public Double getEnergy() {
        return energy;
    }

    public void setEnergy(Double energy) {
        this.energy = energy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
