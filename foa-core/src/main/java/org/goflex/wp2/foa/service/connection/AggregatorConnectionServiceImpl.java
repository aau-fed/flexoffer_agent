package org.goflex.wp2.foa.service.connection;


import org.goflex.wp2.core.entities.FlexOffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Laurynas on 01/06/2017.
 */
//@Service
public class AggregatorConnectionServiceImpl implements AggregatorConnectionService {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public FlexOffer[] getFlexOffers() {
        return null;
    }

}
