/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.core.jmx.mbean;

import java.io.IOException;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.jmx.Chronological;
import org.ow2.proactive.resourcemanager.core.jmx.RMJMXHelper;
import org.ow2.proactive.resourcemanager.utils.RMStatistics;


/**
 * This class implements {@link RuntimeDataMBean} to expose the runtime data of the Resource
 * Manager following the JMX standard for management. It provides a consistent view of
 * the Resource Manager statistics.
 *
 * @author The ProActive Team
 * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean
 * @since ProActive Scheduling 1.1
 */
@PublicAPI
public class RuntimeDataMBeanImpl extends StandardMBean implements RuntimeDataMBean {

    /**
     * The reference on the statistics holder.
     */
    protected final RMStatistics rmStatistics;

    /**
     * Creates a new instance of this class.
     *
     * @param rmStatistics The instance of the statistics holder
     * @throws NotCompliantMBeanException if the {@link RuntimeDataMBean} interface does not follow
     * JMX design patterns for Management Interfaces, or if <var>this</var> does not
     * implement the specified interface.
     */
    public RuntimeDataMBeanImpl(final RMStatistics rmStatistics) throws NotCompliantMBeanException {
        super(RuntimeDataMBean.class);
        this.rmStatistics = rmStatistics;
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getStatus()
     */
    public String getStatus() {
        return this.rmStatistics.getRMStatus();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getAvailableNodesCount()
     */
    @Chronological
    public int getAvailableNodesCount() {
        return this.rmStatistics.getAvailableNodesCount();
    }

    @Chronological
    public int getPendingTasksCount() {
        return this.rmStatistics.getPendingTasksCount();
    }

    /**
     * @see RuntimeDataMBean#getConfigNodesCount()
     */
    @Chronological
    public int getConfigNodesCount() {
        return this.rmStatistics.getConfiguringNodesCount();
    }

    /**
     * @see  RuntimeDataMBean#getLostNodesCount()
     */
    @Chronological
    public int getLostNodesCount() {
        return this.rmStatistics.getLostNodesCount();
    }

    /**
     * @see RuntimeDataMBean#getDeployingNodesCount()
     */
    @Chronological
    public int getDeployingNodesCount() {
        return this.rmStatistics.getDeployingNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getFreeNodesCount()
     */
    @Chronological
    public int getFreeNodesCount() {
        return this.rmStatistics.getFreeNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getBusyNodesCount()
     */
    @Chronological
    public int getBusyNodesCount() {
        return this.rmStatistics.getBusyNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getToBeReleasedNodesCount()
     */
    @Chronological
    public int getToBeReleasedNodesCount() {
        return this.rmStatistics.getToBeRemovedNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getDownNodesCount()
     */
    @Chronological
    public int getDownNodesCount() {
        return this.rmStatistics.getDownNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getMaxConfiguringNodes()
     */
    public int getMaxConfiguringNodes() {
        return this.rmStatistics.getMaxConfiguringNodes();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getMaxLostNodes()
     */
    public int getMaxLostNodes() {
        return this.rmStatistics.getMaxLostNodes();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getMaxDeployingNodes()
     */
    public int getMaxDeployingNodes() {
        return this.rmStatistics.getMaxDeployingNodes();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getMaxFreeNodes()
     */
    @Chronological
    public int getMaxFreeNodes() {
        return this.rmStatistics.getMaxFreeNodes();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getMaxBusyNodes()
     */
    public int getMaxBusyNodes() {
        return this.rmStatistics.getMaxBusyNodes();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getMaxToBeReleasedNodes()
     */
    public int getMaxToBeReleasedNodes() {
        return this.rmStatistics.getToBeRemovedNodesCount();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getMaxDownNodes()
     */
    public int getMaxDownNodes() {
        return this.rmStatistics.getMaxDownNodes();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getAverageActivity()
     */
    @Chronological
    public double getAverageActivity() {
        return this.rmStatistics.getActivityTimePercentage();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getAverageInactivity()
     */
    @Chronological
    public double getAverageInactivity() {
        return this.rmStatistics.getInactivityTimePercentage();
    }

    /**
     * @see org.ow2.proactive.resourcemanager.core.jmx.mbean.RuntimeDataMBean#getStatisticHistory()
     */
    public byte[] getStatisticHistory() throws IOException {
        return RMJMXHelper.getInstance().getDataStore().getBytes();
    }
}
