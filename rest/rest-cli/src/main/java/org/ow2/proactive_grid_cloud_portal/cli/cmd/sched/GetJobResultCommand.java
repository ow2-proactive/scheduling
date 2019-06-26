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

import java.util.List;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractJobCommand;
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;


public class GetJobResultCommand extends AbstractJobTagCommand implements Command {

    public GetJobResultCommand(String jobId) {
        super(jobId);
    }

    public GetJobResultCommand(String jobId, String tag) {
        super(jobId, tag);
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {

        try {
            SchedulerRestInterface scheduler = currentContext.getRestClient().getScheduler();
            if (this.tag == null) {
                JobResultData results = scheduler.jobResult(currentContext.getSessionId(), jobId);
                resultStack(currentContext).push(results);
                if (!currentContext.isForced()) {
                    writeLine(currentContext, "%s", StringUtility.jobResultAsString(job(), results));
                }
            } else {
                List<TaskResultData> results = scheduler.taskResultByTag(currentContext.getSessionId(), jobId, tag);
                resultStack(currentContext).push(results);
                if (!currentContext.isForced()) {
                    writeLine(currentContext, "%s", StringUtility.taskResultsAsString(results));
                }
            }

        } catch (Exception e) {
            handleError(String.format("An error occurred while retrieving %s result:", job()), e, currentContext);
        }
    }

}
