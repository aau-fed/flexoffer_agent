package org.goflex.wp2.app.scheduler.reporting;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.goflex.wp2.foa.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class LogDataCollector {

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private EmailService emailService;

    private Map<Date, Integer> logReported = new HashMap<>();

    @Value("${filePath}")
    private String filePath;

    @Value("${foaAppUrl}")
    private String foaAppUrl;

    @Autowired
    private RestTemplate restTemplate;

    //public FOAProperties foaProperties;

    @Autowired
    public LogDataCollector(EmailService emailService) {
        this.emailService = emailService;
        //this.foaProperties = foaProperties;
    }

    public ResponseEntity<String> checkFOAStatus(String requestBody, String URL) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.GET, entity, String.class); //Make POST call
        return response;
    }

    private Date getPreviousDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, -1);
        return c.getTime();
    }




    //@Scheduled(fixedRate = 2400000)
    //@Scheduled(fixedRate = 10000000)
    public void sendErrorLog() throws ParseException {
        String fileArchivePath = this.filePath + "/Archive/";

        int foaStatus = 0;
        try {
            /**Check if FOA service is running*/
            ResponseEntity<String> response = this.checkFOAStatus("", this.foaAppUrl);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJsn = null;
            try {
                responseJsn = mapper.readTree(response.getBody());
                if (responseJsn.has("status")) {
                    if (!responseJsn.get("status").textValue().equals("UP")) {
                        foaStatus = 1;
                    }
                } else {
                    foaStatus = 1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            foaStatus = 1;
            logger.info(ex.toString());
        }

        /**If foa is down send the latest log files*/
        if (foaStatus == 1) {
            try {
                String logFileName = this.filePath + "/foaLogfile.txt";
                List<String> logFilePaths = new ArrayList<>();
                logFilePaths.add(logFileName);

                /**handles how to reports log, current implementation is email next version call an api to send files*/
                emailService.sendMessageWithAttachment("goflexdevuser@gmail.com", "", "test message", logFilePaths);

                /**move files to achieve */
                File f = new File(logFileName);
                UUID newId = UUID.randomUUID();
                String newLocation = fileArchivePath + "foaLogfile_" + newId.toString() + ".txt";
                f.renameTo(new File(newLocation));

            } catch (Exception ex1) {
                logger.info(ex1.toString());
            }

        }
        /**Email log data for yesterday*/
        boolean reportYesterdayLog = false;

        Date today = new Date();
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        Date yesterday = df.parse(df.format(this.getPreviousDate(today)));

        if (logReported.containsKey(yesterday)) {
            if (logReported.get(yesterday) == 0) {
                reportYesterdayLog = true;
            }
        } else {
            logReported.put(yesterday, 0);
            reportYesterdayLog = true;
        }

        if (reportYesterdayLog) {
            try {
                File folder = new File(this.filePath);
                File[] listOfFiles = folder.listFiles();
                List<String> logFilePaths = new ArrayList<>();
                String logFileName;
                for (int i = 0; i < 1000; i++) {
                    logFileName = this.filePath + "/foaLogfile.txt." + df.format(yesterday) + "." + i + ".gz";
                    File f = new File(logFileName);
                    if (f.isFile()) {
                        logFilePaths.add(logFileName);
                    } else {
                        break;
                    }
                }
                emailService.sendMessageWithAttachment("goflexdevuser@gmail.com", "", "test message", logFilePaths);
                logReported.put(yesterday, 1);
                /**move files to achieve */
                String[] paths = logFilePaths.get(0).split("/");
                for (int j = 0; j < logFilePaths.size(); j++) {
                    File f = new File(logFilePaths.get(j));

                    String newLocation = fileArchivePath + paths[paths.length - 1];
                    f.renameTo(new File(newLocation));
                }
            } catch (Exception ex) {
                logger.info(ex.toString());
            }
        }


    }
}
