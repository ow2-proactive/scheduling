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
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.ow2.proactive.resourcemanager.nodesource.NodeSourceDescriptor;
import org.ow2.proactive.resourcemanager.nodesource.Plugin;
import org.ow2.proactive.resourcemanager.nodesource.PluginNotFoundException;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.common.ConfigurableField;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;
import org.ow2.proactive.utils.Lambda;


public class NodeSourceParameterHelper {

    private String pluginClassName;

    private Collection<ConfigurableField> configurableFields;

    public NodeSourceParameterHelper() {
        this.pluginClassName = "";
        this.configurableFields = new LinkedList<>();
    }

    public NodeSourceParameterHelper(String pluginClassName) throws PluginNotFoundException {

        this.pluginClassName = pluginClassName;

        Class<Plugin> pluginClass = this.getPluginClassOrFail(this.pluginClassName);
        PluginDescriptor pluginDescriptor = new PluginDescriptor(pluginClass, new HashMap<>());
        this.configurableFields = pluginDescriptor.getConfigurableFields();
    }

    public Collection<ConfigurableField> getConfigurableFields() {
        return this.configurableFields;
    }

    public List<Serializable> getParametersWithDynamicParametersUpdatedOnly(Object[] newParameters,
            List<Serializable> oldParameters) {

        List<Serializable> mergedParameters = new LinkedList<>();
        mergedParameters.addAll(oldParameters);

        Lambda.forEachWithIndex(this.configurableFields, (configurableField, index) -> {

            Configurable meta = configurableField.getMeta();

            String newValue = getStringValue(newParameters, index, meta);
            String oldValue = getStringValue(oldParameters.toArray(), index, meta);

            this.updateDynamicParameterIfNotEqual(mergedParameters, newValue, oldValue, index, configurableField);
        });

        return mergedParameters;
    }

    public void setUpdatedDynamicParameters(Plugin plugin, Object[] parameters) {

        Lambda.forEachWithIndex(this.configurableFields, (configurableField, index) -> {
            if (configurableField.getMeta().dynamic()) {

                String configurableFieldName = configurableField.getName();
                try {
                    Field field = plugin.getClass().getDeclaredField(configurableFieldName);
                    field.setAccessible(true);
                    field.set(plugin, parameters[index]);

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new IllegalArgumentException("Cannot apply new value to updated parameter " +
                                                       configurableFieldName);
                }
            }
        });
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
        return plugins.stream().map(cls -> new PluginDescriptor(cls, new HashMap<>())).collect(Collectors.toList());
    }

    public PluginDescriptor getPolicyPluginDescriptor(String nodeSourceName,
            NodeSourceDescriptor nodeSourceDescriptor) {

        Class<Plugin> policyClass;

        try {
            policyClass = this.getPluginClassOrFail(nodeSourceDescriptor.getPolicyType());
        } catch (PluginNotFoundException e) {
            throw new IllegalStateException(e.getMessageWithContext(nodeSourceName), e);
        }

        return new PluginDescriptor(policyClass, nodeSourceDescriptor.getPolicyParameters());
    }

    public PluginDescriptor getPluginDescriptor(String pluginClassName, Object[] parameters, String nodeSourceName) {

        Class<Plugin> pluginClass;

        try {
            pluginClass = this.getPluginClassOrFail(pluginClassName);
        } catch (PluginNotFoundException e) {
            throw new IllegalStateException(e.getMessageWithContext(nodeSourceName), e);
        }

        return new PluginDescriptor(pluginClass, parameters);
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

    private Class<Plugin> getPluginClassOrFail(String pluginClassName) throws PluginNotFoundException {

        Class<Plugin> pluginClass;

        try {
            pluginClass = (Class<Plugin>) Class.forName(pluginClassName);
        } catch (ClassNotFoundException e) {
            throw new PluginNotFoundException(pluginClassName, e);
        }

        return pluginClass;
    }

}
