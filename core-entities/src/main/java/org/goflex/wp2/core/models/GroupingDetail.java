
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
import java.util.Set;

/**
 * Created by bijay on 12/2/17.
 * The table to store grouping details for device
 */
@Entity
@Table(name = "grouping_detail")
public class GroupingDetail implements Serializable {

    private static final long serialVersionUID = 6410946071190455958L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "group_id")
    private long groupId;

    @Column(name = "location_id")
    private int locationId = -1;

    @Column(name = "group_name", unique = true)
    private String groupName;

    //@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "groupingDetail", fetch=FetchType.LAZY)
    @OneToMany(mappedBy = "groupingDetail", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<DeviceDetail> deviceDetail = new HashSet<>();

    public GroupingDetail() {

    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }


    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Set<DeviceDetail> getDeviceDetail() {
        return deviceDetail;
    }

    public void setDeviceDetail(Set<DeviceDetail> deviceDetail) {
        this.deviceDetail = deviceDetail;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }
}
