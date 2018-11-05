/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.db;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.nodesource.NodeSourceDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.NodeSourceStatus;


@Entity
@NamedQueries({ @NamedQuery(name = "deleteNodeSourceDataByName", query = "delete from NodeSourceData where name=:name"),
                @NamedQuery(name = "deleteAllNodeSourceData", query = "delete from NodeSourceData"),
                @NamedQuery(name = "getNodeSourceData", query = "from NodeSourceData"),
                @NamedQuery(name = "getNodeSourceDataByName", query = "from NodeSourceData where name=:name") })
@Table(name = "NodeSourceData")
public class NodeSourceData implements Serializable {

    private String name;

    private String infrastructureType;

    private List<Serializable> infrastructureParameters;

    private String policyType;

    private List<Serializable> policyParameters;

    private Client provider;

    private boolean nodesRecoverable;

    private NodeSourceStatus status;

    /**
     * name of the variable --> value of the variable
     */
    private Map<String, Serializable> infrastructureVariables;

    public NodeSourceData() {
    }

    public NodeSourceData(String name) {
        this.name = name;
    }

    public NodeSourceData(String nodeSourceName, String infrastructureType, List<Serializable> infrastructureParameters,
            String policyType, List<Serializable> policyParameters, Client provider, boolean nodesRecoverable,
            NodeSourceStatus status) {

        this.name = nodeSourceName;
        this.infrastructureType = infrastructureType;
        this.infrastructureParameters = infrastructureParameters;
        this.policyType = policyType;
        this.policyParameters = policyParameters;
        this.provider = provider;
        this.nodesRecoverable = nodesRecoverable;
        this.status = status;
        this.infrastructureVariables = new HashMap<>();
    }

    public static NodeSourceData fromNodeSourceDescriptor(NodeSourceDescriptor descriptor) {
        NodeSourceData nodeSourceData = new NodeSourceData(descriptor.getName(),
                                                           descriptor.getInfrastructureType(),
                                                           descriptor.getSerializableInfrastructureParameters(),
                                                           descriptor.getPolicyType(),
                                                           descriptor.getSerializablePolicyParameters(),
                                                           descriptor.getProvider(),
                                                           descriptor.nodesRecoverable(),
                                                           descriptor.getStatus());
        nodeSourceData.setInfrastructureVariables(descriptor.getLastRecoveredInfrastructureVariables());
        return nodeSourceData;
    }

    @Id
    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.MaterializedClobType")
    public String getInfrastructureType() {
        return infrastructureType;
    }

    public void setInfrastructureType(String infrastructureType) {
        this.infrastructureType = infrastructureType;
    }

    @Column(length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public List<Serializable> getInfrastructureParameters() {
        return infrastructureParameters;
    }

    public void setInfrastructureParameters(List<Serializable> infrastructureParameters) {
        this.infrastructureParameters = infrastructureParameters;
    }

    @Column(nullable = false)
    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    @Column(length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public List<Serializable> getPolicyParameters() {
        return policyParameters;
    }

    public void setPolicyParameters(List<Serializable> policyParameters) {
        this.policyParameters = policyParameters;
    }

    @Column(length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Client getProvider() {
        return provider;
    }

    public void setProvider(Client provider) {
        this.provider = provider;
    }

    @Column
    public boolean getNodesRecoverable() {
        return nodesRecoverable;
    }

    public void setNodesRecoverable(boolean nodesRecoverable) {
        this.nodesRecoverable = nodesRecoverable;
    }

    @Column
    public NodeSourceStatus getStatus() {
        return status;
    }

    public void setStatus(NodeSourceStatus status) {
        this.status = status;
    }

    @Column(length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Map<String, Serializable> getInfrastructureVariables() {
        return infrastructureVariables;
    }

    public void setInfrastructureVariables(Map<String, Serializable> infrastructureVariables) {
        this.infrastructureVariables = infrastructureVariables;
    }

    public NodeSourceDescriptor toNodeSourceDescriptor() {
        return new NodeSourceDescriptor.Builder().name(this.name)
                                                 .infrastructureType(this.infrastructureType)
                                                 .infrastructureParameters(this.infrastructureParameters)
                                                 .policyType(this.policyType)
                                                 .policyParameters(this.policyParameters)
                                                 .provider(this.provider)
                                                 .nodesRecoverable(this.nodesRecoverable)
                                                 .status(this.status)
                                                 .lastRecoveredInfrastructureVariables(this.infrastructureVariables)
                                                 .build();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (obj instanceof NodeSourceData) {
            NodeSourceData other = (NodeSourceData) obj;
            return other.getName().equals(name);
        } else {
            return false;
        }
    }

}
