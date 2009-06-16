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
package org.ow2.proactive.scheduler.core.jmx;

import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.authentication.AuthenticationImpl;
import org.ow2.proactive.jmx.connector.PAAuthenticationConnectorServer;
import org.ow2.proactive.jmx.naming.JMXProperties;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.jmx.mbean.SchedulerWrapperAdmin;
import org.ow2.proactive.scheduler.common.jmx.mbean.SchedulerWrapperAnonym;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * JMX Helper Class for the Scheduler to create the MBeanServer Views and the Connectors
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class JMXMonitoringHelper implements SchedulerEventListener {
    /** logger device */
    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.FRONTEND);

    private static final String SCHEDULER_BEAN_NAME = "SchedulerFrontend:name=SchedulerWrapperMBean";
    private static final String JMX_CONNECTOR_NAME = PASchedulerProperties.SCHEDULER_JMX_CONNECTOR_NAME
            .getValueAsString();

    /**
     * The default jmx Connector Server url for the scheduler, is specified the port to use for exchanging objects
     * (the first one) and the port where the RMI registry is reachable (the second port) so that a firewall
     * will not block the requests to the JMX connector
     * An example of address for connection to the RMI Connector (e.g. service:jmx:rmi:///jndi/rmi://hostName/serverName)
     */
    private static final String DEFAULT_JMX_CONNECTOR_URL;

    static {
        if (PASchedulerProperties.SCHEDULER_JMX_PORT.getValueAsString() == null) {
            DEFAULT_JMX_CONNECTOR_URL = "service:jmx:rmi://localhost/jndi/rmi://localhost:" +
                PAProperties.PA_RMI_PORT.getValue() + "/";
        } else {
            DEFAULT_JMX_CONNECTOR_URL = "service:jmx:rmi://localhost:" +
                PASchedulerProperties.SCHEDULER_JMX_PORT.getValueAsInt() + "/jndi/rmi://localhost:" +
                PAProperties.PA_RMI_PORT.getValue() + "/";
        }
    }

    /** Scheduler's MBean */
    private static SchedulerWrapperAnonym schedulerBeanAnonym = new SchedulerWrapperAnonym();
    private static SchedulerWrapperAdmin schedulerBeanAdmin = new SchedulerWrapperAdmin();

    /** Scheduler`s MBeanServer */
    private MBeanServer mbsAnonym;
    private MBeanServer mbsAdmin;

    /**
     * Create the MBean Objects at the starting of the Scheduler
     * and register them on the related MBeanServer based on the View
     */
    public void createMBeanServers() {
        // Create one MBeanServer for each View
        this.mbsAnonym = MBeanServerFactory.createMBeanServer();
        this.mbsAdmin = MBeanServerFactory.createMBeanServer();
        ObjectName mBeanNameAnonym = null;
        ObjectName mBeanNameAdmin = null;
        try {
            // Create the Object Names
            mBeanNameAnonym = new ObjectName(SCHEDULER_BEAN_NAME);
            mBeanNameAdmin = new ObjectName(SCHEDULER_BEAN_NAME + "_" + JMXProperties.JMX_ADMIN);
            // Register the MBean Views in the related MBeanServer
            mbsAnonym.registerMBean(schedulerBeanAnonym, mBeanNameAnonym);
            mbsAdmin.registerMBean(schedulerBeanAdmin, mBeanNameAdmin);
        } catch (Exception e) {
            logger_dev.error("", e);
        }
    }

    /**
     * method to create the MBeanServer Connectors for the Scheduler and to start them
     */
    public void createConnectors(AuthenticationImpl authentication) {
        // Reference to the JMX Scheduler Connector Server Using the RMI Protocol 
        PAAuthenticationConnectorServer rmiConnectorAnonym;
        PAAuthenticationConnectorServer rmiConnectorAdmin;
        // Create the RMI MBean Server Connectors
        rmiConnectorAnonym = new PAAuthenticationConnectorServer(DEFAULT_JMX_CONNECTOR_URL,
            JMX_CONNECTOR_NAME, mbsAnonym, authentication, true, logger_dev);
        rmiConnectorAdmin = new PAAuthenticationConnectorServer(DEFAULT_JMX_CONNECTOR_URL,
            JMX_CONNECTOR_NAME + "_" + JMXProperties.JMX_ADMIN, mbsAdmin, authentication, false, logger_dev);
        // Start the Connectors	
        rmiConnectorAnonym.start();
        rmiConnectorAdmin.start();
    }

    /**
     * Method to get the status of the Scheduler from the MBean
     */
    public SchedulerStatus getSchedulerStatus_() {
        return schedulerBeanAnonym.getSchedulerStatus_();
    }

    /**
     * Recover the JMX objects
     *
     * @param jobList the list of job to be recovered
     */
    public void recover(Map<JobId, InternalJob> jobList) {
        schedulerBeanAnonym.recover(jobList);
        schedulerBeanAdmin.recover(jobList);
    }

    // ----------------- Event Management --------------------

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        schedulerBeanAnonym.jobStateUpdatedEvent(notification);
        schedulerBeanAdmin.jobStateUpdatedEvent(notification);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobSubmittedEvent(org.ow2.proactive.scheduler.common.job.JobState)
     */
    public void jobSubmittedEvent(JobState job) {
        schedulerBeanAnonym.jobSubmittedEvent(job);
        schedulerBeanAdmin.jobSubmittedEvent(job);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStateUpdatedEvent(org.ow2.proactive.scheduler.common.SchedulerEvent)
     */
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        schedulerBeanAnonym.schedulerStateUpdatedEvent(eventType);
        schedulerBeanAdmin.schedulerStateUpdatedEvent(eventType);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        schedulerBeanAnonym.taskStateUpdatedEvent(notification);
        schedulerBeanAdmin.taskStateUpdatedEvent(notification);
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#usersUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        schedulerBeanAnonym.usersUpdatedEvent(notification);
        schedulerBeanAdmin.usersUpdatedEvent(notification);
    }

}
