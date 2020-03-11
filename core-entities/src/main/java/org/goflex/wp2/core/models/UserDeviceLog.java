
/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
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
 *  Last Modified 2/22/18 10:09 PM
 */

package org.goflex.wp2.core.models;

import org.goflex.wp2.core.entities.DeviceType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by bijay on 12/2/17.
 * The table to logs of device history.
 */
@Entity
@Table(name = "UserDeviceLog")
public class UserDeviceLog implements Serializable {


    private static final long serialVersionUID = -6980403753859786117L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long logId;

    private String userName;

    private String deviceID;

    private Date eventDate;

    private DeviceType prevDeviceType;

    private String details;


    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public DeviceType getPrevDeviceType() {
        return prevDeviceType;
    }

    public void setPrevDeviceType(DeviceType prevDeviceType) {
        this.prevDeviceType = prevDeviceType;
    }

    @Override
    public String toString() {
        return "UserDeviceLog{" +
                "logId=" + logId +
                ", userName='" + userName + '\'' +
                ", deviceID='" + deviceID + '\'' +
                ", eventDate=" + eventDate +
                ", prevDeviceType=" + prevDeviceType +
                ", details='" + details + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDeviceLog)) return false;

        UserDeviceLog that = (UserDeviceLog) o;

        if (logId != that.logId) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
        if (deviceID != null ? !deviceID.equals(that.deviceID) : that.deviceID != null) return false;
        if (eventDate != null ? !eventDate.equals(that.eventDate) : that.eventDate != null) return false;
        if (prevDeviceType != that.prevDeviceType) return false;
        return details != null ? details.equals(that.details) : that.details == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (logId ^ (logId >>> 32));
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (deviceID != null ? deviceID.hashCode() : 0);
        result = 31 * result + (eventDate != null ? eventDate.hashCode() : 0);
        result = 31 * result + (prevDeviceType != null ? prevDeviceType.hashCode() : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
        return result;
    }
}
