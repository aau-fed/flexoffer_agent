
/*
 * Created by bijay
 * GOFLEX :: WP2 :: core-api
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

package org.goflex.wp2.app.fmanintegration.measurements;

import org.goflex.wp2.core.models.UserT;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by bijay on 11/27/17.
 * Table to store flex_offer data
 */
@Entity
@Table(name = "measurement", indexes = {@Index(columnList = "timeStamp"),
        @Index(columnList = "timeInterval"),
        @Index(columnList = "userName"),
        @Index(columnList = "userName, timeInterval")})
public class MeasurementT implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date timeStamp;

    @Column(nullable = false)
    private long timeInterval; // Discretized, like in FlexOffer

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private UserT user;

    @Column(nullable = false, columnDefinition = "VARCHAR(127)")
    private String userName;

    @Column(nullable = false)
    private Double cumulativeEnergy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(long timeInterval) {
        this.timeInterval = timeInterval;
    }

    public UserT getUser() {
        return user;
    }

    public void setUser(UserT user) {
        this.user = user;
    }

    public Double getCumulativeEnergy() {
        return cumulativeEnergy;
    }

    public void setCumulativeEnergy(Double cumulativeEnergy) {
        this.cumulativeEnergy = cumulativeEnergy;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
