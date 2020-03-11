/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
 * Copyright (c) 2018.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining  a copy of this software and associated documentation
 *  files (the "Software") to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify, merge,
 *  publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions: The above copyright notice and
 *  this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON
 *  INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Last Modified 2/22/18 2:28 AM
 */

package org.goflex.wp2.core.wrappers;

/*
 * Created by Ivan Bizaca on 13/07/2017.
 */

/* FLEX OFFER MESSAGE FROM FOA TO FMAN */

import org.goflex.wp2.core.entities.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class FlexOfferRequestWrapper implements Serializable {

    private static final long serialVersionUID = 7206807671699504133L;


    private UUID id;
    private FlexOfferState state;
    private Date creationTime;
    private String offeredById;
    private long assignmentBeforeInterval;
    private FlexOfferConstraint totalEnergyConstraint;
    private long startAfterInterval;
    private long startBeforeInterval;
    private FlexOfferSchedule defaultSchedule;
    private FlexOfferSchedule flexOfferSchedule;
    private int numSecondsPerInterval;
    private FlexOfferSlice[] flexOfferProfileConstraints;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public FlexOfferState getState() {
        return state;
    }

    public void setState(FlexOfferState state) {
        this.state = state;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getOfferedById() {
        return offeredById;
    }

    public void setOfferedById(String offeredById) {
        this.offeredById = offeredById;
    }

    public long getAssignmentBeforeInterval() {
        return assignmentBeforeInterval;
    }

    public void setAssignmentBeforeInterval(long assignmentBeforeInterval) {
        this.assignmentBeforeInterval = assignmentBeforeInterval;
    }

    public FlexOfferConstraint getTotalEnergyConstraint() {
        return totalEnergyConstraint;
    }

    public void setTotalEnergyConstraint(FlexOfferConstraint totalEnergyConstraint) {
        this.totalEnergyConstraint = totalEnergyConstraint;
    }

    public long getStartAfterInterval() {
        return startAfterInterval;
    }

    public void setStartAfterInterval(long startAfterInterval) {
        this.startAfterInterval = startAfterInterval;
    }

    public FlexOfferSchedule getDefaultSchedule() {
        return defaultSchedule;
    }

    public void setDefaultSchedule(FlexOfferSchedule defaultSchedule) {
        this.defaultSchedule = defaultSchedule;
    }

    public int getNumSecondsPerInterval() {
        return numSecondsPerInterval;
    }

    public void setNumSecondsPerInterval(int numSecondsPerInterval) {
        this.numSecondsPerInterval = numSecondsPerInterval;
    }

    public FlexOfferSlice[] getFlexOfferProfileConstraints() {
        return flexOfferProfileConstraints;
    }

    public void setFlexOfferProfileConstraints(FlexOfferSlice[] flexOfferSlices) {
        this.flexOfferProfileConstraints = flexOfferSlices;
    }

    public long getStartBeforeInterval() {
        return startBeforeInterval;
    }

    public void setStartBeforeInterval(long startBeforeInterval) {
        this.startBeforeInterval = startBeforeInterval;
    }

    public FlexOfferSchedule getFlexOfferSchedule() {
        return flexOfferSchedule;
    }

    public void setFlexOfferSchedule(FlexOfferSchedule flexOfferSchedule) {
        this.flexOfferSchedule = flexOfferSchedule;
    }

    public FlexOffer mapToFlexoffer() {
        FlexOffer fo = new FlexOffer();
        return fo;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlexOfferRequestWrapper)) return false;

        FlexOfferRequestWrapper that = (FlexOfferRequestWrapper) o;

        if (assignmentBeforeInterval != that.assignmentBeforeInterval) return false;
        if (startAfterInterval != that.startAfterInterval) return false;
        if (startBeforeInterval != that.startBeforeInterval) return false;
        if (numSecondsPerInterval != that.numSecondsPerInterval) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (state != that.state) return false;
        if (creationTime != null ? !creationTime.equals(that.creationTime) : that.creationTime != null) return false;
        if (offeredById != null ? !offeredById.equals(that.offeredById) : that.offeredById != null) return false;
        if (totalEnergyConstraint != null ? !totalEnergyConstraint.equals(that.totalEnergyConstraint) : that.totalEnergyConstraint != null)
            return false;
        if (defaultSchedule != null ? !defaultSchedule.equals(that.defaultSchedule) : that.defaultSchedule != null)
            return false;
        if (flexOfferSchedule != null ? !flexOfferSchedule.equals(that.flexOfferSchedule) : that.flexOfferSchedule != null)
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(flexOfferProfileConstraints, that.flexOfferProfileConstraints);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (creationTime != null ? creationTime.hashCode() : 0);
        result = 31 * result + (offeredById != null ? offeredById.hashCode() : 0);
        result = 31 * result + (int) (assignmentBeforeInterval ^ (assignmentBeforeInterval >>> 32));
        result = 31 * result + (totalEnergyConstraint != null ? totalEnergyConstraint.hashCode() : 0);
        result = 31 * result + (int) (startAfterInterval ^ (startAfterInterval >>> 32));
        result = 31 * result + (int) (startBeforeInterval ^ (startBeforeInterval >>> 32));
        result = 31 * result + (defaultSchedule != null ? defaultSchedule.hashCode() : 0);
        result = 31 * result + (flexOfferSchedule != null ? flexOfferSchedule.hashCode() : 0);
        result = 31 * result + numSecondsPerInterval;
        result = 31 * result + Arrays.hashCode(flexOfferProfileConstraints);
        return result;
    }

    @Override
    public String toString() {
        return "FlexOfferRequestWrapper{" +
                "id=" + id +
                ", state=" + state +
                ", creationTime=" + creationTime +
                ", offeredById='" + offeredById + '\'' +
                ", assignmentBeforeInterval=" + assignmentBeforeInterval +
                ", totalEnergyConstraint=" + totalEnergyConstraint +
                ", startAfterInterval=" + startAfterInterval +
                ", startBeforeInterval=" + startBeforeInterval +
                ", defaultSchedule=" + defaultSchedule +
                ", flexOfferSchedule=" + flexOfferSchedule +
                ", numSecondsPerInterval=" + numSecondsPerInterval +
                ", flexOfferProfileConstraints=" + Arrays.toString(flexOfferProfileConstraints) +
                '}';
    }
}
