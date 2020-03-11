package org.goflex.wp2.app.fmanintegration.listener.event;

import org.goflex.wp2.core.models.UserT;

public class UserCreatedEvent {

    private UserT user;

    private String organizationName;

    private String password;

    public UserCreatedEvent(UserT user, String organizationName, String password) {
        this.user = user;
        this.organizationName = organizationName;
        this.password = password;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public UserT getUser() {
        return user;
    }

    public void setUser(UserT user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
