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
 *  Last Modified 2/2/18 4:13 PM
 */

package org.goflex.wp2.core.repository;


import org.goflex.wp2.core.entities.DeviceType;
import org.goflex.wp2.core.entities.PlugType;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.GroupingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.BitSet;
import java.util.List;

/**
 * Created by bijay on 12/2/17.
 */
@Repository
@Transactional
public interface DeviceRepository extends JpaRepository<DeviceDetail, Long> {

    /**
     * find all device in the memory/database
     */
    List<DeviceDetail> findAll();


    /**
     * find all active device
     */
    @Query("SELECT count(d) FROM DeviceDetail d " +
            "WHERE d.deviceState in (0,1)")
    int findAllActiveDevice();


    /**
     * find device by deviceID
     */
    DeviceDetail findByDeviceId(String deviceId);

    /**
     * find device by devicePlugID
     */
    DeviceDetail findBydevicePlugId(String devicePlugId);


    /**
     * find device by deviceID
     */
    DeviceDetail findByDeviceDetailId(Long deviceDetailId);

    /**
     * find device by userid and enabled (username@tplinkdeviceid)
     */
    @Query("SELECT d FROM UserT t " +
            "inner join t.deviceDetail d " +
            "WHERE t.userId = :userId and d.isFlexible = true")
    List<DeviceDetail> findByUserIdAndFlexible(@Param("userId") Long userId);

    /**
     * find device by userid (username@tplinkdeviceid)
     */
    @Query("SELECT d FROM UserT t " +
            "inner join t.deviceDetail d " +
            "WHERE t.userId = :userId ")
    List<DeviceDetail> findByUserId(@Param("userId") Long userId);

    /**
     * find wet devices by userid (username@tplinkdeviceid)
     */
    @Query("SELECT d FROM UserT t " +
            "inner join t.deviceDetail d WHERE t.userId = :userId " +
            "and d.deviceType in (org.goflex.wp2.core.entities.DeviceType.DishWasher, org.goflex.wp2.core.entities.DeviceType.WasherDryer)"
    )
    List<DeviceDetail> findWetDevicesByUserId(@Param("userId") Long userId);

    /**
     * find device by device id (username@tplinkdeviceid)
     */
    @Query("SELECT d FROM UserT t " +
            "inner join t.deviceDetail d " +
            "WHERE d.deviceId = :deviceId")
    DeviceDetail findByDeviceDetailDeviceId(@Param("deviceId") String deviceId);

    @Query("SELECT d FROM UserT t " +
            "inner join t.deviceDetail d " +
            "WHERE d.deviceId LIKE CONCAT('%',:tpLinkDeviceId)")
    DeviceDetail findByTplinkDeviceId(String tpLinkDeviceId);


    @Query("SELECT new org.goflex.wp2.core.wrappers.LocationWrapper(d.deviceId , d.latitude , d.longitude, d.consumptionTs, d.deviceState) FROM UserT u " +
            "inner join u.deviceDetail d " +
            "WHERE u.userName = :userName and u.organizationId = :organizationId")
    List<DeviceDetail> findAllLocationForUser(@Param("userName") String userName, @Param("organizationId") Long organizationId);

    @Query("SELECT new org.goflex.wp2.core.wrappers.LocationWrapper(d.deviceId , d.latitude , d.longitude, d.consumptionTs, d.deviceState) FROM UserT u " +
            "inner join u.deviceDetail d where u.organizationId = :organizationId")
    List<DeviceDetail> findAllLocation(@Param("organizationId") Long organizationId);

    /**
     * delete all devices of the plug type in the argument
     */
    void deleteByPlugType(@Param("plugType") PlugType plugType);

    List<DeviceDetail> findByPlugType(@Param("plugType") PlugType plugType);

    List<DeviceDetail> findByGroupingDetail_GroupId(@Param("groupId") int groupId);

    List<DeviceDetail> findByGroupingDetail_GroupName(@Param("groupName") String groupName);

    @Query("SELECT g from GroupingDetail g WHERE g.groupName = :groupName")
    GroupingDetail findGroupingDetailByGroupName(@Param("groupName") String groupName);

    @Query("SELECT g from GroupingDetail g WHERE g.groupId = :groupId")
    GroupingDetail findGroupingDetailByGroupId(@Param("groupId") long groupId);

    void deleteAllByDeviceHierarchy_HierarchyId(@Param("hierarchyId") long hierarchyId);

    List<DeviceDetail> findAllByDeviceHierarchy_HierarchyId(@Param("hierarchyId") long hierarchyId);

    @Query("SELECT d FROM UserT t " +
            "inner join t.deviceDetail d WHERE t.userId = :userId " +
            "and d.deviceType in " +
            "(org.goflex.wp2.core.entities.DeviceType.AirConditioner," +
            " org.goflex.wp2.core.entities.DeviceType.Boiler," +
            " org.goflex.wp2.core.entities.DeviceType.Freezer," +
            " org.goflex.wp2.core.entities.DeviceType.HeatPump," +
            " org.goflex.wp2.core.entities.DeviceType.RoomHeater," +
            " org.goflex.wp2.core.entities.DeviceType.Refrigerator" +
            ")"
    )
    List<DeviceDetail> findTCLDevicesByUserId(@Param("userId") long id);
}
