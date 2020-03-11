
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
 *  Last Modified 2/22/18 2:28 AM
 */

package org.goflex.wp2.core.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

/**
 * Entity to store device consumption data
 * Created by bijay on 1/6/18.
 */
@Entity
@Table(name = "consumption_ts")
public class ConsumptionTsEntity implements Serializable {

    private static final long serialVersionUID = -8396825526815515824L;
    public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final DecimalFormat df = new DecimalFormat("###.#");

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long timeseriesId;

    // holds overall device power avg (updated by mysql event named average_power_overall)
    private double defaultValue = 0;

    // TODO: do we need to add 'isDeviceActive' or 'isDeviceDeleted' column?


//    /** data is stored as date and consumption as double */
//    @ElementCollection
//    @JsonIgnore
//    @OrderBy("data_key ASC")
//    private Map<Date, Double> data = new LinkedHashMap<>();
//
//
//    /** data stored as  flex-offer duration Ts */
//    @ElementCollection
//    @JsonIgnore
//    private Map<Long, Double> dataFoDs = new LinkedHashMap<>();

    private double latestVoltage = -1.0;
    private double latestPower = -1.0;


    @OneToMany(targetEntity = DeviceData.class, cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, mappedBy = "consumptionTsEntity", orphanRemoval = true)
    @JsonManagedReference
    @JsonIgnore
    private List<DeviceData> deviceData = new ArrayList<>();


    // With multiple one-to-many relationships, only one can be EAGER
    @OneToMany(targetEntity = DeviceDataSuppl.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY,
            mappedBy = "consumptionTsEntity")
    @JsonIgnore
    private List<DeviceDataSuppl> deviceDataSuppls = new ArrayList<>();


    @OneToOne(mappedBy = "consumptionTs")
    @JsonBackReference
    private DeviceDetail deviceDetail;

    // holds last seven days device power avg (updated by mysql event named average_seven_days)
    private double averagePowerForPrediction = 0;

    // holds the average time that a device takes to complete its operation
    // only used for wet devices
    private int averageOnDuration = 15; // min one slice

    public ConsumptionTsEntity(double defaultValue) {
        this.defaultValue = Double.parseDouble(df.format(defaultValue));
    }

    public ConsumptionTsEntity(List<DeviceData> deviceData) {
        this.deviceData = deviceData;
    }

    public ConsumptionTsEntity() {

    }

    @JsonIgnore
    public List<DeviceData> getDeviceData() {
        return deviceData;
    }

    public void setDeviceData(List<DeviceData> deviceData) {
        this.deviceData = deviceData;
    }

    @JsonIgnore
    public List<DeviceData> getDeviceEnergyData() {
        return deviceData;
    }

    @JsonIgnore
    public List<DeviceDataSuppl> getDeviceDataSuppls() {
        return deviceDataSuppls;
    }

    public void setDeviceDataSuppls(List<DeviceDataSuppl> deviceDataSuppls) {
        this.deviceDataSuppls = deviceDataSuppls;
    }

    public double getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(double defaultValue) {
        this.defaultValue = Double.parseDouble(df.format(defaultValue));
    }

    public void addData(DeviceData deviceData) {
        deviceData.setConsumptionTsEntity(this);
        this.deviceData.add(deviceData);
    }

    public void addSupplData(DeviceDataSuppl deviceDataSuppl) {
        deviceDataSuppl.setConsumptionTsEntity(this);
        this.deviceDataSuppls.add(deviceDataSuppl);
    }

    public DeviceDetail getDeviceDetail() {
        return deviceDetail;
    }

    public void setDeviceDetail(DeviceDetail deviceDetail) {
        this.deviceDetail = deviceDetail;
    }

    public Long getId() {
        return timeseriesId;
    }

    public void setId(Long Id) {
        this.timeseriesId = Id;
    }


    public List<DeviceData> getLatestConsumptionTs(Date lastDate) {
        return this.getDeviceData();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsumptionTsEntity that = (ConsumptionTsEntity) o;
        return Double.compare(that.defaultValue, defaultValue) == 0 &&
                Objects.equals(timeseriesId, that.timeseriesId) &&
                Objects.equals(deviceData, that.deviceData) &&
                Objects.equals(deviceDetail, that.deviceDetail);
    }

    public double getLatestVoltage() {
        return latestVoltage;
    }

    public void setLatestVoltage(double latestVoltage) {
        this.latestVoltage = Double.parseDouble(df.format(latestVoltage));
    }

    public double getLatestPower() {
        return latestPower;
    }

    public void setLatestPower(double latestPower) {
        this.latestPower = Double.parseDouble(df.format(latestPower));
    }

    public double getAveragePowerForPrediction() {
        return averagePowerForPrediction;
    }

    public void setAveragePowerForPrediction(double averagePowerForPrediction) {
        this.averagePowerForPrediction = averagePowerForPrediction;
    }

    public int getAverageOnDuration() {
        return averageOnDuration;
    }

    public void setAverageOnDuration(int averageOnDuration) {
        this.averageOnDuration = averageOnDuration;
    }

    /*
    @Override
    public int hashCode() {

        return Objects.hash(timeseriesId, defaultValue, deviceData, deviceDetail);
    }
*/
}
