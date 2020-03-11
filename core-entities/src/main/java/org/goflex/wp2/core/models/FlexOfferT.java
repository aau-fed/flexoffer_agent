
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
 *  Last Modified 3/23/18 1:19 PM
 */

package org.goflex.wp2.core.models;

import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexOfferState;
import org.goflex.wp2.core.util.FlexOfferJsonConvertor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by bijay on 11/27/17.
 * Table to store flex_offer data
 */
@Entity
@Table(name = "flex_offer_table")
public class FlexOfferT implements Serializable {


    private static final long serialVersionUID = -8258447042933957835L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long organizationId;

    private String clientID;

    private String plugID;

    private String foID;

    private Date creationTime;

    private Date scheduleStartTime;

    private FlexOfferState status;

    private int receivedByFMAN = 0;

    private int receivedByFMAR = 0;

    private int foType = 0;

    @Column(name = "FlexOffer", nullable = false, length = 100000)
    @Convert(converter = FlexOfferJsonConvertor.class)
    private FlexOffer flexoffer;

    public FlexOfferT() {

    }

    public FlexOfferT(String clientID, String plugID, Date creationTime, Date scheduleStartTime, FlexOfferState status, String foID, FlexOffer flexoffer, long organizationId) {
        this.clientID = clientID;
        this.plugID = plugID;
        this.creationTime = creationTime;
        this.scheduleStartTime = scheduleStartTime;
        this.status = status;
        this.foID = foID;
        this.flexoffer = flexoffer;
        this.organizationId = organizationId;

    }

    public FlexOfferT(String clientID, String plugID, Date creationTime, Date scheduleStartTime, FlexOfferState status,
                      String foID, FlexOffer flexoffer, long organizationId, int foType) {
        this.clientID = clientID;
        this.plugID = plugID;
        this.creationTime = creationTime;
        this.scheduleStartTime = scheduleStartTime;
        this.status = status;
        this.foID = foID;
        this.flexoffer = flexoffer;
        this.organizationId = organizationId;
        this.foType = foType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getPlugID() {
        return plugID;
    }

    public void setPlugID(String plugID) {
        this.plugID = plugID;
    }


    public FlexOfferState getStatus() {
        return status;
    }

    public void setStatus(FlexOfferState status) {
        this.status = status;
    }

    public FlexOffer getFlexoffer() {
        return flexoffer;
    }

    public void setFlexoffer(FlexOffer flexoffer) {
        this.flexoffer = flexoffer;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlexOfferT)) return false;

        FlexOfferT that = (FlexOfferT) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (clientID != null ? !clientID.equals(that.clientID) : that.clientID != null) return false;
        if (plugID != null ? !plugID.equals(that.plugID) : that.plugID != null) return false;
        if (creationTime != null ? !creationTime.equals(that.creationTime) : that.creationTime != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        return flexoffer != null ? flexoffer.equals(that.flexoffer) : that.flexoffer == null;
    }

    public String getFoID() {
        return foID;
    }

    public void setFoID(String foID) {
        this.foID = foID;
    }

    public int getReceivedByFMAN() {
        return receivedByFMAN;
    }

    public void setReceivedByFMAN(int receivedByFMAN) {
        this.receivedByFMAN = receivedByFMAN;
    }

    public int getReceivedByFMAR() {
        return receivedByFMAR;
    }

    public void setReceivedByFMAR(int receivedByFMAR) {
        this.receivedByFMAR = receivedByFMAR;
    }

    public Date getScheduleStartTime() {
        return scheduleStartTime;
    }

    public void setScheduleStartTime(Date scheduleStartTime) {
        this.scheduleStartTime = scheduleStartTime;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(long organizationId) {
        this.organizationId = organizationId;
    }

    public int getFoType() {
        return foType;
    }

    public void setFoType(int foType) {
        this.foType = foType;
    }
}
