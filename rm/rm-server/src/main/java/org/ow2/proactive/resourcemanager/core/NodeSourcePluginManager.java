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
package org.ow2.proactive.resourcemanager.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.resourcemanager.nodesource.NodeSourceDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.common.ConfigurableField;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;


public class NodeSourcePluginManager {

    public Collection<ConfigurableField> getPolicyConfigurableFields(String nodeSourceName,
            NodeSourceDescriptor descriptor) {

        Class<NodeSourcePolicy> nodeSourcePolicyClass = this.getNodeSourcePolicyClassOrFail(nodeSourceName, descriptor);
        PluginDescriptor policyPluginDescriptor = new PluginDescriptor(nodeSourcePolicyClass, new HashMap<>());

        return policyPluginDescriptor.getConfigurableFields();
    }

    public Collection<ConfigurableField> getInfrastructureConfigurableFields(String nodeSourceName,
            NodeSourceDescriptor descriptor) {

        Class<InfrastructureManager> infrastructureManagerClass = this.getInfrastructureManagerClassOrFail(nodeSourceName,
                                                                                                           descriptor);
        PluginDescriptor infrastructurePluginDescriptor = new PluginDescriptor(infrastructureManagerClass,
                                                                               new HashMap<>());

        return infrastructurePluginDescriptor.getConfigurableFields();
    }

    public List<Serializable> getParametersWithDynamicUpdated(Object[] newParameters, List<Serializable> oldParameters,
            Collection<ConfigurableField> configurableFields) {

        List<Serializable> parametersWithDynamicUpdated = new LinkedList<>();
        parametersWithDynamicUpdated.addAll(oldParameters);

        int oldValueIndex;
        String newValue;
        String oldValue;

        for (int newValueIndex = 0; newValueIndex < newParameters.length; newValueIndex++) {

            oldValueIndex = 0;

            for (ConfigurableField configurableField : configurableFields) {

                // we know if two parameters are comparable thanks to the
                // order in which they appear in the parameter list
                if (oldValueIndex == newValueIndex) {

                    if (configurableField.getMeta().credential() || configurableField.getMeta().fileBrowser() ||
                        configurableField.getMeta().password()) {
                        newValue = new String((byte[]) newParameters[newValueIndex]);
                        oldValue = new String((byte[]) oldParameters.get(newValueIndex));
                    } else {
                        newValue = (String) newParameters[newValueIndex];
                        oldValue = (String) oldParameters.get(newValueIndex);
                    }

                    this.updateDynamicParameterIfNotEqual(newParameters,
                                                          parametersWithDynamicUpdated,
                                                          newValue,
                                                          oldValue,
                                                          newValueIndex,
                                                          configurableField);
                }

                oldValueIndex++;
            }
        }

        return parametersWithDynamicUpdated;
    }

    public Collection<PluginDescriptor> getPluginsDescriptor(Collection<Class<?>> plugins) {
        Collection<PluginDescriptor> descriptors = new ArrayList<>(plugins.size());
        for (Class<?> cls : plugins) {
            Map<String, String> defaultValues = new HashMap<>();
            descriptors.add(new PluginDescriptor(cls, defaultValues));
        }
        return descriptors;
    }

    public PluginDescriptor getPolicyPluginDescriptor(String nodeSourceName,
            NodeSourceDescriptor nodeSourceDescriptor) {
        Class<NodeSourcePolicy> policyClass = this.getNodeSourcePolicyClassOrFail(nodeSourceName, nodeSourceDescriptor);

        return new PluginDescriptor(policyClass, nodeSourceDescriptor.getPolicyParameters());
    }

    public PluginDescriptor getInfrastructurePluginDescriptor(String nodeSourceName,
            NodeSourceDescriptor nodeSourceDescriptor) {
        Class<InfrastructureManager> infrastructureClass = this.getInfrastructureManagerClassOrFail(nodeSourceName,
                                                                                                    nodeSourceDescriptor);

        return new PluginDescriptor(infrastructureClass, nodeSourceDescriptor.getInfrastructureParameters());
    }

    private void updateDynamicParameterIfNotEqual(Object[] newParameters,
            List<Serializable> parametersWithDynamicUpdated, String newValue, String oldValue, int valueIndex,
            ConfigurableField configurableField) {

        if (!newValue.equals(oldValue)) {

            if (configurableField.getMeta().dynamic()) {
                parametersWithDynamicUpdated.set(valueIndex, (Serializable) newParameters[valueIndex]);
            } else {
                throw new IllegalArgumentException("Attempt to update parameter " + configurableField.getName() +
                                                   " failed because this parameter is not dynamic");
            }
        }
    }

    private Class<InfrastructureManager> getInfrastructureManagerClassOrFail(String nodeSourceName,
            NodeSourceDescriptor nodeSourceDescriptor) {
        Class<InfrastructureManager> infrastructureClass;
        try {
            infrastructureClass = (Class<InfrastructureManager>) Class.forName(nodeSourceDescriptor.getInfrastructureType());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Infrastructure class of node source " + nodeSourceName + " cannot be read",
                                            e);
        }
        return infrastructureClass;
    }

    private Class<NodeSourcePolicy> getNodeSourcePolicyClassOrFail(String nodeSourceName,
            NodeSourceDescriptor nodeSourceDescriptor) {
        Class<NodeSourcePolicy> policyClass;
        try {
            policyClass = (Class<NodeSourcePolicy>) Class.forName(nodeSourceDescriptor.getPolicyType());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Policy class of node source " + nodeSourceName + " cannot be read", e);
        }
        return policyClass;
    }

}
