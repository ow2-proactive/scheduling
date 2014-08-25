package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractJobCommand;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;

import com.google.common.base.Strings;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class LiveLogCommand extends AbstractJobCommand {
    public LiveLogCommand(String jobId) {
        super(jobId);
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        writeLine(currentContext, "Displaying live log for job %s. Press 'q' to stop.", jobId);
        try {
            LogReader reader = new LogReader(currentContext);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(reader);
            String line  = readLine(currentContext, "> ");
            while (Strings.isNullOrEmpty(line) || ! line.trim().equals("q")) {
                line = readLine(currentContext, "> ");
            }
            reader.terminate();
            executor.shutdownNow();
        } catch (Exception e) {
            handleError("An error occurred while displaying live log", e, currentContext);
        }
    }

    private class LogReader implements Runnable {
        private final ApplicationContext currentContext;
        private volatile boolean terminate = false;

        public LogReader(ApplicationContext currentContext) {
            this.currentContext = currentContext;
        }

        public void terminate() {
            this.terminate =true;
        }

        @Override
        public void run() {
            SchedulerRestInterface scheduler = currentContext.getRestClient().getScheduler();
            writeLine(currentContext, "Displaying live log for job %s. Press 'q' to stop.", jobId);
            try {
                while (!terminate) {
                    String log = scheduler.getLiveLogJob(currentContext.getSessionId(), jobId);
                    writeLine(currentContext, "%s", log.trim());
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ignorable) {
                    }
                }
            } catch (Exception e) {
                handleError("An error occurred while displaying live log", e, currentContext);
            }
        }
    }
}
