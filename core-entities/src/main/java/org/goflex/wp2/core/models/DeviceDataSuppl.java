package org.goflex.wp2.core.models;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(
        name = "device_data_suppl"
//        uniqueConstraints = {@UniqueConstraint(columnNames = {"ambient_temperature", "ambient_humidity", "boiler_temperature", "date", "timeseriesId"})}
)
public class DeviceDataSuppl {

    private static final DecimalFormat df = new DecimalFormat("###.#");

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name = "ambient_temperature")
    private double ambientTemperature;

    @Column(name = "ambient_humidity")
    private double ambientHumidity;

    @Column(name = "boiler_temperature")
    private double boilerTemperature;

    @Column(name = "date")
    private Date date;

    @ManyToOne
    @JoinColumn(name = "timeseriesId")
    @JsonBackReference
    private ConsumptionTsEntity consumptionTsEntity;

    public DeviceDataSuppl() {
    }

    public DeviceDataSuppl(double ambientTemperature, double ambientHumidity, double boilerTemperature, Date date, ConsumptionTsEntity consumptionTsEntity) {
        this.ambientTemperature = Double.parseDouble(df.format(ambientTemperature));
        this.ambientHumidity = Double.parseDouble(df.format(ambientHumidity));
        this.boilerTemperature = Double.parseDouble(df.format(boilerTemperature));
        this.date = date;
        this.consumptionTsEntity = consumptionTsEntity;
    }

    public double getAmbientTemperature() {
        return ambientTemperature;
    }

    public void setAmbientTemperature(double ambientTemperature) {
        this.ambientTemperature = ambientTemperature;
    }

    public double getAmbientHumidity() {
        return ambientHumidity;
    }

    public void setAmbientHumidity(double ambientHumidity) {
        this.ambientHumidity = ambientHumidity;
    }

    public double getBoilerTemperature() {
        return boilerTemperature;
    }

    public void setBoilerTemperature(double boilerTemperature) {
        this.boilerTemperature = boilerTemperature;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ConsumptionTsEntity getConsumptionTsEntity() {
        return consumptionTsEntity;
    }

    public void setConsumptionTsEntity(ConsumptionTsEntity consumptionTsEntity) {
        this.consumptionTsEntity = consumptionTsEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDataSuppl that = (DeviceDataSuppl) o;
        return id == that.id &&
                Double.compare(that.ambientTemperature, ambientTemperature) == 0 &&
                Double.compare(that.ambientHumidity, ambientHumidity) == 0 &&
                Double.compare(that.boilerTemperature, boilerTemperature) == 0 &&
                Objects.equals(date, that.date) &&
                Objects.equals(consumptionTsEntity, that.consumptionTsEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ambientTemperature, ambientHumidity, boilerTemperature, date, consumptionTsEntity);
    }

    @Override
    public String toString() {
        return "DeviceDataSuppl{" +
                "id=" + id +
                ", ambientTemperature=" + ambientTemperature +
                ", ambientHumidity=" + ambientHumidity +
                ", boilerTemperature=" + boilerTemperature +
                ", date=" + date +
                ", consumptionTsEntity=" + consumptionTsEntity +
                '}';
    }
}
