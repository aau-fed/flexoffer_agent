package org.goflex.wp2.foa.implementation;

import org.goflex.wp2.core.models.OrganizationalConsumption;
import org.goflex.wp2.core.repository.OrganizationalConsumptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author muhaftab
 * created: 3/29/19
 */
@Service
public class OrganizationalConsumptionService {

    @Autowired
    private OrganizationalConsumptionRepository organizationalConsumptionRepository;

    public void saveOrganizationalConsumption(OrganizationalConsumption organizationalConsumption) {
        this.organizationalConsumptionRepository.save(organizationalConsumption);
    }

    public List<OrganizationalConsumption> getCumulativeEnergyByOrganization() {
        return this.organizationalConsumptionRepository.getLatestCumulativeEnergy();
    }

    public boolean hasDuplicateRecord(long organizationId, Date timestamp) {
        return this.organizationalConsumptionRepository
                .findByOrganizationIdAndTimestamp(organizationId, timestamp).size() > 0;
    }
}
