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
 *  Last Modified 2/22/18 2:43 AM
 */

package org.goflex.wp2.core.entities;


import org.goflex.wp2.core.models.DeviceData;
import org.goflex.wp2.core.models.DeviceDataSuppl;

import java.io.Serializable;
import java.util.Date;

/**
 * This class is used to parse data received from the tplink
 * Created by bijay on 1/6/18.
 */
public class DeviceDetailData implements Serializable {


    private static final long serialVersionUID = 5989670059289437850L;
    private Date time;
    private double value;
    private DeviceState state;
    private int ErrorCode;
    private DeviceData deviceData;
    private DeviceDataSuppl deviceDataSuppl;
    private double latitude;
    private double longitude;
    private String alias;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public DeviceState getState() {
        return state;
    }

    public void setState(DeviceState state) {
        this.state = state;
    }

    public int getErrorCode() {
        return ErrorCode;
    }

    public void setErrorCode(int errorCode) {
        ErrorCode = errorCode;
    }

    public DeviceData getDeviceData() {
        return deviceData;
    }

    public void setDeviceData(DeviceData deviceData) {
        this.deviceData = deviceData;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public DeviceDataSuppl getDeviceDataSuppl() {
        return deviceDataSuppl;
    }

    public void setDeviceDataSuppl(DeviceDataSuppl deviceDataSuppl) {
        this.deviceDataSuppl = deviceDataSuppl;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceDetailData)) return false;

        DeviceDetailData that = (DeviceDetailData) o;

        if (Double.compare(that.value, value) != 0) return false;
        if (ErrorCode != that.ErrorCode) return false;
        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = time != null ? time.hashCode() : 0;
        temp = Double.doubleToLongBits(value);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + ErrorCode;
        return result;
    }

    @Override
    public String toString() {
        return "DeviceDetailData{" +
                "time=" + time +
                ", value=" + value +
                ", state=" + state +
                ", ErrorCode=" + ErrorCode +
                '}';
    }
}
