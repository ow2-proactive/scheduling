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
package org.objectweb.proactive.ic2d.chronolog.data.provider;

import org.objectweb.proactive.ic2d.chronolog.data.provider.predefined.FloatArrayDataProvider;
import org.objectweb.proactive.ic2d.chronolog.data.provider.predefined.LoadedClassCountDataProvider;
import org.objectweb.proactive.ic2d.chronolog.data.provider.predefined.ThreadCountDataProvider;
import org.objectweb.proactive.ic2d.chronolog.data.provider.predefined.UsedHeapMemoryDataProvider;


/**
 * A descriptor used to register all available predefined providers.
 * <p>
 * If additional predefined providers are needed :
 * <ul>
 * 
 * <li>
 * <p>
 * Create the provider class that implements <code>IDataProvider</code>
 * interface
 * </p>
 * </li>
 * 
 * <li>
 * <p>
 * Add its descriptor in this class
 * </p>
 * </li>
 * 
 * </ul>
 * <p>
 * Then, it will automatically appear in the list of additional attributes in
 * the UI.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public enum ProviderDescriptor {

    LOADED_CLASS_COUNT(LoadedClassCountDataProvider.NAME, LoadedClassCountDataProvider.class),

    THREAD_COUNT(ThreadCountDataProvider.NAME, ThreadCountDataProvider.class),

    USED_HEAP_MEMORY(UsedHeapMemoryDataProvider.NAME, UsedHeapMemoryDataProvider.class),

    FLOAT_ARRAY(FloatArrayDataProvider.NAME, FloatArrayDataProvider.class);

    /** The name of the provider */
    private final String name;
    /** The class of the provider */
    private final Class<? extends IDataProvider> clazz;

    /**
     * Builds a new instance of the provider descriptor.
     * 
     * @param name
     *            The name of the provider
     * @param clazz
     *            The class of the provider
     */
    private ProviderDescriptor(final String name, final Class<? extends IDataProvider> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    /**
     * Returns the name of this provider.
     * 
     * @return The name of this provider
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the class of this provider.
     * 
     * @return The class of this provider
     */
    public Class<? extends IDataProvider> getClazz() {
        return clazz;
    }
}
