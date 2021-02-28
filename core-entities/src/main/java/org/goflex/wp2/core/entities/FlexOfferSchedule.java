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


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.goflex.wp2.core.adapters.FODateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;

import static jdk.nashorn.internal.objects.NativeMath.round;

/**
 * The flexoffer schedule
 *
 * @author IL0021B
 * @version 1.0
 * @created 19-Kov-2014 11:41:17
 */


@XmlRootElement(name = "flexOfferSchedule")
@XmlAccessorType(XmlAccessType.FIELD)
public class FlexOfferSchedule implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(FlexOfferSchedule.class);
    private static final long serialVersionUID = -8391718850052856501L;


    /***
     * Specifies the energy fix. For each EnergyInterval in the original
     * flex-offer, there is one value (energy fix) in this object. The schedule
     * is correct if for each energy interval fixed value is between lower and
     * upper bounds of the EnergyConstraint. The length of this time series must
     * be equal to the length of the energyIntervals array of the FlexOffer
     * class.
     *
     * It is not a time series on purpose!!! The time series does not support intervals of
     * variable duration! Therefore, using "double[]" would distinct the semantical differences
     * from the time series.
     */


    /***
     * The startInterval represents a time fix of the flex-offer. In other
     * words, this flex-offer is scheduled at the start time of this time
     * series. For the correct schedule, it must always be within the flex-offer
     * flexibilities.
     */
    // This was raising error, when provided as time @ XmlElement
    //@XmlTransient
    @JsonIgnore
    private long startInterval;


    @XmlAttribute
    private int scheduleId = 0;

    @XmlAttribute
    private int updateId = 0;

    @XmlElement
    private FlexOfferScheduleSlice[] scheduleSlices = new FlexOfferScheduleSlice[0];


    /**
     * Initializes an empty flex-schedule
     */
    public FlexOfferSchedule() {
    }

    /**
     * Initializes some random schedule for a provided flex-offer
     */
    public FlexOfferSchedule(FlexOffer f) {
        this.setStartTime(f.getStartAfterTime());
        this.setScheduleSlices(new FlexOfferScheduleSlice[f.getFlexOfferProfileConstraints().length]);

        for (int i = 0; i < this.getScheduleSlices().length; i++) {
            double initialAmount = (f.getFlexOfferProfile(i).getEnergyUpper(0) +
                    f.getFlexOfferProfile(i).getEnergyLower(0)) / 2;
            this.getScheduleSlices()[i].setEnergyAmount(initialAmount);
        }
    }

    public FlexOfferSchedule(FlexOfferSchedule sch) {
        this.setStartTime(sch.getStartTime());
        this.setScheduleSlices(sch.getScheduleSlices().clone());
    }

    public FlexOfferSchedule(Date startTime, FlexOfferScheduleSlice[] scheduleSlices) {
        super();
        this.scheduleSlices = scheduleSlices;
        this.startInterval = FlexOffer.toFlexOfferTime(startTime);
    }

    public FlexOfferScheduleSlice getScheduleSlice(int i) {
        return this.scheduleSlices[i];
    }

    public FlexOfferScheduleSlice[] getScheduleSlices() {
        return this.scheduleSlices;
    }

    public void setScheduleSlices(FlexOfferScheduleSlice[] scheduleSlices) {
        this.scheduleSlices = scheduleSlices;
    }

    public long getStartInterval() {
        return this.startInterval;
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
     * Clones the flex-offer schedule.
     *
     * @return a clone of this instance
     */
    @Override
    public FlexOfferSchedule clone() {
        return new FlexOfferSchedule(this);
    }

//	public void setStartInterval(long startInterval) {
//		this.startInterval = startInterval;
//	}

    @JsonIgnore
    public int getNumSecondsPerInterval() {
        return FlexOffer.numSecondsPerInterval;
    }

    /**
     * Return the sum of energy defined by this FlexSchedule
     *
     * @return
     */
    @JsonIgnore
    public double getTotalEnergy() {
        double sum = 0;
        for (int i = 0; i < this.scheduleSlices.length; i++)
            sum += this.scheduleSlices[i].getEnergyAmount();
        return sum;
    }

    @JsonIgnore
    public int getTotalDuration() {
        int duration = 0;
        if (duration == Integer.MIN_VALUE) {
            duration = 0;
            for (FlexOfferScheduleSlice slice : scheduleSlices) {
                duration += slice.getDuration();
            }
        }
        return duration;
    }

	/*@Override
	public String toString() {
		final String tab = "\t";
		StringBuilder result = new StringBuilder();
		result.append("S:" + tab + this.startInterval + tab);
		result.append(";E:(" + tab);
		for (int i = 0; i < this.getScheduleSlices().length; i++)
		{
			result.append(this.scheduleSlices[i].getEnergyAmount());
			result.append(";T:(" + tab);
			result.append(this.scheduleSlices[i].getTariff());
			if (i < this.scheduleSlices.length - 1)
				result.append(",");
		}
		result.append(")");
		return result.toString();
	}*/


    /**
     * Fix small numerical errors in the schedule
     */
    public void fixNumericalErrors(FlexOffer flexOffer) {
        final double EPSILON = 1e-4;

        if ((this.getScheduleSlices() != null) && (this.getScheduleSlices().length == flexOffer.getFlexOfferProfileConstraints().length)) {
            for (int i = 0; i < this.getScheduleSlices().length; i++) {
                /* Check and fix lower bound */
                if (this.scheduleSlices[i].getEnergyAmount() < flexOffer.getFlexOfferProfileConstraints()[i].getEnergyLower(0) &&
                        this.scheduleSlices[i].getEnergyAmount() > flexOffer.getFlexOfferProfileConstraints()[i].getEnergyLower(0) - EPSILON) {
                    this.scheduleSlices[i].setEnergyAmount(flexOffer.getFlexOfferProfileConstraints()[i].getEnergyLower(0));
                }

                /* Check and fix lower bound */
                if (this.scheduleSlices[i].getEnergyAmount() > flexOffer.getFlexOfferProfileConstraints()[i].getEnergyUpper(0) &&
                        this.scheduleSlices[i].getEnergyAmount() < flexOffer.getFlexOfferProfileConstraints()[i].getEnergyUpper(0) + EPSILON) {
                    this.scheduleSlices[i].setEnergyAmount(flexOffer.getFlexOfferProfileConstraints()[i].getEnergyUpper(0));
                }
            }
        }
    }

    public boolean isCorrect(FlexOffer flexOffer) {

        DecimalFormat decimalFormat = new DecimalFormat("##.00");

        if (flexOffer == null) return false;
        // schedules startInterval should be greater or equal to startAfterInterval and less than or equal to startBeforeInterval(
        if (this.getStartInterval() < flexOffer.getStartAfterInterval() || this.getStartInterval() > flexOffer.getStartBeforeInterval()) {
            logger.info("Interval issue: FOSch start=>{},  FO start before=>{}, FO start after=>{}",
                    FODateUtils.toAbsoluteTime(this.getStartInterval()),
                    FODateUtils.toAbsoluteTime(flexOffer.getStartBeforeInterval()),
                    FODateUtils.toAbsoluteTime(flexOffer.getStartAfterInterval()));
            return false;
        }
        // length of slices in schedule should be equal to length of flx-offer profile
        // TODO: A single slice can represent more than 1 FO slices with using duration attribute
        if (((this.getScheduleSlices() == null)
                || (this.getScheduleSlices().length != flexOffer.getFlexOfferProfileConstraints().length))
                && flexOffer.getMaxDurationIntervals() != this.getTotalDuration()) {
            logger.info("Energy Amount issue");
            return false;
        }
        // scheduled energy should be within the lowerBound and upperBound of the flex-offer profile
        for (int i = 0; i < this.getScheduleSlices().length; i++) {
            if (round(this.getScheduleSlices()[i].getEnergyAmount(), 4) < round(flexOffer.getFlexOfferProfileConstraints()[i].getEnergyLower(0), 4)
                    || round(this.getScheduleSlices()[i].getEnergyAmount(), 4) > round(flexOffer.getFlexOfferProfileConstraints()[i].getEnergyUpper(0), 4)) {
                logger.info("Energy Amount 2 issue");
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "FlexOfferSchedule{" +
                "startInterval=" + startInterval +
                ", scheduleId=" + scheduleId +
                ", updateId=" + updateId +
                ", scheduleSlices=" + Arrays.toString(scheduleSlices) +
                '}';
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(scheduleSlices);
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
        FlexOfferSchedule other = (FlexOfferSchedule) obj;
        if (!Arrays.equals(scheduleSlices, other.scheduleSlices))
            return false;
        return startInterval == other.startInterval;
    }

    public int getUpdateId() {
        return updateId;
    }

    public void setUpdateId(int updateId) {
        this.updateId = updateId;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

}