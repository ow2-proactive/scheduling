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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.ow2.proactive.resourcemanager.nodesource.NodeSourcePlugin;
import org.ow2.proactive.resourcemanager.nodesource.PluginNotFoundException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.common.ConfigurableField;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.resourcemanager.utils.AddonClassUtils;
import org.ow2.proactive.utils.Lambda;


public class NodeSourceParameterHelper {

    public Collection<ConfigurableField> getPluginConfigurableFields(String pluginClassName)
            throws PluginNotFoundException {

        Class<NodeSourcePlugin> pluginClass = this.getPluginClassOrFail(pluginClassName);
        PluginDescriptor policyPluginDescriptor = new PluginDescriptor(pluginClass,
                                                                       AddonClassUtils.instantiateAddon(pluginClass),
                                                                       new HashMap<>());

        return policyPluginDescriptor.getConfigurableFields();
    }

    public List<Serializable> getParametersWithDynamicParametersUpdatedOnly(
            Collection<ConfigurableField> configurableFields, Object[] newParameters,
            List<Serializable> oldParameters) {

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

    public Collection<PluginDescriptor> getPluginsDescriptor(Collection<Class<?>> plugins) {
        return plugins.stream()
                      .map(cls -> new PluginDescriptor(cls, AddonClassUtils.instantiateAddon(cls), new HashMap<>()))
                      .collect(Collectors.toList());
    }

    public PluginDescriptor getPluginDescriptor(String pluginClassName, Object[] parameters, String nodeSourceName) {

        Class<NodeSourcePlugin> pluginClass;

        try {
            pluginClass = this.getPluginClassOrFail(pluginClassName);
        } catch (PluginNotFoundException e) {
            throw new IllegalStateException(e.getMessageWithContext(nodeSourceName), e);
        }

        return new PluginDescriptor(pluginClass, AddonClassUtils.instantiateAddon(pluginClass), parameters);
    }

    private String getStringValue(Object[] newParameters, int index, Configurable meta) {

        String newValue;

        if (meta.credential() || meta.fileBrowser() || meta.password()) {
            newValue = new String((byte[]) newParameters[index]);
        } else {
            newValue = String.valueOf(newParameters[index]);
        }

        return newValue;
    }

    private void updateDynamicParameterIfNotEqual(List<Serializable> mergedParameters, String newValue, String oldValue,
            int valueIndex, ConfigurableField configurableField) {

        String cleanNewValue = newValue.trim();
        String cleanOldValue = oldValue.trim();

        if (configurableField.getMeta().dynamic() && !cleanNewValue.equals(cleanOldValue)) {
            mergedParameters.set(valueIndex, cleanNewValue);
        }
    }

    private Class<NodeSourcePlugin> getPluginClassOrFail(String pluginClassName) throws PluginNotFoundException {

        Class<NodeSourcePlugin> pluginClass;

        try {
            ClassLoader currentClassLoader = this.getClass().getClassLoader();
            pluginClass = (Class<NodeSourcePlugin>) AddonClassUtils.loadClass(pluginClassName, currentClassLoader);
        } catch (ClassNotFoundException e) {
            throw new PluginNotFoundException(pluginClassName, e);
        }

        return pluginClass;
    }

}
