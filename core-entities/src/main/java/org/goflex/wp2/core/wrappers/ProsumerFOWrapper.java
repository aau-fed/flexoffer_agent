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
 *  Last Modified 3/21/18 3:08 PM
 */

package org.goflex.wp2.core.wrappers;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.goflex.wp2.core.entities.FlexOffer;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by bijay on 3/21/18.
 */
public class ProsumerFOWrapper {

    public static final String dateFormat = "yyyy-MM-dd HH:mm:ss";

    private Date scheduleStartTime;

    private Date flexOfferCreationTime;

    private List<Double> amount = new ArrayList<>();

    private String deviceId;

    private FlexOffer flexOffer;

    private int foType;

    public ProsumerFOWrapper() {
    }

    public ProsumerFOWrapper(Date scheduleStartTime, List<Double> amount) {
        this.scheduleStartTime = scheduleStartTime;
        this.amount = amount;
    }

    public ProsumerFOWrapper(Date flexOfferCreationTime, Date scheduleStartTime, List<Double> amount, String deviceId,
                             FlexOffer flexOffer) {
        this.flexOfferCreationTime = flexOfferCreationTime;
        this.scheduleStartTime = scheduleStartTime;
        this.amount = amount;
        this.deviceId = deviceId;
        this.flexOffer = flexOffer;
    }

    public ProsumerFOWrapper(Date flexOfferCreationTime, Date scheduleStartTime,
                             List<Double> amount, String deviceId,
                             FlexOffer flexOffer, int foType) {
        this.flexOfferCreationTime = flexOfferCreationTime;
        this.scheduleStartTime = scheduleStartTime;
        this.amount = amount;
        this.deviceId = deviceId;
        this.flexOffer = flexOffer;
        this.foType = foType;
    }


    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getScheduleStartTime() {
        return scheduleStartTime;
    }

    public void setScheduleStartTime(Date scheduleStartTime) {
        this.scheduleStartTime = scheduleStartTime;
    }

    public List<Double> getAmount() {
        return amount;
    }

    public void setAmount(List<Double> slices) {
        this.amount = slices;
    }

    public void addAmount(double value) {
        this.amount.add(value);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public FlexOffer getFlexOffer() {
        return flexOffer;
    }

    public void setFlexOffer(FlexOffer flexOffer) {
        this.flexOffer = flexOffer;
    }

    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getFlexOfferCreationTime() {
        return flexOfferCreationTime;
    }

    public void setFlexOfferCreationTime(Date flexOfferCreationTime) {
        this.flexOfferCreationTime = flexOfferCreationTime;
    }

    public int getFoType() {
        return foType;
    }

    public void setFoType(int foType) {
        this.foType = foType;
    }
}
