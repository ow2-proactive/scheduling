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
package org.ow2.proactive.scheduler.core;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.TaskLauncher;


abstract class ListenJobLogsSupport {

    protected static final Logger logger = Logger.getLogger(SchedulingService.class);

    abstract void listenJobLogs(JobId jobId, AppenderProvider appenderProvider) throws UnknownJobException;

    abstract void activeLogsIfNeeded(JobId jobId, TaskLauncher launcher) throws LogForwardingException;

    abstract void cleanLoggers(JobId jobId);

    abstract void shutdown();

    boolean isEnabled() {
        return this instanceof EnabledListenJobLogsSupport;
    }

    static ListenJobLogsSupport newInstance(SchedulerDBManager dbManager, LiveJobs liveJobs)
            throws LogForwardingException {
        String providerClassname = PASchedulerProperties.LOGS_FORWARDING_PROVIDER.getValueAsString();
        if (providerClassname == null || providerClassname.equals("")) {
            logger.info("LogForwardingProvider property is not set, disabling listen job logs support");
            return new DisabledListenJobLogsSupport();
        } else {
            return new EnabledListenJobLogsSupport(dbManager, liveJobs, providerClassname);
        }
    }

}
