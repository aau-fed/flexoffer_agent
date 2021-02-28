package org.goflex.wp2.core.entities;

/**
 * @author muhaftab
 * created: 2/12/19
 */
public enum ScheduleValidationStatus {
    ValidAndPublished(0),
    InvalidSchedule(1),
    RejectedDueToAdaptation(2),
    RejectedDueToExecution(3),
    InvalidFlexOfferId(4);

    private final int value;

    ScheduleValidationStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
