package org.goflex.wp2.core.models;

import org.goflex.wp2.core.entities.ScheduleValidationStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @author muhaftab
 * created: 2/12/19
 */

@Entity
@Table(name = "schedule_event_log")
public class ScheduleEventLog implements Serializable {
    public static final long serialVersionUUID =  6480948072290455958L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private long receivedScheduleId;

    @Column
    private String flexOfferId;

    @Column
    private ScheduleValidationStatus status;

    @Column
    private String schedule;

    @Column
    private String remarks;

    @Column
    private Date timestamp;

    public ScheduleEventLog() {}

    public ScheduleEventLog(long receivedScheduleId, String flexOfferId, ScheduleValidationStatus status, String remarks, Date timestamp) {
        this.receivedScheduleId = receivedScheduleId;
        this.flexOfferId = flexOfferId;
        this.status = status;
        this.remarks = remarks;
        this.timestamp = timestamp;
    }


    public long getReceivedScheduleId() {
        return receivedScheduleId;
    }

    public void setReceivedScheduleId(long receivedScheduleId) {
        this.receivedScheduleId = receivedScheduleId;
    }

    public String getFlexOfferId() {
        return flexOfferId;
    }

    public void setFlexOfferId(String flexOfferId) {
        this.flexOfferId = flexOfferId;
    }

    public ScheduleValidationStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleValidationStatus status) {
        this.status = status;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleEventLog that = (ScheduleEventLog) o;
        return receivedScheduleId == that.receivedScheduleId &&
                Objects.equals(id, that.id) &&
                Objects.equals(flexOfferId, that.flexOfferId) &&
                status == that.status &&
                Objects.equals(remarks, that.remarks) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, receivedScheduleId, flexOfferId, status, remarks, timestamp);
    }

    @Override
    public String toString() {
        return "ScheduleEventLog{" +
                "id=" + id +
                ", receivedScheduleId=" + receivedScheduleId +
                ", flexOfferId='" + flexOfferId + '\'' +
                ", status=" + status +
                ", remarks='" + remarks + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
