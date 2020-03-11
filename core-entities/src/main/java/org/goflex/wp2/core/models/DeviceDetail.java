
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
 *  Last Modified 2/22/18 3:05 AM
 */

package org.goflex.wp2.core.models;

import com.fasterxml.jackson.annotation.*;
import org.goflex.wp2.core.entities.DeviceState;
import org.goflex.wp2.core.entities.DeviceType;
import org.goflex.wp2.core.entities.PlugType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Entity to store tplink devices associated to a user.
 * Created by bijay on 12/11/17.
 */

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DeviceDetail implements Serializable {

    private static final long serialVersionUID = -7665248816778773034L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long deviceDetailId;

    @Column(name = "alias")
    private String alias = "";

    // attribute to store date and time of first  discovery date of the device
    @Column(name = "connected_time")
    private Date connectedTime;

    // Attribute to store default state of a device
    @Column(name = "default_state")
    private int defaultState = 0;

    // this is a unique id given by FOA
    @Column(name = "device_id", unique = true)
    private String deviceId;

    @Column(name = "device_plug_id", unique = true)
    @JsonIgnore
    private String devicePlugId;

    @Column(name = "device_state")
    private DeviceState deviceState;

    @Column(name = "device_type")
    private DeviceType deviceType = DeviceType.Unknown;

    // attribute to store date and time of last connection date of the device
    @Column(name = "last_connected_time")
    private Date lastConnectedTime;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "longitude")
    private double longitude;

    // Attribute to store number of unsuccessful connection made to tplink cloud.
    // this is required to avoid user getting blocked from tplink-cloud
    @Column(name = "no_of_unsuccessful_con")
    private int noOfUnsuccessfulCon = 0;

    @Column(name = "plug_type")
    private PlugType plugType;

    @Column(name = "time_zone")
    private String timeZone = "00:00";

    @Column(name = "registration_date")
    private Date registrationDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "consumption_ts_id")
    @JsonManagedReference
    private ConsumptionTsEntity consumptionTs;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "deviceflexibilitydetail_id")
    @JsonManagedReference
    private DeviceFlexibilityDetail deviceFlexibilityDetail;

    //@ManyToOne(cascade = CascadeType.PERSIST, targetEntity = GroupingDetail.class, fetch = FetchType.LAZY)
    @ManyToOne(targetEntity = GroupingDetail.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "groupId")
    @JsonBackReference(value = "group-detail")
    private GroupingDetail groupingDetail;

    @ManyToOne(targetEntity = DeviceHierarchy.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "hierarchy_id")
    //@JsonBackReference
    private DeviceHierarchy deviceHierarchy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    @JsonBackReference(value = "user-detail")
    private UserT user;

    // if true then Tp-link cloud will not override device location  (lat, lon)
    private boolean changedByUser = false;

    // if false then device will not participate in flexibility
    private boolean isFlexible = true;

    public DeviceDetail() {
    }

    public DeviceDetail(String deviceId) {
        this.deviceId = deviceId;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getDeviceDetailId() {
        return deviceDetailId;
    }

    public void setDeviceDetailId(long deviceDetailId) {
        this.deviceDetailId = deviceDetailId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Date getConnectedTime() {
        return connectedTime;
    }

    public void setConnectedTime(Date connectedTime) {
        this.connectedTime = connectedTime;
    }

    public int getDefaultState() {
        return defaultState;
    }

    public void setDefaultState(int defaultState) {
        this.defaultState = defaultState;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDevicePlugId() {
        return devicePlugId;
    }

    public void setDevicePlugId(String devicePlugId) {
        this.devicePlugId = devicePlugId;
    }

    public DeviceState getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public Date getLastConnectedTime() {
        return lastConnectedTime;
    }

    public void setLastConnectedTime(Date lastConnectedTime) {
        this.lastConnectedTime = lastConnectedTime;
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

    public int getNoOfUnsuccessfulCon() {
        return noOfUnsuccessfulCon;
    }

    public void setNoOfUnsuccessfulCon(int noOfUnsuccessfulCon) {
        this.noOfUnsuccessfulCon = noOfUnsuccessfulCon;
    }

    public PlugType getPlugType() {
        return this.plugType;
    }

    public void setPlugType(PlugType plugType) {
        this.plugType = plugType;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public ConsumptionTsEntity getConsumptionTs() {
        return consumptionTs;
    }

    public void setConsumptionTs(ConsumptionTsEntity consumptionTs) {
        this.consumptionTs = consumptionTs;
    }

    public DeviceFlexibilityDetail getDeviceFlexibilityDetail() {
        return deviceFlexibilityDetail;
    }

    public void setDeviceFlexibilityDetail(DeviceFlexibilityDetail deviceFlexibilityDetail) {
        this.deviceFlexibilityDetail = deviceFlexibilityDetail;
    }

    public GroupingDetail getGroupingDetail() {
        return groupingDetail;
    }

    public void setGroupingDetail(GroupingDetail groupingDetail) {
        this.groupingDetail = groupingDetail;
    }

    public UserT getUser() {
        return user;
    }

    public void setUser(UserT user) {
        this.user = user;
    }

    public DeviceHierarchy getDeviceHierarchy() {
        return deviceHierarchy;
    }

    public void setDeviceHierarchy(DeviceHierarchy deviceHierarchy) {
        this.deviceHierarchy = deviceHierarchy;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isChangedByUser() {
        return changedByUser;
    }

    public void setChangedByUser(boolean changedByUser) {
        this.changedByUser = changedByUser;
    }

    public boolean isFlexible() {
        return isFlexible;
    }

    public void setFlexible(boolean flexible) {
        isFlexible = flexible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceDetail)) return false;

        DeviceDetail that = (DeviceDetail) o;

        if (deviceDetailId != that.deviceDetailId) return false;
        if (defaultState != that.defaultState) return false;
        if (noOfUnsuccessfulCon != that.noOfUnsuccessfulCon) return false;
        if (deviceId != null ? !deviceId.equals(that.deviceId) : that.deviceId != null) return false;
        if (user != null ? !user.equals(that.user) : that.user != null) return false;
        if (deviceType != that.deviceType) return false;
        if (deviceState != that.deviceState) return false;
        if (connectedTime != null ? !connectedTime.equals(that.connectedTime) : that.connectedTime != null)
            return false;
        if (lastConnectedTime != null ? !lastConnectedTime.equals(that.lastConnectedTime) : that.lastConnectedTime != null)
            return false;
        if (consumptionTs != null ? !consumptionTs.equals(that.consumptionTs) : that.consumptionTs != null)
            return false;
        return alias != null ? alias.equals(that.alias) : that.alias == null;
    }

    @Override
    public int hashCode() {

        return Objects.hash(deviceDetailId, deviceId, devicePlugId, plugType, latitude, longitude, user, deviceType, deviceState, connectedTime, lastConnectedTime, consumptionTs, alias, defaultState, noOfUnsuccessfulCon, timeZone);
    }

}
