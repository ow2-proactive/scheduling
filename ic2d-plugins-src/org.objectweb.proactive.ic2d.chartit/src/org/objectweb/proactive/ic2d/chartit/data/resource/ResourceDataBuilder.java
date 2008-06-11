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

import java.lang.management.ManagementFactory;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.objectweb.proactive.ic2d.chartit.data.ChartModelContainer;
import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;
import org.objectweb.proactive.ic2d.chartit.data.provider.predefined.FloatArrayDataProvider;
import org.objectweb.proactive.ic2d.chartit.data.provider.predefined.LoadedClassCountDataProvider;
import org.objectweb.proactive.ic2d.chartit.data.provider.predefined.StringArrayDataProvider;
import org.objectweb.proactive.ic2d.chartit.data.provider.predefined.ThreadCountDataProvider;
import org.objectweb.proactive.ic2d.chartit.data.provider.predefined.UsedHeapMemoryDataProvider;
import org.objectweb.proactive.ic2d.chartit.data.store.Rrd4jDataStore;


/**
 * Contains a set of static methods for creating <code>ResourceData</code> and
 * related instances.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class ResourceDataBuilder {

    /**
     * The default resource name
     */
    public static final String DEFAULT_RESOURCE_NAME = "LocalRuntime";

    /**
     * Creates a new instance of <code>IResourceDescriptor</code> class for local
     * runtime.
     * 
     * @return An instance of <code>IResourceDescriptor</code>
     */
    public static IResourceDescriptor createResourceDescriptorForLocalRuntime() {
        return new IResourceDescriptor() {

            /** Some custom data providers */
            final IDataProvider[] customDataProviders = new IDataProvider[] { new ThreadCountDataProvider(),
                    new LoadedClassCountDataProvider(), new UsedHeapMemoryDataProvider(),
                    new StringArrayDataProvider(), new FloatArrayDataProvider() };

            public String getHostUrlServer() {
                return "localhost";
            }

            public MBeanServerConnection getMBeanServerConnection() {
                return ManagementFactory.getPlatformMBeanServer();
            }

            public String getName() {
                return ResourceDataBuilder.DEFAULT_RESOURCE_NAME;
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

            public IDataProvider[] getCustomDataProviders() {
                return customDataProviders;
            }

        };
    }

    /**
     * Creates a new instance of <code>ResourceData</code> class for local
     * runtime.
     * 
     * @return An instance of <code>ResourceData</code>
     */
    public static ResourceData createResourceDataForLocalRuntime() {
        final IResourceDescriptor resourceDescriptor = createResourceDescriptorForLocalRuntime();
        final Rrd4jDataStore dataStore = new Rrd4jDataStore(DEFAULT_RESOURCE_NAME);
        final ChartModelContainer modelsCollector = new ChartModelContainer(dataStore);
        final ResourceData resourceData = new ResourceData(resourceDescriptor, modelsCollector);
        return resourceData;
    }

    /**
     * Returns a new instance of <code>ResourceData</code> class created from
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
}