package org.goflex.wp2.foa.service.connection;


import org.goflex.wp2.core.entities.FlexOffer;

/**
 * This service handles the connection with the aggregator
 * <p>
 * Created by Laurynas on 01/06/2017.
 */
public interface AggregatorConnectionService {

    String aggregatorUrl = null;

    FlexOffer[] getFlexOffers();

}
