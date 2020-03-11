package org.goflex.wp2.core.models;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * @author muhaftab
 * created: 3/29/19
 */
@Entity
@Table
public class OrganizationalConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;


    private long organizationId;

    private String organizationName;

    private Double cumulativeEnergy;

    private Double aggregateLivePower;

    private Date timestamp;

    public OrganizationalConsumption() {}

    public OrganizationalConsumption(long organizationId, String organizationName,
                                     Double cumulativeEnergy, Date timestamp) {
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.cumulativeEnergy = cumulativeEnergy;
        this.timestamp = timestamp;
    }

    public OrganizationalConsumption(long organizationId, String organizationName,
                                     Double cumulativeEnergy, Double aggregateLivePower, Date timestamp) {
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.cumulativeEnergy = cumulativeEnergy;
        this.aggregateLivePower = aggregateLivePower;
        this.timestamp = timestamp;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(long organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Double getCumulativeEnergy() {
        return cumulativeEnergy;
    }

    public void setCumulativeEnergy(Double cumulativeEnergy) {
        this.cumulativeEnergy = cumulativeEnergy;
    }

    public Double getAggregateLivePower() {
        return aggregateLivePower;
    }

    public void setAggregateLivePower(Double aggregateLivePower) {
        this.aggregateLivePower = aggregateLivePower;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationalConsumption that = (OrganizationalConsumption) o;
        return id == that.id &&
                organizationId == that.organizationId &&
                Objects.equals(organizationName, that.organizationName) &&
                Objects.equals(cumulativeEnergy, that.cumulativeEnergy) &&
                Objects.equals(aggregateLivePower, that.aggregateLivePower) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, organizationId, organizationName, cumulativeEnergy, aggregateLivePower, timestamp);
    }
}
