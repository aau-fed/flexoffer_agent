package org.goflex.wp2.foa.prediction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author muhaftab
 * created: 9/12/19
 */
@Repository
public interface OrganizationPredictionRepository extends JpaRepository<OrganizationPrediction, Long> {
    OrganizationPrediction findByOrAndOrganizationIdAndTimestamp(long organizationId, long timestamp);
    OrganizationPrediction findByOrAndOrganizationNameAndTimestamp(String organizationName, long timestamp);

    @Query("SELECT op from OrganizationPrediction op WHERE op.organizationId = :organizationId AND " +
            "op.timestamp >= :timestampFrom AND op.timestamp < :timestampTo")
    List<OrganizationPrediction> findByOrganizationIdAndInterval(long organizationId, long timestampFrom, long timestampTo);
}
