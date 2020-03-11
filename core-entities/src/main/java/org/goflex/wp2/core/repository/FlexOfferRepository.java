/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
 * Copyright (c) 2018 The GoFlex Consortium
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
 *  Last Modified 3/23/18 5:54 PM
 */

package org.goflex.wp2.core.repository;

import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexOfferState;
import org.goflex.wp2.core.models.FlexOfferT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;


@Repository
@Transactional
public interface FlexOfferRepository extends JpaRepository<FlexOfferT, Long> {

    List<FlexOfferT> findAll();

    FlexOfferT findByFoID(String foID);

    List<FlexOfferT> findAllByCreationTimeAfter(Date today);

    List<FlexOfferT> findByOrganizationId(Long id);

    @Query(value = "SELECT count(f.flexoffer) FROM FlexOfferT f " +
            "WHERE f.creationTime >= :startDate and f.creationTime < :endDate ")
    int findFOsCountForDate(@Param("startDate") Date startDate,
                            @Param("endDate") Date endDate);

    @Query(value = "SELECT f.flexoffer FROM FlexOfferT f " +
            "WHERE f.creationTime >= :startDate and f.creationTime < :endDate ")
    List<FlexOffer> findFOsForDate(@Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate);

    @Query("SELECT f.flexoffer FROM FlexOfferT f " +
            "WHERE f.clientID = :clientID")
    List<FlexOffer> findByClientID(@Param("clientID") String clientID);


    @Query(value = "SELECT f.flexoffer FROM FlexOfferT f " +
            "WHERE f.clientID = :clientID and f.creationTime >= :startDate and f.creationTime < :endDate ")
    List<FlexOffer> findByClientIDAndMonth(@Param("clientID") String clientID, @Param("startDate") Date startDate,
                                           @Param("endDate") Date endDate);

    @Query("SELECT f.flexoffer FROM FlexOfferT f " +
            "WHERE f.plugID = :plugID")
    List<FlexOffer> findByPlugID(@Param("plugID") String plugID);

    @Query("SELECT f.flexoffer FROM FlexOfferT f " +
            "WHERE f.status = :status")
    List<FlexOffer> findByStatus(@Param("status") FlexOfferState status);

    @Query("SELECT f.flexoffer FROM FlexOfferT f " +
            "WHERE f.foID = :foID")
    FlexOffer findByFlexOfferID(@Param("foID") String foID);

    @Query("SELECT f.flexoffer FROM FlexOfferT f " +
            "WHERE f.creationTime >= :creationTime and f.clientID = :clientID")
    List<FlexOffer> findByClientIDAndCreationTime(@Param("clientID") String clientID,
                                                  @Param("creationTime") Date creationTime);

    @Query("SELECT f FROM FlexOfferT f " +
            "WHERE f.creationTime >= :creationTime and f.clientID = :clientID")
    List<FlexOfferT> findFlexOfferTByClientIDAndCreationTime(@Param("clientID") String clientID,
                                                             @Param("creationTime") Date creationTime);

    @Query("SELECT f.flexoffer FROM FlexOfferT f " +
            "WHERE f.scheduleStartTime >= :scheduleStartTime and f.scheduleStartTime < :endTime and f.clientID = " +
            ":clientID")
    List<FlexOffer> findByClientIDAndScheduleStartTime(@Param("clientID") String clientID,
                                                       @Param("scheduleStartTime") Date scheduleStartTime,
                                                       @Param("endTime") Date endTime);

    @Query("SELECT f.flexoffer FROM FlexOfferT f " +
            "WHERE f.scheduleStartTime >= :scheduleStartTime and f.scheduleStartTime < :endTime and f.organizationId " +
            "= :organizationId")
    List<FlexOffer> findByOrganizationIdAndScheduleStartTime(@Param("organizationId") Long organizationId,
                                                             @Param("scheduleStartTime") Date scheduleStartTime,
                                                             @Param("endTime") Date endTime);

    @Query("SELECT f.flexoffer FROM FlexOfferT f " +
            "WHERE f.creationTime >= :creationTime and f.organizationId = :organizationId")
    List<FlexOffer> findByOrganizationIdCreationTime(@Param("organizationId") Long organizationId,
                                                     @Param("creationTime") Date creationTime);


    @Query("SELECT f.flexoffer FROM FlexOfferT f " +
            "WHERE f.creationTime >= :creationTime and f.creationTime < :endTime and f.plugID = :plugID")
    List<FlexOffer> findByPlugIDAndCreationTime(@Param("plugID") String plugID,
                                                @Param("creationTime") Date creationTime,
                                                @Param("endTime") Date endTime);

    @Query("SELECT f FROM FlexOfferT f " +
            "WHERE f.creationTime >= :startDate and f.creationTime < :endDate and f.plugID = :plugID")
    List<FlexOfferT> findFlexOfferTByPlugIDAndDate(@Param("plugID") String plugID, @Param("startDate") Date startDate,
                                                   @Param("endDate") Date endDate);

    @Query("SELECT f.flexoffer FROM FlexOfferT f " +
            "WHERE f.creationTime >= :creationTime and f.creationTime < :endTime and f.plugID = :plugID and f" +
            ".clientID = :clientID")
    List<FlexOffer> findByPlugIDCreationTimeAndClientID(@Param("clientID") String clientID,
                                                        @Param("plugID") String plugID,
                                                        @Param("creationTime") Date creationTime,
                                                        @Param("endTime") Date endTime);

    @Query("SELECT f.flexoffer FROM FlexOfferT f " +
            "WHERE f.creationTime >= :creationTime and f.status = :status")
    List<FlexOffer> findByCreationTimeStatus(@Param("creationTime") Date creationTime,
                                             @Param("status") FlexOfferState status);

    @Query("SELECT f.flexoffer FROM FlexOfferT f " +
            "WHERE f.creationTime >= :creationTime and f.status = :status and f.clientID = :clientID")
    List<FlexOffer> findByClientIDCreationTimeStatus(@Param("clientID") String clientID,
                                                     @Param("creationTime") Date creationTime,
                                                     @Param("status") FlexOfferState status);

    @Query("SELECT f.flexoffer FROM FlexOfferT f " +
            "WHERE f.creationTime >= :creationTime and f.status = :status and f.clientID = :clientID and f.plugID = " +
            ":plugID")
    List<FlexOffer> findByStatusCreationTimeClientIDPlugID(@Param("clientID") String clientID,
                                                           @Param("plugID") String plugID,
                                                           @Param("creationTime") Date creationTime,
                                                           @Param("status") FlexOfferState status);


    @Query("SELECT f.flexoffer from FlexOfferT f where f.organizationId = :organizationId")
    List<FlexOffer> findFOsByOrganizationId(@Param("organizationId") Long organizationId);
}
