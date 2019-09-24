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
package org.ow2.proactive.resourcemanager.frontend;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;


/**
 *
 * A descriptor of pluggable policies and infrastructure manages.
 * Used to dynamically obtain a meta information about the service
 * without having a direct link to the service.
 *
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "pluginDescriptor")
public class PluginDescriptorData implements Serializable {

    // for Jackson de-serialization purpose
    public PluginDescriptorData() {
    }

    public PluginDescriptorData(PluginDescriptor pluginDescriptor) {
        defaultValues = pluginDescriptor.getDefaultValues();
        meta = pluginDescriptor.getMeta();
        pluginDescription = pluginDescriptor.getPluginDescription();
        pluginName = pluginDescriptor.getPluginName();
        sectionDescriptions = pluginDescriptor.getSectionDescriptions();
        configurableFields = pluginDescriptor.getConfigurableFields()
                                             .stream()
                                             .map(ConfigurableFieldData::new)
                                             .collect(Collectors.toList());
    }

    private String pluginName;

    private String pluginDescription;

    private Collection<ConfigurableFieldData> configurableFields = new LinkedList<>();

    private Map<String, String> defaultValues;

    private Map<Integer, String> sectionDescriptions = new HashMap<>();

    private Map<String, String> meta = new HashMap<>();

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginDescription() {
        return pluginDescription;
    }

    public void setPluginDescription(String pluginDescription) {
        this.pluginDescription = pluginDescription;
    }

    public Collection<ConfigurableFieldData> getConfigurableFields() {
        return configurableFields;
    }

    public void setConfigurableFields(Collection<ConfigurableFieldData> configurableFields) {
        this.configurableFields = configurableFields;
    }

    public Map<String, String> getDefaultValues() {
        return defaultValues;
    }

    public void setDefaultValues(Map<String, String> defaultValues) {
        this.defaultValues = defaultValues;
    }

    public Map<Integer, String> getSectionDescriptions() {
        return sectionDescriptions;
    }

    public void setSectionDescriptions(Map<Integer, String> sectionDescriptions) {
        this.sectionDescriptions = sectionDescriptions;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
    }
}
