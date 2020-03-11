package org.goflex.wp2.foa.devicestate;

import org.goflex.wp2.core.entities.DeviceState;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * @author muhaftab
 * created: 8/5/19
 */
@Entity
public class DeviceStateHistory {
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long id;

   @Column(nullable = false)
   private String deviceId;

   @Column(nullable = false)
   private DeviceState deviceState;

   @Column(nullable = false)
   private Date timestamp;

   public DeviceStateHistory() {}
   public DeviceStateHistory(String deviceId, DeviceState deviceState, Date timestamp) {
      this.deviceId = deviceId;
      this.deviceState = deviceState;
      this.timestamp = timestamp;
   }

   public DeviceState getDeviceState() {
      return deviceState;
   }

   public void setDeviceState(DeviceState deviceState) {
      this.deviceState = deviceState;
   }

   public Date getTimestamp() {
      return timestamp;
   }

   public void setTimestamp(Date timestamp) {
      this.timestamp = timestamp;
   }

   public String getDeviceId() {
      return deviceId;
   }

   public void setDeviceId(String deviceId) {
      this.deviceId = deviceId;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DeviceStateHistory that = (DeviceStateHistory) o;
      return Objects.equals(deviceId, that.deviceId) &&
              deviceState == that.deviceState &&
              Objects.equals(timestamp, that.timestamp);
   }

   @Override
   public int hashCode() {
      return Objects.hash(deviceId, deviceState, timestamp);
   }

   @Override
   public String toString() {
      return "DeviceStateHistory{" +
              "deviceId='" + deviceId + '\'' +
              ", deviceState=" + deviceState +
              ", timestamp=" + timestamp +
              '}';
   }
}
