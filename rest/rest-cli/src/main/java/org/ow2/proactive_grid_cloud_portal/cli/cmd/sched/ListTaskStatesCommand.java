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
import org.ow2.proactive_grid_cloud_portal.cli.cmd.Command;
import org.ow2.proactive_grid_cloud_portal.cli.utils.StringUtility;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;


/**
 * @author  the activeeon team.
 */
public class ListTaskStatesCommand extends AbstractJobTagPaginatedCommand implements Command {

    public ListTaskStatesCommand(String jobId) {
        super(jobId);
    }

    public ListTaskStatesCommand(String jobId, String tag) {
        super(jobId, tag);
    }

    public ListTaskStatesCommand(String jobId, String tag, String offset, String limit) {
        super(jobId, tag, offset, limit);
    }

    public ListTaskStatesCommand(String jobId, String offset, String limit) {
        super(jobId, offset, limit);
    }

    @Override
    public void execute(ApplicationContext currentContext) throws CLIException {
        SchedulerRestInterface scheduler = currentContext.getRestClient().getScheduler();
        try {
            List<TaskStateData> tasks = null;
            if (this.tag == null) {
                if (this.limit == 0) {
                    tasks = scheduler.getJobTaskStates(currentContext.getSessionId(), jobId).getList();
                } else {
                    tasks = scheduler.getJobTaskStatesPaginated(currentContext.getSessionId(), jobId, offset, limit)
                                     .getList();
                }
            } else {
                if (this.limit == 0) {
                    tasks = scheduler.getJobTaskStatesByTag(currentContext.getSessionId(), jobId, tag).getList();
                } else {
                    tasks = scheduler.getJobTaskStatesByTagPaginated(currentContext.getSessionId(),
                                                                     jobId,
                                                                     tag,
                                                                     offset,
                                                                     limit)
                                     .getList();
                }

            }
            resultStack(currentContext).push(tasks);
            if (!currentContext.isSilent()) {
                writeLine(currentContext, "%s", StringUtility.taskStatesAsString(tasks, false));
            }
        } catch (Exception e) {
            handleError(String.format("An error occurred while retrieving %s state:", job()), e, currentContext);
        }
    }
}
