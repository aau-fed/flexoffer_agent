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


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

/**
 * A tariff constraint profile that includes tariff constraints slices.
 *
 * @author Bijay
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TariffConstraintProfile implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(TariffConstraintProfile.class);
    private static final long serialVersionUID = -7044867554177164642L;


    @JsonIgnore
    private long startInterval;


    @XmlElement
    private TariffSlice[] tariffSlices = new TariffSlice[0];

    /**
     * Initializes an empty tariff constraint profile
     */
    public TariffConstraintProfile() {
        super();
    }

    /**
     * Initializes the tariff constraint profile using the given profile.
     *
     * @param tariffProfile the given tariff constraint profile
     */

    public TariffConstraintProfile(TariffConstraintProfile tariffProfile) {
        this.setStartTime(tariffProfile.getStartTime());
        this.setTariffSlices(tariffProfile.getTariffSlices().clone());

    }


    /**
     * Initializes the tariff constraint profile.
     *
     * @param startTime    start time for tariff constraint
     * @param tariffSlices slices of tariff constraint slice
     */

    public TariffConstraintProfile(Date startTime, TariffSlice[] tariffSlices) {
        super();
        this.tariffSlices = tariffSlices;
        this.startInterval = FlexOffer.toFlexOfferTime(startTime);
    }

    /**
     * Clones the tariff constraint profile
     *
     * @return a clone of this instance
     */
    @Override
    public TariffConstraintProfile clone() {
        return new TariffConstraintProfile(this);
    }

    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FlexOffer.dateFormat)
    public Date getStartTime() {
        return FlexOffer.toAbsoluteTime(this.startInterval);
    }

    public void setStartTime(Date time) {
        this.startInterval = FlexOffer.toFlexOfferTime(time);
    }


    /**
     * @param i the index of the tariff slices
     * @return the i-th tariff slices
     */
    public TariffSlice getTariffSlice(int i) {
        return tariffSlices[i];
    }

    /**
     * Added by Bijay to address flex-offer enhancement V1
     * Returns the tariff slices.
     *
     * @return the tariff slices
     */
    public TariffSlice[] getTariffSlices() {
        return tariffSlices;
    }

    public void setTariffSlices(TariffSlice[] tariffSlices) {
        this.tariffSlices = tariffSlices;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(tariffSlices);
        result = prime * result
                + (int) (startInterval ^ (startInterval >>> 32));
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
        TariffConstraintProfile other = (TariffConstraintProfile) obj;
        if (!Arrays.equals(tariffSlices, other.tariffSlices))
            return false;
        if (startInterval != other.startInterval)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TariffConstraintProfile{" +
                "startInterval=" + startInterval +
                ", tariffSlices=" + Arrays.toString(tariffSlices) +
                '}';
    }

    public boolean isCorrect(FlexOffer flexOffer) {

        if (flexOffer == null) return false;
        // tariff constraint startInterval should be greater or equal to startAfterInterval and less than or equal to startBeforeInterval(
        if (this.getStartInterval() < flexOffer.getStartAfterInterval()
                || this.getStartInterval() > flexOffer.getStartBeforeInterval()) {
            logger.info("Interval issue: FOSch start=>{} FO start after=>{} FO start before=>{}",
                    this.getStartInterval(), flexOffer.getStartAfterInterval(), flexOffer.getStartBeforeInterval());
            return false;
        }
        // length of slices in schedule should be equal to length of flx-offer profile
        // TODO: A single slice can represent more than 1 FO slices with using duration attribute
        if ((this.getTariffSlices() == null)
                || (this.getTariffSlices().length != flexOffer.getFlexOfferProfileConstraints().length)) {
            logger.info("Energy Amount issue");
            return false;
        }

        return true;
    }

    public long getStartInterval() {
        return this.startInterval;
    }

//	public void setStartInterval(long startInterval) {
//		this.startInterval = startInterval;
//	}
}
