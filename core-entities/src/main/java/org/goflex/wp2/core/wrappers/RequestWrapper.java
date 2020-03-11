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

import org.goflex.wp2.core.entities.FlexOffer;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/*
 * Created by Ivan Bizaca on 08/07/2017.
 */

//Request JSON shape
public class RequestWrapper implements Serializable {

    private static final long serialVersionUID = 4822062876847490574L;


    private FlexOffer flexOffer;
    private String userId;
    private UUID uuid;
    private UUID newUuid;
    private Date from;
    private Date to;
    private int number;

    public RequestWrapper() {
        this.flexOffer = null;
        this.userId = null;
        this.uuid = null;
        this.from = null;
        this.to = null;
        this.number = 0;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public FlexOffer getFlexOffer() {
        return flexOffer;
    }

    public void setFlexOffer(FlexOffer flexOffer) {
        this.flexOffer = flexOffer;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getNewUuid() {
        return newUuid;
    }

    public void setNewUuid(UUID newUuid) {
        this.newUuid = newUuid;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequestWrapper)) return false;

        RequestWrapper that = (RequestWrapper) o;

        if (number != that.number) return false;
        if (flexOffer != null ? !flexOffer.equals(that.flexOffer) : that.flexOffer != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (newUuid != null ? !newUuid.equals(that.newUuid) : that.newUuid != null) return false;
        if (from != null ? !from.equals(that.from) : that.from != null) return false;
        return to != null ? to.equals(that.to) : that.to == null;
    }

    @Override
    public int hashCode() {
        int result = flexOffer != null ? flexOffer.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        result = 31 * result + (newUuid != null ? newUuid.hashCode() : 0);
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (to != null ? to.hashCode() : 0);
        result = 31 * result + number;
        return result;
    }

    @Override
    public String toString() {
        return "RequestWrapper{" +
                "flexOffer=" + flexOffer +
                ", userId='" + userId + '\'' +
                ", uuid=" + uuid +
                ", newUuid=" + newUuid +
                ", from=" + from +
                ", to=" + to +
                ", number=" + number +
                '}';
    }
}