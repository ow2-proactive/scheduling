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
package org.objectweb.proactive.ic2d.chartit.data.provider;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;


/**
 * This class provides a generic way to get attribute values for a remote MBean
 * using an MBean server reference.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class ByAttributeDataProvider implements IDataProvider {

    /** The way to talk to an MBean server */
    private final MBeanServerConnection mbs;
    /** The object name of the MBean */
    private final ObjectName objectName;
    /** The attribute exposed for management */
    private final MBeanAttributeInfo attributeInfo;

    /**
     * Builds a new instance of ByAttributeDataProvider class.
     * 
     * @param mbs
     *            The connection to talk to an MBean server
     * @param objectName
     *            The object name of the MBean
     * @param attribute
     *            The attribute exposed for management
     */
    public ByAttributeDataProvider(final MBeanServerConnection mbs, final ObjectName objectName,
            final MBeanAttributeInfo attributeInfo) {
        this.mbs = mbs;
        this.objectName = objectName;
        this.attributeInfo = attributeInfo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#provideValue()
     */
    public Object provideValue() {
        try {
            return mbs.getAttribute(this.objectName, this.attributeInfo.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getDescription()
     */
    public String getDescription() {
        return this.attributeInfo.getDescription();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getName()
     */
    public String getName() {
        return this.attributeInfo.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider#getType()
     */
    public String getType() {
        return this.attributeInfo.getType();
    }
}