package org.goflex.wp2.foa.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author muhaftab
 * created: 7/25/19
 */
public class TimeZoneUtil {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z";
    private static final String TIME_WITHOUT_DATE = "HH:mm";

    public static String getTimeZoneAdjustedStringTime(Date receivedTime, String org) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIME_WITHOUT_DATE);
        ZonedDateTime zonedDateTime = getZonedDateTime(receivedTime, org);
        return zonedDateTime.format(formatter);
    }

    public static String getTimeZoneAdjustedStringDateTime(Date receivedTime, String org) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        ZonedDateTime zonedDateTime = getZonedDateTime(receivedTime, org);
        return zonedDateTime.format(formatter);
    }

    private static ZonedDateTime getZonedDateTime(Date receivedTime, String org) {
        ZoneId zoneId;
        switch (org) {
            case "CYPRUS":
                zoneId = ZoneId.of("Europe/Nicosia");
                break;
            case "SWW":
                zoneId = ZoneId.of("Europe/Berlin");
                break;
            case "SWISS":
                zoneId = ZoneId.of("Europe/Zurich");
                break;
            default:
                zoneId = ZoneId.systemDefault();
                break;
        }
        return ZonedDateTime.ofInstant(receivedTime.toInstant(), zoneId);
    }
}
