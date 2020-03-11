
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
 *  Last Modified 2/22/18 3:04 AM
 */

package org.goflex.wp2.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by aftab on 07/8/18.
 * The table to store hierarchy details for device
 */
@Entity
@Table(name = "device_hierarchy")
public class DeviceHierarchy implements Serializable {

    private static final long serialVersionUID = -7895248816788773034L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "hierarchy_id")
    private long hierarchyId;

    @Column(name = "hierarchy_name", nullable = false)
    private String hierarchyName;

    @OneToMany(mappedBy = "deviceHierarchy", fetch = FetchType.EAGER)
    //@JsonManagedReference
    @JsonIgnore
    private Set<DeviceDetail> deviceDetail = new HashSet<>();


    @Column(name = "user_id")
    private long userId;

    public DeviceHierarchy() {

    }

    public DeviceHierarchy(String hierarchyName) {
        this.hierarchyName = hierarchyName;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getHierarchyId() {
        return hierarchyId;
    }

    public void setHierarchyId(long hierarchyId) {
        this.hierarchyId = hierarchyId;
    }

    public String getHierarchyName() {
        return hierarchyName;
    }

    public void setHierarchyName(String hierarchyName) {
        this.hierarchyName = hierarchyName;
    }

    public Set<DeviceDetail> getDeviceDetail() {
        return deviceDetail;
    }

    public void setDeviceDetail(Set<DeviceDetail> deviceDetail) {
        this.deviceDetail = deviceDetail;
    }

    @Override
    public String toString() {
        return "DeviceHierarchy{" +
                "hierarchyId=" + hierarchyId +
                ", hierarchyName='" + hierarchyName + '\'' +
                ", deviceDetail=" + deviceDetail +
                ", userId=" + userId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceHierarchy)) return false;
        DeviceHierarchy that = (DeviceHierarchy) o;
        return hierarchyId == that.hierarchyId &&
                userId == that.userId &&
                Objects.equals(hierarchyName, that.hierarchyName) &&
                Objects.equals(deviceDetail, that.deviceDetail);
    }

    @Override
    public int hashCode() {

        return Objects.hash(hierarchyId, hierarchyName, deviceDetail, userId);
    }
}
