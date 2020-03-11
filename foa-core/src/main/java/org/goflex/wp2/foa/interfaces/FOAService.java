/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
 * Copyright (c) 2018.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining  a copy of this software and associated documentation
 *  files (the "Software") to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify, merge,
 *  publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions: The above copyright notice and
 *  this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON
 *  INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Last Modified 2/2/18 4:13 PM
 */

package org.goflex.wp2.foa.interfaces;

import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexOfferException;
import org.goflex.wp2.core.entities.FlexOfferState;
import org.goflex.wp2.core.models.FlexOfferT;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * All Flex-Offer generators and external parties will talk to this service to publish Flex-Offers
 */
public interface FOAService {
    List<FlexOfferT> getAllFO();

    /**
     * Insert a flex-offer
     * @throws FlexOfferException
     * @return A modified Flex-Offer by filling all missing details, ready to be used
     */
    //FlexOfferTempT createFlexOffer(FlexOfferTempT flexOffer) throws FlexOfferException;

    List<FlexOfferT> getFlexOfferTForDate(Date date);

    /**
     * Notify the update of the flex-offer
     */
    boolean updateFlexOffer(FlexOffer flexOffer);

    boolean updateFlexOfferStatus(UUID flexOfferId, FlexOfferState flexOfferState);

    boolean updateScheduleStartTime(UUID flexOfferID, Date dtTime);

    /**
     * Delete the Flex-Offer
     *
     * @return
     */
    boolean deleteFlexOffer(UUID flexOffer);

    /**
     * Notify power consumption
     */

    //TODO: FlexOfferOnly returns strings make it serializable to FO format

    /**
     * Get Flex-Offers associated with a client for a particular date
     */
    FlexOfferT getFlexOffer(UUID foId);

    int getFlexOffersCountForDate(String userName, Date date, String resolution);

    List<FlexOffer> getFlexOffersForDate(Date date);

    List<FlexOffer> getFlexOfferByClientID(List<Object> params);

    List<FlexOffer> getFlexOfferByClientIDCreationTime(List<Object> params);

    List<FlexOfferT> getFlexOfferTByClientIDCreationTime(List<Object> params);


    List<FlexOffer> getFlexOfferByClientIDScheduleStartTime(List<Object> params);

    List<FlexOfferT> getFlexOfferTByOrganizationID(Long id);
    List<FlexOffer> getFlexOffersByOrganizationID(Long id);

    List<FlexOffer> getFlexOfferByPlugId(List<Object> params);

    List<FlexOffer> getFlexOfferByPlugIDCreationTime(List<Object> params);
    List<FlexOfferT> getFlexOfferTByPlugIdAndDate(List<Object> params);

    List<FlexOfferT> getFlexOfferTByPlugIdAndStartTimeAndEndTime(List<Object> params);

    List<FlexOffer> getFlexOfferByClientIDPlugIDCreationTime(List<Object> params);

    List<FlexOffer> getFlexOfferByStatus(List<Object> params);

    List<FlexOffer> getFlexOfferByCreationTimeStatus(List<Object> params);

    List<FlexOffer> getFlexOfferByClientIDCreationTimeStatus(List<Object> params);

    List<FlexOffer> getFlexOfferByClientIDPlugIDCreationTimeStatus(List<Object> params);

    List<FlexOffer> getFlexOfferByOrganizationIDScheduleStartTime(List<Object> params);

    /**
     * Get Flex-Offers associated with a smart-plug for a particular date
     */
    List<FlexOffer> getFlexOfferForDevice(String plugID, Date date);

    /**
     * Get all active Flex-Offers associated with a client
     */
    List<FlexOffer> getFlexOffers(String clientID);

    /**
     * * Get all active Flex-Offers associated with a smart-plug
     */
    List<FlexOffer> getFlexOffersForDevice(String plugId);

    FlexOffer getFlexOfferByFoID(UUID foID);

    FlexOfferT save(FlexOfferT flexOfferTempT);

    List<Double> getRewardForMonth(String userName, int year, int month);

    void updateSendToFMAN(UUID foID, int state);

    List<FlexOffer> getFlexOffersByOrganizationIDCreationTime(List<Object> params);
}
