/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.chartit.data.resource;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;


/**
 * Describes a resource with several information like the host name etc...
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public interface IResourceDescriptor {

    /**
     * Returns the object name of the MBean associated to this resource
     * descriptor.
     * 
     * @return The object name of the MBean associated to this resource
     *         descriptor
     */
    public ObjectName getObjectName();

    /**
     * Returns the url of the host server.
     * 
     * @return The url of the host server
     */
    public String getHostUrlServer();

    /**
     * Returns the name of the described resource.
     * 
     * @return The name of the described resource
     */
    public String getName();

    /**
     * Returns the reference on a <code>MBean</code> server.
     * 
     * @return The reference on a <code>MBean</code> server
     */
    public MBeanServerConnection getMBeanServerConnection();

    /**
     * Returns an array of custom data providers. 
     * 
     * @return An array of custom data providers
     */
    public IDataProvider[] getCustomDataProviders();
}