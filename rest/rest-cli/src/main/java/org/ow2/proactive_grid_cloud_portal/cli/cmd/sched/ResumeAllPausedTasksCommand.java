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

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractJobCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;


public class ResumeAllPausedTasksCommand extends AbstractJobCommand implements Command {

    public ResumeAllPausedTasksCommand(String jobId) {
        super(jobId);
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {

        try {
            SchedulerRestInterface scheduler = currentContext.getRestClient().getScheduler();
            boolean result = scheduler.resumeJob(currentContext.getSessionId(), jobId);

            handleResult(currentContext, result);
        } catch (Exception e) {
            handleError(String.format("An error occurred while attempting to resume all paused tasks for %s:", job()),
                        e,
                        currentContext);
        }
    }

    private void handleResult(ApplicationContext currentContext, boolean result) {
        resultStack(currentContext).push(result);

        if (result) {
            writeLine(currentContext, "All paused tasks for %s successfully resumed.", job());
        } else {
            writeLine(currentContext, "Cannot resume paused tasks for %s.", job());
        }
    }

}
