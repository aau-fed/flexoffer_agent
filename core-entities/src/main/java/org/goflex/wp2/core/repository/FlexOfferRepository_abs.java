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

package org.goflex.wp2.core.repository;

import org.goflex.wp2.core.models.FlexOfferT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//import org.springframework.transaction.annotation.Transactional;

/**
 * Created by bijay on 11/27/17.
 * This repository stores all Flex-Offers in memory
 */
@Repository
public interface FlexOfferRepository_abs extends JpaRepository<FlexOfferT, Long> {

    //@Query("SELECT f.flexoffer FROM flex_offer f where f.clientID = :clientID")
    List<FlexOfferT> findAll();

    List<FlexOfferT> findByclientID(String clientID);

//     List<FlexOffer> findFlexOfferByclientIDAndDateAndstatus(String clientID, Date creationTime, String status);
//     List<FlexOffer> findFlexOfferByplugIDAndDateAndstatus(String plugID, Date creationTime, String status);
//     List<FlexOffer> findFlexOfferByclientIDAndstatus(String clientID, String status);
//     List<FlexOffer> findFlexOfferByplugIDAndstatus(String plugID, String status);

}
