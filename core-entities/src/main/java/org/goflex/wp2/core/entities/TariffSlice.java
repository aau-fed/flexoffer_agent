package org.goflex.wp2.core.entities;

/*-
 * #%L
 * GOFLEX::WP2::FOA core
 * %%
 * Copyright (C) 2017 The GOFLEX Consortium
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * A single slice for tariffConstraint profile.
 *
 * @author Bijay (to address flex-offer enhancement V1)
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TariffSlice implements Serializable {


    /**
     * Span of slice
     */
    @XmlTransient
    private long duration = 1;

    private FlexOfferTariffConstraint tariffConstraint;


    public TariffSlice() {
        super();
    }

    /**
     * Initializes the tariff slice.
     *
     * @param durationN   duration of the slice
     * @param lowerBoundN the lower tariff bound for this slice
     * @param upperBoundN the upper tariff bound for this slice
     */
    public TariffSlice(long durationN, double lowerBoundN, double upperBoundN) {
        this.duration = durationN;
        this.tariffConstraint = new FlexOfferTariffConstraint(lowerBoundN, upperBoundN);
    }

    /**
     * Initializes the tariff slice using the given slice.
     *
     * @param tariffSlice the given energy slice
     */
    public TariffSlice(TariffSlice tariffSlice) {
        this.duration = tariffSlice.getDuration();
        this.tariffConstraint = new FlexOfferTariffConstraint(tariffSlice.getTariffConstraint());
    }

    /**
     * Returns the duration of the tariff slice.
     *
     * @return the duration of the tariff slice
     */
    public long getDuration() {
        return duration;
    }

    /***
     * Sets new duration
     *
     * @param duration
     * @author Bijay
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    @JsonIgnore
    public long getDurationSeconds() {
        return duration * FlexOffer.numSecondsPerInterval;
    }

    /**
     * Returns the tariff constraint of this interval.
     *
     * @return the tariff constraint of this interval
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
    @JsonIgnore
    public double getTariffLower() {
        return tariffConstraint.getMinTariff();
    }

    /**
     * Returns the upper bound for this interval.
     *
     * @return the upper bound for this interval
     */
    @JsonIgnore
    public double getTariffUpper() {
        return tariffConstraint.getMaxTariff();
    }


    @Override
    public String toString() {
        return "TariffSlice{" +
                "duration=" + duration +
                ", tariffConstraint=" + tariffConstraint +
                '}';
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(duration);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        TariffSlice other = (TariffSlice) obj;
        if (duration != other.duration)
            return false;
        if (tariffConstraint == null) {
            if (other.tariffConstraint != null)
                return false;
        } else if (!tariffConstraint.equals(other.tariffConstraint))
            return false;
        return true;
    }

}
