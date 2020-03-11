package org.goflex.wp2.foa.events;

import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.models.Organization;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * @author muhaftab
 * created: 9/2/19
 */
public class FlexOfferUpdatedEvent extends ApplicationEvent {
    private String eventName;
    private String securityToken;
    private List<FlexOffer> flexOffers;
    private Organization organization;

    public FlexOfferUpdatedEvent(Object source, String eventName, String securityToken, List<FlexOffer> flexOffers, Organization organization) {
        super(source);
        this.eventName = eventName;
        this.securityToken = securityToken;
        this.flexOffers = flexOffers;
        this.organization = organization;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public List<FlexOffer> getFlexOffers() {
        return flexOffers;
    }

    public void setFlexOffers(List<FlexOffer> flexOffers) {
        this.flexOffers = flexOffers;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
