
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

package org.goflex.wp2.core.models;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by bijay on 11/27/17.
 * Table to store flex_offer data
 */
@Entity
@Table(name = "fmanuser")
public class FmanUser implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    private String foaUserName;

    @Column(unique = true)
    private long organizationId;

    @Column(unique = true)
    private String userName;

    private String password;

    private String APIKey;

    private boolean isActive;

    private Date registrationDate;

    public FmanUser() {

    }

    public FmanUser(String foaUserName, long organizationId, String userName, String password, String APIKey, boolean isActive, Date registrationDate) {
        this.foaUserName = foaUserName;
        this.organizationId = organizationId;
        this.userName = userName;
        this.password = password;
        this.APIKey = APIKey;
        this.isActive = isActive;
        this.registrationDate = registrationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAPIKey() {
        return APIKey;
    }

    public void setAPIKey(String APIKey) {
        this.APIKey = APIKey;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(long organizationId) {
        this.organizationId = organizationId;
    }

    public String getFoaUserName() {
        return foaUserName;
    }

    public void setFoaUserName(String foaUserName) {
        this.foaUserName = foaUserName;
    }
}
