/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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

package org.ow2.proactive_grid_cloud_portal.cli.cmd;

import static org.ow2.proactive_grid_cloud_portal.cli.ResponseStatus.OK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.ow2.proactive.utils.ObjectArrayFormatter;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive_grid_cloud_portal.cli.json.JobStateView;
import org.ow2.proactive_grid_cloud_portal.cli.json.TaskIdView;
import org.ow2.proactive_grid_cloud_portal.cli.json.TaskInfoView;
import org.ow2.proactive_grid_cloud_portal.cli.json.TaskStateView;

public class GetJobStateCommand extends AbstractJobCommand implements Command {

    public GetJobStateCommand(String jobId) {
        super(jobId);
    }

    @Override
    public void execute() throws Exception {
        HttpGet request = new HttpGet(resourceUrl("jobs/" + jobId));
        HttpResponse response = execute(request);
        if (statusCode(OK) == statusCode(response)) {
            JobStateView jobState = readValue(response, JobStateView.class);

            String jobInfo = (new StringBuilder()).append(job())
                    .append("\tNAME: ").append(jobState.getName())
                    .append("\tOWNER: ").append(jobState.getOwner())
                    .append("\tSTATUS: ")
                    .append(jobState.getJobInfo().getStatus())
                    .append("\t#TASKS: ")
                    .append(jobState.getJobInfo().getTotalNumberOfTasks())
                    .toString();

            // create formatter
            ObjectArrayFormatter oaf = new ObjectArrayFormatter();
            oaf.setMaxColumnLength(30);
            // space between column
            oaf.setSpace(2);
            // title line
            List<String> list = new ArrayList<String>();
            list.add("ID");
            list.add("NAME");
            list.add("ITER");
            list.add("DUP");
            list.add("STATUS");
            list.add("HOSTNAME");
            list.add("EXEC DURATION");
            list.add("TOT DURATION");
            list.add("#NODES USED");
            list.add("#EXECUTIONS");
            list.add("#NODES KILLED");
            oaf.setTitle(list);
            // separator
            oaf.addEmptyLine();
            // add each lines
            Collection<TaskStateView> tasks = jobState.getTasks().values();
            // TaskState.setSortingBy(sort);
            // Collections.sort(tasks);
            for (TaskStateView taskState : tasks) {
                list = new ArrayList<String>();
                TaskInfoView taskInfo = taskState.getTaskInfo();
                TaskIdView taskId = taskInfo.getTaskId();

                list.add(taskId.getId());
                list.add(taskId.getReadableName());
                list.add((taskState.getIterationIndex() > 0) ? ""
                        + taskState.getIterationIndex() : "");
                list.add((taskState.getReplicationIndex() > 0) ? ""
                        + taskState.getReplicationIndex() : "");
                list.add(taskInfo.getTaskStatus());
                list.add((taskInfo.getExecutionHostName() == null) ? "unknown"
                        : taskInfo.getExecutionHostName());
                list.add(Tools.getFormattedDuration(0,
                        taskInfo.getExecutionDuration()));
                list.add(Tools.getFormattedDuration(taskInfo.getFinishedTime(),
                        taskInfo.getStartTime()));
                list.add("" + taskState.getNumberOfNodesNeeded());
                if (taskState.getMaxNumberOfExecution()
                        - taskInfo.getNumberOfExecutionLeft() < taskState
                            .getMaxNumberOfExecution()) {
                    list.add((taskState.getMaxNumberOfExecution()
                            - taskInfo.getNumberOfExecutionLeft() + 1)
                            + "/" + taskState.getMaxNumberOfExecution());
                } else {
                    list.add((taskState.getMaxNumberOfExecution() - taskInfo
                            .getNumberOfExecutionLeft())
                            + "/"
                            + taskState.getMaxNumberOfExecution());
                }
                list.add((taskState.getMaxNumberOfExecutionOnFailure() - taskInfo
                        .getNumberOfExecutionOnFailureLeft())
                        + "/"
                        + taskState.getMaxNumberOfExecutionOnFailure());
                oaf.addLine(list);
            }
            // print formatter
            writeLine(jobInfo);
            writeLine("");
            writeLine(string(oaf));
        } else {
            handleError(String.format(
                    "An error occurred while retrieving %s state:", job()),
                    response);
        }

    }

}
