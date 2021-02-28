/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
 * Copyright (c) 2018 The GoFlex Consortium
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
 *  Last Modified 4/10/18 6:18 PM
 */

package org.goflex.wp2.foa.implementation;

import org.goflex.wp2.core.entities.PlugType;
import org.goflex.wp2.foa.interfaces.xEmsServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by bijay on 4/10/18.
 */
@Component
public class ImplementationsHandler {

    @Autowired
    private Map<String, xEmsServices> xEmsServiceImplementations;

    public xEmsServices get(String impl) {
        return this.xEmsServiceImplementations.get(impl);
    }

    public xEmsServices get(PlugType plugType) {
        String xemsImplementaionType = "tpLinkDeviceService";
        if (plugType == PlugType.TPLink_HS110) {
            xemsImplementaionType = "tpLinkDeviceService";
        } else if (plugType == PlugType.Simulated) {
            xemsImplementaionType = "simulatedDeviceService";
        } else if (plugType == PlugType.MQTT) {
            xemsImplementaionType = "swissDeviceService";
        }
        return this.xEmsServiceImplementations.get(xemsImplementaionType);
    }
}
