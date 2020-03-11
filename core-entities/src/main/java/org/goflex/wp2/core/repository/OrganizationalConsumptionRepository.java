package org.goflex.wp2.core.repository;

import org.goflex.wp2.core.models.OrganizationalConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * @author muhaftab
 * created: 3/29/19
 */
public interface OrganizationalConsumptionRepository extends JpaRepository<OrganizationalConsumption, Long> {

//    @Query("select max(c.organizationId), max(c.organizationName), " +
//            "max(c.cumulativeEnergy), max(c.timestamp) from OrganizationalConsumption c group by c.organizationId")
    @Query("select new org.goflex.wp2.core.models.OrganizationalConsumption(max(c.organizationId), " +
            "max(c.organizationName), max(c.cumulativeEnergy), max(c.timestamp)) " +
            "from OrganizationalConsumption c group by c.organizationId")
    List<OrganizationalConsumption> getLatestCumulativeEnergy();

    List<OrganizationalConsumption> findByOrganizationIdAndTimestamp(long organizationId, Date timestamp);
}
