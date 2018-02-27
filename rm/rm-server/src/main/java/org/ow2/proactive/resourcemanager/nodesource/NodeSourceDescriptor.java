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
public class NodeSourceDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private String infrastructureType;

    private Object[] infrastructureParameters;

    private String policyType;

    private Object[] policyParameters;

    private Client provider;

    private boolean nodesRecoverable;

    private NodeSourceStatus status;

    private Map<String, Serializable> lastRecoveredInfrastructureVariables;

    private NodeSourceDescriptor(String name, String infrastructureType, Object[] infrastructureParameters,
            String policyType, Object[] policyParameters, Client provider, boolean nodesRecoverable,
            NodeSourceStatus status) {
        this.name = name;
        this.infrastructureType = infrastructureType;
        this.infrastructureParameters = infrastructureParameters;
        this.policyType = policyType;
        this.policyParameters = policyParameters;
        this.provider = provider;
        this.nodesRecoverable = nodesRecoverable;
        this.status = status;
    }

    public String getName() {
        return this.name;
    }

    public String getInfrastructureType() {
        return this.infrastructureType;
    }

    public Object[] getInfrastructureParameters() {
        return this.infrastructureParameters;
    }

    public String getPolicyType() {
        return this.policyType;
    }

    public Object[] getPolicyParameters() {
        return this.policyParameters;
    }

    public Client getProvider() {
        return this.provider;
    }

    public boolean nodesRecoverable() {
        return this.nodesRecoverable;
    }

    public NodeSourceStatus getStatus() {
        return this.status;
    }

    public Map<String, Serializable> getLastRecoveredInfrastructureVariables() {
        return this.lastRecoveredInfrastructureVariables;
    }

    public void setStatus(NodeSourceStatus status) {
        this.status = status;
    }

    private void
            setLastRecoveredInfrastructureVariables(Map<String, Serializable> lastRecoveredInfrastructureVariables) {
        this.lastRecoveredInfrastructureVariables = lastRecoveredInfrastructureVariables;
    }

    public static class Builder {

        private String name;

        private String infrastructureType;

        private Object[] infrastructureParameters;

        private String policyType;

        private Object[] policyParameters;

        private Client provider;

        private boolean nodesRecoverable;

        private NodeSourceStatus status;

        private Map<String, Serializable> lastRecoveredInfrastructureVariables;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder infrastructureType(String infrastructureType) {
            this.infrastructureType = infrastructureType;
            return this;
        }

        public Builder infrastructureParameters(Object[] infrastructureParameters) {
            this.infrastructureParameters = infrastructureParameters;
            return this;
        }

        public Builder policyType(String policyType) {
            this.policyType = policyType;
            return this;
        }

        public Builder policyParameters(Object[] policyParameters) {
            this.policyParameters = policyParameters;
            return this;
        }

        public Builder provider(Client provider) {
            this.provider = provider;
            return this;
        }

        public Builder nodesRecoverable(boolean nodesRecoverable) {
            this.nodesRecoverable = nodesRecoverable;
            return this;
        }

        public Builder status(NodeSourceStatus status) {
            this.status = status;
            return this;
        }

        public Builder
                lastRecoveredInfrastructureVariables(Map<String, Serializable> lastRecoveredInfrastructureVariables) {
            this.lastRecoveredInfrastructureVariables = lastRecoveredInfrastructureVariables;
            return this;
        }

        public NodeSourceDescriptor build() {
            NodeSourceDescriptor built = new NodeSourceDescriptor(this.name,
                                                                  this.infrastructureType,
                                                                  this.infrastructureParameters,
                                                                  this.policyType,
                                                                  this.policyParameters,
                                                                  this.provider,
                                                                  this.nodesRecoverable,
                                                                  this.status);
            built.setLastRecoveredInfrastructureVariables(this.lastRecoveredInfrastructureVariables);
            return built;
        }

    }

}
