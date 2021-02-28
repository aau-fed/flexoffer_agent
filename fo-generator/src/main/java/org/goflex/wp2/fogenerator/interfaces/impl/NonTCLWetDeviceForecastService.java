package org.goflex.wp2.fogenerator.interfaces.impl;

import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexOfferConstraint;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.repository.DevicePrognosisRepository;
import org.goflex.wp2.fogenerator.interfaces.ForecastService;
import org.goflex.wp2.fogenerator.model.PredictedDemand;
import org.goflex.wp2.fogenerator.model.PredictionTs;
import org.goflex.wp2.fogenerator.services.DemandPredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class NonTCLWetDeviceForecastService implements ForecastService {

    private final DemandPredictionService demandPredictionService;

    @Autowired
    public NonTCLWetDeviceForecastService(
            DevicePrognosisRepository devicePredictionRepo,
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
        int foType = 1; // foType is always 1 (i.e. consumption increase) for Wet and EV devices
        //double lower = 0.0;
        double upper = this.demandPredictionService.getDeviceEnergyPredictionByTimestamp(device, timestamp);
        double lower = upper;

        //int numSlices = 1; // number of slices
        int numSlices = device.getConsumptionTs().getAverageOnDuration()/15;
        List<PredictedDemand> predictedConsumption = new ArrayList<>(numSlices);
        for (int i=0; i < numSlices; i++) {
            predictedConsumption.add(new PredictedDemand(FlexOffer.toAbsoluteTime(timestamp+i),
                    new FlexOfferConstraint(lower, upper)));
        }

        return new PredictionTs(predictedConsumption, foType, numSlices);
    }
}
