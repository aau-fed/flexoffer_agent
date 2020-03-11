package org.goflex.wp2.app.fmanintegration.measurements;

import org.goflex.wp2.core.entities.TimeSeries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MeasurementService {
    @Autowired
    MeasurementRepository repo;

    @Autowired
    ApplicationEventPublisher publisher;

    public Page<MeasurementT> findAll(Specification<MeasurementT> spec, Pageable pageable) {
        return repo.findAll(spec, pageable);
    }


    private Optional<TimeSeries> _interpolateData(List<MeasurementRepository.TimeIntervalAndValue> mt) {
        if (mt.size() == 0) {
            return Optional.empty();
        }

        long timeEarliest = mt.get(0).getTimeInterval();
        long timeLatest = mt.get(mt.size() - 1).getTimeInterval();

        double[] values = new double[(int) (timeLatest - timeEarliest + 1)];

        values[0] = mt.get(0).getValue();

        for (int i = 1; i < mt.size(); i++) {
            long timeBefore = mt.get(i - 1).getTimeInterval();
            long timeNow = mt.get(i).getTimeInterval();
            double cummulEnergyBefore = mt.get(i - 1).getValue();
            double cummulEnergyNow = mt.get(i).getValue();

            // Linearly-interpolate cummulative energy values
            for (long ti = timeBefore; ti <= timeNow; ti++) {
                values[(int) (ti - timeEarliest)] = cummulEnergyBefore + (cummulEnergyNow - cummulEnergyBefore) *
                        ((double) (ti - timeBefore) / (timeNow - timeBefore));
            }
        }

        TimeSeries ts = new TimeSeries(timeEarliest, values);

        return Optional.of(ts);
    }

    /**
     * This returns a timeseries of measurements for a given user and time window. Missing values are interpolated linearly.
     *
     * @param userName
     * @param startInterval
     * @param stopInterval
     * @param normalized    If false, then return a cummulative energy time series, otherwise - normalized to the initial value of energy
     * @return
     */
    public TimeSeries getUserMeasurementSeries(String userName, long startInterval, long stopInterval, boolean normalized) {

        List<MeasurementRepository.TimeIntervalAndValue> mt = this.repo.aggregateForTimeIntervals(userName, startInterval, stopInterval);

        TimeSeries ts = this._interpolateData(mt)
                .orElse(new TimeSeries(startInterval, new double[]{}));

        // Converts cummulative energy to normalized energy
        if (normalized) {
            ts.minus(this.repo.getCommulativeEnergyUntil(userName, startInterval));
        }

        return ts;

    }

    /**
     * This returns a timeseries of measurements aggregated for all users and time window.
     * Missing values are interpolated linearly.
     *
     * @param startInterval
     * @param stopInterval
     * @param normalized
     * @return
     */

    public TimeSeries getAggregatedMeasurementSeries(long startInterval, long stopInterval, boolean normalized) {
        List<MeasurementRepository.TimeIntervalAndValue> mt = this.repo.aggregateForTimeIntervals(startInterval, stopInterval);

        TimeSeries ts = this._interpolateData(mt)
                .orElse(new TimeSeries(startInterval, new double[]{}));

        if (normalized) {
            /*Double commuEnergy = this.repo.getCommulativeEnergyUntil(startInterval);
            ts.minus(commuEnergy != null ? commuEnergy : 0);*/
            ts = ts.minus(ts.getData().length > 0 ? ts.getData()[0] : 0);
        }

        return ts;
    }
}
