package org.goflex.wp2.foa.prediction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author muhaftab
 * created: 9/12/19
 */
@Repository
public interface DevicePredictionRepository extends JpaRepository<DevicePrediction, Long> {
    DevicePrediction findByTimeSeriesIdAndTimestamp(long timeSeriesId, long timestamp);
}
