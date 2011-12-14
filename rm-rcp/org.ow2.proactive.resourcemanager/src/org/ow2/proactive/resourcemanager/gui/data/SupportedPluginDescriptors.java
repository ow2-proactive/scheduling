/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.gui.data;

import java.util.Collection;

import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;

/**
 * Contains data needed to initialize 'Create Node Source' dialog:
 * <ul>
 * <li>supported node source infrastructures descriptors 
 * <li>supported node source policies descriptors
 * </ul> 
 * 
 * @author The ProActive Team
 *
 */
public class SupportedPluginDescriptors {

	private final Collection<PluginDescriptor> supportedNodeSourceInfrastructures;
	
	private final Collection<PluginDescriptor> supportedNodeSourcePolicies;

	public SupportedPluginDescriptors(
			Collection<PluginDescriptor> supportedNodeSourceInfrastructures,
			Collection<PluginDescriptor> supportedNodeSourcePolicies) {
		this.supportedNodeSourceInfrastructures = supportedNodeSourceInfrastructures;
		this.supportedNodeSourcePolicies = supportedNodeSourcePolicies;
	}

	public Collection<PluginDescriptor> getSupportedNodeSourceInfrastructures() {
		return supportedNodeSourceInfrastructures;
	}

	public Collection<PluginDescriptor> getSupportedNodeSourcePolicies() {
		return supportedNodeSourcePolicies;
	}
	
	
}
