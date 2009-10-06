/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.core.jmx.mbean;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.utils.AtomicRMStatisticsHolder;


/**
 * This class implements {@link RMAnonymMBean} to allow the management of the Resource
 * Manager following the JMX standard for management. It provides a consistent view of 
 * the Resource Manager statistics.
 * 
 * @author The ProActive Team
 * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAnonymMBean
 * @since ProActive Scheduling 1.1
 */
@PublicAPI
public class RMAnonymMBeanImpl extends StandardMBean implements RMAnonymMBean {

    /**
     * The reference on the statistics holder.
     */
    protected final AtomicRMStatisticsHolder rmStatisticsHolder;

    /**
     * Creates a new instance of this class.
     * 
     * @param rmStatisticsHolder The instance of the statistics holder
     * @throws NotCompliantMBeanException if the {@link RMAnonymMBean} interface does not follow
     * JMX design patterns for Management Interfaces, or if <var>this</var> does not 
     * implement the specified interface.
     */
    public RMAnonymMBeanImpl(final AtomicRMStatisticsHolder rmStatisticsHolder)
            throws NotCompliantMBeanException {
        this(RMAnonymMBean.class, rmStatisticsHolder);
    }

    /**
     * Creates a new instance of this class. Only for sub-classes.
     * 
     * @param mbeanInterface The interface of the mbean
     * @param rmStatisticsHolder The instance of the statistics holder
     * @throws NotCompliantMBeanException if the {@link RMAnonymMBean} interface does not follow
     * JMX design patterns for Management Interfaces, or if <var>this</var> does not 
     * implement the specified interface.
     */
    protected RMAnonymMBeanImpl(final Class<?> mbeanInterface,
            final AtomicRMStatisticsHolder rmStatisticsHolder) throws NotCompliantMBeanException {
        super(mbeanInterface);
        this.rmStatisticsHolder = rmStatisticsHolder;
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAnonymMBean#getRMStatus()
     */
    public String getRMStatus() {
        return this.rmStatisticsHolder.getStatistics().getRMStatus();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAnonymMBean#getAvailableNodesCount()
     */
    public int getAvailableNodesCount() {
        return this.rmStatisticsHolder.getStatistics().getAvailableNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAnonymMBean#getFreeNodesCount()
     */
    public int getFreeNodesCount() {
        return this.rmStatisticsHolder.getStatistics().getFreeNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAnonymMBean#getBusyNodesCount()
     */
    public int getBusyNodesCount() {
        return this.rmStatisticsHolder.getStatistics().getBusyNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAnonymMBean#getToBeReleasedNodesCount()
     */
    public int getToBeReleasedNodesCount() {
        return this.rmStatisticsHolder.getStatistics().getToBeReleasedNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAnonymMBean#getDownNodesCount()
     */
    public int getDownNodesCount() {
        return this.rmStatisticsHolder.getStatistics().getDownNodesCount();
    }
}