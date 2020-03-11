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
 *  Last Modified 2/22/18 9:35 PM
 */

package org.goflex.wp2.fogenerator.services;

import org.goflex.wp2.core.entities.DeviceFlexOfferGroup;
import org.goflex.wp2.core.entities.DeviceType;
import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexibilityGroupType;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.DevicePrognosis;
import org.goflex.wp2.core.models.Organization;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.core.repository.DevicePrognosisRepository;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Created by muhaftab on 31/3/19.
 */
@Component
public class DemandPredictionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandPredictionService.class);

    private OrganizationRepository organizationRepository;
    private UserService userService;
    private DeviceDetailService deviceDetailService;
    private DevicePrognosisRepository devicePrognosisRepository;

    public DemandPredictionService() {
    }

    @Autowired
    public DemandPredictionService(UserService userService, DeviceDetailService deviceDetailService,
                                   OrganizationRepository organizationRepository,
                                   DevicePrognosisRepository devicePrognosisRepository) {
        this.userService = userService;
        this.deviceDetailService = deviceDetailService;
        this.organizationRepository = organizationRepository;
        this.devicePrognosisRepository = devicePrognosisRepository;
    }

    private void processForOrganization(Organization organization) {
        LOGGER.debug("Updating devices for organization: " + organization.getOrganizationName());
        List<UserT> users = userService.getActiveUsersForOrganization(organization.getOrganizationId());
        Hibernate.initialize(users);
        users.forEach(this::processForUser);
    }

    private void processForUser(UserT user) {
        deviceDetailService.getActiveDeviceByUser(user.getId()).stream()
                .filter(deviceDetail -> deviceDetail.getDeviceType() != null)
                .forEach(this::updateDevicePrediction);
    }

    private void updateDevicePrediction(DeviceDetail device) {
        LOGGER.debug("Updating prediction for device: {}", device.getDeviceId());
        this.saveSevenDayAvgAsDevicePrediction(device);
    }

    private void saveSevenDayAvgAsDevicePrediction(DeviceDetail device) {

        try {
            long timestamp = FlexOffer.toFlexOfferTime(new Date()) + 1;
            DevicePrognosis prognosis =
                    this.devicePrognosisRepository.findByDeviceIdAndTimestamp(device.getDeviceId(), timestamp);
            if (prognosis == null) {
                prognosis = new DevicePrognosis();
                prognosis.setDeviceId(device.getDeviceId());
                prognosis.setTimeSeriesId(device.getConsumptionTs().getId());
            }
            //Double sevenDayAvg = device.getConsumptionTs().getAveragePowerForPrediction();
            double sevenDayAvg = this.deviceDetailService.getAvgPowerConsumptionForLastSevenDays(device.getDeviceId());
            prognosis.setEnergy(sevenDayAvg / (1000D * 4));
            prognosis.setTimestamp(timestamp);

            LOGGER.debug("deviceId: {}, sevenDayAvg: {}", device.getDeviceId(), sevenDayAvg);
            this.devicePrognosisRepository.save(prognosis);
        } catch (Exception ex) {
            LOGGER.error("{}", ex.getLocalizedMessage());
        }
    }

    public double getDeviceEnergyPredictionByTimestamp(DeviceDetail device, long timestamp) {
        FlexibilityGroupType flexGroup =   new DeviceFlexOfferGroup().getDeviceFOGroupType(device.getDeviceType());
        double avgPower;
        if (flexGroup == FlexibilityGroupType.ThermostaticControlLoad) {
            avgPower = this.deviceDetailService.getAvgPowerConsumptionForLastSevenDays(device.getDeviceId());
        } else {
            avgPower = device.getConsumptionTs().getDefaultValue();
        }
        return avgPower / (1000D * 4); // 1000 to convert from W to kWh and 4 because it's over 15 min slice
        //return avgPower * (1/1000D) * (15/60D); // assuming constant power consumption over the 15 min slice
    }

    // second, minute, hour, day of month, month, day of week.
    //e.g. "0 * * * * MON-FRI" means once per minute on weekdays
    @Scheduled(cron = "0 0/5 * * * *")
    public void runDevicePredictionRoutine() {
        LOGGER.info("Updating devices prediction for all users");
        organizationRepository.findAll().forEach(this::processForOrganization);
        LOGGER.info("Updated devices prediction for all users");
    }

}
