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
 *  Last Modified 2/22/18 9:57 PM
 */

package org.goflex.wp2.foa.implementation;


import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexOfferState;
import org.goflex.wp2.core.models.Contract;
import org.goflex.wp2.core.models.FlexOfferT;
import org.goflex.wp2.core.repository.FlexOfferRepository;
import org.goflex.wp2.foa.interfaces.FOAService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This is a passive component for exchanging Flex-Offer data with the aggregator
 */
@Service
public class FOAServiceImpl implements FOAService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Resource(name = "deviceLatestFO")
    private ConcurrentHashMap<String, FlexOfferT> deviceLatestFO;

    private final FlexOfferRepository flexofferRepository;
    private final ContactServiceImpl contractService;

    public FOAServiceImpl(FlexOfferRepository flexofferRepository,
                          ContactServiceImpl contractService) {
        this.flexofferRepository = flexofferRepository;
        this.contractService = contractService;
    }

    @Override
    public FlexOfferT save(FlexOfferT flexOfferT) {
        return flexofferRepository.saveAndFlush(flexOfferT);
    }

    @Override
    public int getFlexOffersCountForDate(String userName, Date date, String resolution) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        if (resolution.equals("month")) {
            c.add(Calendar.MONTH, 1);
        } else {
            c.add(Calendar.DATE, 1);
        }
        Date endTime = c.getTime();
        if (userName.equals("")) {
            return flexofferRepository.findFOsCountForDate(date, endTime);
        } else {
            return flexofferRepository.findByClientIDAndMonth(userName, date, endTime).size();
        }
    }


    @Override
    public List<FlexOffer> getFlexOffersForDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, 1);
        Date endTime = c.getTime();
        return flexofferRepository.findFOsForDate(date, endTime);
    }

    @Override
    public List<FlexOfferT> getAllFO() {
        return flexofferRepository.findAll();
    }


    @Override
    public List<FlexOfferT> getFlexOfferTForDate(Date date) {

        return flexofferRepository.findAllByCreationTimeAfter(date);
    }

    @Override
    @Transactional
    public boolean updateFlexOffer(FlexOffer flexOffer) {
        try {
            FlexOfferT fo = flexofferRepository.findByFoID(flexOffer.getId().toString());
            fo.setFlexoffer(flexOffer);
            this.deviceLatestFO.put(flexOffer.getOfferedById(), fo);
            LOGGER.info("updateFlexOffer(): deviceLatestFO map updated for device: {}",
                    fo.getFlexoffer().getOfferedById());
            return true;
        } catch (Exception ex) {
            LOGGER.error("Error updating FO. Msg: {}", ex.getLocalizedMessage());
        }
        return false;
    }

    @Override
    @Transactional
    public boolean updateFlexOfferStatus(UUID flexOfferId, FlexOfferState flexOfferState) {
        try {
            FlexOfferT fo = flexofferRepository.findByFoID(flexOfferId.toString());
            fo.setStatus(flexOfferState);
            fo.getFlexoffer().setState(flexOfferState);
            this.deviceLatestFO.put(fo.getFlexoffer().getOfferedById(), fo);
            LOGGER.info("updateFlexOfferStatus(): deviceLatestFO map updated for device: {}",
                    fo.getFlexoffer().getOfferedById());
            return true;
        } catch (Exception ex) {
            LOGGER.error("Error updating FO status. Msg: {}", ex.getLocalizedMessage());
        }
        return false;
    }

    @Override
    @Transactional
    public boolean updateScheduleStartTime(UUID flexOfferID, Date dtTime) {
        try {
            FlexOfferT fo = flexofferRepository.findByFoID(flexOfferID.toString());
            fo.setScheduleStartTime(dtTime);
            this.deviceLatestFO.put(fo.getFlexoffer().getOfferedById(), fo);
            LOGGER.info("updateScheduleStartTime(): deviceLatestFO map updated for device: {}",
                    fo.getFlexoffer().getOfferedById());
            return true;
        } catch (Exception ex) {
            LOGGER.error("Error updating schedule start time. Msg: {}", ex.getLocalizedMessage());
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteFlexOffer(UUID flexOfferId) {
        FlexOfferT fo = flexofferRepository.findByFoID(flexOfferId.toString());
        flexofferRepository.delete(fo);
        if (this.deviceLatestFO.containsKey(fo.getFlexoffer().getOfferedById())) {
            this.deviceLatestFO.remove(fo.getFlexoffer().getOfferedById());
            LOGGER.info("FO removed from deviceLatestFO map. Device Id: {}, FlexOffer: {}",
                    fo.getFlexoffer().getOfferedById(), fo);
        }
        return true;
    }


    @Override
    public List<FlexOffer> getFlexOfferByClientID(List<Object> params) {
        return flexofferRepository.findByClientID((String) params.get(0));
    }

    @Override
    public FlexOfferT getFlexOffer(UUID foID) {
        return flexofferRepository.findByFoID(foID.toString());
    }

    @Override
    public FlexOffer getFlexOfferByFoID(UUID foID) {
        return flexofferRepository.findByFlexOfferID(foID.toString());
    }

    @Override
    public List<FlexOffer> getFlexOfferByClientIDCreationTime(List<Object> params) {
        return flexofferRepository.findByClientIDAndCreationTime((String) params.get(0), (Date) params.get(1));
    }

    @Override
    public List<FlexOfferT> getFlexOfferTByClientIDCreationTime(List<Object> params) {
        return flexofferRepository.findFlexOfferTByClientIDAndCreationTime((String) params.get(0), (Date) params.get(1));
    }

    @Override
    public List<FlexOffer> getFlexOfferByClientIDScheduleStartTime(List<Object> params) {

        return flexofferRepository
                .findByClientIDAndScheduleStartTime((String) params.get(0), (Date) params.get(1), (Date) params.get(2));
    }

    @Override
    public List<FlexOffer> getFlexOfferByOrganizationIDScheduleStartTime(List<Object> params) {

        return flexofferRepository.findByOrganizationIdAndScheduleStartTime((Long) params.get(0), (Date) params.get(1),
                (Date) params.get(2));
    }

    @Override
    public List<FlexOfferT> getFlexOfferTByOrganizationID(Long id) {
        return flexofferRepository.findByOrganizationId(id);
    }

    @Override
    public List<FlexOffer> getFlexOffersByOrganizationID(Long id) {
        return flexofferRepository.findFOsByOrganizationId(id);
    }

    @Override
    public List<FlexOffer> getFlexOfferByPlugId(List<Object> params) {
        return flexofferRepository.findByPlugID((String) params.get(0));
    }

    @Override
    public List<FlexOffer> getFlexOfferByPlugIDCreationTime(List<Object> params) {
        return null;//flexofferRepository.findByPlugIDAndCreationTime((String) params.get(0), (Date) params.get(1));
    }

    @Override
    public List<FlexOfferT> getFlexOfferTByPlugIdAndDate(List<Object> params) {
        Date startTime = (Date) params.get(1);
        Calendar c = Calendar.getInstance();
        c.setTime(startTime);
        c.add(Calendar.DATE, 1);
        Date endTime = c.getTime();
        return flexofferRepository.findFlexOfferTByPlugIDAndDate((String) params.get(0), startTime, endTime);
    }

    @Override
    public List<FlexOfferT> getFlexOfferTByPlugIdAndStartTimeAndEndTime(List<Object> params) {
        Date startTime = (Date) params.get(1);
        Date endTime = (Date) params.get(2);
        return flexofferRepository.findFlexOfferTByPlugIDAndDate((String) params.get(0), startTime, endTime);
    }

    @Override
    public List<FlexOffer> getFlexOfferByClientIDPlugIDCreationTime(List<Object> params) {
        Calendar c = Calendar.getInstance();
        c.setTime((Date) params.get(2));
        c.add(Calendar.DATE, 1);
        Date endTime = c.getTime();
        return flexofferRepository.findByPlugIDAndCreationTime((String) params.get(1), (Date) params.get(2), endTime);
    }

    @Override
    public List<FlexOffer> getFlexOfferByStatus(List<Object> params) {
        return flexofferRepository.findByStatus((FlexOfferState) params.get(0));
    }

    @Override
    public List<FlexOffer> getFlexOfferByCreationTimeStatus(List<Object> params) {
        return flexofferRepository.findByCreationTimeStatus((Date) params.get(0), (FlexOfferState) params.get(1));
    }

    @Override
    public List<FlexOffer> getFlexOfferByClientIDCreationTimeStatus(List<Object> params) {
        return flexofferRepository.findByClientIDCreationTimeStatus((String) params.get(0), (Date) params.get(1),
                (FlexOfferState) params.get(2));
    }

    @Override
    public List<FlexOffer> getFlexOfferByClientIDPlugIDCreationTimeStatus(List<Object> params) {
        return flexofferRepository
                .findByStatusCreationTimeClientIDPlugID((String) params.get(0), (String) params.get(1),
                        (Date) params.get(2), (FlexOfferState) params.get(3));
    }


    @Override
    public List<FlexOffer> getFlexOfferForDevice(String plugID, Date date) {
        return flexofferRepository.findByClientID(plugID);
        //return flexofferRepository.findFlexOfferByplugIDAndDateAndstatus(plugID, date, "Initial");
    }

    @Override
    public List<FlexOffer> getFlexOffers(String clientID) {
        return flexofferRepository.findByClientID(clientID);
    }

    @Override
    public List<FlexOffer> getFlexOffersForDevice(String plugID) {
        return flexofferRepository.findByClientID(plugID);
        //return flexofferRepository.findFlexOfferByplugIDAndstatus(plugID, "Initial");
    }

    @Override
    @Transactional
    public void updateSendToFMAN(UUID foID, int state) {
        FlexOfferT fo = flexofferRepository.findByFoID(foID.toString());
        fo.setReceivedByFMAN(state);
        this.deviceLatestFO.put(fo.getFlexoffer().getOfferedById(), fo);
        LOGGER.info("updateSendToFMAN(): deviceLatestFO map updated for device: {}",
                fo.getFlexoffer().getOfferedById());
    }

    @Override
    public List<FlexOffer> getFlexOffersByOrganizationIDCreationTime(List<Object> params) {
        return flexofferRepository.findByOrganizationIdCreationTime((Long) params.get(0), (Date) params.get(1));
    }

    @Override
    public List<Double> getRewardForMonth(String userName, int year, int month) {

        Contract contract = contractService.getContract(userName);

        Date startDate = new GregorianCalendar(year, month, 1).getTime();
        Date endDate = new GregorianCalendar(year, month + 1, 1).getTime();
        List<FlexOffer> fos = flexofferRepository.findByClientIDAndMonth(userName, startDate, endDate);

        List<Double> rewards = new ArrayList<>();
        rewards.add(0.0);
        rewards.add(0.0);
        rewards.add(0.0);
        rewards.add(0.0);

        for (FlexOffer fo : fos
        ) {
            // Calculate reward for executed FOs*/
            if (fo.getState() == FlexOfferState.Executed || fo.getState() == FlexOfferState.Assigned ||
                    fo.getState() == FlexOfferState.Accepted) {

                double timeFlex =
                        (fo.getStartBeforeInterval() - fo.getStartAfterInterval()) * contract.getTimeFlexReward();

                List<Double> reward =
                        contractService.getReward(fo.getFlexOfferProfileConstraints(), userName, contract);
                rewards.set(0, rewards.get(0) + reward.get(0));
                rewards.set(1, rewards.get(1) + reward.get(1));
                rewards.set(2, rewards.get(2) + timeFlex);
                rewards.set(3, rewards.get(3) + reward.get(2));
            }
        }
        rewards.set(0, Math.round(rewards.get(0) * 1000D) / 1000D);
        rewards.set(1, Math.round(rewards.get(1) * 1000D) / 1000D);
        rewards.set(2, Math.round(rewards.get(2) * 1000D) / 1000D);
        rewards.set(3, Math.round(rewards.get(3) * 1000D) / 1000D);

        rewards.add((double) fos.size());
        return rewards;
    }

}