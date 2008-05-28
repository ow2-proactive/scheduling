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

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;

import org.objectweb.proactive.ic2d.chartit.data.ChartModelContainer;
import org.objectweb.proactive.ic2d.chartit.data.provider.ByAttributeDataProvider;
import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;


/**
 * This class represents a resource. A resource is any data that can be managed
 * by an MBean.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class ResourceData {

    /** The descriptor of the resource */
    protected final IResourceDescriptor resourceDescriptor;

    /** The data store associated to this resource data */
    protected final ChartModelContainer modelsContainer;

    /**
     * Creates a new instance of <code>ResourceData</code> class.
     * 
     * @param resourceDescriptor
     *            The descriptor of this resource
     * @param modelsContainer
     *            The models container
     */
    public ResourceData(final IResourceDescriptor resourceDescriptor,
            final ChartModelContainer modelsContainer) {
        this.resourceDescriptor = resourceDescriptor;
        this.modelsContainer = modelsContainer;
    }

    /**
     * Returns the descriptor of this resource.
     * 
     * @return the descriptor of this resource
     */
    public IResourceDescriptor getResourceDescriptor() {
        return resourceDescriptor;
    }

    /**
     * Returns the model container associated to this resource.
     * 
     * @return the model container associated to this resource
     */
    public ChartModelContainer getModelsContainer() {
        return modelsContainer;
    }

    /**
     * Returns an array of attribute informations based on the current 
     * resource object name. 
     * 
     * @return an array of attribute informations
     */
    public MBeanAttributeInfo[] getMBeanAttributeInfoFromResource() {
        try {
            final MBeanInfo info = this.getResourceDescriptor().getMBeanServerConnection().getMBeanInfo(
                    getResourceDescriptor().getObjectName());
            return info.getAttributes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MBeanAttributeInfo[0];
    }

    /**
     * Finds all available attributes information from the MBean associated to
     * this resource and creates corresponding data providers.
     * 
     * @return An array of data provider created from attribute information
     */
    public IDataProvider[] findAndCreateDataProviders() {
        // List all available attributes for the current resource associated
        // mbean
        final MBeanAttributeInfo[] attInfos = this.getMBeanAttributeInfoFromResource();
        // Custom providers can be supplied
        final IDataProvider[] customProviders = this.getResourceDescriptor().getCustomDataProviders();
        // Create the result array of data providers
        final IDataProvider[] res = new IDataProvider[attInfos.length + customProviders.length];
        // Create all corresponding data providers
        int i = 0;
        for (final MBeanAttributeInfo in : attInfos) {
            res[i++] = new ByAttributeDataProvider(this.getResourceDescriptor().getMBeanServerConnection(),
                this.getResourceDescriptor().getObjectName(), in);
        }
        // Add all custom providers		
        System.arraycopy(customProviders, 0, res, i, customProviders.length);
        return res;
    }
}