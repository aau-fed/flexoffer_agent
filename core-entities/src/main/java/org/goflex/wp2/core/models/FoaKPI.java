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
 *  Last Modified 4/9/18 8:29 PM
 */

package org.goflex.wp2.core.models;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by bijay on 4/9/18.
 */
@Entity
@Table(name = "foakpi")
public class FoaKPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long organizationId;

    private long userId;

    private Date dateofAggregation;

    private int activeDevice;

    private int activeUser;

    private double flexibilityRatio;

    private int foCount;

    public FoaKPI() {

    }

    public FoaKPI(long organizationId, long userId, Date dateofAggregation, int activeDevice, int activeUser, double flexibilityRatio, int foCount) {
        this.organizationId = organizationId;
        this.userId = userId;
        this.dateofAggregation = dateofAggregation;
        this.activeDevice = activeDevice;
        this.activeUser = activeUser;
        this.flexibilityRatio = flexibilityRatio;
        this.foCount = foCount;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(long organizationId) {
        this.organizationId = organizationId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getActiveDevice() {
        return activeDevice;
    }

    public void setActiveDevice(int activeDevice) {
        this.activeDevice = activeDevice;
    }

    public int getActiveUser() {
        return activeUser;
    }

    public void setActiveUser(int activeUser) {
        this.activeUser = activeUser;
    }

    public double getFlexibilityRatio() {
        return flexibilityRatio;
    }

    public void setFlexibilityRatio(double flexibilityRatio) {
        this.flexibilityRatio = flexibilityRatio;
    }

    public int getFoCount() {
        return foCount;
    }

    public void setFoCount(int foCount) {
        this.foCount = foCount;
    }


    public Date getDateofAggregation() {
        return dateofAggregation;
    }

    public void setDateofAggregation(Date dateofAggregation) {
        this.dateofAggregation = dateofAggregation;
    }
}
