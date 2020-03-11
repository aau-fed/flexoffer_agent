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
 *  Last Modified 2/21/18 10:21 PM
 */

package org.goflex.wp2.foa.interfaces;


import org.goflex.wp2.core.entities.DeviceDetailData;
import org.goflex.wp2.core.entities.PlugType;
import org.goflex.wp2.core.entities.UserRole;
import org.goflex.wp2.core.models.*;
import org.goflex.wp2.core.wrappers.LocationWrapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by bijay on 12/2/17.
 * this class provides all services related to FOA users
 */
public interface DeviceDetailService {

    List<DeviceDetail> getAllDevices();

    int getAllActiveDevices();

    List<DeviceDetail> getActiveDeviceByUser(Long userId);

    DeviceDetail getDevice(String deviceId);

    DeviceDetail getDeviceByPlugId(String tpLinkDeviceId);

    DeviceDetail getDeviceByDetailId(Long deviceDetailId);

    void addConsumptionTs(long consumptionTSid, DeviceDetailData deviceDetailData);

    void deleteAllDevices();

    void deleteDevicesByDeviceId(long id);

    void updateLastConnectedTime(Date currentDate, String deviceId);

    Map<Date, Double> getConsumptionDataFromDate(String deviceId, Date lastDate);

    Map<Date, Double> getLatestConsumptionData(Map<Date, Double> data);

    Double getAvgConsumptionSince(String deviceId, Date lastDate);

    Map<Date, Double> getConsumptionDataForDate(String deviceId, Date lastDate);

    Map<Date, Double> getPowerConsumption(String deviceId, Date fromDate, Date toDate);

    Double getAvgPowerConsumptionForLastHour(String deviceId);

    Double getMinPowerConsumptionForLastHour(String deviceId);

    Double getAvgPowerConsumptionForLastSevenDays(String deviceId);

    void logDeviceChange(DeviceDetail deviceDetail);

    List<DeviceDetail> getDevicesByPlugType(PlugType plugType);

    Map<Date, Double> getAllConsumptionDataForDevice(String deviceId);

    List<DeviceDetail> getDevicesLocation(String userName, UserRole role, long organizationId);

    List<LocationWrapper> getDevicesLocation2(String userName, UserRole role, long organizationId);

    List<LocationWrapper> getDevicesLocationByHierarchyId(UserT user, long hierarchyId);

    List<DeviceDetail> getDevicesByGroupId(int groupId);

    List<DeviceDetail> getDevicesByGroupName(String groupName);

    GroupingDetail getGroupingDetailByGroupName(String groupName);

    GroupingDetail getGroupingDetailByGroupId(long groupId);

    void addGroupingDetail(GroupingDetail groupingDetail);

    DeviceHierarchy getDeviceHierarchyByHierarchyName(String hierarchyName);

    DeviceHierarchy getDeviceHierarchyByHierarchyId(long hierarchyId);

    void addDeviceHierarchy(DeviceHierarchy deviceHierarchy);

    void deleteDeviceHierarchyByHierarchyName(String hierarchyName);

    List<DeviceHierarchy> getAllDeviceHierarchiesByUserId(long userId);

    DeviceHierarchy getDeviceHierarchyByUserId(long userId);

    void updateTimezone(DeviceDetail deviceDetail);

    void deleteDevicesByHierarchyId(long hierarchyId);

    String updateDeviceLocation(String deviceId, double latitude, double longitude);

    @Transactional
    String toggleDeviceFlexibility(String deviceId, boolean flex);

    DeviceDataSuppl getLatestSupplData(long id);

    @Transactional
    String resetDeviceFlexibilityParams(String deviceId, Long organizationId);

    void updateDeviceList(List<DeviceDetail> devices);

    Double getMinConsumptionSince(String deviceId, Date sinceTime);

    @Transactional
    String updateDeviceFlexibilityParams(String deviceId, DeviceFlexibilityDetail newDeviceFlexParams);

    @Transactional
    Integer getDeviceOnDuration(long timeSeriesId);
}
