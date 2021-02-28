package org.goflex.wp2.fmanproxy.fmaninstance;

/**
 * @author muhaftab
 * created: 01/11/18
 */
public enum InstanceStatus {

    /**
     * Reqest has been received from FOA to create a new FMAN instance
     */
    REQUESTED,

    /**
     * New FMAN instance created but not activated yet
     */
    CREATED,

    /**
     * FMAN instance is operational
     */
    ACTIVE,

    /**
     * FMAN instance is disabled by admin for some reason
     */
    DISABLED,

    /**
     * FMAN instance no longer exists
     */
    DELETED
}
