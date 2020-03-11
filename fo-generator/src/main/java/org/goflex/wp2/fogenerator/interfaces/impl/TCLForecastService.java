package org.goflex.wp2.fogenerator.interfaces.impl;

import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexOfferConstraint;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.fogenerator.interfaces.ForecastService;
import org.goflex.wp2.fogenerator.model.PredictedDemand;
import org.goflex.wp2.fogenerator.model.PredictionTs;
import org.goflex.wp2.fogenerator.services.DemandPredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class TCLForecastService implements ForecastService {

    private DemandPredictionService demandPredictionService;

    @Autowired
    public TCLForecastService(
            DemandPredictionService demandPredictionService) {
        this.demandPredictionService = demandPredictionService;
    }

    @Override
    public boolean hasForecastModel(DeviceDetail device) {
        //check weather a forecast model exists for the device or not
        return true;
    }

    @Override
    public boolean buildForecastModel(DeviceDetail device, String deviceType) {
        //build forecast model using historical data and device parameters and store it
        return true;
    }

    @Override
    public PredictionTs getPredictionForNextTs(DeviceDetail device, Date dt) {

        long timestamp = FlexOffer.toFlexOfferTime(dt) + 1;
        int foType;
        double lower = 0.0;
        double upper = this.demandPredictionService.getDeviceEnergyPredictionByTimestamp(device, timestamp);
        double foType1Prob = 0.75; // probability ofe generating foType 1


        // randomly choose between consumption and production FOs
        Random random = new Random();

        // if true then production FO
        //if (random.nextBoolean()) {
        if (random.nextDouble() < foType1Prob) {
            foType = 1;
            //lower = upper;
        } else {
            foType = 2;
        }

        int numSlices = 1;
        // numSlices+1 because TCL FO needs prediction at earliest start time which might be the second slice time
        List<PredictedDemand> predictedConsumption = new ArrayList<>(numSlices+1);
        for (int i=0; i < numSlices+1; i++) {
            predictedConsumption.add(new PredictedDemand(FlexOffer.toAbsoluteTime(timestamp+i),
                    new FlexOfferConstraint(lower, upper)));
        }

        return new PredictionTs(predictedConsumption, foType, numSlices);
    }
}
