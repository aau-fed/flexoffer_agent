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
public class OrganizationPrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String organizationName;

    private long organizationId;

    private double power;

    private  long  timestamp;

    private Date datetime;

    public OrganizationPrediction() {}
    public OrganizationPrediction(String organizationName, long organizationId, double power, long timestamp, Date datetime) {
        this.organizationName = organizationName;
        this.organizationId = organizationId;
        this.power = power;
        this.timestamp = timestamp;
        this.datetime = datetime;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(long organizationId) {
        this.organizationId = organizationId;
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
        OrganizationPrediction that = (OrganizationPrediction) o;
        return id == that.id &&
                organizationId == that.organizationId &&
                Double.compare(that.power, power) == 0 &&
                timestamp == that.timestamp &&
                Objects.equals(organizationName, that.organizationName) &&
                Objects.equals(datetime, that.datetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, organizationName, organizationId, power, timestamp, datetime);
    }

    @Override
    public String toString() {
        return "OrganizationPrediction{" +
                "id=" + id +
                ", organizationName='" + organizationName + '\'' +
                ", organizationId=" + organizationId +
                ", power=" + power +
                ", timestamp=" + timestamp +
                ", datetime=" + datetime +
                '}';
    }
}
