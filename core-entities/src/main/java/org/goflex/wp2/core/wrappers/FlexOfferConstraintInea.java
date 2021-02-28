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
public class FlexOfferConstraintInea implements Serializable {
    private static final long serialVersionUID = -4389233123533413103L;
    /*
     * energy constrains
     */
    @XmlAttribute
    private double lowerBound;
    @XmlAttribute
    private double upperBound;

    public FlexOfferConstraintInea() {
        this.lowerBound = 0;
        this.upperBound = 0;
    }

    /**
     * Initializes the energy constraint using two numbers
     *
     * @param lowerN
     * @param upperN
     * @author Laurynas
     */
    public FlexOfferConstraintInea(double lowerN, double upperN) {
        this.lowerBound = lowerN;
        this.upperBound = upperN;
    }

    /**
     * Initializes the energy constraint using the given energyConstraint.
     *
     * @param constraint the given energy constraint
     */
    public FlexOfferConstraintInea(FlexOfferConstraintInea constraint) {
        this.lowerBound = constraint.getLowerBound();
        this.upperBound = constraint.getUpperBound();
    }

    /**
     * Clones the energy constraint.
     *
     * @return a clone of this instance
     */
    @Override
    public FlexOfferConstraintInea clone() {
        return new FlexOfferConstraintInea(this);
    }

    /**
     * Returns the lower energy bound.
     *
     * @return the lower energy bound
     */
    public double getLowerBound() {
        return lowerBound;
    }

    /**
     * Sets the lower energy bound.
     *
     * @param lowerN new value for the lower energy bound
     */
    public void setLowerBound(double lowerN) {
        lowerBound = lowerN + 0.0;
    }

    /**
     * Returns the upper energy bound.
     *
     * @return the upper energy bound
     */
    public double getUpperBound() {
        return upperBound;
    }

    /**
     * Sets the upper energy bound.
     *
     * @param upperN new value for the upper energy bound
     */
    public void setUpperBound(double upperN) {
        upperBound = upperN + 0.0;
    }

    /**
     * Returns true if only one energy bound is given and false otherwise.
     * Meaning that there is no energy flexibility
     *
     * @return true if only one energy bound is given and false otherwise
     */
    @JsonIgnore
    public boolean isSingle() {
        return (Math.abs(lowerBound - upperBound) < 1E-5);
    }

    @Override
    public String toString() {
        return "{" + lowerBound + "," + upperBound + "}";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(lowerBound);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(upperBound);
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
        FlexOfferConstraintInea other = (FlexOfferConstraintInea) obj;
        if (Double.doubleToLongBits(lowerBound) != Double
                .doubleToLongBits(other.lowerBound))
            return false;
        return Double.doubleToLongBits(upperBound) == Double
                .doubleToLongBits(other.upperBound);
    }

}
