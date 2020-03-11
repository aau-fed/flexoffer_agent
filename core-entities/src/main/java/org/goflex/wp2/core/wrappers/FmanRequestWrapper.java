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
 *  Last Modified 2/22/18 2:29 AM
 */

package org.goflex.wp2.core.wrappers;

/*
 * Created by Ivan Bizaca on 13/07/2017.
 */

/* FLEX OFFER SCHEDULE MESSAGE FROM FMAN TO FOA */

import org.goflex.wp2.core.entities.FlexOfferSchedule;
import org.goflex.wp2.core.entities.FlexOfferState;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class FmanRequestWrapper implements Serializable {

    private static final long serialVersionUID = -7202204416806456418L;

    private UUID id;
    private FlexOfferState state;
    private Date creationTime;
    private FlexOfferSchedule flexOfferSchedule;
    private int numSecondsPerInterval;

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

    public FlexOfferSchedule getFlexOfferSchedule() {
        return flexOfferSchedule;
    }

    public void setFlexOfferSchedule(FlexOfferSchedule flexOfferSchedule) {
        this.flexOfferSchedule = flexOfferSchedule;
    }

    public int getNumSecondsPerInterval() {
        return numSecondsPerInterval;
    }

    public void setNumSecondsPerInterval(int numSecondsPerInterval) {
        this.numSecondsPerInterval = numSecondsPerInterval;
    }

    @Override
    public String toString() {
        return "FmanRequestWrapper{" +
                "id=" + id +
                ", state=" + state +
                ", creationTime=" + creationTime +
                ", flexOfferSchedule=" + flexOfferSchedule +
                ", numSecondsPerInterval=" + numSecondsPerInterval +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FmanRequestWrapper)) return false;

        FmanRequestWrapper that = (FmanRequestWrapper) o;

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
        result = 31 * result + (flexOfferSchedule != null ? flexOfferSchedule.hashCode() : 0);
        result = 31 * result + numSecondsPerInterval;
        return result;
    }
}
