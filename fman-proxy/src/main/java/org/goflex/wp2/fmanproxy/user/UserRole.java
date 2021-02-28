package org.goflex.wp2.fmanproxy.user;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
        /**
         * The user is a normal Prosumer, generating FOs, etc.
         */
        ROLE_PROSUMER,
        /** The user is a broker (e.g., FOA), capable of sending FOs on behalf of one or more Prosumers         *
         */
        ROLE_BROKER,
        /** The user is admin, i.e., the aggregator who can configure and change everything, including Prosumer contracts, etc.
         */
        ROLE_ADMIN;

        public String getAuthority() {
                return name();
        }

}
