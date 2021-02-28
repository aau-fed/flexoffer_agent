package org.goflex.wp2.fmanproxy.fmaninstance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.goflex.wp2.fmanproxy.common.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author muhaftab
 * created: 11/1/18
 */
@Component
public class FmanRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FmanRequestHandler.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FmanInstanceRepository fmanInstanceRepository;

    @Value("${LoginURI}")
    private String newTokenUrl;

    public FmanRequestHandler() {
    }

    public ResponseEntity requestFmanInstance(FmanInstanceT fmanInstanceT, String uri,
                                              HttpMethod method, Object requestBody, Class c) {

        if (fmanInstanceT.getJwtToken() == null || fmanInstanceT.getJwtToken().equals("")) {
            this.getAndSaveNewToken(fmanInstanceT);
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("Authorization", "Bearer " + fmanInstanceT.getJwtToken());
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, httpHeaders);

        try {
            String url = fmanInstanceT.getInstanceUrl() + uri;
            LOGGER.debug(url);
            return restTemplate.exchange(url, method, entity, c);

        } catch (HttpClientErrorException e) {
            LOGGER.warn("Error making request to FMAN instance: " + e.getMessage());
            LOGGER.warn("Response body: " + e.getResponseBodyAsString());
            LOGGER.warn("Response status code: " + e.getStatusCode());
            if (handleHttpClientError(fmanInstanceT, e)) {
                return requestFmanInstance(fmanInstanceT, uri, method, requestBody, c);
            }
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            LOGGER.warn("Error making request to FMAN instance: " + e.getMessage());
            return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private boolean handleHttpClientError(FmanInstanceT fmanInstanceT, HttpClientErrorException e) {
        try {
            String errorResponse = e.getResponseBodyAsString();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(errorResponse);
            if (jsonNode.has("message") && jsonNode.get("message").asText().equals("Access Denied")) {
                this.getAndSaveNewToken(fmanInstanceT);
                return true;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return false;
    }

    private void getAndSaveNewToken(FmanInstanceT fmanInstanceT) {
        String newToken = this.getNewToken(fmanInstanceT);
        if (newToken == null) {
            throw new CustomException(String.format("Failed to authorize with FMAN: '%s'",
                    fmanInstanceT.getInstanceName()), HttpStatus.UNAUTHORIZED);
        }
        fmanInstanceT.setJwtToken(newToken);
        this.saveNewToken(fmanInstanceT);
    }

    private String getNewToken(FmanInstanceT fmanInstanceT) {

        try {
            LOGGER.info(String.format("Requesting new token from FMAN instance: '%s'", fmanInstanceT.getInstanceName()));

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            //String auth = username + ":" + password;
            //byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
            //String authHeader = "Basic " + new String(encodedAuth);
            //httpHeaders.set("Authorization", authHeader);

            Map<String, String> body = new HashMap<>();
            body.put("userName", "sysadmin");
            body.put("password", "pwd4FMAN!");

            String url = fmanInstanceT.getInstanceUrl() + this.newTokenUrl;
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST,
                    new HttpEntity<>(body, httpHeaders), String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.getBody());

            String newToken = jsonNode.get("token").textValue();

            LOGGER.info(newToken);
            return newToken;

        } catch (Exception e) {
            LOGGER.error(e.getStackTrace().toString());
            return null;
        }
    }


    private void saveNewToken(FmanInstanceT fmanInstanceT) {
        this.fmanInstanceRepository.save(fmanInstanceT);
    }

}
