package org.goflex.wp2.core.entities;

/*-
 * #%L
 * GOFLEX::WP2::Core Data Structures
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


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * A single slice for Schedule profile.
 *
 * @author Bijay (to address flex-offer enhancement V1)
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FlexOfferScheduleSlice implements Serializable {
    //private static final long serialVersionUID = -1249370306408067834L;
    /**
     * Span of slice
     */

    @XmlTransient
    private long duration = 1;

    @XmlTransient
    private double energyAmount = 0;

    @XmlTransient
    private double tariff = 0;


    public FlexOfferScheduleSlice() {
        super();
    }

    /**
     * Initializes the tariff slice.
     *
     * @param energyAmount schedule energy for slice
     * @param tariff       tariff for the schedule energy
     */
    public FlexOfferScheduleSlice(double energyAmount, double tariff) {
        this.energyAmount = energyAmount;
        this.tariff = tariff;
    }

    /**
     * Initializes the tariff slice using the given slice.
     *
     * @param scheduleSlice the given schedule slice
     */
    public FlexOfferScheduleSlice(FlexOfferScheduleSlice scheduleSlice) {
        this.energyAmount = scheduleSlice.getEnergyAmount();
        this.tariff = scheduleSlice.getTariff();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp, temp2;
        temp = Double.doubleToLongBits(energyAmount);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp2 = Double.doubleToLongBits(tariff);
        result = prime * result + (int) (temp2 ^ (temp2 >>> 32));

        return result;
    }

    @Override
    public String toString() {
        return "FlexOfferScheduleSlice{" +
                "duration=" + duration +
                ", energyAmount=" + energyAmount +
                ", tariff=" + tariff +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FlexOfferScheduleSlice other = (FlexOfferScheduleSlice) obj;
        if (energyAmount != other.energyAmount)
            return false;
        if (tariff != other.tariff)
            return false;
        return duration == other.duration;
    }

    public double getEnergyAmount() {
        return energyAmount;
    }

    public void setEnergyAmount(double energyAmount) {
        this.energyAmount = energyAmount;
    }

    public double getTariff() {
        return tariff;
    }

    public void setTariff(double tariff) {
        this.tariff = tariff;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

}
