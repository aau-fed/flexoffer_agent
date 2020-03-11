package org.goflex.wp2.app.fmanintegration.listener;

import org.goflex.wp2.app.fmanintegration.listener.event.UserCreatedEvent;
import org.goflex.wp2.app.fmanintegration.user.FmanUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class UserCreatedEventListener {
    private static final Logger logger = LoggerFactory.getLogger(UserCreatedEventListener.class);

    @Autowired
    private FmanUserService fmanUserService;

    @Async
    @EventListener
    public void processUserCreatedEventListner(UserCreatedEvent event) {
        Map<String, String> userDetails = new HashMap<>();
        logger.info("User Registration event triggered");
        userDetails.put("userName", event.getUser().getUserName());
        userDetails.put("organizationName", event.getOrganizationName());
        userDetails.put("type", "prosumer");
        userDetails.put("email", event.getUser().getEmail());
        userDetails.put("role", "ROLE_PROSUMER");
        userDetails.put("password", event.getPassword());
        String token = fmanUserService._register(userDetails);
    }

}
