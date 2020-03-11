
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
 *  Last Modified 1/13/18 11:15 AM
 */

package org.goflex.wp2.core.entities;


import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bijay on 12/11/17.
 */

@Component
public class DeviceDefaultState {
    private Map<DeviceType, Integer> defultState = new HashMap<>();

    public DeviceDefaultState() {
        defultState.put(DeviceType.DishWasher, 1);
        defultState.put(DeviceType.HeatPump, 1);
        defultState.put(DeviceType.WasherDryer, 1);
        defultState.put(DeviceType.Boiler, 1);
        defultState.put(DeviceType.ElectricCar, 1);
        defultState.put(DeviceType.Freezer, 1);
        defultState.put(DeviceType.AirConditioner, 1);
        defultState.put(DeviceType.ElectricBike, 1);
        defultState.put(DeviceType.Lamp, 1);
        defultState.put(DeviceType.ElectricLawnMower, 1);
        defultState.put(DeviceType.Refrigerator, 1);
        defultState.put(DeviceType.RoomHeater, 1);
        defultState.put(DeviceType.PV, 1);
        defultState.put(DeviceType.Wind, 1);
        defultState.put(DeviceType.Unknown, 1);
    }

    public int getDeviceDefaultState(DeviceType deviceType) {
        if (defultState.containsKey(deviceType)) {
            return defultState.get(deviceType);
        } else {
            return -1;
        }
    }

    public Map<DeviceType, Integer> getDefultState() {
        return defultState;
    }

    public void setDefultState(Map<DeviceType, Integer> defultState) {
        this.defultState = defultState;
    }
}


