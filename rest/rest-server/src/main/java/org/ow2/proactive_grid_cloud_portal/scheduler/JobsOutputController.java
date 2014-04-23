/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler;

import java.io.IOException;

import org.apache.log4j.Appender;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;
import org.apache.log4j.Logger;


/**
 * Create, show and remove jobs output
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class JobsOutputController {
    private static final Logger LOGGER = ProActiveLogger.getLogger(JobsOutputController.class);

    private LogForwardingService lfs;

    private static JobsOutputController instance;

    private JobsOutputController() {
        lfs = new LogForwardingService(PortalConfiguration.getProperties().getProperty(
                PortalConfiguration.scheduler_logforwardingservice_provider));
        try {
            lfs.initialize();
        } catch (LogForwardingException e) {
            LOGGER.warn("Could not initialize log forwarding service", e);
        }
    }

    public synchronized static JobsOutputController getInstance() {
        if (instance == null) {
            instance = new JobsOutputController();
        }
        return instance;
    }

    public JobOutputAppender createJobOutputAppender(Scheduler scheduler, String jobId) throws NotConnectedException,
            UnknownJobException, PermissionException, LogForwardingException, IOException {

        JobOutputAppender jobOutputAppender = new JobOutputAppender();
        lfs.addAppender(Log4JTaskLogs.getLoggerName(jobId), jobOutputAppender);
        scheduler.listenJobLogs(jobId, lfs.getAppenderProvider());
        return jobOutputAppender;
    }


    public void destroyJobOutputAppender(String jobId, Appender appender) {
        lfs.removeAppender(Log4JTaskLogs.getLoggerName(jobId), appender);
    }
}
