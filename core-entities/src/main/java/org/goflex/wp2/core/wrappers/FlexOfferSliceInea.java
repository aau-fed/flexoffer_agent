package org.goflex.wp2.core.wrappers;

/*-
 * #%L
 * ARROWHEAD::WP5::Core Data Structures
 * %%
 * Copyright (C) 2016 The ARROWHEAD Consortium
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.goflex.wp2.core.entities.FlexOfferTariffConstraint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * A single slice (interval) in the flexoffer profile.
 *
 * @author Laurynas
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FlexOfferSliceInea implements Serializable {
    private static final long serialVersionUID = -1249370306408067834L;


    @XmlElement
    private FlexOfferConstraintInea[] energyConstraintList;
    @XmlElement
    private FlexOfferTariffConstraint tariffConstraint;

    public FlexOfferSliceInea() {
        super();
    }

    /**
     * Initializes the energy slice.
     *
     * @param lowerDurationN    the lower bound for the duration
     * @param upperDurationN    the upper bound for the duration
     * @param energyConstraints list of energy constraints for the slice, default 1
     * @param lowerTariffN      the lower bound of the tariff for the slice
     * @param upperTariffN      the upper bound of the tariff for the slice
     */

    public FlexOfferSliceInea(long lowerDurationN, long upperDurationN, FlexOfferConstraintInea[] energyConstraints,
                              double lowerTariffN, double upperTariffN) {

        //this.minDuration = lowerDurationN;
        //this.maxDuration = upperDurationN;
        this.tariffConstraint = new FlexOfferTariffConstraint(lowerTariffN, upperTariffN);
        this.energyConstraintList = energyConstraints;

    }

    /**
     * Initializes the energy slice using the given slice.
     *
     * @param energySlice the given energy slice
     */
    public FlexOfferSliceInea(FlexOfferSliceInea energySlice) {

        //this.minDuration = energySlice.getMinDuration();
        //this.maxDuration = energySlice.getMaxDuration();
        this.tariffConstraint = new FlexOfferTariffConstraint(energySlice.getTariffConstraint());
        this.energyConstraintList = energySlice.getEnergyConstraintList();
    }

    /**
     * Added to address flex-offer enhancement V1
     * Need to create durationConstraint to seperate duration and min-max duration attributes
     */
	/*@JsonIgnore
	private long minDuration=1;

	@JsonIgnore
	private long maxDuration=1;*/
    @Override
    public String toString() {
        return energyConstraintList.toString() + " (" + tariffConstraint.toString() + "â‚¬)";
    }

    /**
     * Returns the energy constraint of this interval.
     *
     * @return the energy constraint of this interval
     */
    public FlexOfferConstraintInea[] getEnergyConstraintList() {
        return energyConstraintList;
    }

    /**
     * Sets the energy constraint of this interval.
     *
     * @param energyConstraints the energy constraint to set
     */
    public void setEnergyConstraintList(FlexOfferConstraintInea[] energyConstraints) {
        this.energyConstraintList = energyConstraints;
    }

    public FlexOfferConstraintInea getEnergyConstraint(int i) {
        return energyConstraintList[i];
    }

    /**
     * Returns the energy constraint of this interval.
     *
     * @return the energy constraint of this interval
     */
    public FlexOfferTariffConstraint getTariffConstraint() {
        return tariffConstraint;
    }

    /**
     * Sets the tariff constraint of this interval.
     *
     * @param tariffConstraint the tariff constraint to set
     */
    public void setTariffConstraint(FlexOfferTariffConstraint tariffConstraint) {
        this.tariffConstraint = tariffConstraint;
    }

    /**
     * Returns the lower bound for this interval.
     *
     * @return the lower bound for this interval
     */
    public double getEnergyLower(int i) {
        return energyConstraintList[i].getLowerBound();
    }

    /**
     * Returns the upper bound for this interval.
     *
     * @return the upper bound for this interval
     */
    public double getEnergyUpper(int i) {
        return energyConstraintList[i].getUpperBound();
    }

    /**
     * Returns the upper bound for this interval.
     *
     * @return the upper bound for this interval
     */
    public double getDefaultEnergyVal(int i) {
        return (energyConstraintList[i].getUpperBound() - energyConstraintList[i].getLowerBound()) / 2;
    }


    /**
     * Checks if the slice is for consumption
     *
     * @return boolean
     * TODO it only check the value for the first slice need to update the logic
     */
    @JsonIgnore
    public boolean isConsumption() {
        return energyConstraintList[0].getLowerBound() < 0;
    }

    /**
     * Checks if the slice is for production
     *
     * @return
     */
    @JsonIgnore
    public boolean isProduction() {
        return energyConstraintList[0].getUpperBound() > 0;

    }


    /**
     * Sets the lower and upper energy of the slice to the given value and sets the single value to true.
     *
     * @param energy
     */
    public void setSingleProfileConstraint(int i, double energy) {
        energyConstraintList[i].setLowerBound(energy);
        energyConstraintList[i].setUpperBound(energy);
    }

    /**
     * Sets the lower and upper Tariff Constraint of the slice to the given value.
     *
     * @param tariff
     */

    public void setSingleTariffConstraint(long tariff) {
        tariffConstraint.setMinTariff((double) tariff);
        tariffConstraint.setMaxTariff((double) tariff);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        //temp = Double.doubleToLongBits(minDuration);
        temp = Double.doubleToLongBits(100);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        //result = prime * result + (int) (maxDuration ^ (maxDuration >>> 32));
        result = prime
                * result
                + ((energyConstraintList == null) ? 0 : energyConstraintList.hashCode());
        result = prime
                * result
                + ((tariffConstraint == null) ? 0 : tariffConstraint.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FlexOfferSliceInea other = (FlexOfferSliceInea) obj;
        if (energyConstraintList == null) {
            if (other.energyConstraintList != null)
                return false;
        } else if (!energyConstraintList.equals(other.energyConstraintList))
            return false;
        if (tariffConstraint == null) {
            if (other.tariffConstraint != null)
                return false;
        } else if (!tariffConstraint.equals(other.tariffConstraint))
            return false;
        return true;
    }


    /**
     //	 * Returns the min duration constraint of this interval.
     //	 *
     //	 * @return the min duration constraint of this interval
     //	 */
//	@JsonIgnore
//	public long getMinDuration() {
//		return minDuration;
//	}
//
//	/**
//	 * Sets the min  duration constraint of this interval.
//	 *
//	 * @param minDuration the duration constraint to set
//	 */
//
//	public void setMinDuration(long minDuration) {
//		this.minDuration = minDuration;
//	}
//
//	/**
//	 * Returns the max duration constraint of this interval.
//	 *
//	 * @return the max duration constraint of this interval
//	 */
//
//	public long getMaxDuration() {
//		return maxDuration;
//	}
//
//	/**
//	 * Sets the max duration constraint of this interval.
//	 *
//	 * @param maxDuration the duration constraint to set
//	 */
//	public void setMaxDuration(long maxDuration) {
//		this.maxDuration = maxDuration;
//	}
}
