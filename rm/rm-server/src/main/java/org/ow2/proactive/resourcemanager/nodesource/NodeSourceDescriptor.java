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
import java.util.*;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.core.NodeSourceParameterHelper;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;


/**
 *
 */
@PublicAPI
public class NodeSourceDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;

    private final String infrastructureType;

    private final String policyType;

    private final Client provider;

    private final boolean nodesRecoverable;

    private List<Serializable> infrastructureParameters;

    private List<Serializable> policyParameters;

    private NodeSourceStatus status;

    private LinkedHashMap<String, String> additionalInformation;

    private Map<String, Serializable> lastRecoveredInfrastructureVariables;

    private transient PluginDescriptor infrastructurePluginDescriptor;

    private transient PluginDescriptor policyPluginDescriptor;

    private NodeSourceDescriptor(String name, String infrastructureType, List<Serializable> infrastructureParameters,
            String policyType, List<Serializable> policyParameters, Client provider, boolean nodesRecoverable,
            NodeSourceStatus status, LinkedHashMap<String, String> additionalInformation) {
        this.name = name;
        this.infrastructureType = infrastructureType;
        this.infrastructureParameters = infrastructureParameters;
        this.policyType = policyType;
        this.policyParameters = policyParameters;
        this.provider = provider;
        this.nodesRecoverable = nodesRecoverable;
        this.status = status;
        this.additionalInformation = Optional.ofNullable(additionalInformation).orElse(new LinkedHashMap<>());
    }

    /**
     * The name of this Node Source
     * @return node source name
     */
    public String getName() {
        return this.name;
    }

    /**
     * The class implementation of this Node Source's infrastructure
     * @return fully qualified class name of the infrastructure class
     */
    public String getInfrastructureType() {
        return this.infrastructureType;
    }

    /**
     * The infrastructure parameters as a list of serializable objects
     * @return list of parameters
     */
    public List<Serializable> getSerializableInfrastructureParameters() {
        return this.infrastructureParameters;
    }

    /**
     * The infrastructure parameters as an array
     * @return array of parameters
     */
    public Object[] getInfrastructureParameters() {
        if (this.infrastructureParameters != null) {
            return this.infrastructureParameters.toArray();
        } else {
            return null;
        }
    }

    /**
     * The infrastructure parameters as a map
     * @return a map where keys are parameter names, and values are parameter values
     */
    public Map<String, String> getInfrastructureNamedParameters() {
        PluginDescriptor infrastructurePluginDescriptor = getInfrastructurePluginDescriptor();
        Map<String, String> answer = new LinkedHashMap<>(infrastructureParameters.size());

        infrastructurePluginDescriptor.getConfigurableFields()
                                      .forEach(field -> answer.put(field.getName(), field.getValue()));
        return answer;
    }

    /**
     * Returns the value of a single infrastructure parameter
     * @param name infrastructure parameter name
     * @return infrastructure parameter value
     */
    public String getInfrastructureParameter(String name) {
        return getInfrastructureNamedParameters().get(name);
    }

    /**
     * The class implementation of this Node Source's policy
     * @return fully qualified class name of the policy class
     */
    public String getPolicyType() {
        return this.policyType;
    }

    /**
     * The policy parameters as a list of serializable objects
     * @return list of parameters
     */
    public List<Serializable> getSerializablePolicyParameters() {
        return this.policyParameters;
    }

    /**
     * The policy parameters as an array
     * @return array of parameters
     */
    public Object[] getPolicyParameters() {
        if (this.policyParameters != null) {
            return this.policyParameters.toArray();
        } else {
            return null;
        }
    }

    /**
     * The policy parameters as a map
     * @return a map where keys are parameter names, and values are parameter values
     */
    public Map<String, String> getPolicyNamedParameters() {
        PluginDescriptor policyPluginDescriptor = getPolicyPluginDescriptor();
        Map<String, String> answer = new LinkedHashMap<>(policyParameters.size());

        policyPluginDescriptor.getConfigurableFields().forEach(field -> answer.put(field.getName(), field.getValue()));
        return answer;
    }

    /**
     * Returns the value of a single policy parameter
     * @param name policy parameter name
     * @return policy parameter value
     */
    public String getPolicyParameter(String name) {
        return getPolicyNamedParameters().get(name);
    }

    /**
     * Returns the owner of this Node Source
     * @return node source owner
     */
    public Client getProvider() {
        return this.provider;
    }

    /**
     * Are the nodes in this Node Source recoverable (e.g. stored in the database, and may be recovered in case of a server crash)
     * @return recoverable configuration
     */
    public boolean nodesRecoverable() {
        return this.nodesRecoverable;
    }

    /**
     * Returns this node source current status (deployed or un-deployed)
     * @return a status
     */
    public NodeSourceStatus getStatus() {
        return this.status;
    }

    /**
     * Returns additional information attached to this Node Source.
     * This is used only by certain Node Source types (for example an Azure Node Source with billing monitoring  enabled)
     * @return map of additional information
     */
    public LinkedHashMap<String, String> getAdditionalInformation() {
        return this.additionalInformation;
    }

    /**
     * Only used internally
     */
    public Map<String, Serializable> getLastRecoveredInfrastructureVariables() {
        return this.lastRecoveredInfrastructureVariables;
    }

    public void setStatus(NodeSourceStatus status) {
        this.status = status;
    }

    public void setInfrastructureParameters(List<Serializable> infrastructureParameters) {
        this.infrastructureParameters = infrastructureParameters;
    }

    public void setPolicyParameters(List<Serializable> policyParameters) {
        this.policyParameters = policyParameters;
    }

    /**
     * Returns the infrastructure descriptor, which contains the definition of the infrastructure parameters (name, type, description, etc)
     * @return infrastructure descriptor
     */
    public PluginDescriptor getInfrastructurePluginDescriptor() {
        if (infrastructurePluginDescriptor == null) {
            NodeSourceParameterHelper nodeSourceParameterHelper = new NodeSourceParameterHelper();
            infrastructurePluginDescriptor = nodeSourceParameterHelper.getPluginDescriptor(infrastructureType,
                                                                                           getInfrastructureParameters(),
                                                                                           name);
        }
        return infrastructurePluginDescriptor;
    }

    /**
     * Returns the policy descriptor, which contains the definition of the policy parameters (name, type, description, etc)
     * @return policy descriptor
     */
    public PluginDescriptor getPolicyPluginDescriptor() {
        if (policyPluginDescriptor == null) {
            NodeSourceParameterHelper nodeSourceParameterHelper = new NodeSourceParameterHelper();
            policyPluginDescriptor = nodeSourceParameterHelper.getPluginDescriptor(policyType,
                                                                                   getPolicyParameters(),
                                                                                   name);
        }
        return policyPluginDescriptor;
    }

    public static class Builder {

        private String name;

        private String infrastructureType;

        private List<Serializable> infrastructureParameters;

        private String policyType;

        private List<Serializable> policyParameters;

        private Client provider;

        private boolean nodesRecoverable;

        private NodeSourceStatus status;

        private LinkedHashMap<String, String> additionalInformation;

        private Map<String, Serializable> lastRecoveredInfrastructureVariables;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder infrastructureType(String infrastructureType) {
            this.infrastructureType = infrastructureType;
            return this;
        }

        public Builder infrastructureParameters(List<Serializable> infrastructureParameters) {
            this.infrastructureParameters = infrastructureParameters;
            return this;
        }

        public Builder policyType(String policyType) {
            this.policyType = policyType;
            return this;
        }

        public Builder policyParameters(List<Serializable> policyParameters) {
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

        public Builder additionalInformation(LinkedHashMap<String, String> additionalInformation) {
            this.additionalInformation = additionalInformation;
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
                                                                  this.status,
                                                                  this.additionalInformation);
            built.lastRecoveredInfrastructureVariables = this.lastRecoveredInfrastructureVariables;
            return built;
        }

    }

}
