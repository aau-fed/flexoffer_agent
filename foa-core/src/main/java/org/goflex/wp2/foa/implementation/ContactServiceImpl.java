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
 *  Last Modified 4/9/18 2:01 PM
 */

package org.goflex.wp2.foa.implementation;


import org.goflex.wp2.core.entities.FlexOfferSlice;
import org.goflex.wp2.core.models.Contract;
import org.goflex.wp2.core.repository.ContractRepository;
import org.goflex.wp2.foa.interfaces.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * This is a passive component for exchanging Flex-Offer data with the aggregator
 */
@Service
public class ContactServiceImpl implements ContractService {

    @Autowired
    private ContractRepository contractRepository;

    public Contract getContract(String userName) {
        Contract contract = contractRepository.findByUserName(userName);
        //if no contract exist use the default one
        if (contract == null) {
            contract = contractRepository.getDefault();
        }
        return contract;
    }

    public void saveContract(Contract contract) {
        contractRepository.saveAndFlush(contract);
    }

    public void updateContract(Contract contract) {
        this.inValidateContract(contract.getUserName());
        this.saveContract(contract);

    }

    public double getFixedReward(Contract contract) {
        return contract.getFixedReward();
    }

    public List<Double> getDemandFlexReward(FlexOfferSlice slice, Contract contract) {
        List<Double> price = new ArrayList<>();
        price.add(0.0);
        price.add(0.0);
        for (int j = 0; j < slice.getEnergyConstraintList().length; j++) {
            price.set(0, price.get(0) + (slice.getEnergyConstraint(j).getUpper() -
                    slice.getEnergyConstraint(j).getLower()) * contract.getEnergyFlexReward());
            price.set(1, price.get(1) + (slice.getEnergyConstraint(j).getUpper() -
                    slice.getEnergyConstraint(j).getLower()));
        }
        return price;
    }

    @Override
    public List<Double> getReward(FlexOfferSlice[] slices, String userName, Contract contract) {
        List<Double> rewards = new ArrayList<>();
        if (contract == null) {
            contract = this.getContract(userName);
        }
        double df = 0.0;
        double dfr = 0.0;

        for (int i = 0; i < slices.length; i++) {
            List<Double> dfReward = this.getDemandFlexReward(slices[i], contract);
            dfr = dfr + dfReward.get(0);
            df = df + dfReward.get(1);
        }
        rewards.add(getFixedReward(contract));
        rewards.add(dfr);
        rewards.add(df);
        return rewards;
    }

    public List<Double> calculatePrice(FlexOfferSlice[] slices, String userName) {
        List<Double> foPrices = new ArrayList<>();
        Contract contract;
        contract = contractRepository.findByUserName(userName);
        if (contract == null) {
            contract = contractRepository.getDefault();
        }
        for (int i = 0; i < slices.length; i++) {
            foPrices.add(this.calculatePrice(slices[i], contract));
        }
        return foPrices;
    }

    public Double calculatePrice(FlexOfferSlice slice, Contract contract) {
        double price = 0.0;
        for (int j = 0; j < slice.getEnergyConstraintList().length; j++) {
            price = price + (slice.getEnergyConstraint(j).getUpper() -
                    slice.getEnergyConstraint(j).getLower()) * contract.getEnergyFlexReward() +
                    contract.getFixedReward();
        }
        return price;
    }

    @Override
    public boolean inValidateContract(String userName) {
        contractRepository.inValidateContract(userName);
        return true;
    }

}