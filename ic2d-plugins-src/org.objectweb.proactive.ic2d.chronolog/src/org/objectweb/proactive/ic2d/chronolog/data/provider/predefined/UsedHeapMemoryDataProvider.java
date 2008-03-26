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
package org.objectweb.proactive.ic2d.chronolog.data.provider.predefined;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import javax.management.MBeanServerConnection;

import org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider;


/**
 * Provides heap usage in bytes from a MemoryMXbean
 * 
 * @author The ProActive Team
 */
public final class UsedHeapMemoryDataProvider implements IDataProvider {

    public static final String NAME = "UsedHeapMemory";
    public static final String DESCRIPTION = "Heap Memory Usage";
    public static final String TYPE = "long";

    /**
     * The reference on the mbean
     */
    private final MemoryMXBean mBean;

    /**
     * Builds a new instance of UsedHeapMemoryDataProvider class.
     * 
     * @param mBean
     *            The reference on mbean
     */
    public UsedHeapMemoryDataProvider(final MemoryMXBean mBean) {
        this.mBean = mBean;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#provideValue()
     */
    public Object provideValue() {
        return this.mBean.getHeapMemoryUsage().getUsed();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getName()
     */
    public String getName() {
        return UsedHeapMemoryDataProvider.NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getDescription()
     */
    public String getDescription() {
        return UsedHeapMemoryDataProvider.DESCRIPTION;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getType()
     */
    public String getType() {
        return UsedHeapMemoryDataProvider.TYPE;
    }

    // /////////////////////////////////////////////
    // Static methods for local and remote creation
    // /////////////////////////////////////////////

    /**
     * Returns the reference on the remote MBean
     * 
     * @param mBeanServerConnection
     *            The connection to the remote MBean server
     * @return The reference on the remote MBean
     */
    public static UsedHeapMemoryDataProvider build(final MBeanServerConnection mBeanServerConnection) {
        if (mBeanServerConnection == null) {
            new UsedHeapMemoryDataProvider(ManagementFactory.getMemoryMXBean());
        }
        try {
            return new UsedHeapMemoryDataProvider(ManagementFactory.newPlatformMXBeanProxy(
                    mBeanServerConnection, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class));
        } catch (Exception e) {
            // TODO : log the exception
            e.printStackTrace();
        }
        return null;
    }
}
