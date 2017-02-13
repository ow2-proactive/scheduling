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
package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractJobCommand;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;

import com.google.common.base.Strings;


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
            String line = readLine(currentContext, "> ");
            while (Strings.isNullOrEmpty(line) || !line.trim().equals("q")) {
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
            this.terminate = true;
        }

        @Override
        public void run() {
            SchedulerRestInterface scheduler = currentContext.getRestClient().getScheduler();
            writeLine(currentContext, "Displaying live log for job %s. Press 'q' to stop.", jobId);
            try {
                while (!terminate) {
                    String log = scheduler.getLiveLogJob(currentContext.getSessionId(), jobId);
                    if (!Strings.isNullOrEmpty(log)) {
                        writeLine(currentContext, "%s", log.trim());
                    }
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
