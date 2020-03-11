package org.goflex.wp2.core.wrappers;

/*
 * Created by Ivan Bizaca on 13/07/2017.
 */

/* FLEX OFFER MESSAGE FROM FOA TO FMAN */


import com.fasterxml.jackson.annotation.JsonFormat;
import org.goflex.wp2.core.entities.Assignment;
import org.goflex.wp2.core.entities.FlexOfferState;
import org.goflex.wp2.core.entities.FlexOfferType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.util.Date;


@XmlAccessorType(XmlAccessType.FIELD)
public class IneaFlexOfferWrapper implements Serializable, Cloneable {

    public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private int id;
    private FlexOfferState state;
    private int offeredById;
    //private Date acceptanceBeforeTime;
    private int numSecondsPerInterval = 900;
    private Date startAfterTime;
    private Date startBeforeTime;
    private Date creationTime;
    private FlexOfferSliceInea[] flexOfferProfileConstraints;
    private TariffConstraintProfileInea flexOfferTariffConstraint = null;
    private Assignment assignment = Assignment.obligatory;
    private FlexOfferType flexOfferType = FlexOfferType.imbalance;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public FlexOfferState getState() {
        return state;
    }

    public void setState(FlexOfferState state) {
        this.state = state;
    }

    public int getOfferedById() {
        return offeredById;
    }

    public void setOfferedById(int offeredById) {
        this.offeredById = offeredById;
    }


    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public int getNumSecondsPerInterval() {
        return numSecondsPerInterval;
    }

    public void setNumSecondsPerInterval(int numSecondsPerInterval) {
        this.numSecondsPerInterval = numSecondsPerInterval;
    }

    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getStartAfterTime() {
        return startAfterTime;
    }

    public void setStartAfterTime(Date startAfterTime) {
        this.startAfterTime = startAfterTime;
    }

    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getStartBeforeTime() {
        return startBeforeTime;
    }

    public void setStartBeforeTime(Date startBeforeTime) {
        this.startBeforeTime = startBeforeTime;
    }

    public FlexOfferSliceInea[] getFlexOfferProfileConstraints() {
        return flexOfferProfileConstraints;
    }

    public void setFlexOfferProfileConstraints(FlexOfferSliceInea[] flexOfferProfileConstraints) {
        this.flexOfferProfileConstraints = flexOfferProfileConstraints;
    }


    public TariffConstraintProfileInea getFlexOfferTariffConstraint() {
        return flexOfferTariffConstraint;
    }

    public void setFlexOfferTariffConstraint(TariffConstraintProfileInea flexOfferTariffConstraint) {
        this.flexOfferTariffConstraint = flexOfferTariffConstraint;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public FlexOfferType getFlexOfferType() {
        return flexOfferType;
    }

    public void setFlexOfferType(FlexOfferType flexOfferType) {
        this.flexOfferType = flexOfferType;
    }
}
