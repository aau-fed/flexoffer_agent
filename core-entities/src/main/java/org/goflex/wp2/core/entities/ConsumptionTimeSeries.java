package org.goflex.wp2.core.entities;

/*-
 * #%L
 * ARROWHEAD::WP5::Core Data Structures
 * %%
 * Copyright (C) 2016 The ARROWHEAD Consortium
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Utility class for representing time series
 *
 * @author Laurynas
 */
@XmlRootElement
public class ConsumptionTimeSeries implements Serializable {
    /* Specified the relation between the intervals and minutes */
    public static final int numSecondsPerInterval = 6;
    private static final long serialVersionUID = 5996734100363832480L;
    /**
     * The value returned for intervals not explicitly set by the user.
     */
    private final double defaultValue = 0;
    /**
     * The time series data
     */

    private final Map<Long, Double> data = new HashMap<>();

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

    public void addData(long interval, Double val) {
        data.put(interval, val);

    }

    public void addData(Date time, Double val) {
        data.put(toFlexOfferTime(time), val);

    }

    public double getDefaultValue() {
        return defaultValue;
    }
}
