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
package org.objectweb.proactive.ic2d.chartit.data;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;
import org.objectweb.proactive.ic2d.chartit.data.provider.ProviderDescriptor;
import org.objectweb.proactive.ic2d.chartit.data.store.Rrd4jDataStore;


/**
 * Contains a set of static methods for creating <code>ResourceDta</code> and
 * related instances.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class ResourceDataBuilder {

    public static final String DEFAULT_RESSOURCE_NAME = "LocalRuntime";

    /**
     * Returns a new instance of <code>ResourceData</code> class for local
     * runtime.
     * 
     * @return An instance of <code>ResourceData</code>
     */
    public static ResourceData buildResourceDataForLocalRuntime() {
        final IResourceDescriptor resourceDescriptor = new IResourceDescriptor() {

            public String getHostUrlServer() {
                return "localhost";
            }

            public MBeanServerConnection getMBeanServerConnection() {
                return ManagementFactory.getPlatformMBeanServer();
            }

            public String getName() {
                return DEFAULT_RESSOURCE_NAME;
            }

            public ObjectName getObjectName() {
                try {
                    return new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME);
                } catch (MalformedObjectNameException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                return null;
            }

        };
        final Rrd4jDataStore dataStore = new Rrd4jDataStore(DEFAULT_RESSOURCE_NAME);
        final ChartModelContainer modelsCollector = new ChartModelContainer(dataStore);
        final ResourceData resourceData = new ResourceData(resourceDescriptor, modelsCollector);
        return resourceData;
    }

    /**
     * Returns a new instance of <code>ResourceData</code> class builded from
     * a resource descriptor.
     * 
     * @param resourceDescriptor
     *            The resource descriptor
     * @return An instance of <code>ResourceData</code>
     */
    public static ResourceData buildResourceDataFromDescriptor(final IResourceDescriptor resourceDescriptor) {
        final Rrd4jDataStore dataStore = new Rrd4jDataStore(resourceDescriptor.getName());
        final ChartModelContainer modelsCollector = new ChartModelContainer(dataStore);
        final ResourceData resourceData = new ResourceData(resourceDescriptor, modelsCollector);
        return resourceData;
    }

    /**
     * Invoke by reflection the static <code>build</code> method that must be
     * defined for each predefined provider.
     * 
     * @param name
     *            The attribute name used to build a predefined provider
     * @param mBeanServerConnection
     *            The connection to the remote MBean server
     * @return An instance of <code>IDataProvider</code>
     */
    public static IDataProvider buildProviderFromName(final String name,
            final MBeanServerConnection mBeanServerConnection) {
        try {
            for (final ProviderDescriptor p : ProviderDescriptor.values()) {
                if (p.getName().equals(name)) {
                    final Method buildMethod = p.getClazz().getMethod("build",
                            new Class<?>[] { MBeanServerConnection.class });
                    // no instance required for static method
                    return p.getClazz()
                            .cast(buildMethod.invoke(null, new Object[] { mBeanServerConnection }));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}