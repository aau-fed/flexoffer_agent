/*
 * Created by bijay
 * GOFLEX :: WP2 :: core-entities
 * Copyright (c) 2018 The GoFlex Consortium
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
 *  Last Modified 2/24/18 1:09 AM
 */

package org.goflex.wp2.core.wrappers;


import org.goflex.wp2.core.entities.FlexOfferSchedule;
import org.goflex.wp2.core.models.Contract;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bijay on 7/22/17.
 */
public class ScheduleWrapper implements Serializable {


    private static final long serialVersionUID = -3148529921067444696L;
    private UUID id;
    /*private FlexOfferState state;
    private Date  creationTime;
    private int numSecondsPerInterval;*/
    //private FlexOfferSchedule flexOfferSchedule;

    @XmlElement(nillable = true)
    private ConcurrentHashMap<UUID, FlexOfferSchedule> flexOfferSchedule;

    @XmlElement(nillable = true)
    private Contract contract;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ConcurrentHashMap<UUID, FlexOfferSchedule> getFlexOfferSchedule() {
        return flexOfferSchedule;
    }

    public void setFlexOfferSchedule(ConcurrentHashMap<UUID, FlexOfferSchedule> flexOfferSchedule) {
        this.flexOfferSchedule = flexOfferSchedule;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    /*public FlexOfferState getState() {
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

    public int getNumSecondsPerInterval() {
        return numSecondsPerInterval;
    }
*/
   /* public void setNumSecondsPerInterval(int numSecondsPerInterval) {
        this.numSecondsPerInterval = numSecondsPerInterval;
    }*/

    /*public FlexOfferSchedule getFlexOfferSchedule() {
        return flexOfferSchedule;
    }*/

    /*public void setFlexOfferSchedule(FlexOfferSchedule flexOfferSchedule) {
        this.flexOfferSchedule = flexOfferSchedule;
    }*/

    /*@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduleWrapper)) return false;

        ScheduleWrapper that = (ScheduleWrapper) o;

        if (numSecondsPerInterval != that.numSecondsPerInterval) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (state != that.state) return false;
        if (creationTime != null ? !creationTime.equals(that.creationTime) : that.creationTime != null) return false;
        return flexOfferSchedule != null ? flexOfferSchedule.equals(that.flexOfferSchedule) : that.flexOfferSchedule == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (creationTime != null ? creationTime.hashCode() : 0);
        result = 31 * result + numSecondsPerInterval;
        result = 31 * result + (flexOfferSchedule != null ? flexOfferSchedule.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ScheduleWrapper{" +
                "id=" + id +
                ", state=" + state +
                ", creationTime=" + creationTime +
                ", numSecondsPerInterval=" + numSecondsPerInterval +
                ", flexOfferSchedule=" + flexOfferSchedule +
                '}';
    }*/
}
