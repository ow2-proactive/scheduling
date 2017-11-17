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
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;


@Entity
@NamedQueries({ @NamedQuery(name = "deleteNodeSourceDataByName", query = "delete from NodeSourceData where name=:name"),
                @NamedQuery(name = "deleteAllNodeSourceData", query = "delete from NodeSourceData"),
                @NamedQuery(name = "getNodeSourceData", query = "from NodeSourceData"),
                @NamedQuery(name = "getNodeSourceDataByName", query = "from NodeSourceData where name=:name") })
@Table(name = "NodeSourceData")
public class NodeSourceData implements Serializable {

    private String name;

    private String infrastructureType;

    private Object[] infrastructureParameters;

    private String policyType;

    private Object[] policyParameters;

    private Client provider;

    /**
     * name of the variable --> value of the variable
     */
    private Map<String, Serializable> infrastructureVariables;

    public NodeSourceData() {
    }

    public NodeSourceData(String nodeSourceName, String infrastructureType, Object[] infrastructureParameters,
            String policyType, Object[] policyParameters, Client provider) {

        this.name = nodeSourceName;
        this.infrastructureType = infrastructureType;
        this.infrastructureParameters = infrastructureParameters;
        this.policyType = policyType;
        this.policyParameters = policyParameters;
        this.provider = provider;
        this.infrastructureVariables = new HashMap<>();
    }

    @Id
    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(length = 64984)
    public String getInfrastructureType() {
        return infrastructureType;
    }

    public void setInfrastructureType(String infrastructureType) {
        this.infrastructureType = infrastructureType;
    }

    @Column(length = Integer.MAX_VALUE)
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

    @Column(length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Object[] getPolicyParameters() {
        return policyParameters;
    }

    public void setPolicyParameters(Object[] policyParameters) {
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

    @Column(length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Map<String, Serializable> getInfrastructureVariables() {
        return infrastructureVariables;
    }

    public void setInfrastructureVariables(Map<String, Serializable> infrastructureVariables) {
        this.infrastructureVariables = infrastructureVariables;
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
