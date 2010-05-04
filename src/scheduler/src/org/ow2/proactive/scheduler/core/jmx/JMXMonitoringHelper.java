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
package org.ow2.proactive.scheduler.core.jmx;

import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.jmx.AbstractJMXMonitoringHelper;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.jmx.mbean.SchedulerWrapperMBeanImpl;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * This helper class represents the RMI and RO based JMX monitoring infrastructure of the Scheduler.
 * @see org.ow2.proactive.jmx.AbstractJMXMonitoringHelper
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class JMXMonitoringHelper extends AbstractJMXMonitoringHelper implements SchedulerEventListener {
    private static final Logger LOGGER = ProActiveLogger.getLogger(SchedulerDevLoggers.FRONTEND);
    /** The single instance of this class */
    private static final JMXMonitoringHelper instance = new JMXMonitoringHelper();
    /** The name of the Resource Manager bean */
    public static final String SCHEDULER_BEAN_NAME = "SchedulerFrontend:name=SchedulerWrapperMBean";

    /** Scheduler's MBean */
    private SchedulerWrapperMBeanImpl schedulerMBean;

    private JMXMonitoringHelper() {
        super(LOGGER);
    }

    /**
     * Returns the single instance of this class.
     * 
     * @return the single instance of this class
     */
    public static JMXMonitoringHelper getInstance() {
        return JMXMonitoringHelper.instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectName registerMBean(final MBeanServer mbs) {
        ObjectName beanName = null;
        try {
            this.schedulerMBean = new SchedulerWrapperMBeanImpl();
            // Create the Object Names
            beanName = new ObjectName(SCHEDULER_BEAN_NAME);
            // Register the MBean Views in the related MBeanServer
            mbs.registerMBean(this.schedulerMBean, beanName);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return beanName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConnectorServerName() {
        return PASchedulerProperties.SCHEDULER_JMX_CONNECTOR_NAME.getValueAsString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getJMXRMIConnectorServerPort() {
        return PASchedulerProperties.SCHEDULER_JMX_PORT.getValueAsInt();
    }

    /**
     * Method to get the status of the Scheduler from the MBean
     */
    public SchedulerStatus getSchedulerStatus_() {
        return schedulerMBean.getSchedulerStatus_();
    }

    /**
     * Recover the JMX objects
     *
     * @param jobList the list of job to be recovered
     */
    public void recover(Set<JobState> jobList) {
        schedulerMBean.recover(jobList);
    }

    // ----------------- Event Management --------------------

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        schedulerMBean.jobStateUpdatedEvent(notification);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobSubmittedEvent(org.ow2.proactive.scheduler.common.job.JobState)
     */
    public void jobSubmittedEvent(JobState job) {
        schedulerMBean.jobSubmittedEvent(job);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStateUpdatedEvent(org.ow2.proactive.scheduler.common.SchedulerEvent)
     */
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        schedulerMBean.schedulerStateUpdatedEvent(eventType);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        schedulerMBean.taskStateUpdatedEvent(notification);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#usersUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        schedulerMBean.usersUpdatedEvent(notification);
    }
}
