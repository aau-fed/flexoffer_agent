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
 *  Last Modified 2/21/18 11:50 AM
 */

package org.goflex.wp2.foa.implementation;


import org.aspectj.weaver.ast.Or;
import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.core.models.*;
import org.goflex.wp2.core.repository.DeviceHierarchyRepository;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.core.repository.UserRepository;
import org.goflex.wp2.foa.events.SetupTpLinkDevicesEvent;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.goflex.wp2.foa.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by bijay on 12/2/17.
 * this class implements the userservice methods
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserRepository userRepository;

    private DeviceHierarchyRepository deviceHierarchyRepository;

    private DeviceDetailService deviceDetailService;

    private ApplicationEventPublisher applicationEventPublisher;

    private OrganizationRepository organizationRepository;

    @Resource(name = "poolDeviceDetail")
    private ConcurrentHashMap<String, Map<String, PoolDeviceModel>> poolDeviceDetail;

    public UserServiceImpl() {}

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           DeviceHierarchyRepository deviceHierarchyRepository,
                           DeviceDetailService deviceDetailService,
                           ApplicationEventPublisher applicationEventPublisher,
                           OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.deviceHierarchyRepository = deviceHierarchyRepository;
        this.deviceDetailService = deviceDetailService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.organizationRepository = organizationRepository;
    }


    @Override
    public UserT getUser(String userName, String password) {

        UserT user = userRepository.findByUserNameAndEnabled(userName, true);
        return user;

    }


    @Override
    public List<UserT> getAllUsersForOrganization(long organizationID) {
        List<UserT> users = userRepository.findAllByOrganizationId(organizationID);
        return users;
    }


    @Override
    public List<UserT> getActiveUsersForOrganization(long organizationID) {
        List<UserT> users = userRepository.findAllByOrganizationIdAndEnabled(organizationID, true);
        return users;
    }

    @Override
    public UserT getUserByLoadId(String plugUserName) {
        UserT user = userRepository.findByTpLinkUserName(plugUserName);
        return user;

    }

    @Override
    public List<String> getAllUserName(long organizationID) {
        List<String> user = userRepository.getAllUserName(organizationID);
        return user;

    }

    @Override
    public UserT getUser(String userName) {
        UserT user = userRepository.findByUserName(userName);
        return user;
    }

    @Override
    public UserT getActiveUser(String userName) {
        UserT user = userRepository.findByUserNameAndEnabled(userName, true);
        return user;
    }

    @Override
    public List<UserT> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public int getActiveUserCount(long organizationId) {
        return userRepository.findActiveUserCount(organizationId);
    }

    @Override
    public int getActiveProsumerCount(long organizationId) {
        return userRepository.findActiveUserCount(organizationId);
    }


    @Override
    public Set<UserT> getUsersFetchDevice() {
        return userRepository.findAllFetchDevice();
    }

    @Override
    @Required
    public Set<DeviceDetail> getDevices(String userName) {
        UserT user = userRepository.findByUserName(userName);
        return user.getDeviceDetail();
    }

    @Override
    public int getDevicesforOrganization(long organizationId) {
        return userRepository.findByOrganizationId(organizationId).size();

    }


    @Override
    public Set<DeviceDetail> getDeviceList(String userName) {
        UserT user = userRepository.findByUserName(userName);
        return user.getDeviceDetail();
    }

    private DeviceBasicInfoDto getBasicInfo(DeviceDetail device) {
        DeviceBasicInfoDto deviceBasicInfoDto = new DeviceBasicInfoDto();
        deviceBasicInfoDto.setDeviceId(device.getDeviceId());
        deviceBasicInfoDto.setVoltage(device.getConsumptionTs().getLatestVoltage());
        deviceBasicInfoDto.setPower(device.getConsumptionTs().getLatestPower());
        deviceBasicInfoDto.setDeviceState(device.getDeviceState());
        deviceBasicInfoDto.setDeviceType(device.getDeviceType());
        deviceBasicInfoDto.setAlias(device.getAlias());
        deviceBasicInfoDto.setFlexible(device.isFlexible());
        if (device.getDeviceHierarchy() != null) {
            deviceBasicInfoDto.setGroupName(device.getDeviceHierarchy().getHierarchyName());
        }

        return deviceBasicInfoDto;

    }

    @Override
    public Set<DeviceBasicInfoDto> getDeviceBasicInfoList(String userName) {

        UserT user = userRepository.findByUserName(userName);
        return user.getDeviceDetail().stream().map(device -> this.getBasicInfo(device))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<DeviceBasicInfoDto> getDeviceSummaryListForOrganization(long organizationId) {
        return userRepository.findByOrganizationId(organizationId).stream().map(device -> this.getBasicInfo(device))
                .collect(Collectors.toSet());
    }

    @Override
    public List<DeviceDetail> getDeviceListforOrganization(long organizationId) {
        return userRepository.findByOrganizationId(organizationId);

    }

    public boolean tpLinkUserExist(String tplinkUserName) {
        return userRepository.findByTpLinkUserName(tplinkUserName) != null;

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserT save(UserT user) {
        //user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.saveAndFlush(user);
    }

    /*update device list for the given user*/
    @Override
    @Transactional
    public UserT updateDeviceList(String user, DeviceDetail device) {

        if (user == "" || user == null) {
            return null;
        }
        /** check user exists*/
        UserT userData = userRepository.findByUserName(user);
        if (userData == null) {
            return null;
        }

        /** check if device with the given deviceId already exists exists*/
        if (deviceDetailService.getDevice(device.getDeviceId()) != null) {
            return null;
        }

        /** devicedetailid is autogenerated, check it is not preassigned a value*/
        if (device.getDeviceDetailId() > 0) {
            return null;
        }

        /** add device to user device list*/
        userData.addDeviceDetail(device);
        return userData;
    }

    @Override
    @Transactional
    public void saveTPLinkAPIKey(String user, String APIKey) {
        UserT userData = userRepository.findByUserName(user);
        if (userData != null) {
            userData.setAPIKey(APIKey);
        }
    }

    @Override
    public void storeDeviceConsumption(DeviceDetail deviceDetail, DeviceDetailData deviceDetailData) {
        try {
            if (deviceDetailData.getTime() != null) {
                deviceDetailService.addConsumptionTs(deviceDetail.getConsumptionTs().getId(), deviceDetailData);
            } else {
                LOGGER.debug("Error storing device consumption date. deviceDetailData.getTime is null for {}",
                        deviceDetail.getDeviceId());
            }
        } catch (Exception e) {
            LOGGER.error("Error in updateDeviceStateAndConsumption. " + e.getLocalizedMessage());
        }
    }

    @Override
    public DeviceDetail updateDeviceState(DeviceDetail deviceDetail, DeviceDetailData deviceDetailData) {
        try {

            // update alias if new alias received
            if (!StringUtils.isEmpty(deviceDetailData.getAlias())) {
                deviceDetail.setAlias(deviceDetailData.getAlias());
            }

            // update device state
            if (deviceDetailData.getState() != null) {
                deviceDetail.setDeviceState(deviceDetailData.getState());
            } else {
                deviceDetail.setDeviceState(DeviceState.Unknown);
            }

            // track device availability
            if (deviceDetailData.getState() == DeviceState.Idle ||
                    deviceDetailData.getState() == DeviceState.Operating) {
                Date lastConnected = new Date();
                deviceDetail.setLastConnectedTime(lastConnected);
                deviceDetail.setNoOfUnsuccessfulCon(0);
            } else {
                deviceDetail.setNoOfUnsuccessfulCon(deviceDetail.getNoOfUnsuccessfulCon() + 1);
            }

            // update location if new location received
            if (deviceDetailData.getLatitude() != 0.0) {
                deviceDetail.setLatitude(deviceDetailData.getLatitude());
            }
            if (deviceDetailData.getLongitude() != 0.0) {
                deviceDetail.setLongitude(deviceDetailData.getLongitude());
            }

            // update consumption ts
            if (deviceDetailData.getTime() != null) {
                double currentDefVal = deviceDetail.getConsumptionTs().getDefaultValue();
                if (currentDefVal == 0.0 && currentDefVal  < deviceDetailData.getValue()) {
                    deviceDetail.getConsumptionTs().setDefaultValue(deviceDetailData.getValue());
                }
            } else {
                deviceDetail.getConsumptionTs().setLatestPower(-1);
                deviceDetail.getConsumptionTs().setLatestVoltage(-1);
                LOGGER.debug("deviceDetailData.getTime is null for {}", deviceDetail.getDeviceId());
            }

            String userName = deviceDetail.getDeviceId().split("@")[0];
            UserT userT = getUser(userName);
            Organization org = organizationRepository.findByOrganizationId(userT.getOrganizationId());

            // update pool device model if pool based control is enabled
            String deviceId = deviceDetail.getDeviceId();
            if (org.isPoolBasedControl() && poolDeviceDetail.get(org.getOrganizationName()).containsKey(deviceId)) {
                poolDeviceDetail.get(org.getOrganizationName()).get(deviceId).setCurrentState(deviceDetail.getDeviceState().getValue());
                poolDeviceDetail.get(org.getOrganizationName()).get(deviceId).setCurrentPower(deviceDetail.getConsumptionTs().getLatestPower());
                DeviceDataSuppl deviceDataSuppl = deviceDetailData.getDeviceDataSuppl();
                if (deviceDataSuppl != null) {
                    if (deviceDetail.getDeviceType() == DeviceType.Boiler) {
                        poolDeviceDetail.get(org.getOrganizationName()).get(deviceId).setCurrentTemperature(deviceDetailData.getDeviceDataSuppl().getBoilerTemperature());
                    } else {
                        poolDeviceDetail.get(org.getOrganizationName()).get(deviceId).setCurrentTemperature(deviceDetailData.getDeviceDataSuppl().getAmbientTemperature());
                    }
                }
            }

            return deviceDetail;
        } catch (Exception e) {
            LOGGER.error("Error in updateDeviceState. " + e.getLocalizedMessage());
            return null;
        }
    }


    @Override
    @Transactional
    public void resetDeviceLastCon(long deviceDetailID) {

        DeviceDetail deviceDetail = deviceDetailService.getDeviceByDetailId(deviceDetailID);
        deviceDetail.setNoOfUnsuccessfulCon(0);
        Date lastConnected = new Date();
        deviceDetail.setLastConnectedTime(lastConnected);


    }

    @Override
    public UserT getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserT getUserByTpLinkUserName(String tpLinkUserName) {
        return userRepository.findByTpLinkUserName(tpLinkUserName);
    }


    /**
     * remove device from the set of devices
     */
    @Override
    @Transactional
    public Boolean removeDeviceID(String user, String deviceId) {
        UserT userData = userRepository.findByUserName(user);

        if (userData == null) {
            return false;
        }
        /** get device with given Id*/
        DeviceDetail device = deviceDetailService.getDevice(deviceId);
        if (device != null) {
            userData.removeDevice(device);
            return true;
        }
        return false;
    }

    /**
     * remove devices from the set of devices
     */
    @Override
    @Transactional
    public Boolean removeDevices(UserT userData) {
        Set<DeviceDetail> devices = new HashSet<>(userData.getDeviceDetail());
        if (devices.size() > 0) {
            devices.forEach(userData::removeDevice);
            return true;
        }
        return false;
    }

    @Transactional
    public Boolean removeTpLinkDevices(UserT userData) {
        Set<DeviceDetail> devices = new HashSet<>(userData.getDeviceDetail());

        //TODO: Validate lambda method and replace
        //devices.stream().filter(device -> device.getPlugType()==PlugType.TPLink_HS110)
        //.forEach(device->userData.removeDevice(device));

        if (devices.size() > 0) {
            for (DeviceDetail device : devices) {
                if (device.getPlugType() == PlugType.TPLink_HS110) {
                    //deviceDetailService.deleteDevicesByDeviceId(device.getDeviceDetailId());
                    userData.removeDevice(device);
                }
            }
            return true;
        }
        return false;
    }

    /*Update user credential*/
    @Override
    @Transactional
    public String updateUserCredential(String userName, Map<String, Object> payload) {
        UserT userData = userRepository.findByUserName(userName);
        /**initiate class to store user tpLink connection credential*/
        DeviceParameters deviceParameters = new DeviceParameters();
        String message = "";
        int tpLinkCredentialUpdated = 0;

        if (userData == null) {
            return String.format("User: %s not found", userName);
        } else {
            message = String.format("Fields: (");
            if (payload.containsKey("tpLinkUserName")) {
                if (payload.get("tpLinkUserName") != null) {
                    userData.setTpLinkUserName(payload.get("tpLinkUserName").toString());
                    deviceParameters.setCloudUserName(payload.get("tpLinkUserName").toString());
                } else {
                    userData.setTpLinkUserName(null);
                    deviceParameters.setCloudUserName("");
                }
                message = message.concat(String.format("tpLinkUserName, "));

                //reset tplink APIkey
                tpLinkCredentialUpdated = 1;
            }
            if (payload.containsKey("tpLinkPassword")) {
                message = message.concat(String.format("tpLinkPassword, "));
                if (payload.get("tpLinkPassword") != null) {
                    userData.setTpLinkPassword(payload.get("tpLinkPassword").toString());
                    deviceParameters.setCloudPassword(payload.get("tpLinkPassword").toString());
                } else {
                    userData.setTpLinkPassword(null);
                    deviceParameters.setCloudPassword("");
                }

                //reset tplink APIkey
                tpLinkCredentialUpdated = 1;

            }
            if (tpLinkCredentialUpdated == 1) {
                userData.setAPIKey("");
                removeTpLinkDevices(userData);
            }

            if (payload.containsKey("password")) {
                message = message.concat(String.format("password, "));
                userData.setPassword(payload.get("password").toString());
            }

            if (payload.containsKey("email")) {
                if (payload.get("email") != null) {
                    message = message.concat(String.format("email, "));
                    userData.setEmail(payload.get("email").toString());
                } else {
                    userData.setEmail(null);
                }
            }

            if (payload.containsKey("organizationId")) {
                message = message.concat(String.format("organizationId, "));
                userData.setOrganization(Integer.parseInt(payload.get("organizationId").toString()));
            }

            if (payload.containsKey("address")) {
                Object address = payload.get("address");
                message = message.concat(String.format("address, "));

                if (userData.getUserAddress() == null) {
                    userData.setUserAddress(new UserAddress());
                }

                userData.getUserAddress().setAddress1(((UserAddress) address).getAddress1());
                userData.getUserAddress().setAddress2(((UserAddress) address).getAddress2());
                userData.getUserAddress().setCity(((UserAddress) address).getCity());
                userData.getUserAddress().setCountry(((UserAddress) address).getCountry());
                userData.getUserAddress().setState(((UserAddress) address).getState());
                userData.getUserAddress().setPostalcode(((UserAddress) address).getPostalcode());
                userData.getUserAddress().setPhone(((UserAddress) address).getPhone());
                userData.getUserAddress().setPhone2(((UserAddress) address).getPhone2());
                userData.getUserAddress().setLatitude(((UserAddress) address).getLatitude());
                userData.getUserAddress().setLongitude(((UserAddress) address).getLongitude());
            }

            if (payload.containsKey("enabled")) {
                userData.setEnabled(Boolean.parseBoolean(payload.get("enabled").toString()));
            }

            if (payload.containsKey("role")) {
                userData.setRole((UserRole) payload.get("role"));
            }

            SetupTpLinkDevicesEvent event =
                    new SetupTpLinkDevicesEvent(this, "Setup TpLink devices", userData, deviceParameters);
            applicationEventPublisher.publishEvent(event);
            return String.format("Successfully updated account details for user: %s", userName);
            //tpLinkDeviceService.setTpLinkDevices(userData, deviceParameters);
            //message = message.concat(String.format(") have been updated for user: %s", userName));
            //return message;
        }
    }

    /*Devicedetail update*/
    @Override
    @Transactional
    public String updateDeviceDetail(String deviceId, Map<String, Object> payload) {
        DeviceDetail deviceDetail = deviceDetailService.getDevice(deviceId);
        String message = "";
        if (deviceDetail == null) {
            return String.format("Device: %s not found", deviceId);
        } else {

            if (payload.containsKey("deviceType")) {

                if (!JsonUtil.isInEnum(payload.get("deviceType").toString(), DeviceType.class)) {
                    return String.format("Device: %s not found", payload.get("deviceType").toString());
                }

                /**if user has set previous device store it in log*/
                DeviceType newType = DeviceType.valueOf(payload.get("deviceType").toString());
                deviceDetail.setDeviceType(newType);
                if (deviceDetail.getDeviceType() != DeviceType.Unknown) {
                    deviceDetailService.logDeviceChange(deviceDetail);
                }
                message = message.concat("DeviceType, ");

                deviceDetail.setDefaultState(new DeviceDefaultState().getDeviceDefaultState(newType));
            }

            if (payload.containsKey("grouping")) {
                deviceDetail.getGroupingDetail()
                        .setLocationId(((GroupingDetail) payload.get("grouping")).getLocationId());
                message = message.concat("Grouping Details, ");
            }
            if (payload.containsKey("flexibilityDetail")) {
                message = message.concat(String.format(" Flexibility Details, "));
                DeviceFlexibilityDetail dd = (DeviceFlexibilityDetail) payload.get("flexibilityDetail");
                if (dd.getMaxInterruptionDelay() >= 1) {
                    deviceDetail.getDeviceFlexibilityDetail().setMaxInterruptionDelay(dd.getMaxInterruptionDelay());
                }
                if (dd.getMaxInterruptionLength() >= 1) {
                    deviceDetail.getDeviceFlexibilityDetail().setMaxInterruptionLength(dd.getMaxInterruptionLength());
                }
                if (dd.getMinInterruptionInterval() >= 1) {
                    deviceDetail.getDeviceFlexibilityDetail()
                            .setMinInterruptionInterval(dd.getMinInterruptionInterval());
                }
                if (dd.getNoOfInterruptionInADay() >= 1) {
                    deviceDetail.getDeviceFlexibilityDetail().setNoOfInterruptionInADay(dd.getNoOfInterruptionInADay());
                }

                if (dd.getDailyControlStart() >= 0 && dd.getDailyControlStart() <= 24) {
                    deviceDetail.getDeviceFlexibilityDetail().setDailyControlStart(dd.getDailyControlStart());
                }

                if (dd.getDailyControlEnd() >= 0 && dd.getDailyControlEnd() <= 24) {
                    deviceDetail.getDeviceFlexibilityDetail().setDailyControlEnd(dd.getDailyControlEnd());
                }

                if (dd.getLatestAcceptanceTime() >= 0) {
                    deviceDetail.getDeviceFlexibilityDetail().setLatestAcceptanceTime(dd.getLatestAcceptanceTime());
                }
            }

            if (payload.containsKey("deviceHierarchy")) {
                if (payload.get("deviceHierarchy") == null) {
                    deviceDetail.setDeviceHierarchy(null);
                } else {
                    long hierarchyId = ((DeviceHierarchy) payload.get("deviceHierarchy")).getHierarchyId();
                    DeviceHierarchy deviceHierarchy = deviceHierarchyRepository.findByHierarchyId(hierarchyId);
                    deviceDetail.setDeviceHierarchy(deviceHierarchy);
                }
                message = message.concat(String.format("Device Hierarchy, "));
            }

            if (payload.containsKey("flexible")) {
                boolean flexible = (boolean) payload.get("flexible");
                deviceDetail.setFlexible(flexible);
            }

            message = message.concat(String.format("has been updated for device: %s", deviceDetail.getDeviceId()));
            return message;
        }

    }


    /*remove user from the database*/
    @Override
    @Transactional
    public Boolean removeUser(String userName) {
        UserT userData = userRepository.findByUserName(userName);

        if (userData == null) {
            return false;
        } else {
            userRepository.deleteUserTByUserName(userName);
            return true;
        }

    }


    @Override
    @Transactional
    public Boolean disableUser(UserT userToDelete) {

        if (userToDelete == null) {
            return false;
        }
        userToDelete.setEnabled(false);
        userRepository.save(userToDelete);
        return true;
    }

    @Override
    public boolean updateProfilePhoto(UserT user, MultipartFile photo) {
        try {
            user.setPic(photo.getBytes());
            this.userRepository.save(user);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public void registerLoginTime(UserT user) {
        user.setLastLoginDate(new Date());
    }

    @Transactional
    public void updateDeviceList(List<DeviceDetail> devices) {
    }
}
