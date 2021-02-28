package org.goflex.wp2.app.fmanintegration.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.goflex.wp2.core.models.FmanUser;
import org.goflex.wp2.core.models.Organization;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.core.repository.FmanUserRepository;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.service.connection.FMANAccessTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Service
public class FmanUserService {
    private static final Logger logger = LoggerFactory.getLogger(FmanUserService.class);

    @Autowired
    FmanUserRepository repo;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    FMANAccessTokenService fmanAccessTokenService;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private FOAProperties foaProperties;

    public FmanUser getUserbyOrganization(long organizationId) {
        return repo.findByOrganizationId(organizationId);
    }

    public FmanUser getUserbyUsername(String userName) {
        return repo.findByUserName(userName);
    }

    public List<FmanUser> getUserbyfoaUserName(String foaUserName) {
        return repo.findByFoaUserName(foaUserName);
    }

    public Map<String, String>[] getUsersFromFman(String organizationName) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.getUserbyUsername("AAU").getAPIKey());
        HttpEntity<Map<String, String>> postEntity = new HttpEntity<>(headers);
        try {
            String registerUrl = foaProperties.getFmanConnectionConfig().getGetAllUsersURI() + "/" + organizationName;
            ResponseEntity<Map[]> response = restTemplate.exchange(registerUrl, HttpMethod.GET, postEntity, Map[].class);
            Map[] users = response.getBody();
            return response.getBody();
        } catch (Exception ex) {
            logger.error("Error in getting user list from FMAN for organization: " + organizationName);
            return null;
        }

    }

    //Register Organization or Prosumer to FMAN
    @Retryable(value={HttpServerErrorException.class}, maxAttempts=3, backoff=@Backoff(delay=100, maxDelay=500))
    public String _register(Map<String, String> userDetails) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.getUserbyUsername("AAU").getAPIKey());
        HttpEntity<Map<String, String>> postEntity = new HttpEntity<>(userDetails, headers);
        try {
            String registerUrl = foaProperties.getFmanConnectionConfig().getRegisterURI();
            ResponseEntity<String> response = restTemplate.postForEntity(registerUrl, postEntity, String.class);
            /**if user already registered in FMAN just save it on our system */
            if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                return "User Already Registered";
            }
            logger.info(response.toString());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJsn = null;
            try {
                responseJsn = mapper.readTree(((ResponseEntity) response).getBody().toString());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            if (responseJsn.has("token")) {
                return responseJsn.get("token").textValue();
            }
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT)
                logger.info(String.format("Organization or user already registered. Details: %s ", userDetails.toString()));
            return null;
        } catch (HttpServerErrorException ex) {
            throw  ex;
        } catch (Exception ex) {
            logger.error("Error in registering organization/user");
            return null;
        }
        return null;
    }

    public String _registerUsers(List<Map<String, String>> userDetails, String organizationName) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + this.getUserbyUsername("AAU").getAPIKey());
        HttpEntity<List<Map<String, String>>> postEntity = new HttpEntity<>(userDetails, headers);
        try {
            String registerUrl = foaProperties.getFmanConnectionConfig().getRegisterAllUsersURI() + "/" + organizationName;
            ResponseEntity<String> response = restTemplate.postForEntity(registerUrl, postEntity, String.class);
            return response.getBody();
        } catch (Exception ex) {
            logger.error("Error in registering users for organization: " + organizationName);
            return null;
        }
    }

    //Get token for broker account
    public String getFMANBrokerToken() {


        Map<String, String> credential = new HashMap<>();
        credential.put("userName", "AAU");
        credential.put("password", "password");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(credential, null);
        try {
            String url = foaProperties.getFmanConnectionConfig().getLoginURI();
            Object response = restTemplate.postForEntity(url, entity, String.class);
            logger.debug(Objects.requireNonNull(((ResponseEntity) response).getBody()).toString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJsn = null;

            try {
                responseJsn = mapper.readTree(((ResponseEntity) response).getBody().toString());
            } catch (IOException e) {
                e.printStackTrace();

            }
            if (responseJsn.has("token")) {
                FmanUser fmanUser = repo.findByUserName("AAU");
                fmanUser.setAPIKey(responseJsn.get("token").textValue());
                repo.save(fmanUser);
                return responseJsn.get("token").textValue();
            }

        } catch (Exception ex) {
            logger.warn(ex.toString());
        }
        return "";
    }

    public void createBrokerAccount() {

        FmanUser fmanUser = new FmanUser("AAU", 00000,
                "AAU", "password", "testkey", true, new Date());
        repo.save(fmanUser);
    }

    public void registerOrganization(Organization organization) {

        Map<String, String> credential = new HashMap<>();

        credential.put("organizationName", organization.getOrganizationName());
        credential.put("type", "organization");
        credential.put("role", "ROLE_PROSUMER");
        String fmanToken = this._register(credential);
        if (fmanToken != null) {
            FmanUser fmanUser = new FmanUser("AAU", organization.getOrganizationId(),
                    organization.getOrganizationName(), "", "", true, new Date());
            repo.save(fmanUser);
        }

    }


    @Scheduled(fixedRate = 3600000)
    public void registerUserToFman() {

        FmanUser usr = repo.findByUserName("AAU");
        //check if broker account exists
        if (usr == null) {
            //Create broker account on FOA, FMAN provides the credential
            this.createBrokerAccount();
        }
        this.getFMANBrokerToken();
        for (Organization organization : organizationRepository.findAll()
        ) {
            if (this.getUserbyOrganization(organization.getOrganizationId()) == null && !organization.getOrganizationName().equals("AAU")) {
                this.registerOrganization(organization);
            }
        }

    }

    public ResponseEntity getUserContract(UserT user) {

        String url = this.foaProperties.getFmanConnectionConfig().getContractURI()
                + "/" + this.organizationRepository.findByOrganizationId(user.getOrganizationId()).getOrganizationName()
                + "/" + user.getUserName();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("Authorization", "Bearer " + this.getUserbyUsername("AAU").getAPIKey());
        HttpEntity<Object> entity = new HttpEntity<>(null, httpHeaders);

        return this.restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
    }


    public ResponseEntity getUserBill(UserT user, String year, String month) {

        String url = this.foaProperties.getFmanConnectionConfig().getBillURI()
                + "/" + this.organizationRepository.findByOrganizationId(user.getOrganizationId()).getOrganizationName()
                + "/" + user.getUserName() + "/" + year + "/" + month;

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("Authorization", "Bearer " + this.getUserbyUsername("AAU").getAPIKey());
        HttpEntity<Object> entity = new HttpEntity<>(null, httpHeaders);

        return this.restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
    }

}
