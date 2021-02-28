package org.goflex.wp2.fmanproxy.fmaninstance;

import org.goflex.wp2.fmanproxy.user.UserRole;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @author muhaftab
 * created: 01/11/18
 */
@Entity
@Table(name = "fman_instance")
public class FmanInstanceT implements Serializable {

    private static final long serialVersionUID = 3968679635819650055L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long instanceId;

    @Column(unique = true)
    private String instanceName;

    @Column(unique = true)
    private String instanceUrl = null;

    @Column(unique = false)
    private Long brokerId;

    private UserRole authorizedRole;

    private InstanceStatus instanceStatus = InstanceStatus.REQUESTED;

    private Date registrationDate;

    private Date activationDate;

    private String jwtToken = null;

    public FmanInstanceT() {
    }

    public FmanInstanceT(String instanceName, String instanceUrl, Long brokerId, UserRole authorizedRole, InstanceStatus instanceStatus, Date registrationDate, Date activationDate) {
        this.instanceName = instanceName;
        this.instanceUrl = instanceUrl;
        this.brokerId = brokerId;
        this.authorizedRole = authorizedRole;
        this.instanceStatus = instanceStatus;
        this.registrationDate = registrationDate;
        this.activationDate = activationDate;
    }


    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getInstanceUrl() {
        return instanceUrl;
    }

    public void setInstanceUrl(String instanceUrl) {
        this.instanceUrl = instanceUrl;
    }

    public Long getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(Long brokerId) {
        this.brokerId = brokerId;
    }

    public UserRole getAuthorizedRole() {
        return authorizedRole;
    }

    public void setAuthorizedRole(UserRole authorizedRole) {
        this.authorizedRole = authorizedRole;
    }

    public InstanceStatus getInstanceStatus() {
        return instanceStatus;
    }

    public void setInstanceStatus(InstanceStatus instanceStatus) {
        this.instanceStatus = instanceStatus;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Date activationDate) {
        this.activationDate = activationDate;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FmanInstanceT that = (FmanInstanceT) o;
        return Objects.equals(instanceId, that.instanceId) &&
                Objects.equals(instanceName, that.instanceName) &&
                Objects.equals(instanceUrl, that.instanceUrl) &&
                Objects.equals(brokerId, that.brokerId) &&
                authorizedRole == that.authorizedRole &&
                instanceStatus == that.instanceStatus &&
                Objects.equals(registrationDate, that.registrationDate) &&
                Objects.equals(activationDate, that.activationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceId, instanceName, instanceUrl, brokerId, authorizedRole, instanceStatus, registrationDate, activationDate);
    }

    @Override
    public String toString() {
        return "FmanInstanceT{" +
                "instanceId=" + instanceId +
                ", instanceName='" + instanceName + '\'' +
                ", instanceUrl='" + instanceUrl + '\'' +
                ", brokerId=" + brokerId +
                ", authorizedRole=" + authorizedRole +
                ", instanceStatus=" + instanceStatus +
                ", registrationDate=" + registrationDate +
                ", activationDate=" + activationDate +
                '}';
    }
}
