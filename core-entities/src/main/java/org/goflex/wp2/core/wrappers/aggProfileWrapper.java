package org.goflex.wp2.core.wrappers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.goflex.wp2.core.entities.FlexOfferConstraint;
import org.goflex.wp2.core.entities.FlexOfferTariffConstraint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * Created by bijay on 8/28/17.
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class aggProfileWrapper implements Serializable {

    private int durationSeconds;
    private int costPerEnergyUnitLimit;
    private FlexOfferConstraint energyConstraint;


    private FlexOfferTariffConstraint tariffConstraint;

    @JsonCreator
    public aggProfileWrapper(@JsonProperty(value = "durationSeconds") int durationSeconds, @JsonProperty(value = "costPerEnergyUnitLimit") int costPerEnergyUnitLimit,
                             @JsonProperty(value = "energyProfile") FlexOfferConstraint energyProfile, @JsonProperty(value = "tariffConstraint", required = false) FlexOfferTariffConstraint tariffConstraint) {
        this.durationSeconds = durationSeconds;
        this.costPerEnergyUnitLimit = costPerEnergyUnitLimit;
        this.energyConstraint = energyProfile;
        this.tariffConstraint = tariffConstraint;
    }


    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public int getCostPerEnergyUnitLimit() {
        return costPerEnergyUnitLimit;
    }

    public void setCostPerEnergyUnitLimit(int costPerEnergyUnitLimit) {
        this.costPerEnergyUnitLimit = costPerEnergyUnitLimit;
    }

    public FlexOfferConstraint getEnergyConstraint() {
        return energyConstraint;
    }

    public void setEnergyConstraint(FlexOfferConstraint energyConstraint) {
        this.energyConstraint = energyConstraint;
    }

    @JsonProperty
    public FlexOfferTariffConstraint getTariffConstraint() {
        return tariffConstraint;
    }

    @JsonIgnore
    public void setTariffConstraint(FlexOfferTariffConstraint tariffConstraint) {
        this.tariffConstraint = tariffConstraint;
    }
}
