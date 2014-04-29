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