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
 *  Last Modified 2/22/18 2:33 AM
 */

package org.goflex.wp2.core.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "foaKpis")
public class KpiData implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(KpiData.class);

    @XmlElement
    private List<Integer> activeDevices = new ArrayList<>();

    @XmlElement
    private List<Integer> activeProsumers = new ArrayList<>();

    @XmlElement
    private List<Double> flexRatio;

    @XmlElement
    private List<Integer> foCount;

    @XmlElement
    private List<Double> rewards;

    public KpiData(List<Integer> activeDevices, List<Integer> activeProsumers, List<Double> flexRatio,
                   List<Integer> foCount, List<Double> rewards) {

        this.activeDevices = activeDevices;
        this.activeProsumers = activeProsumers;
        this.flexRatio = flexRatio;
        this.foCount = foCount;
        this.rewards = rewards;
    }

    public List<Integer> getActiveDevices() {
        return activeDevices;
    }

    public void setActiveDevices(List<Integer> activeDevices) {
        this.activeDevices = activeDevices;
    }

    public List<Integer> getActiveProsumers() {
        return activeProsumers;
    }

    public void setActiveProsumers(List<Integer> activeProsumers) {
        this.activeProsumers = activeProsumers;
    }


    public List<Double> getFlexRatio() {
        return flexRatio;
    }

    public void setFlexRatio(List<Double> flexRatio) {
        this.flexRatio = flexRatio;
    }

    public List<Integer> getFoCount() {
        return foCount;
    }

    public void setFoCount(List<Integer> foCount) {
        this.foCount = foCount;
    }

    public List<Double> getRewards() {
        return rewards;
    }

    public void setRewards(List<Double> rewards) {
        this.rewards = rewards;
    }
}