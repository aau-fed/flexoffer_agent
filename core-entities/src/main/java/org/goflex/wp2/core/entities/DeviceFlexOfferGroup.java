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
 *  Last Modified 1/18/18 9:06 AM
 */

package org.goflex.wp2.core.entities;


import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bijay on 12/11/17.
 */

@Component
public class DeviceFlexOfferGroup {

    Map<DeviceType, FlexibilityGroupType> deviceFOGroup = new HashMap<>();


    public DeviceFlexOfferGroup() {
        this.deviceFOGroup.put(DeviceType.HeatPump, FlexibilityGroupType.ThermostaticControlLoad);
        this.deviceFOGroup.put(DeviceType.Boiler, FlexibilityGroupType.ThermostaticControlLoad);
        this.deviceFOGroup.put(DeviceType.DishWasher, FlexibilityGroupType.WetLoad);
        this.deviceFOGroup.put(DeviceType.WasherDryer, FlexibilityGroupType.WetLoad);
        this.deviceFOGroup.put(DeviceType.AirConditioner, FlexibilityGroupType.ThermostaticControlLoad);
        this.deviceFOGroup.put(DeviceType.ElectricBike, FlexibilityGroupType.BatterySystem);
        this.deviceFOGroup.put(DeviceType.RoomHeater, FlexibilityGroupType.ThermostaticControlLoad);
        this.deviceFOGroup.put(DeviceType.ElectricLawnMower, FlexibilityGroupType.BatterySystem);
        this.deviceFOGroup.put(DeviceType.ElectricCar, FlexibilityGroupType.BatterySystem);
        this.deviceFOGroup.put(DeviceType.Refrigerator, FlexibilityGroupType.ThermostaticControlLoad);
        this.deviceFOGroup.put(DeviceType.Freezer, FlexibilityGroupType.ThermostaticControlLoad);
        this.deviceFOGroup.put(DeviceType.Lamp, FlexibilityGroupType.OthersNonFlexible);
        this.deviceFOGroup.put(DeviceType.Unknown, FlexibilityGroupType.Unknown);
        this.deviceFOGroup.put(DeviceType.PV, FlexibilityGroupType.Production);
        this.deviceFOGroup.put(DeviceType.Wind, FlexibilityGroupType.Production);
    }

    public Map<DeviceType, FlexibilityGroupType> getDeviceFOGroup() {
        return deviceFOGroup;
    }

    public void setDeviceFOGroup(Map<DeviceType, FlexibilityGroupType> deviceFOGroup) {
        this.deviceFOGroup = deviceFOGroup;
    }

    public FlexibilityGroupType getDeviceFOGroupType(DeviceType deviceType) {
        if (this.deviceFOGroup.containsKey(deviceType)) {
            return this.deviceFOGroup.get(deviceType);
        } else {
            return FlexibilityGroupType.Unknown;
        }
    }
}
