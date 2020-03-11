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
 *  Last Modified 2/8/18 4:17 PM
 */

package org.goflex.wp2.fogenerator.interfaces;


import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.Organization;
import org.goflex.wp2.foa.prediction.OrganizationPrediction;
import org.goflex.wp2.fogenerator.model.PredictionTs;

import java.util.List;

/**
 * This is a generic interface of a module (plug-in) capable of generating Flex-Offers.
 * We expect having an implementation of this
 * <p>
 * Created by Laurynas on 31/05/2017.
 */
public interface FlexOfferGenerator {

    /* Get Id Of FOA */
    String getFoaId();

    /* Get a name of FlexOffer generator */
    String getName();

    /* Get a description of the FlexOffer generator */
    String getDescription();

    /* Get (redirection) URL for configuring the generator */
    String getConfigurationURL();

//     /* Set/update Flex-Offer schedule*/
//    void notifyScheduleUpdate(FlexOffer flexOffer, FlexOfferSchedule schedule);

    FlexOffer[] createFlexOffer(String deviceId, String foaId, int number);

    FlexOffer createOnOffFlexOffer(DeviceDetail device, PredictionTs predictedData);

    FlexOffer generateOrganizationFO(Organization organization, List<OrganizationPrediction> organizationPredictions);

    FlexOffer generateFO(DeviceDetail deviceDetail, PredictionTs predictedValues, Organization organizationId);

    void generateRandomFO();


    void updateFoGenerationCount(String dt, double currentHour, String deviceId);

    //boolean shouldGenerateFO(String dt, String prevDt, double currentHour, String deviceId, DeviceFlexibilityDetail deviceFlexibilityDetail);
    boolean shouldGenerateFO(String dt, String prevDt, double currentHour, DeviceDetail deviceDetail);

    FlexOffer createOrganizationFlexOffer(Organization organization, List<OrganizationPrediction> organizationPrediction);
}
