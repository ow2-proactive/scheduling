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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 *
 * Encapsulation of field name, value and its meta data.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ConfigurableField implements Serializable {
    private String name;

    private String value;

    @XmlJavaTypeAdapter(ConfigurableAdapter.class)
    private Configurable meta;

    public ConfigurableField(String name, String value, Configurable configurable) {
        this.name = name;
        this.value = value;
        this.meta = configurable;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Configurable getMeta() {
        return meta;
    }
}
