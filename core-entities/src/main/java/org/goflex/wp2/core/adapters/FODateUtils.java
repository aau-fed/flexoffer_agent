package org.goflex.wp2.core.adapters;


import java.util.Date;

public class FODateUtils {

    /* Specified the relation between the intervals and minutes */
    public static final int numSecondsPerInterval = 15 * 60;    /* 1 flex-offer interval corresponds to 15*60 seconds/15 minutes */


    /* Convert discrete FlexOffer time to absolute time */
    public static final Date toAbsoluteTime(long foTime) {
        long timeInMillisSinceEpoch = foTime * (numSecondsPerInterval * 1000);
        return new Date(timeInMillisSinceEpoch);
    }

    /* Convert abosolute time to discrete FlexOffer time */
    public static final long toFlexOfferTime(Date time) {
        long timeInMillisSinceEpoch = time.getTime();
        return timeInMillisSinceEpoch / (numSecondsPerInterval * 1000);
    }


}