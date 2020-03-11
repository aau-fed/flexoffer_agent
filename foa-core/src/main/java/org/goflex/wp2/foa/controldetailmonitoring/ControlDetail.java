package org.goflex.wp2.foa.controldetailmonitoring;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Table(name = "controldetail")
public class ControlDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long userId;

    private long organizationId;
    private String prosumerName;
    private String deviceId;
    private Date controlDatetime;
    private int controlType;
    private String flexofferId;
    private long ScheduleId;
    private boolean triggeredToDevice;

    public ControlDetail() {

    }

    public ControlDetail(long organizationId, String prosumerName, String deviceId, Date controlDatetime,
                         int controlType, String flexofferId, long scheduleId, boolean triggeredToDevice) {
        this.organizationId = organizationId;
        this.prosumerName = prosumerName;
        this.deviceId = deviceId;
        this.controlDatetime = controlDatetime;
        this.controlType = controlType;
        this.flexofferId = flexofferId;
        this.triggeredToDevice = triggeredToDevice;
        ScheduleId = scheduleId;
    }

    public long getOrganizationName() {
        return organizationId;
    }

    public void setOrganizationName(long organizationName) {
        this.organizationId = organizationName;
    }

    public String getProsumerName() {
        return prosumerName;
    }

    public void setProsumerName(String prosumerName) {
        this.prosumerName = prosumerName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Date getControlDatetime() {
        return controlDatetime;
    }

    public void setControlDatetime(Date controlDatetime) {
        this.controlDatetime = controlDatetime;
    }

    public int getControlType() {
        return controlType;
    }

    public void setControlType(int controlType) {
        this.controlType = controlType;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getFlexofferId() {
        return flexofferId;
    }

    public void setFlexofferId(String flexofferId) {
        this.flexofferId = flexofferId;
    }

    public long getScheduleId() {
        return ScheduleId;
    }

    public void setScheduleId(long scheduleId) {
        ScheduleId = scheduleId;
    }

    public boolean isTriggeredToDevice() {
        return triggeredToDevice;
    }

    public void setTriggeredToDevice(boolean triggeredToDevice) {
        this.triggeredToDevice = triggeredToDevice;
    }
}
