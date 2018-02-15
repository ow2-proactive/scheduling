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
package org.ow2.proactive.resourcemanager.nodesource;

import java.io.Serializable;
import java.util.Map;

import org.ow2.proactive.resourcemanager.authentication.Client;


/**
 *
 */
public class NodeSourceDescriptor {

    private String name;

    private String infrastructureType;

    private Object[] infrastructureParameters;

    private String policyType;

    private Object[] policyParameters;

    private Client provider;

    private String description;

    private boolean nodesRecoverable;

    private NodeSourceStatus status;

    private Map<String, Serializable> lastRecoveredInfrastructureVariables;

    public NodeSourceDescriptor(String name, String infrastructureType, Object[] infrastructureParameters,
            String policyType, Object[] policyParameters, Client provider, String description, boolean nodesRecoverable,
            NodeSourceStatus status, Map<String, Serializable> lastRecoveredInfrastructureVariables) {
        this.name = name;
        this.infrastructureType = infrastructureType;
        this.infrastructureParameters = infrastructureParameters;
        this.policyType = policyType;
        this.policyParameters = policyParameters;
        this.provider = provider;
        this.description = description;
        this.nodesRecoverable = nodesRecoverable;
        this.status = status;
        this.lastRecoveredInfrastructureVariables = lastRecoveredInfrastructureVariables;
    }

    public String getName() {
        return name;
    }

    public String getInfrastructureType() {
        return infrastructureType;
    }

    public Object[] getInfrastructureParameters() {
        return infrastructureParameters;
    }

    public String getPolicyType() {
        return policyType;
    }

    public Object[] getPolicyParameters() {
        return policyParameters;
    }

    public Client getProvider() {
        return provider;
    }

    public String getDescription() {
        return description;
    }

    public boolean isNodesRecoverable() {
        return nodesRecoverable;
    }

    public NodeSourceStatus getStatus() {
        return status;
    }

    public void setStatus(NodeSourceStatus status) {
        this.status = status;
    }

    public Map<String, Serializable> getLastRecoveredInfrastructureVariables() {
        return lastRecoveredInfrastructureVariables;
    }

}
