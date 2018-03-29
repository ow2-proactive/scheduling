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
import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Object holding the current configuration of a node source.
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "nodeSourceConfiguration")
public class NodeSourceConfiguration implements Serializable {

    private String nodeSourceName;

    private boolean nodesRecoverable;

    private PluginDescriptor infrastructurePluginDescriptor;

    private PluginDescriptor policyPluginDescriptor;

    public NodeSourceConfiguration(String nodeSourceName, boolean nodesRecoverable,
            PluginDescriptor infrastructurePluginDescriptor, PluginDescriptor policyPluginDescriptor) {
        this.nodeSourceName = nodeSourceName;
        this.nodesRecoverable = nodesRecoverable;
        this.infrastructurePluginDescriptor = infrastructurePluginDescriptor;
        this.policyPluginDescriptor = policyPluginDescriptor;
    }

    public String getNodeSourceName() {
        return this.nodeSourceName;
    }

    public boolean getNodesRecoverable() {
        return this.nodesRecoverable;
    }

    public PluginDescriptor getInfrastructurePluginDescriptor() {
        return this.infrastructurePluginDescriptor;
    }

    public PluginDescriptor getPolicyPluginDescriptor() {
        return this.policyPluginDescriptor;
    }

}
