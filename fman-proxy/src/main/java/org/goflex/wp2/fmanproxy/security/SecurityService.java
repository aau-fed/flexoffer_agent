package org.goflex.wp2.fmanproxy.security;

import org.goflex.wp2.fmanproxy.user.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    public boolean isBrokerAdmin(Authentication auth) {
        return auth.getAuthorities()
                .stream()
                .anyMatch(r -> (r == UserRole.ROLE_BROKER) || (r == UserRole.ROLE_ADMIN));
    }

    public boolean isBroker(Authentication auth) {
        return auth.getAuthorities()
                .stream()
                .anyMatch(r -> (r == UserRole.ROLE_BROKER));
    }

    public boolean isAdmin(Authentication auth) {
        return auth.getAuthorities()
                .stream()
                .anyMatch(r -> r == UserRole.ROLE_ADMIN);
    }

    public String getUserName(Authentication auth) {
        return auth.getName();
    }
}
