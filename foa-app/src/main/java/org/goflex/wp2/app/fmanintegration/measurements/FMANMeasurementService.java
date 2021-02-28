package org.goflex.wp2.app.fmanintegration.measurements;

import org.goflex.wp2.core.entities.ConsumptionTuple;
import org.goflex.wp2.core.models.OrganizationalConsumption;
import org.goflex.wp2.core.models.ScheduleT;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.core.wrappers.OperationInformation;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.implementation.OrganizationalConsumptionService;
import org.goflex.wp2.foa.interfaces.ScheduleService;
import org.goflex.wp2.foa.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FMANMeasurementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FMANMeasurementService.class);

    @Resource(name = "deviceLatestAggData")
    LinkedHashMap<String, Map<Date, Double>> deviceLatestAggData;

    @Resource(name = "orgAccEnergyData")
    LinkedHashMap<String, Map<Date, Double>> orgAccEnergyData;

    @Resource(name = "startGeneratingFo")
    private ConcurrentHashMap<String, Integer> startGeneratingFo;

    private final ScheduleService scheduleService;
    private final FOAProperties foaProperties;
    private final OrganizationRepository organizationRepository;
    private final OrganizationalConsumptionService organizationalConsumptionService;

    public FMANMeasurementService(ScheduleService scheduleService,
                                  FOAProperties foaProperties,
                                  OrganizationRepository organizationRepository,
                                  OrganizationalConsumptionService organizationalConsumptionService) {
        this.scheduleService = scheduleService;
        this.foaProperties = foaProperties;
        this.organizationRepository = organizationRepository;
        this.organizationalConsumptionService = organizationalConsumptionService;
    }


    String getFMANMeasurementURL() {
        return foaProperties.getFmanConnectionConfig().getMeasurementURI();
    }


    void _clearAggData() {
        this.deviceLatestAggData.clear();
    }

    public Map<String, OperationInformation> getOperationInfo() {

        // create operational info
        Map<String, OperationInformation> optInfos = new HashMap<>();
        for (Map.Entry<String, Map<Date, Double>> tuple : deviceLatestAggData.entrySet()) {

            // get cumulative
            String org = tuple.getKey();
            Double cumulativeEnergy = 0.0;
            Date ts = new Date();

            // there is only one date key. We are looping because it's easier than searching for a specific key
            if (orgAccEnergyData.containsKey(org)) {
                for (Date key : orgAccEnergyData.get(org).keySet()) {
                    cumulativeEnergy = orgAccEnergyData.get(org).get(key) + cumulativeEnergy;
                    ts = key;
                }
            }


            // create operational info*/
            OperationInformation operationInfo = new OperationInformation();
            ConsumptionTuple[] csTupleProg = new ConsumptionTuple[tuple.getValue().size()];

            // Implement logic for  operation State*/
            operationInfo.setOperationState(this.getOperationState(tuple.getKey()));
            int sz = tuple.getValue().size();
            ConsumptionTuple[] csTuplePower = new ConsumptionTuple[tuple.getValue().size()];
            int i = 0;
            // operationPower
            csTuplePower[i] = new ConsumptionTuple(ts, cumulativeEnergy / 60000D);
            for (Map.Entry<Date, Double> innerTuple : tuple.getValue().entrySet()) {
                // operationPrognosis (just send live aggregate power consumption for org)
                csTupleProg[i] = new ConsumptionTuple(innerTuple.getKey(), innerTuple.getValue() / 1000D);
                i++;
            }

            operationInfo.setOperationPrognosis(csTupleProg);
            operationInfo.setOperationPower(csTuplePower);
            optInfos.put(tuple.getKey(), operationInfo);
            LOGGER.debug("orgAccEnergyData " + orgAccEnergyData.toString());
            LOGGER.debug("deviceLatestAggData" + deviceLatestAggData.toString());

        }

        return optInfos;
    }

    void saveOrganizationalConsumption() {
        try {
            organizationRepository.findAll().forEach(org -> {
                if (orgAccEnergyData.containsKey(org.getOrganizationName())) {

                    Double cumulativeEnergy = 0.0;
                    Double latestAggPower = 0.0;
                    Date ts = new Date();

                    // even though there is only one key-value pair for each organization, we are running a loop to
                    // automatically get date key
                    for (Date key : orgAccEnergyData.get(org.getOrganizationName()).keySet()) {
                        cumulativeEnergy += orgAccEnergyData.get(org.getOrganizationName()).get(key);
                        if (deviceLatestAggData.containsKey(org.getOrganizationName())) {
                            latestAggPower += deviceLatestAggData.get(org.getOrganizationName()).get(key);
                        }
                        ts = key;
                    }

                    if (!this.organizationalConsumptionService.hasDuplicateRecord(org.getOrganizationId(), ts)) {
                        this.organizationalConsumptionService.saveOrganizationalConsumption(
                                new OrganizationalConsumption(org.getOrganizationId(), org.getOrganizationName(),
                                        cumulativeEnergy, latestAggPower, ts)
                        );
                    }
                }
            });
        } catch (Exception ex) {
            LOGGER.error("Error in saveOrganizationalConsumption(): {}", ex.getLocalizedMessage());
        }

    }


    private int getOperationState(String organization) {
        int state = 1;
        Date currentTime = new Date();
        long foTimeStamp = DateUtil.toFlexOfferTime(currentTime);

        Date startTime = DateUtil.toAbsoluteTime(foTimeStamp);
        Date endTime = DateUtil.toAbsoluteTime(foTimeStamp + 1);

        List<ScheduleT> currentSchedules = scheduleService.getByFODate(startTime, endTime);
        List<ScheduleT> futureSchedules = scheduleService.getFutureSchedule(endTime);

        if (startGeneratingFo.containsKey("start")) {
            state = 2;
        }
        if (futureSchedules.size() > 0) {
            state = 3;
        }
        if (currentSchedules.size() > 0) {
            state = 4;
        }

        return state;
    }

}
