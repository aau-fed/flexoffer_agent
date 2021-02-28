package org.goflex.wp2.fmanproxy.fmaninstance;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * @author muhaftab
 * created: 01/11/18
 */
public interface FmanInstanceService {

    FmanInstanceT createFmanInstance(FmanInstanceT fmanInstanceT);

    FmanInstanceT updateFmanInstance(FmanInstanceT fmanInstanceT);

    void deleteFmanInstanceByName(String instanceName);

    void deleteFmanInstanceById(Long instanceId);

    FmanInstanceT getFmanInstanceByName(String instanceName);

    FmanInstanceT getFmanInstanceByUrl(String instanceUrl);

    Boolean fmanInstanceActive(String instanceName);

    List<FmanInstanceT> getAllFmanInstances();

    ResponseEntity registerUserWithFman(Map<String, String> newFmanUser);

    ResponseEntity sendFlexOfferToFMAN(FmanInstanceT fmanInstanceT, Object flexOffer);

    ResponseEntity updateFlexOfferOnFMAN(FmanInstanceT fmanInstanceT, Object flexOffer);

    Map sendMeasurementsToFmanInstances(Map<String, Object> measurements);

    ResponseEntity getUserContractFromFmanInstance(FmanInstanceT fmanInstanceT, String userName);

    ResponseEntity getUserBillFromFmanInstance(FmanInstanceT fmanInstanceT, String userName, String year, String month);

    ResponseEntity getAllUsersFromFmanInstance(FmanInstanceT fmanInstanceT);

    ResponseEntity getAllUserNamesFromFmanInstance(FmanInstanceT fmanInstanceT);
}
