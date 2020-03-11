package org.goflex.wp2.core.entities;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.util.Date;


public class ConsumptionTuple implements Serializable {

    public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final long serialVersionUID = -2981830643149343763L;
    private Date timestamp;

    private Double value;

    public ConsumptionTuple() {

    }

    public ConsumptionTuple(Date timestamp, Double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ConsumptionTuple{" +
                "timestamp=" + timestamp +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConsumptionTuple)) return false;

        ConsumptionTuple that = (ConsumptionTuple) o;

        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = timestamp != null ? timestamp.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }


}
