package org.ow2.proactive.scheduler.core;

import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.util.logforwarder.AppenderProvider;
import org.ow2.proactive.scheduler.common.util.logforwarder.LogForwardingException;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;


public class DisabledListenJobLogsSupport extends ListenJobLogsSupport {

    @Override
    void listenJobLogs(JobId jobId, AppenderProvider appenderProvider) throws UnknownJobException {
    }

    @Override
    void activeLogsIfNeeded(JobId jobId, TaskLauncher launcher) throws LogForwardingException {
    }

    @Override
    void cleanLoggers(JobId jobId) {
    }

    @Override
    void shutdown() {
    }

}
