package org.goflex.wp2.fogenerator.model;

import org.goflex.wp2.core.entities.FlexOfferConstraint;

import java.util.Date;

/**
 * @author muhaftab
 * created: 2/17/19
 */
public class PredictedDemand {
    private Date date;
    private FlexOfferConstraint flexOfferConstraint;

    public PredictedDemand(Date date, FlexOfferConstraint flexOfferConstraint) {
        this.date = date;
        this.flexOfferConstraint = flexOfferConstraint;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public FlexOfferConstraint getFlexOfferConstraint() {
        return flexOfferConstraint;
    }

    public void setFlexOfferConstraint(FlexOfferConstraint flexOfferConstraint) {
        this.flexOfferConstraint = flexOfferConstraint;
    }

    @Override
    public String toString() {
        return "PredictedDemand{" +
                "date=" + date +
                ", flexOfferConstraint=" + flexOfferConstraint +
                '}';
    }
}
