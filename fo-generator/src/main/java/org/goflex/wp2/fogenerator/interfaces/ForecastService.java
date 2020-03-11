package org.goflex.wp2.fogenerator.interfaces;


import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.fogenerator.model.PredictionTs;

import java.util.Date;

public interface ForecastService {
    boolean hasForecastModel(DeviceDetail device);

    boolean buildForecastModel(DeviceDetail device, String deviceType);

    PredictionTs getPredictionForNextTs(DeviceDetail device, Date dt);
}
