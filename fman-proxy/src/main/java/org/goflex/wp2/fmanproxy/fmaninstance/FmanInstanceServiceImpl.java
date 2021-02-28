package org.goflex.wp2.fmanproxy.fmaninstance;

import org.goflex.wp2.fmanproxy.common.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author muhaftab
 * created: 01/11/18
 */
@Service
public class FmanInstanceServiceImpl implements FmanInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FmanInstanceServiceImpl.class);

    @Autowired
    FmanInstanceRepository fmanInstanceRepository;

    @Autowired
    FmanRequestHandler fmanRequestHandler;

    @Value("${URItoSendFO}")
    private String uriToSendFo;

    @Value("${HeartbeatURI}")
    private String uriToGetSchedules;

    @Value("${MeasurementURI}")
    private String uriToSendMeasurements;

    @Value("${RegisterURI}")
    private String uriToRegisterUserWithFman;

    @Value("${ContractURI}")
    private String uriToGetContract;

    @Value("${BillURI}")
    private String uriToGetBill;

    @Value("${AllUsersURI}")
    private String uriToGetAllUsers;

    @Value("${AllUserNamesURI}")
    private String uriToGetAllUserNames;

    @Value("${URItoForceUpdateFO}")
    private String uriToForceUpdateFO;

    @Resource(name = "scheduleDetail")
    ConcurrentHashMap<UUID, Object> scheduleDetail;

    @Override
    public FmanInstanceT createFmanInstance(FmanInstanceT newFmanInstance) {
        try {
            if (getFmanInstanceByName(newFmanInstance.getInstanceName()) != null) {
                throw new CustomException(String.format("FMAN instance '%s' already exists",
                        newFmanInstance.getInstanceName()), HttpStatus.CONFLICT);
            }
            if (getFmanInstanceByUrl(newFmanInstance.getInstanceUrl()) != null) {
                throw new CustomException(String.format("Url '%s' already assigned to another FMAN instance",
                        newFmanInstance.getInstanceUrl()), HttpStatus.CONFLICT);
            }
            fmanInstanceRepository.save(newFmanInstance);
            return newFmanInstance;
        } catch (CustomException ex) {
            throw new CustomException(ex.getMessage(), ex.getHttpStatus());
        } catch (Exception ex) {
            throw new CustomException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public FmanInstanceT updateFmanInstance(FmanInstanceT receivedFmanInstance) {
        try {
            FmanInstanceT existingFmanInstance = getFmanInstanceByName(receivedFmanInstance.getInstanceName());
            if (existingFmanInstance == null) {
                throw new CustomException(String.format("No FMAN instance exists with name: '%s'",
                        receivedFmanInstance.getInstanceName()), HttpStatus.NOT_FOUND);
            }

            if (receivedFmanInstance.getInstanceUrl() != null) {
                if (getFmanInstanceByUrl(receivedFmanInstance.getInstanceUrl()) == null) {
                    existingFmanInstance.setInstanceUrl(receivedFmanInstance.getInstanceUrl());
                    existingFmanInstance.setInstanceStatus(InstanceStatus.ACTIVE);
                    existingFmanInstance.setActivationDate(new Date());
                } else if (!receivedFmanInstance.getInstanceUrl().equals(existingFmanInstance.getInstanceUrl())) {
                    throw new CustomException(String.format("Url '%s' already assigned to another FMAN instance",
                            receivedFmanInstance.getInstanceUrl()), HttpStatus.CONFLICT);
                } else if (receivedFmanInstance.getInstanceUrl().equals(existingFmanInstance.getInstanceUrl())) {
                    if (existingFmanInstance.getInstanceStatus() != InstanceStatus.ACTIVE) {
                        existingFmanInstance.setInstanceStatus(InstanceStatus.ACTIVE);
                    }
                }
            }

            if (receivedFmanInstance.getAuthorizedRole() != null || !receivedFmanInstance.getAuthorizedRole().equals("")) {
                existingFmanInstance.setAuthorizedRole(receivedFmanInstance.getAuthorizedRole());
            }

            this.fmanInstanceRepository.save(existingFmanInstance);
            return existingFmanInstance;
        } catch (CustomException ex) {
            throw new CustomException(ex.getMessage(), ex.getHttpStatus());
        } catch (Exception ex) {
            throw new CustomException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteFmanInstanceByName(String instanceName) {
        try {
            this.fmanInstanceRepository.deleteById(this.getFmanInstanceByName(instanceName).getInstanceId());
        } catch (Exception ex) {
            throw new CustomException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteFmanInstanceById(Long instanceId) {
        try {
            this.fmanInstanceRepository.deleteById(instanceId);
        } catch (Exception ex) {
            throw new CustomException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public FmanInstanceT getFmanInstanceByName(String instanceName) {
        if (instanceName == null || instanceName.equals("")) {
            return null;
        }
        return this.fmanInstanceRepository.findFmanInstanceTByInstanceName(instanceName);
    }

    @Override
    public FmanInstanceT getFmanInstanceByUrl(String instanceUrl) {
        if (instanceUrl == null || instanceUrl.equals("")) {
            return null;
        }
        return this.fmanInstanceRepository.findFmanInstanceTByInstanceUrl(instanceUrl);
    }

    @Override
    public Boolean fmanInstanceActive(String instanceName) {
        if (getFmanInstanceByName(instanceName) == null) {
            return false;
        }
        return this.fmanInstanceRepository.findFmanInstanceTByInstanceName(instanceName)
                .getInstanceStatus().equals(InstanceStatus.ACTIVE);
    }

    @Override
    public List<FmanInstanceT> getAllFmanInstances() {
        return this.fmanInstanceRepository.findAll();
    }


    @Override
    public ResponseEntity registerUserWithFman(Map<String, String> newFmanUser) {

        if (!newFmanUser.containsKey("organizationName") || newFmanUser.get("organizationName").equals("")) {
            throw new CustomException("Must provide 'organizationName' to register new user with FMAN!", HttpStatus.BAD_REQUEST);
        }
        String instanceName = newFmanUser.get("organizationName");
        FmanInstanceT fmanInstanceT = this.getFmanInstanceByName(instanceName);
        // check if instance (organization) exists and active
        if (fmanInstanceT == null) {
            throw new CustomException(String.format("FMAN instance for organization '%s' does not exist", instanceName), HttpStatus.NOT_FOUND);
        }
        if (fmanInstanceT.getInstanceStatus() != InstanceStatus.ACTIVE) {
            throw new CustomException(String.format("FMAN instance for organization '%s' not operational", instanceName), HttpStatus.NOT_FOUND);
        }

        // no need to send these to FMAN
        if (newFmanUser.containsKey("organizationName")) {
            newFmanUser.remove("organizationName");
        }
        if (newFmanUser.containsKey("type")) {
            newFmanUser.remove("type");
        }


        try {
            ResponseEntity response = this.fmanRequestHandler.requestFmanInstance(fmanInstanceT,
                    this.uriToRegisterUserWithFman, HttpMethod.POST, newFmanUser, String.class);
            return response;
        } catch (HttpClientErrorException ex) {
            throw new CustomException(ex.getMessage(), ex.getStatusCode());
        } catch (Exception ex) {
            throw new CustomException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public ResponseEntity sendFlexOfferToFMAN(FmanInstanceT fmanInstanceT, Object flexOffer) {
        String uri = this.uriToSendFo + "/" + fmanInstanceT.getInstanceName();
        return this.fmanRequestHandler.requestFmanInstance(fmanInstanceT, uri, HttpMethod.POST, flexOffer, String.class);
    }

    @Override
    public ResponseEntity updateFlexOfferOnFMAN(FmanInstanceT fmanInstanceT, Object flexOffers) {
        String uri = this.uriToForceUpdateFO + "/" + fmanInstanceT.getInstanceName();
        return this.fmanRequestHandler.requestFmanInstance(fmanInstanceT, uri, HttpMethod.PUT, flexOffers, String.class);
    }

    @Scheduled(fixedRate = 60000)
    public void getSchedulesFromFmanInstances() {
        for (FmanInstanceT fman : this.getAllFmanInstances()) {
            try {

                if (!this.fmanInstanceActive(fman.getInstanceName())) {
                    LOGGER.warn(String.format("FMAN instance: '%s' not active", fman.getInstanceName()));
                    continue;
                }

                int opertionState = 1;
                ResponseEntity response = this.fmanRequestHandler.requestFmanInstance(fman, this.uriToGetSchedules, HttpMethod.POST, opertionState, Object.class);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assert body != null;
                body.forEach((key, val) -> scheduleDetail.put(UUID.fromString(key), val));

            } catch (Exception ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }


    @Override
    public Map sendMeasurementsToFmanInstances(Map<String, Object> measurements) {

        Map<String, String> response = new HashMap<>();

        for (String org : measurements.keySet()) {
            try {
                FmanInstanceT fmanInstanceT = getFmanInstanceByName(org);
                if (!this.fmanInstanceActive(fmanInstanceT.getInstanceName())) {
                    LOGGER.warn(String.format("FMAN instance: '%s' not active", org));
                    response.put(org, "error: FMAN instance inactive");
                    continue;
                }
                this.fmanRequestHandler.requestFmanInstance(fmanInstanceT, this.uriToSendMeasurements,
                        HttpMethod.POST, measurements.get(org), String.class);
                response.put(org, "success");
            } catch (Exception ex) {
                response.put(org, "error: " + ex.getMessage());
            }
        }

        response.put("status", HttpStatus.OK.toString());

        return response;
    }


    @Override
    public ResponseEntity getUserContractFromFmanInstance(FmanInstanceT fmanInstanceT, String userName) {
        try {
            String url = this.uriToGetContract + "/" + userName;
            ResponseEntity response = this.fmanRequestHandler.requestFmanInstance(fmanInstanceT, url, HttpMethod.GET, null, Object.class);
            return response;
        } catch (HttpClientErrorException ex) {
            throw new CustomException(ex.getMessage(), ex.getStatusCode());
        } catch (Exception ex) {
            throw new CustomException("error getting user contract from instance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity getUserBillFromFmanInstance(FmanInstanceT fmanInstanceT, String userName, String year, String month) {
        try {
            String url = this.uriToGetBill + "/" + userName + "/" + year + "/" + month;
            ResponseEntity response = this.fmanRequestHandler.requestFmanInstance(fmanInstanceT, url, HttpMethod.GET, null, Object.class);
            return response;
        } catch (HttpClientErrorException ex) {
            throw new CustomException(ex.getMessage(), ex.getStatusCode());
        } catch (Exception ex) {
            throw new CustomException("error getting user contract from instance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity getAllUsersFromFmanInstance(FmanInstanceT fmanInstanceT) {
        try {
            String url = this.uriToGetAllUsers;
            ResponseEntity response = this.fmanRequestHandler.requestFmanInstance(fmanInstanceT, url, HttpMethod.GET, null, Object.class);
            return response;
        } catch (HttpClientErrorException ex) {
            throw new CustomException(ex.getMessage(), ex.getStatusCode());
        } catch (Exception ex) {
            throw new CustomException("error getting all users list from instance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity getAllUserNamesFromFmanInstance(FmanInstanceT fmanInstanceT) {
        try {
            String url = this.uriToGetAllUserNames;
            ResponseEntity response = this.fmanRequestHandler.requestFmanInstance(fmanInstanceT, url, HttpMethod.GET, null, Object.class);
            return response;
        } catch (HttpClientErrorException ex) {
            throw new CustomException(ex.getMessage(), ex.getStatusCode());
        } catch (Exception ex) {
            throw new CustomException("error getting all users list from instance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
