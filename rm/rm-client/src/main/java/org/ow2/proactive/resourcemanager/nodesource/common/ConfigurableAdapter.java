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
package org.ow2.proactive.resourcemanager.nodesource.common;

import java.io.Serializable;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.ow2.proactive.resourcemanager.nodesource.common.ConfigurableAdapter.ConfigurableWrapper;


/**
 * Converts the @Configurable annotation into a String,
 * else JAXB / JACKSON won't serialize it 
 * 
 * 
 * @author mschnoor
 * @since ProActive Scheduling 3.1.1
 *
 */
public class ConfigurableAdapter extends XmlAdapter<ConfigurableWrapper, Configurable> {

    /**
     * Keep track of the possible values of the @Configurable annotation
     * 
     */
    public static enum ConfigurableValues {
        CREDENTIAL("credential"),
        PASSWORD("password"),
        FILEBROWSER("fileBrowser"),
        NONE("none");

        private String name;

        ConfigurableValues(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    /**
     * Actual serialized object
     *
     */
    public static class ConfigurableWrapper implements Serializable {
        public ConfigurableValues type;

        public String description;

        ConfigurableWrapper() {
        }

        public ConfigurableWrapper(ConfigurableValues type, String desc) {
            this.description = desc;
            this.type = type;
        }
    }

    @Override
    public ConfigurableWrapper marshal(Configurable arg0) throws Exception {
        if (arg0.credential())
            return new ConfigurableWrapper(ConfigurableValues.CREDENTIAL, arg0.description());
        else if (arg0.password())
            return new ConfigurableWrapper(ConfigurableValues.PASSWORD, arg0.description());
        else if (arg0.fileBrowser())
            return new ConfigurableWrapper(ConfigurableValues.FILEBROWSER, arg0.description());
        else
            return new ConfigurableWrapper(ConfigurableValues.NONE, arg0.description());

    }

    @Override
    public Configurable unmarshal(ConfigurableWrapper arg0) throws Exception {

        if (arg0 == null || arg0.type == null)
            return null;

        return (Configurable) Configurable.class.getMethod(arg0.type.toString()).getDefaultValue();
    }

}
