package org.goflex.wp2.core.entities;

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * A representation of constraint specified as a continuous range
 *
 * @author Laurynas
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FlexOfferTariffConstraint implements Serializable {

    private static final long serialVersionUID = 9033194567153614L;



    /*
     * energy constrains
     */

    @XmlAttribute
    private double minTariff;
    @XmlAttribute
    private double maxTariff;

    public FlexOfferTariffConstraint() {
        this.minTariff = 0;
        this.maxTariff = 0;
    }

    /**
     * Initializes the energy constraint using two numbers
     *
     * @param lowerN
     * @param upperN
     * @author Laurynas
     */
    public FlexOfferTariffConstraint(double lowerN, double upperN) {
        this.minTariff = lowerN;
        this.maxTariff = upperN;
    }

    /**
     * Initializes the energy constraint using the given energyConstraint.
     *
     * @param constraint the given energy constraint
     */
    public FlexOfferTariffConstraint(FlexOfferTariffConstraint constraint) {
        this.minTariff = constraint.getMinTariff();
        this.maxTariff = constraint.getMaxTariff();
    }

    /**
     * Clones the energy constraint.
     *
     * @return a clone of this instance
     */
    @Override
    public FlexOfferTariffConstraint clone() {
        return new FlexOfferTariffConstraint(this);
    }

    /**
     * Returns the lower energy bound.
     *
     * @return the lower energy bound
     */
    public double getMinTariff() {
        return minTariff;
    }

    /**
     * Sets the lower energy bound.
     *
     * @param lowerN new value for the lower energy bound
     */
    public void setMinTariff(double lowerN) {
        minTariff = lowerN + 0.0;
    }

    /**
     * Returns the upper energy bound.
     *
     * @return the upper energy bound
     */
    public double getMaxTariff() {
        return maxTariff;
    }

    /**
     * Sets the upper energy bound.
     *
     * @param upperN new value for the upper energy bound
     */
    public void setMaxTariff(double upperN) {
        maxTariff = upperN + 0.0;
    }

    /**
     * Returns true if only one energy bound is given and false otherwise.
     * Meaning that there is no energy flexibility
     *
     * @return true if only one energy bound is given and false otherwise
     */
    @JsonIgnore
    public boolean isSingle() {
        return (Math.abs(minTariff - maxTariff) < 1E-5);
    }

    @Override
    public String toString() {
        return "{" + minTariff + "," + maxTariff + "}";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(minTariff);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxTariff);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        FlexOfferTariffConstraint other = (FlexOfferTariffConstraint) obj;
        if (Double.doubleToLongBits(minTariff) != Double
                .doubleToLongBits(other.minTariff))
            return false;
        if (Double.doubleToLongBits(maxTariff) != Double
                .doubleToLongBits(other.maxTariff))
            return false;
        return true;
    }
}
