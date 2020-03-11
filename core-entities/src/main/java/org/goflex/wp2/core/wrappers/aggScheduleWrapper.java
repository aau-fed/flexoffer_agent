package org.goflex.wp2.core.wrappers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by bijay on 8/28/17.
 */

public class aggScheduleWrapper implements Serializable {
    public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private Date startTime;
    private double[] energyAmounts;

    @JsonCreator
    public aggScheduleWrapper(@JsonProperty(value = "startTime") Date startTime, @JsonProperty(value = "energyAmounts") double[] energyAmounts) {
        this.startTime = startTime;
        this.energyAmounts = energyAmounts;
    }

    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public double[] getEnergyAmounts() {
        return energyAmounts;
    }

    public void setEnergyAmounts(double[] energyAmounts) {
        this.energyAmounts = energyAmounts;
    }
}
