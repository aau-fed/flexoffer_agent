package org.goflex.wp2.fogenerator.model;

import java.util.List;

public class PredictionTs {
    private List<PredictedDemand> predictedDemands;

    private int foType = 1; //1 = consumption fo, 2 = production fo
    private int numSlices = 1;

    public PredictionTs(List<PredictedDemand> predictedDemands, int foType) {
        this.predictedDemands = predictedDemands;
        this.foType = foType;
    }

    public PredictionTs(List<PredictedDemand> predictedDemands, int foType, int numSlices) {
        this.predictedDemands = predictedDemands;
        this.foType = foType;
        this.numSlices = numSlices;
    }

    public PredictionTs(List<PredictedDemand> predictedDemands) {
        this.predictedDemands = predictedDemands;
    }

    public List<PredictedDemand> getPredictedDemands() {
        return predictedDemands;
    }

    public void setPredictedDemands(List<PredictedDemand> predictedDemands) {
        this.predictedDemands = predictedDemands;
    }

    public boolean isCorrect() {
        // TODO: check time units are valid and values are reasonable (i.e. not too big or nan or null)
        for (PredictedDemand pd : this.predictedDemands) {
            if (pd.getFlexOfferConstraint().getLower() == 0.0 && pd.getFlexOfferConstraint().getUpper() == 0.0) {
                return false;
            }
        }
        return true;
    }

    public int getFoType() {
        return foType;
    }

    public void setFoType(int foType) {
        this.foType = foType;
    }

    public int getNumSlices() {
        return numSlices;
    }

    public void setNumSlices(int numSlices) {
        this.numSlices = numSlices;
    }

    @Override
    public String toString() {
        return "PredictionTs{" +
                "predictedDemands=" + predictedDemands +
                ", foType=" + foType +
                '}';
    }
}
