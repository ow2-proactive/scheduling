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
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.common.ConfigurableField;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.infrastructure.InfrastructureManager;
import org.ow2.proactive.resourcemanager.nodesource.policy.NodeSourcePolicy;
import org.ow2.proactive.utils.Lambda;


public class NodeSourceDescriptorManager {

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

        List<Serializable> mergedParameters = new LinkedList<>();
        mergedParameters.addAll(oldParameters);

        Lambda.forEachWithIndex(configurableFields, (configurableField, index) -> {

            Configurable meta = configurableField.getMeta();

            String newValue = getStringValue(newParameters, index, meta);
            String oldValue = getStringValue(oldParameters.toArray(), index, meta);

            this.updateDynamicParameterIfNotEqual(mergedParameters, newValue, oldValue, index, configurableField);
        });

        return mergedParameters;
    }

    private String getStringValue(Object[] newParameters, int index, Configurable meta) {

        String newValue;

        if (meta.credential() || meta.fileBrowser() || meta.password()) {
            newValue = new String((byte[]) newParameters[index]);
        } else {
            newValue = (String) newParameters[index];
        }

        return newValue;
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

    private void updateDynamicParameterIfNotEqual(List<Serializable> mergedParameters, String newValue, String oldValue,
            int valueIndex, ConfigurableField configurableField) {

        if (!newValue.equals(oldValue)) {

            if (configurableField.getMeta().dynamic()) {
                mergedParameters.set(valueIndex, newValue);
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
