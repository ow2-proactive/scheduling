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
package org.objectweb.proactive.ic2d.chartit.data.provider.predefined;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import javax.management.MBeanServerConnection;

import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;


/**
 * Provides heap usage in bytes from a MemoryMXbean
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
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
     * Creates a new instance of UsedHeapMemoryDataProvider class based on the local <code>MemoryMXBean</code>.
     */
    public UsedHeapMemoryDataProvider() {
        this(ManagementFactory.getMemoryMXBean());
    }

    /**
     * Creates a new instance of UsedHeapMemoryDataProvider class.
     * 
     * @param mBean
     *            The MemoryMXBean reference
     */
    public UsedHeapMemoryDataProvider(final MemoryMXBean mBean) {
        this.mBean = mBean;
    }

    /**
     * Creates a new instance of UsedHeapMemoryDataProvider class.
     * 
     * @param mBeanServerConnection The connection used to build an <code>MXBeanProxy</code>
     * @throws IOException Thrown during the creation of the proxy 
     */
    public UsedHeapMemoryDataProvider(final MBeanServerConnection mBeanServerConnection) throws IOException {
        this(ManagementFactory.newPlatformMXBeanProxy(mBeanServerConnection,
                ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class));
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
}
