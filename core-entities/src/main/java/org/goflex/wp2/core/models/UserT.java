
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
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.goflex.wp2.core.entities.UserRole;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by bijay on 12/2/17.
 * The table to store prosumer details
 */
@Entity
@Table(name = "prosumer")
public class UserT implements Serializable {

    private static final long serialVersionUID = -8489623831958938807L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long userId;
    /* Prosumer userName should match with tp-link username in case of cloud FOA*/
    @Column(unique = true)
    private String userName;
    @Column(unique = true)
    private String email;
    private String Password;
    @Column(unique = true)
    private String tpLinkUserName = null;
    /* Tplink username*/
    //@JsonIgnore
    private String tpLinkPassword = null;
    /* Tplink APIKey*/
    @JsonIgnore
    private String APIKey = "";
    /* Tplink TpLink smart plug */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user", fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    private Set<DeviceDetail> deviceDetail = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    @JsonManagedReference
    private UserAddress userAddress;

/*
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user", fetch=FetchType.LAZY)
    //@JsonManagedReference
    @JsonIgnore
    private Set<DeviceHierarchy> deviceHierarchyList = new HashSet<>();*/
    // User Role
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.ROLE_PROSUMER;
    @Lob
    @Column(name = "pic")
    private byte[] pic;
    private Date registrationDate;
    //Store last loginsession
    private Date lastLoginDate;
    private long organizationId;
    // Is login enabled for user
    private boolean enabled = true;

    public UserT() {

    }


    public UserT(String userName, String password, String email, String tpLinkUserName, String tpLinkPassword,
                 long organizationId) {
        this.userName = userName;
        this.Password = password;
        this.email = email;
        this.tpLinkUserName = tpLinkUserName;
        this.tpLinkPassword = tpLinkPassword;
        this.organizationId = organizationId;
    }

    public long getId() {
        return userId;
    }

    public void setId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTpLinkUserName() {
        return tpLinkUserName;
    }

    public void setTpLinkUserName(String tpLinkUserName) {
        this.tpLinkUserName = tpLinkUserName;
    }

    public String getTpLinkPassword() {
        return tpLinkPassword;
    }

    public void setTpLinkPassword(String tpLinkPassword) {
        this.tpLinkPassword = tpLinkPassword;
    }

    @JsonIgnore
    public Set<DeviceDetail> getDeviceDetail() {
        return deviceDetail;
    }

    public void setDeviceDetail(Set<DeviceDetail> deviceDetail) {
        this.deviceDetail = deviceDetail;
    }

    public void addDeviceDetail(DeviceDetail devicedetail) {
        devicedetail.setUser(this);
        this.deviceDetail.add(devicedetail);
    }

    public void addDevice(DeviceDetail device) {
        this.deviceDetail.add(device);
    }

    public void removeDevice(Long id) {
        this.deviceDetail.remove(id);
    }

    public void removeDevice(DeviceDetail device) {
        this.deviceDetail.remove(device);
        if (device != null) {
            device.setUser(null);
        }

    }

    @JsonIgnore
    public String getAPIKey() {
        return APIKey;
    }

    public void setAPIKey(String APIKey) {
        this.APIKey = APIKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserT)) return false;

        UserT userT = (UserT) o;

        if (userId != userT.userId) return false;
        if (userName != null ? !userName.equals(userT.userName) : userT.userName != null) return false;
        if (Password != null ? !Password.equals(userT.Password) : userT.Password != null) return false;
        if (email != null ? !email.equals(userT.email) : userT.email != null) return false;
        if (tpLinkUserName != null ? !tpLinkUserName.equals(userT.tpLinkUserName) : userT.tpLinkUserName != null)
            return false;
        if (tpLinkPassword != null ? !tpLinkPassword.equals(userT.tpLinkPassword) : userT.tpLinkPassword != null)
            return false;
        if (APIKey != null ? !APIKey.equals(userT.APIKey) : userT.APIKey != null) return false;
        return deviceDetail != null ? deviceDetail.equals(userT.deviceDetail) : userT.deviceDetail == null;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public UserAddress getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(UserAddress userAddress) {
        this.userAddress = userAddress;
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

    public void setOrganization(long organizationId) {
        this.organizationId = organizationId;
    }

    public byte[] getPic() {
        return pic;
    }

    public void setPic(byte[] pic) {
        this.pic = pic;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    /* public Set<DeviceHierarchy> getDeviceHierarchyList() {
        return deviceHierarchyList;
    }

    public void setDeviceHierarchyList(Set<DeviceHierarchy> deviceHierarchyList) {
        this.deviceHierarchyList = deviceHierarchyList;
    }*/

/*@Override
    public String toString() {
        return "UserT{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", Password='" + Password + '\'' +
                ", tpLinkUserName='" + tpLinkUserName + '\'' +
                ", tpLinkPassword='" + tpLinkPassword + '\'' +
                ", APIKey='" + APIKey + '\'' +
                ", deviceDetail=" + deviceDetail +
                '}';
    }*/

    /*@Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (Password != null ? Password.hashCode() : 0);
        result = 31 * result + (tpLinkUserName != null ? tpLinkUserName.hashCode() : 0);
        result = 31 * result + (tpLinkPassword != null ? tpLinkPassword.hashCode() : 0);
        result = 31 * result + (APIKey != null ? APIKey.hashCode() : 0);
        result = 31 * result + (deviceDetail != null ? deviceDetail.hashCode() : 0);
        return result;
    }*/


}
