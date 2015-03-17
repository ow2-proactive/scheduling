package org.ow2.proactive.resourcemanager.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.security.auth.Subject;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;
import org.ow2.proactive.resourcemanager.authentication.Client;


@Entity
@Table(name = "NodeSourceData")
public class NodeSourceData implements Serializable {

    private static final long serialVersionUID = 61L;

    private String name;
    private String infrastructureType;
    private Object[] infrastructureParameters;
    private String policyType;
    private Object[] policyParameters;
    private Client provider;

    public NodeSourceData() {
    }

    public NodeSourceData(String nodeSourceName, String infrastructureType,
            Object[] infrastructureParameters, String policyType, Object[] policyParameters, Client provider) {

        this.name = nodeSourceName;
        this.infrastructureType = infrastructureType;
        this.infrastructureParameters = infrastructureParameters;
        this.policyType = policyType;
        this.policyParameters = policyParameters;
        this.provider = provider;
    }

    @Id
    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public String getInfrastructureType() {
        return infrastructureType;
    }

    public void setInfrastructureType(String infrastructureType) {
        this.infrastructureType = infrastructureType;
    }

    @Column
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Object[] getInfrastructureParameters() {
        return infrastructureParameters;
    }

    public void setInfrastructureParameters(Object[] infrastructureParameters) {
        this.infrastructureParameters = infrastructureParameters;
    }

    @Column(nullable = false)
    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    @Column
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Object[] getPolicyParameters() {
        return policyParameters;
    }

    public void setPolicyParameters(Object[] policyParameters) {
        this.policyParameters = policyParameters;
    }

    @Column
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Client getProvider() {
        return provider;
    }

    public void setProvider(Client provider) {
        this.provider = provider;
    }
}
