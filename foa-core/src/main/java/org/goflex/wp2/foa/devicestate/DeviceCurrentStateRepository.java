package org.goflex.wp2.foa.devicestate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author muhaftab
 * created: 4/24/19
 */
@Repository
public interface DeviceCurrentStateRepository extends JpaRepository<DeviceCurrentState, Long> {
    DeviceCurrentState findByDeviceId(String deviceId);
}
