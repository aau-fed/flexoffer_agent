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
 *  Last Modified 2/22/18 9:56 PM
 */

package org.goflex.wp2.foa.implementation;

/**
 * This class sends heartbeat to FMAN-FMAR at regular interval
 * <p>
 * Created by bijay on 7/7/17.
 * Updated by aftab
 */

import org.goflex.wp2.core.entities.DeviceDetailData;
import org.goflex.wp2.core.entities.DeviceType;
import org.goflex.wp2.core.entities.PlugType;
import org.goflex.wp2.core.entities.UserRole;
import org.goflex.wp2.core.models.*;
import org.goflex.wp2.core.repository.*;
import org.goflex.wp2.core.wrappers.LocationWrapper;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class DeviceDetailServiceImpl implements DeviceDetailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceDetailServiceImpl.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    @Resource(name = "defaultFlexibilitySettings")
    Map<Long, DeviceFlexibilityDetail> defaultFlexibilitySettings;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceHierarchyRepository deviceHierarchyRepository;

    @Autowired
    private GroupingDetailRepository groupingDetailRepository;

    @Autowired
    private UserDeviceLogRepository userDeviceLogRepository;

    @Autowired
    private ConsumptionTsServiceImpl consumptionTsService;

    @Autowired
    public List<DeviceDetail> getAllDevices() {
        return deviceRepository.findAll();
    }

    @Autowired
    public int getAllActiveDevices() {
        return deviceRepository.findAllActiveDevice();
    }

    @Override
    public List<DeviceDetail> getActiveDeviceByUser(Long userId) {
        return deviceRepository.findByUserId(userId);
    }

    @Override
    public DeviceDetail getDevice(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId);
    }

    @Override
    public DeviceDetail getDeviceByPlugId(String devicePlugId) {
        return deviceRepository.findBydevicePlugId(devicePlugId);
    }


    @Override
    public DeviceDetail getDeviceByDetailId(Long deviceDetailId) {
        return deviceRepository.findByDeviceDetailId(deviceDetailId);
    }

    @Override
    public void logDeviceChange(DeviceDetail deviceDetail) {
        UserDeviceLog deviceLog = new UserDeviceLog();
        deviceLog.setPrevDeviceType(deviceDetail.getDeviceType());
        deviceLog.setEventDate(new Date());
        deviceLog.setDeviceID(deviceDetail.getDeviceId());
        deviceLog.setDetails("Device Type Changed");
        userDeviceLogRepository.save(deviceLog);
    }

    @Override
    public List<DeviceDetail> getDevicesByPlugType(PlugType plugType) {
        return deviceRepository.findByPlugType(plugType);
    }


    /**
     * delete all devices for all users
     */
    @Override
    public void deleteAllDevices() {
        deviceRepository.deleteAll();
    }

    /**
     * delete device by device id
     */
    @Override
    public void deleteDevicesByDeviceId(long id) {
        deviceRepository.deleteById(id);
    }


    @Override
    public void updateLastConnectedTime(Date currentDate, String deviceId) {
        DeviceDetail cTS = deviceRepository.findByDeviceId(deviceId);
        cTS.setLastConnectedTime(currentDate);
    }


    //Return last 24 TS
    @Override
    public Map<Date, Double> getLatestConsumptionData(Map<Date, Double> data) {
        Map<Date, Double> latestData = new TreeMap<>();

        int lastIdx = data.size() > 1440 ? data.size() - 1440 : 0;
        List<Date> keyList = new ArrayList<>(data.keySet());
        for (int i = data.size(); i >= lastIdx + 1; i--) {
            latestData.put(keyList.get(i - 1), data.get(keyList.get(i - 1)));
        }
        return latestData;
    }

    @Override
    public void addConsumptionTs(long consumptionTSid, DeviceDetailData deviceDetailData) {
        ConsumptionTsEntity cts = consumptionTsService.getCtsById(consumptionTSid);
        cts.setLatestPower(deviceDetailData.getDeviceData().getPower());
        cts.setLatestVoltage(deviceDetailData.getDeviceData().getVoltage());
        cts.addData(deviceDetailData.getDeviceData());
        if (deviceDetailData.getDeviceDataSuppl() != null) {
            cts.addSupplData(deviceDetailData.getDeviceDataSuppl());

        }
    }


    private Date getNextDayDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, 1);
        return c.getTime();
    }


    /**
     * returns consumption data from the specified date and onwards
     *
     * @param deviceId
     * @param lastDate
     * @return
     */
    @Override
    public Map<Date, Double> getConsumptionDataFromDate(String deviceId, Date lastDate) {

        /** get device by device Id*/
        DeviceDetail cTS = deviceRepository.findByDeviceId(deviceId);

        List<DeviceData> dat = consumptionTsService.getCtsFromDate(cTS.getConsumptionTs().getId(), lastDate);

        Map<Date, Double> latestData = dat.stream().collect(
                Collectors.toMap(DeviceData::getDate, DeviceData::getPower, (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new)
        );

        return latestData;
    }

    @Override
    public Double getAvgConsumptionSince(String deviceId, Date lastDate) {
        DeviceDetail deviceDetail = deviceRepository.findByDeviceId(deviceId);
        return consumptionTsService.getAvgCtsFromDate(deviceDetail.getConsumptionTs().getId(), lastDate);
    }

    /**
     * Get consumption data for a particular date
     */
    @Override
    public Map<Date, Double> getConsumptionDataForDate(String deviceId, Date consumptionDate) {

        /** get device by device Id*/
        DeviceDetail cTS = deviceRepository.findByDeviceId(deviceId);

        List<DeviceData> dat = consumptionTsService
                .getCtsForDate(cTS.getConsumptionTs().getId(), consumptionDate, getNextDayDate(consumptionDate));
        Map<Date, Double> latestData = dat.stream().collect(
                Collectors.toMap(DeviceData::getDate, DeviceData::getPower, (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new)
        );

        return latestData;
    }

    @Override
    public Map<Date, Double> getPowerConsumption(String deviceId, Date fromDate, Date toDate) {
        DeviceDetail deviceDetail = this.deviceRepository.findByDeviceId(deviceId);

        List<DeviceData> deviceDataList = consumptionTsService.
                getCtsForDate(deviceDetail.getConsumptionTs().getId(), fromDate, toDate);

        Map<Date, Double> devicePowerDataMap = deviceDataList.stream()
                .collect(Collectors.toMap(DeviceData::getDate, DeviceData::getPower, (oldVal, newVal) -> oldVal,
                        LinkedHashMap::new));

        return devicePowerDataMap;
    }

    @Override
    public Double getAvgPowerConsumptionForLastHour(String deviceId) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, -1);
        Date sinceTime = calendar.getTime();
        Double lastHourData = this.getAvgConsumptionSince(deviceId, sinceTime);
        if (lastHourData == null) {
            lastHourData = 0.0;
        }
        return lastHourData;
    }

    @Override
    public Double getMinPowerConsumptionForLastHour(String deviceId) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, -1);
        Date sinceTime = calendar.getTime();
        Double val = this.getMinConsumptionSince(deviceId, sinceTime);
        if (val == null) {
            val = 0.0;
        }
        return val;
    }

    @Override
    public Double getAvgPowerConsumptionForLastSevenDays(String deviceId) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -7);
        Date sinceTime = calendar.getTime();
        Double lastSevenDaysData = this.getAvgConsumptionSince(deviceId, sinceTime);
        if (lastSevenDaysData == null) {
            lastSevenDaysData = 0.0;
        }
        return lastSevenDaysData;
    }


    /**
     * Get consumption data for a particular device given by deviceId
     */
    @Override
    public Map<Date, Double> getAllConsumptionDataForDevice(String deviceId) {

        /** get device by device Id*/
        DeviceDetail cTS = deviceRepository.findByDeviceId(deviceId);

        List<DeviceData> dat = consumptionTsService.getCtsForDevice(cTS.getConsumptionTs().getId());

        Map<Date, Double> latestData = dat.stream().collect(
                Collectors.toMap(DeviceData::getDate, DeviceData::getPower, (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new)
        );
        return latestData;
    }


    @Override
    public List<DeviceDetail> getDevicesLocation(String userName, UserRole role, long organizationId) {

        if (role != null) {
            if (role == UserRole.ROLE_ADMIN) {
                return deviceRepository.findAllLocation(organizationId);
            }
        }
        return deviceRepository.findAllLocationForUser(userName, organizationId);

    }

    /**
     * Temporarily added by Aftab because we needed to deviceHierarchy to response
     * the above function uses db query method, which turned out to be challenging to modify. :(
     *
     * @param userName
     * @param role
     * @param organizationId
     * @return
     */
    @Override
    public List<LocationWrapper> getDevicesLocation2(String userName, UserRole role, long organizationId) {

        UserT userT = userRepository.findByUserName(userName);
        if (userT == null) {
            return null;
        }

        List<DeviceDetail> deviceDetailList = new ArrayList<>();

        if (role != null && role == UserRole.ROLE_ADMIN) {
            deviceDetailList = userRepository.findByOrganizationId(organizationId);
        } else {
            userT.getDeviceDetail().forEach(deviceDetailList::add);
        }

        List<LocationWrapper> locationWrapperList = new ArrayList<>();
        deviceDetailList.forEach(device -> {
            LocationWrapper locationWrapper = new LocationWrapper();
            locationWrapper.setDeviceId(device.getDeviceId());
            locationWrapper.setLatitude(device.getLatitude());
            locationWrapper.setLongitude(device.getLongitude());
            locationWrapper.setDeviceState(device.getDeviceState());
            locationWrapper.setConsumptionTs(device.getConsumptionTs());
            if (device.getDeviceHierarchy() != null) {
                locationWrapper.setDeviceHierarchy(device.getDeviceHierarchy());
            } else {
                locationWrapper.setDeviceHierarchy(new DeviceHierarchy("Not Assigned"));
            }

            locationWrapperList.add(locationWrapper);
        });
        return locationWrapperList;
    }


    public List<LocationWrapper> getDevicesLocationByHierarchyId(UserT user, long hierarchyId) {
        List<DeviceDetail> deviceDetailList = new ArrayList<>();

        if (user.getRole() != null && user.getRole() == UserRole.ROLE_ADMIN) {
            deviceDetailList = userRepository.findByOrganizationId(user.getOrganizationId());
        } else {
            user.getDeviceDetail().forEach(deviceDetailList::add);
        }

        List<LocationWrapper> locationWrapperList = new ArrayList<>();
        deviceDetailList.forEach(device -> {
            if (device.getDeviceHierarchy() != null) {
                if (device.getDeviceHierarchy().getHierarchyId() == hierarchyId) {
                    LocationWrapper locationWrapper = new LocationWrapper();
                    locationWrapper.setDeviceId(device.getDeviceId());
                    locationWrapper.setLatitude(device.getLatitude());
                    locationWrapper.setLongitude(device.getLongitude());
                    locationWrapper.setDeviceState(device.getDeviceState());
                    locationWrapper.setConsumptionTs(device.getConsumptionTs());
                    locationWrapper.setDeviceHierarchy(device.getDeviceHierarchy());
                    locationWrapperList.add(locationWrapper);
                }
            }
        });
        return locationWrapperList;
    }

    @Override
    @Transactional
    public void updateTimezone(DeviceDetail deviceDetail) {
        if (deviceDetail != null) {
            this.deviceRepository.save(deviceDetail);
        }
    }

    @Override
    public void deleteDevicesByHierarchyId(long hierarchyId) {
        deviceRepository.deleteAllByDeviceHierarchy_HierarchyId(hierarchyId);
    }

    @Override
    @Transactional
    public String updateDeviceLocation(String deviceId, double latitude, double longitude) {
        try {
            DeviceDetail device = deviceRepository.findByDeviceId(deviceId);
            device.setLatitude(latitude);
            device.setLongitude(longitude);
            device.setChangedByUser(true);
            return "successfully updated device location";
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    @Override
    public List<DeviceDetail> getDevicesByGroupId(int groupId) {
        return deviceRepository.findByGroupingDetail_GroupId(groupId);
    }

    @Override
    public List<DeviceDetail> getDevicesByGroupName(String groupName) {
        return deviceRepository.findByGroupingDetail_GroupName(groupName);
    }


    @Override
    public GroupingDetail getGroupingDetailByGroupName(String groupName) {
        //return deviceRepository.findGroupingDetailByGroupName(groupName);
        return groupingDetailRepository.findByGroupName(groupName);
    }

    @Override
    public GroupingDetail getGroupingDetailByGroupId(long groupId) {
        //return deviceRepository.findGroupingDetailByGroupId(groupId);
        return groupingDetailRepository.findByGroupId(groupId);
    }

    @Override
    public void addGroupingDetail(GroupingDetail groupingDetail) {
        groupingDetailRepository.save(groupingDetail);
    }

    @Override
    public DeviceHierarchy getDeviceHierarchyByHierarchyName(String hierarchyName) {
        return deviceHierarchyRepository.findByHierarchyName(hierarchyName);
    }


    @Override
    public DeviceHierarchy getDeviceHierarchyByHierarchyId(long hierarchyId) {
        return deviceHierarchyRepository.findByHierarchyId(hierarchyId);
    }

    @Override
    public void addDeviceHierarchy(DeviceHierarchy deviceHierarchy) {
        deviceHierarchyRepository.save(deviceHierarchy);
    }

    @Override
    public List<DeviceHierarchy> getAllDeviceHierarchiesByUserId(long userid) {
        return deviceHierarchyRepository.findAllByUserId(userid);
    }

    @Override
    public DeviceHierarchy getDeviceHierarchyByUserId(long userId) {
        return deviceHierarchyRepository.findByUserId(userId);
    }

    @Override
    public void deleteDeviceHierarchyByHierarchyName(String hierarchyName) {
        deviceHierarchyRepository.deleteByHierarchyName(hierarchyName);
    }

    @Override
    @Transactional
    public String toggleDeviceFlexibility(String deviceId, boolean flex) {
        try {
            DeviceDetail device = deviceRepository.findByDeviceId(deviceId);
            device.setFlexible(flex);
            return "successfully updated device flexibility";
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    @Override
    @Transactional
    public String resetDeviceFlexibilityParams(String deviceId, Long organizationId) {
        try {
            DeviceDetail device = deviceRepository.findByDeviceId(deviceId);
            DeviceFlexibilityDetail dfd = new DeviceFlexibilityDetail();
            dfd.setDailyControlStart(defaultFlexibilitySettings.get(organizationId).getDailyControlStart());
            dfd.setDailyControlEnd(defaultFlexibilitySettings.get(organizationId).getDailyControlEnd());
            dfd.setNoOfInterruptionInADay(defaultFlexibilitySettings.get(organizationId).getNoOfInterruptionInADay());
            dfd.setMaxInterruptionLength(defaultFlexibilitySettings.get(organizationId).getMaxInterruptionLength());
            dfd.setMinInterruptionInterval(defaultFlexibilitySettings.get(organizationId).getMinInterruptionInterval());
            dfd.setMaxInterruptionDelay(defaultFlexibilitySettings.get(organizationId).getMaxInterruptionDelay());
            dfd.setLatestAcceptanceTime(defaultFlexibilitySettings.get(organizationId).getLatestAcceptanceTime());
            device.setDeviceFlexibilityDetail(dfd);
            return "successfully reset device flexibility params to organization defaults";
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    @Override
    public void updateDeviceList(List<DeviceDetail> devices) {
    }

    @Override
    public Double getMinConsumptionSince(String deviceId, Date sinceTime) {
        DeviceDetail deviceDetail = deviceRepository.findByDeviceId(deviceId);
        return consumptionTsService.getMinCtsFromDate(deviceDetail.getConsumptionTs().getId(), sinceTime);
    }

    public void deleteSimulatedDevices() {
        deviceRepository.deleteByPlugType(PlugType.Simulated);
    }

    public DeviceDataSuppl getLatestSupplData(long id) {
        return consumptionTsService.getLatestSupplData(id);
    }

    @Override
    @Transactional
    public String updateDeviceFlexibilityParams(String deviceId, DeviceFlexibilityDetail newDfd) {
        try {
            DeviceDetail device = deviceRepository.findByDeviceId(deviceId);
            DeviceFlexibilityDetail dfd = new DeviceFlexibilityDetail();
            dfd.setNoOfInterruptionInADay(newDfd.getNoOfInterruptionInADay());
            dfd.setMaxInterruptionLength(newDfd.getMaxInterruptionLength());
            dfd.setMinInterruptionInterval(newDfd.getMinInterruptionInterval());
            dfd.setMaxInterruptionDelay(newDfd.getMaxInterruptionDelay());
            dfd.setLatestAcceptanceTime(newDfd.getLatestAcceptanceTime());
            dfd.setDailyControlStart(newDfd.getDailyControlStart());
            dfd.setDailyControlEnd(newDfd.getDailyControlEnd());
            device.setDeviceFlexibilityDetail(dfd);
            return "successfully updated device flexibility params";
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    @Override
    @Transactional
    public Integer getDeviceOnDuration(long timeSeriesId) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -1);
        Date sinceTime = calendar.getTime();
        Integer val = this.consumptionTsService.getDeviceOnDuration(timeSeriesId, sinceTime);
        // one slice duration is minimum
        if (val == null || val < 15 ) {
            val = 15;
        }
        ConsumptionTsEntity cts = consumptionTsService.getCtsById(timeSeriesId);
        cts.setAverageOnDuration(val);
        return val;
    }
}
