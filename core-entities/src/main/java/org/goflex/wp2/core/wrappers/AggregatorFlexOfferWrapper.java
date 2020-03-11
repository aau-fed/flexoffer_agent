package org.goflex.wp2.core.wrappers;

/*
 * Created by Ivan Bizaca on 13/07/2017.
 */

/* FLEX OFFER MESSAGE FROM FOA TO FMAN */


import com.fasterxml.jackson.annotation.JsonFormat;
import org.goflex.wp2.core.entities.FlexOfferState;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Date;


@XmlAccessorType(XmlAccessType.FIELD)
public class AggregatorFlexOfferWrapper {

    public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private int id;
    private FlexOfferState state;
    private String offeredById;
    private Date acceptanceBeforeTime;
    private long assignmentBeforeDurationSeconds;
    private Date assignmentBeforeTime;
    private Date creationTime;
    private Date endAfterTime;
    private Date endBeforeTime;
    private int numSecondsPerInterval;
    private Date startAfterTime;
    private Date startBeforeTime;
    private aggProfileWrapper[] slices;
    private aggScheduleWrapper defaultSchedule = null;
    private aggScheduleWrapper flexOfferSchedule = null;

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

    public String getOfferedById() {
        return offeredById;
    }

    public void setOfferedById(String offeredById) {
        this.offeredById = offeredById;
    }

    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getAcceptanceBeforeTime() {
        return acceptanceBeforeTime;
    }

    public void setAcceptanceBeforeTime(Date acceptanceBeforeTime) {
        this.acceptanceBeforeTime = acceptanceBeforeTime;
    }

    public long getAssignmentBeforeDurationSeconds() {
        return assignmentBeforeDurationSeconds;
    }

    public void setAssignmentBeforeDurationSeconds(long assignmentBeforeDurationSeconds) {
        this.assignmentBeforeDurationSeconds = assignmentBeforeDurationSeconds;
    }

    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getAssignmentBeforeTime() {
        return assignmentBeforeTime;
    }

    public void setAssignmentBeforeTime(Date assignmentBeforeTime) {
        this.assignmentBeforeTime = assignmentBeforeTime;
    }

    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getEndAfterTime() {
        return endAfterTime;
    }

    public void setEndAfterTime(Date endAfterTime) {
        this.endAfterTime = endAfterTime;
    }

    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getEndBeforeTime() {
        return endBeforeTime;
    }

    public void setEndBeforeTime(Date endBeforeTime) {
        this.endBeforeTime = endBeforeTime;
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

    public aggProfileWrapper[] getSlices() {
        return slices;
    }

    public void setSlices(aggProfileWrapper[] flexOfferProfileConstraints) {
        this.slices = flexOfferProfileConstraints;
    }

    public aggScheduleWrapper getDefaultSchedule() {
        return defaultSchedule;
    }

    public void setDefaultSchedule(aggScheduleWrapper defaultSchedule) {
        this.defaultSchedule = defaultSchedule;
    }

    public aggScheduleWrapper getFlexOfferSchedule() {
        return flexOfferSchedule;
    }

    public void setFlexOfferSchedule(aggScheduleWrapper flexOfferSchedule) {
        this.flexOfferSchedule = flexOfferSchedule;
    }
}
