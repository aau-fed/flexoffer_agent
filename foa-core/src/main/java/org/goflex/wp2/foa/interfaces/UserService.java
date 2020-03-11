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
 *  Last Modified 2/21/18 11:26 AM
 */

package org.goflex.wp2.foa.interfaces;


import org.goflex.wp2.core.entities.DeviceBasicInfoDto;
import org.goflex.wp2.core.entities.DeviceDetailData;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.UserT;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by bijay on 12/2/17.
 * this class provides all services related to FOA users
 */
public interface UserService {

    /**
     * check if a user exist with a given name and password
     */
    UserT getUser(String userName, String password);

    List<UserT> getAllUsersForOrganization(long organizationID);

    List<UserT> getActiveUsersForOrganization(long organizationID);

    /**
     * check if a user exist with a given name
     */
    UserT getUser(String userName);

    List<String> getAllUserName(long organizationID);

    UserT getUserByLoadId(String plugUserName);

    boolean tpLinkUserExist(String tplinkUserName);

    /**
     * Get All Users
     */
    List<UserT> getUsers();

    UserT getActiveUser(String userName);

    /**
     * Get Count of Active Users
     */
    int getActiveUserCount(long organizationId);

    int getDevicesforOrganization(long organizationId);


    int getActiveProsumerCount(long organizationId);

    /**
     * Get All Users with device list
     */
    Set<UserT> getUsersFetchDevice();

    /**
     * Save user in to the user table
     */
    UserT save(UserT user);

    /**
     * Save user in to the user table
     */
    UserT updateDeviceList(String user, DeviceDetail device);

    /**
     * get list of devices registered to a particular user
     */
    Set<DeviceDetail> getDevices(String userName);

    Set<DeviceDetail> getDeviceList(String userName);

    /**
     * get a list containing basic info of devices registered to a user
     */
    Set<DeviceBasicInfoDto> getDeviceBasicInfoList(String sessionUser);

    Set<DeviceBasicInfoDto> getDeviceSummaryListForOrganization(long organizationId);

    /**
     * get list of devices registered to a particular organization
     */
    List<DeviceDetail> getDeviceListforOrganization(long organizationId);

    /**get list of devices registered to a particular user*//*
    DeviceDetail getDevice(String deviceID);*/

    /**get list of all devices in memory*//*
    List<DeviceDetail> getAllDevices();*/

    /**
     * remove devices with particular id
     */
    Boolean removeDeviceID(String user, String deviceId);

    /**
     * remove all devices for a user
     */
    Boolean removeDevices(UserT user);

    /**
     * remove user with particular userName
     */
    Boolean removeUser(String userName);

    Boolean disableUser(UserT userToDelete);

    /**
     * Update user credential
     */
    String updateUserCredential(String user, Map<String, Object> payload);

    /**
     * Update deviceDetail Data, add consumption
     */
    String updateDeviceDetail(String user, Map<String, Object> payload);

    /**
     * update tplink APIKey for the user
     */
    void saveTPLinkAPIKey(String user, String APIKey);

    /**
     * update state for a device
     */
    DeviceDetail updateDeviceState(DeviceDetail deviceDetail, DeviceDetailData consumptionData);

    /**
     * store consumption data for a device
     */
    void storeDeviceConsumption(DeviceDetail deviceDetail, DeviceDetailData consumptionData);

    /**
     * reset unsuccessful connection to device
     */
    void resetDeviceLastCon(long deviceDetailID);

    UserT getUserByEmail(String email);

    UserT getUserByTpLinkUserName(String tpLinkUserName);

    boolean updateProfilePhoto(UserT user, MultipartFile photo);

    void registerLoginTime(UserT user);

}
