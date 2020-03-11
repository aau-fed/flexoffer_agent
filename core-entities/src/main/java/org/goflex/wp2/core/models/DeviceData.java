package org.goflex.wp2.core.models;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(
        name = "device_data",
        indexes = {@Index(name = "dateindex", columnList = "date", unique = false)}
//        uniqueConstraints = {@UniqueConstraint(columnNames = {"voltage", "current", "power", "energy", "date", "timeseriesId"})}
)

public class DeviceData {

    private static final DecimalFormat df = new DecimalFormat("###.#");

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(name = "voltage")
    private double voltage;
    @Column(name = "current")
    private double current;
    @Column(name = "power")
    private double power;
    @Column(name = "energy")
    private double energy;
    @Column(name = "date")
    private Date date;

    //@ManyToOne(targetEntity = ConsumptionTsEntity.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @ManyToOne
    @JoinColumn(name = "timeseriesId")
    @JsonBackReference
    private ConsumptionTsEntity consumptionTsEntity;

    public DeviceData() {
    }

    public DeviceData(double voltage, double current, double power, double energy,
                      double temperature, Date date, ConsumptionTsEntity consumptionTsEntity) {
        this.voltage = Double.parseDouble(df.format(voltage));
        this.current = Double.parseDouble(df.format(current));
        this.power = Double.parseDouble(df.format(power));
        this.energy = Double.parseDouble(df.format(energy));
        this.date = date;
        this.consumptionTsEntity = consumptionTsEntity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = Double.parseDouble(df.format(voltage));
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = Double.parseDouble(df.format(current));
    }

    public double getPower() {
        return Double.parseDouble(df.format(power));
    }

    public void setPower(double power) {
        this.power = Double.parseDouble(df.format(power));
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = Double.parseDouble(df.format(energy));
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
        DeviceData that = (DeviceData) o;
        return id == that.id &&
                Double.compare(that.voltage, voltage) == 0 &&
                Double.compare(that.current, current) == 0 &&
                Double.compare(that.power, power) == 0 &&
                Double.compare(that.energy, energy) == 0 &&
                Objects.equals(date, that.date) &&
                Objects.equals(consumptionTsEntity, that.consumptionTsEntity);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, voltage, current, power, energy, date, consumptionTsEntity);
    }

    @Override
    public String toString() {
        return "DeviceData{" +
                "id=" + id +
                ", voltage=" + voltage +
                ", current=" + current +
                ", power=" + power +
                ", energy=" + energy +
                ", date=" + date +
                ", consumptionTsEntity=" + consumptionTsEntity +
                '}';
    }
}
