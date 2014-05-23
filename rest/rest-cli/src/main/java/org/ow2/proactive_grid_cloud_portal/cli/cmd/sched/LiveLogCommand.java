package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractJobCommand;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;

import java.util.concurrent.TimeUnit;


public class LiveLogCommand extends AbstractJobCommand {
    public LiveLogCommand(String jobId) {
        super(jobId);
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        SchedulerRestInterface scheduler = currentContext.getRestClient().getScheduler();
        writeLine(currentContext, "Displaying live log for job %s. Press 'q' to stop.", jobId);
        try {
            while (true) {
                String log = scheduler.getLiveLogJob(currentContext.getSessionId(), jobId);
                writeLine(currentContext, "%s", log.trim());
                while (currentContext.getDevice().canRead()) {
                    int c = currentContext.getDevice().read();
                    if (c != -1 && c == 'q') {
                        return;
                    }
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            handleError("An error occurred while displaying live log", e, currentContext);
        }
    }
}
