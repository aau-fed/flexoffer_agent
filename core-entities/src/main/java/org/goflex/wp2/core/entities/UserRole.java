package org.goflex.wp2.core.entities;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    ROLE_PROSUMER,
    ROLE_AGGREGATOR,
    ROLE_ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }

}
