/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
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
 * This class implements {@link RMMBean} to allow the management of the Resource
 * Manager following the JMX standard for management. It provides a consistent view of
 * the Resource Manager statistics.
 *
 * @author The ProActive Team
 * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMMBean
 * @since ProActive Scheduling 1.1
 */
@PublicAPI
public class RMMBeanImpl extends StandardMBean implements RMMBean {

    /**
     * The reference on the statistics holder.
     */
    protected final AtomicRMStatisticsHolder rmStatisticsHolder;

    /**
     * Creates a new instance of this class.
     *
     * @param rmStatisticsHolder The instance of the statistics holder
     * @throws NotCompliantMBeanException if the {@link RMMBean} interface does not follow
     * JMX design patterns for Management Interfaces, or if <var>this</var> does not
     * implement the specified interface.
     */
    public RMMBeanImpl(final AtomicRMStatisticsHolder rmStatisticsHolder) throws NotCompliantMBeanException {
        this(RMMBean.class, rmStatisticsHolder);
    }

    /**
     * Creates a new instance of this class. Only for sub-classes.
     *
     * @param mbeanInterface The interface of the mbean
     * @param rmStatisticsHolder The instance of the statistics holder
     * @throws NotCompliantMBeanException if the {@link RMMBean} interface does not follow
     * JMX design patterns for Management Interfaces, or if <var>this</var> does not
     * implement the specified interface.
     */
    protected RMMBeanImpl(final Class<?> mbeanInterface, final AtomicRMStatisticsHolder rmStatisticsHolder)
            throws NotCompliantMBeanException {
        super(mbeanInterface);
        this.rmStatisticsHolder = rmStatisticsHolder;
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMMBean#getRMStatus()
     */
    public String getRMStatus() {
        return this.rmStatisticsHolder.getStatistics().getRMStatus();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMMBean#getAvailableNodesCount()
     */
    public int getAvailableNodesCount() {
        return this.rmStatisticsHolder.getStatistics().getAvailableNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMMBean#getFreeNodesCount()
     */
    public int getFreeNodesCount() {
        return this.rmStatisticsHolder.getStatistics().getFreeNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMMBean#getBusyNodesCount()
     */
    public int getBusyNodesCount() {
        return this.rmStatisticsHolder.getStatistics().getBusyNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMMBean#getToBeReleasedNodesCount()
     */
    @Deprecated
    public int getToBeReleasedNodesCount() {
        return this.rmStatisticsHolder.getStatistics().getToBeRemovedNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMMBean#getDownNodesCount()
     */
    public int getDownNodesCount() {
        return this.rmStatisticsHolder.getStatistics().getDownNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAdminMBean#getMaxFreeNodes()
     */
    public int getMaxFreeNodes() {
        return rmStatisticsHolder.getStatistics().getMaxFreeNodes();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAdminMBean#getMaxBusyNodes()
     */
    public int getMaxBusyNodes() {
        return rmStatisticsHolder.getStatistics().getMaxBusyNodes();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAdminMBean#getMaxToBeReleasedNodes()
     */
    public int getMaxToBeReleasedNodes() {
        return rmStatisticsHolder.getStatistics().getToBeRemovedNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAdminMBean#getMaxDownNodes()
     */
    public int getMaxDownNodes() {
        return rmStatisticsHolder.getStatistics().getMaxDownNodes();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAdminMBean#getAverageActivity()
     */
    public double getAverageActivity() {
        return rmStatisticsHolder.getStatistics().getActivityTimePercentage();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RMAdminMBean#getAverageInactivity()
     */
    public double getAverageInactivity() {
        return rmStatisticsHolder.getStatistics().getInactivityTimePercentage();
    }

}