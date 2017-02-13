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
package org.ow2.proactive_grid_cloud_portal.scheduler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.task.Log4JTaskLogs;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingService;
import org.ow2.proactive_grid_cloud_portal.common.Session;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;


/**
 * Create, show and remove jobs output
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class JobsOutputController {
    private static final Logger LOGGER = ProActiveLogger.getLogger(JobsOutputController.class);

    private LogForwardingService logForwardingService;

    private Map<String, JobOutputAppender> appenders = new HashMap<>();

    private Set<String> listeningTo = new HashSet<>();

    private final Session session;

    public JobsOutputController(Session session) {
        this.session = session;
    }

    public synchronized String getNewLogs(String jobId) throws LogForwardingException, NotConnectedException,
            UnknownJobException, PermissionException, IOException {
        return getJobOutputAppender(jobId).fetchNewLogs();
    }

    public synchronized String getAllLogs(String jobId) throws LogForwardingException, NotConnectedException,
            UnknownJobException, PermissionException, IOException {
        return getJobOutputAppender(jobId).fetchAllLogs();
    }

    public synchronized int availableLinesCount(String jobId) {
        JobOutputAppender appender = appenders.get(jobId);
        if (appender != null) {
            return appender.size();
        }
        return -1;
    }

    public synchronized void removeAppender(String jobId) {
        if (appenders.containsKey(jobId)) {
            JobOutputAppender appender = appenders.remove(jobId);
            getLogForwardingService().removeAllAppenders(Log4JTaskLogs.getLoggerName(jobId));
            appender.close();
        }
    }

    public synchronized void terminate() {
        try {
            if (logForwardingService != null) {
                logForwardingService.terminate();
            }
        } catch (LogForwardingException e) {
            LOGGER.warn("Could not terminate log forwarding service", e);
        }
    }

    private LogForwardingService getLogForwardingService() {
        if (logForwardingService == null) {
            logForwardingService = new LogForwardingService(PortalConfiguration.getProperties()
                                                                               .getProperty(PortalConfiguration.scheduler_logforwardingservice_provider));
            try {
                logForwardingService.initialize();
            } catch (LogForwardingException e) {
                LOGGER.warn("Could not initialize log forwarding service", e);
            }
        }
        return logForwardingService;
    }

    private JobOutputAppender getJobOutputAppender(String jobId) throws UnknownJobException, LogForwardingException,
            NotConnectedException, PermissionException, IOException {

        JobOutputAppender jobOutputAppender = appenders.get(jobId);
        if (jobOutputAppender == null) {
            jobOutputAppender = createJobOutputAppender(jobId);
        }
        return jobOutputAppender;
    }

    private JobOutputAppender createJobOutputAppender(String jobId) throws NotConnectedException, UnknownJobException,
            PermissionException, LogForwardingException, IOException {

        JobOutputAppender jobOutputAppender = new JobOutputAppender();
        addJobOutputAppender(jobId, jobOutputAppender);
        return jobOutputAppender;
    }

    // public for tests
    public synchronized void addJobOutputAppender(String jobId, JobOutputAppender jobOutputAppender)
            throws NotConnectedException, UnknownJobException, PermissionException, LogForwardingException {
        getLogForwardingService().addAppender(Log4JTaskLogs.getLoggerName(jobId), jobOutputAppender);
        if (!listeningTo.contains(jobId)) {
            session.getScheduler().listenJobLogs(jobId, getLogForwardingService().getAppenderProvider());
            listeningTo.add(jobId);
        }
        appenders.put(jobId, jobOutputAppender);
    }

}
