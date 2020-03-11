package org.goflex.wp2.foa.devicestate;

import javax.persistence.*;
import java.util.Date;

/**
 * @author muhaftab
 * created: 4/24/19
 */
@Entity
public class DeviceCurrentState {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(unique = true, nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private boolean state;

    @Column(nullable = false)
    private Date timestamp;

    public DeviceCurrentState() {}

    public DeviceCurrentState(String deviceId, boolean state, Date timestamp) {
        this.deviceId = deviceId;
        this.state = state;
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
