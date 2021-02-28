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
 *  Last Modified 2/22/18 12:41 AM
 */

package org.goflex.wp2.foa.datastructure;

import org.goflex.wp2.core.entities.OrganizationLoadControlState;
import org.goflex.wp2.core.entities.ScheduleDetails;
import org.goflex.wp2.core.models.DeviceFlexibilityDetail;
import org.goflex.wp2.core.models.FlexOfferT;
import org.goflex.wp2.core.models.OnOffSchedule;
import org.goflex.wp2.core.models.PoolDeviceModel;
import org.goflex.wp2.foa.implementation.TpLinkDeviceService;
import org.goflex.wp2.foa.wrapper.PoolSchedule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bijay on 7/26/17.
 */
@Configuration
public class FoaMemoryDOA {

    /*@Bean(name = "foHashMap")
    public ConcurrentHashMap<UUID, FlexOffer> foHashMap() {
        ConcurrentHashMap<UUID, FlexOffer> foHashMap = new ConcurrentHashMap<>();
        return foHashMap;
    }*/

    @Bean(name = "scheduleDetailTable")
    public ConcurrentHashMap<Date, ScheduleDetails> scheduleDetailTable() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "startGeneratingFo")
    public ConcurrentHashMap<String, Integer> startGeneratingFo() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "onOffScheduleTable")
    public ConcurrentHashMap<Date, List<OnOffSchedule>> onOffScheduleDetail() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "orgAccEnergyData")
    public LinkedHashMap<String, Map<Date, Double>> orgAccEnergyData() {
        return new LinkedHashMap<>();
    }


    @Bean(name = "deviceLatestAggData")
    public LinkedHashMap<String, Map<Date, Double>> deviceLatestAggData() {
        return new LinkedHashMap<>();
    }

    @Bean(name = "tpLinkScheduleIdTable")
    public ConcurrentHashMap<UUID, List<String>> tpLinkScheduleId() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "plugHashMap")
    public ConcurrentHashMap<String, TpLinkDeviceService> plugHashMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "deviceGeneratedFOCount")
    public ConcurrentHashMap<String, Map<String, List<Double>>> deviceGeneratedFOCount() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "flexibilityRatio")
    public ConcurrentHashMap<Date, Map<String, Double>> flexibilityRatio() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "directControlMode")
    public ConcurrentHashMap<Long, OrganizationLoadControlState> directControlMode() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "defaultFlexibilitySettings")
    public Map<Long, DeviceFlexibilityDetail> defaultFlexibilitySettings() {
        return new HashMap<>();
    }

    @Bean(name = "deviceLatestFO")
    public ConcurrentHashMap<String, FlexOfferT> deviceLatestFO() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "mqttDataMap")
    public ConcurrentHashMap<String, Map<String, Map<String, Object>>> mqttDataMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "tclDeviceFutureFOs")
    public ConcurrentHashMap<String, Date> tclDeviceFutureFos() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "deviceActiveSchedule")
    public ConcurrentHashMap<String, Map<Date, Integer>> deviceActiveSchedule() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "devicePendingControlSignals")
    public ConcurrentHashMap<String, Integer> devicePendingControlSignals() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "poolDeviceDetail")
    public ConcurrentHashMap<String, Map<String, PoolDeviceModel>> poolDeviceDetail() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "poolScheduleMap")
    public ConcurrentHashMap<String, Map<Long, PoolSchedule>> poolScheduleMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean(name = "poolTurnedOffDevices")
    public ConcurrentHashMap<String, Map<String, Date>> poolTurnedOffDevices() {
        return new ConcurrentHashMap<>();
    }
}
