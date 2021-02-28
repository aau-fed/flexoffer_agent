package org.goflex.wp2.foa.service.connection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.goflex.wp2.core.models.FmanUser;
import org.goflex.wp2.core.repository.FmanUserRepository;
import org.goflex.wp2.core.util.HttpHeaderFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class FMANAccessTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FMANAccessTokenService.class);

    private final String fmanToken = "";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FmanUserRepository fmanUserRepository;


    public String getToken(String userName) {


        return fmanUserRepository.findByUserName(userName).getAPIKey();
    }


    public void updateToken(FmanUser fmanUser) {
        Map<String, String> credential = new HashMap<>();
        credential.put("userName", fmanUser.getUserName());
        credential.put("password", "admin");

        HttpHeaderFormatter httpHeaderFormatter = new HttpHeaderFormatter();
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(credential, null);

        try {

            Object response = restTemplate.postForEntity("http://localhost:8085/user/login", entity, String.class);
            LOGGER.debug(((ResponseEntity) response).getBody().toString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJsn = null;

            try {
                responseJsn = mapper.readTree(((ResponseEntity) response).getBody().toString());
            } catch (IOException e) {
                e.printStackTrace();

            }
            if (responseJsn.has("token")) {
                fmanUser.setAPIKey(responseJsn.get("token").textValue());
                fmanUserRepository.save(fmanUser);

            }

        } catch (Exception ex) {
            LOGGER.warn(ex.toString());
        }

    }


    /*@Scheduled(fixedRate = 600000)
    public void updateFMANToken(){
        for(FmanUser fmanUser:fmanUserRepository.findAll()){
            updateToken(fmanUser);
        }

    }*/


    //@Scheduled(fixedRate = 6000000)
    /*public void getFMANToken_abs() {

        Map<String, String> credential = new HashMap<>();
        credential.put("userName", "admin");
        credential.put("password","admin");

        HttpHeaderFormatter httpHeaderFormatter = new HttpHeaderFormatter();
        //HttpHeaders httpHeaders = httpHeaderFormatter.createHeaders("admin", "admin");
        HttpEntity<Map<String,String>> entity = new HttpEntity<>(credential, null);

        try {

            Object response = restTemplate.postForEntity("http://localhost:8085/user/login", entity, String.class);
            System.out.println(((ResponseEntity) response).getBody());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJsn = null;

            try {
                responseJsn = mapper.readTree(((ResponseEntity) response).getBody().toString());
            } catch (IOException e) {
                e.printStackTrace();

            }
            if(responseJsn.has("token")){
                this.fmanToken = responseJsn.get("token").textValue();

            }

        } catch (Exception ex) {
            System.out.println(ex);
        }

    }*/
}
