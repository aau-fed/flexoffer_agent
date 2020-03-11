package org.goflex.wp2.foa.prediction;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;
import java.util.Objects;

/**
 * @author muhaftab
 * created: 9/12/19
 */
@Entity
public class DevicePrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String deviceId;

    private long timeSeriesId;

    private double power;

    private long timestamp;

    private Date datetime;

    public DevicePrediction() {}
    public DevicePrediction(String deviceId, long timeSeriesId, double power, long timestamp, Date datetime) {
        this.deviceId = deviceId;
        this.timeSeriesId = timeSeriesId;
        this.power = power;
        this.timestamp = timestamp;
        this.datetime = datetime;
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

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DevicePrediction that = (DevicePrediction) o;
        return id == that.id &&
                timeSeriesId == that.timeSeriesId &&
                Double.compare(that.power, power) == 0 &&
                timestamp == that.timestamp &&
                Objects.equals(deviceId, that.deviceId) &&
                Objects.equals(datetime, that.datetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceId, timeSeriesId, power, timestamp, datetime);
    }

    @Override
    public String toString() {
        return "DevicePrediction{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", timeSeriesId=" + timeSeriesId +
                ", power=" + power +
                ", timestamp=" + timestamp +
                ", datetime=" + datetime +
                '}';
    }
}
