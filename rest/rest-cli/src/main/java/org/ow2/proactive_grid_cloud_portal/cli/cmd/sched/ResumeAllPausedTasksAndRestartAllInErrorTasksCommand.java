/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
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
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.cmd.sched;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractJobCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;


public class ResumeAllPausedTasksAndRestartAllInErrorTasksCommand extends AbstractJobCommand implements Command {

    public ResumeAllPausedTasksAndRestartAllInErrorTasksCommand(String jobId) {
        super(jobId);
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {

        SchedulerRestInterface scheduler = currentContext.getRestClient().getScheduler();

        try {
            String sessionId = currentContext.getSessionId();
            boolean result = scheduler.resumeJob(sessionId, jobId);
            result &= scheduler.restartAllInErrorTasks(sessionId, jobId);
            handleResult(currentContext, result);
        } catch (Exception e) {
            handleError(
                    String.format(
                            "An error occurred while attempting to resume/restart all paused/in-error tasks for %s:",
                            job()), e, currentContext);
        }
    }

    private void handleResult(ApplicationContext currentContext, boolean result) {
        resultStack(currentContext).push(result);

        if (result) {
            writeLine(currentContext, "All paused/in-error tasks for %s successfully resumed/restarted.", job());
        } else {
            writeLine(currentContext, "Cannot resume/restart paused/in-error tasks for %s.", job());
        }
    }

}
