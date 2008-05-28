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
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServerConnection;

import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;


/**
 * Used to count the number of classes that are currently loaded in a Java
 * virtual machine.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class LoadedClassCountDataProvider implements IDataProvider {

    public static final String NAME = "LoadedClassCount";
    public static final String DESCRIPTION = "Number of Loaded Classes";
    public static final String TYPE = "int";

    /**
     * The reference on the mbean
     */
    private final ClassLoadingMXBean mBean;

    /**
     * Creates a new instance of LoadedClassCountDataProvider class based on the local <code>ClassLoadingMXBean</code>.
     */
    public LoadedClassCountDataProvider() {
        this(ManagementFactory.getClassLoadingMXBean());
    }

    /**
     * Creates a new instance of LoadedClassCountDataProvider class.
     * 
     * @param mBean
     *            The ClassLoadingMXBean reference
     */
    public LoadedClassCountDataProvider(final ClassLoadingMXBean mBean) {
        this.mBean = mBean;
    }

    /**
     * Creates a new instance of LoadedClassCountDataProvider class.
     * 
     * @param mBeanServerConnection The connection used to build an <code>MXBeanProxy</code>
     * @throws IOException Thrown during the creation of the proxy 
     */
    public LoadedClassCountDataProvider(final MBeanServerConnection mBeanServerConnection) throws IOException {
        this(ManagementFactory.newPlatformMXBeanProxy(mBeanServerConnection,
                ManagementFactory.CLASS_LOADING_MXBEAN_NAME, ClassLoadingMXBean.class));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#provideValue()
     */
    public Object provideValue() {
        return this.mBean.getLoadedClassCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getName()
     */
    public String getName() {
        return LoadedClassCountDataProvider.NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getDescription()
     */
    public String getDescription() {
        return LoadedClassCountDataProvider.DESCRIPTION;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getType()
     */
    public String getType() {
        return LoadedClassCountDataProvider.TYPE;
    }
}