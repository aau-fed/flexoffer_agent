package org.goflex.wp2.foa.implementation;

import com.twilio.Twilio;
import com.twilio.exception.TwilioException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.foa.interfaces.SmsService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
/**
 * twilio account credentials
 *  username = goflexdevuser@gmail.com
 *  password = D****@g****x***7
 */
public class TwilioSmsService implements SmsService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    //Twilio Account Sid and Token
    private static final String ACCOUNT_SID = "AC2671f459f174ae40a6846029a67a986d";
    private static final String AUTH_TOKEN = "3e625cd794ad56f6693f64a08e01f851";

    // Twilio phone number
    private static final String TWILIO_NUMBER = "+12065573976";


    @Autowired
    private UserService userService;

    private boolean isValidNumber(String phoneNumber) {
        return !StringUtils.isEmpty(phoneNumber);
    }

    @Async
    @Override
    public void sendSms(String userName, String content) {
        try {
            UserT user = userService.getUser(userName);
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            logger.info(user.getUserAddress().getPhone());
            if (isValidNumber(user.getUserAddress().getPhone())) {
                try {
                    if (user.getUserAddress().getPhone() != null && !user.getUserAddress().getPhone().equals("")) {
                        Message.creator(new PhoneNumber(user.getUserAddress().getPhone()), new PhoneNumber(TWILIO_NUMBER), content).create();
                        logger.info("Message Sent to user {}, phone number: {}", user.getUserName(), user.getUserAddress().getPhone());
                    }
                    if (user.getUserAddress().getPhone2() != null && !user.getUserAddress().getPhone2().equals("")) {
                        Message.creator(new PhoneNumber(user.getUserAddress().getPhone2()), new PhoneNumber(TWILIO_NUMBER), content).create();
                        logger.info("Message Sent to user {}, phone number: {}", user.getUserName(), user.getUserAddress().getPhone2());
                    }
                } catch (TwilioException e) {
                    logger.error(
                            "An exception occurred trying to send a message to {}, exception: {}",
                            user.getUserAddress().getPhone(),
                            e.getMessage());
                }
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }
}
