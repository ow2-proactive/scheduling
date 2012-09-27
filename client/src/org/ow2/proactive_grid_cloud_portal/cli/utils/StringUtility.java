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
 * %$ACTIVEEON_INITIAL_DEV$
 */

package org.ow2.proactive_grid_cloud_portal.cli.utils;

import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_IO_ERROR;
import static org.ow2.proactive_grid_cloud_portal.cli.CLIException.REASON_OTHER;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.ow2.proactive.utils.ObjectArrayFormatter;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive_grid_cloud_portal.cli.CLIException;
import org.ow2.proactive_grid_cloud_portal.cli.json.JobResultView;
import org.ow2.proactive_grid_cloud_portal.cli.json.JobStateView;
import org.ow2.proactive_grid_cloud_portal.cli.json.MBeanInfoView;
import org.ow2.proactive_grid_cloud_portal.cli.json.NodeEventView;
import org.ow2.proactive_grid_cloud_portal.cli.json.NodeSourceView;
import org.ow2.proactive_grid_cloud_portal.cli.json.SchedulerStateView;
import org.ow2.proactive_grid_cloud_portal.cli.json.TaskIdView;
import org.ow2.proactive_grid_cloud_portal.cli.json.TaskInfoView;
import org.ow2.proactive_grid_cloud_portal.cli.json.TaskResultView;
import org.ow2.proactive_grid_cloud_portal.cli.json.TaskStateView;
import org.ow2.proactive_grid_cloud_portal.cli.json.TopologyView;

public class StringUtility {

    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    public static boolean isEmpty(String[] array) {
        return array == null || array.length == 0;
    }

    public static String objectArrayFormatterAsString(ObjectArrayFormatter oaf) {
        return Tools.getStringAsArray(oaf);
    }

    public static String formattedDate(long time) {
        return Tools.getFormattedDate(time);
    }

    public static String formattedElapsedTime(long time) {
        return Tools.getElapsedTime(time);
    }

    public static String formattedDuration(long start, long end) {
        return Tools.getFormattedDuration(start, end);
    }

    public static String responseAsString(HttpResponseWrapper response) {
            return StringUtils.newStringUtf8(response.getContent());
    }

    public static String string(TopologyView topology) {
        StringBuilder buffer = new StringBuilder();
        Set<String> hostList = topology.getDistances().keySet();
        buffer.append(String.format("%nHost list(%d):", hostList.size()));
        for (String host : hostList) {
            buffer.append(String.format("%s", host));
        }
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(80);
        formatter.setSpace(4);

        List<String> titles = new ArrayList<String>();
        titles.add("Host");
        titles.add("Distance (µs)");
        titles.add("Host");
        formatter.setTitle(titles);
        formatter.addEmptyLine();

        List<String> line;
        for (String host : hostList) {
            Map<String, String> hostTopology = topology.getDistances()
                    .get(host);
            if (hostTopology != null) {
                for (String anotherHost : hostTopology.keySet()) {
                    line = new ArrayList<String>();
                    line.add(host);
                    line.add(hostTopology.get(anotherHost));
                    line.add(anotherHost);
                    formatter.addLine(line);
                }
            }
        }

        buffer.append(objectArrayFormatterAsString(formatter));
        return buffer.toString();
    }

    public static String string(NodeEventView[] nodeEvents) {
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(80);
        formatter.setSpace(4);

        List<String> titles = new ArrayList<String>();
        titles.add("SOURCE_NAME");
        titles.add("HOST_NAME");
        titles.add("STATE");
        titles.add("SINCE");
        titles.add("URL");
        titles.add("PROVIDER");
        titles.add("USED_BY");
        formatter.setTitle(titles);

        formatter.addEmptyLine();

        if (nodeEvents != null) {
            for (NodeEventView nodeEvent : nodeEvents) {
                List<String> line = new ArrayList<String>();
                line.add(nodeEvent.getNodeSource());
                line.add(nodeEvent.getHostName());
                line.add(nodeEvent.getNodeState().toString());
                long timestamp = Long.parseLong(nodeEvent.getTimeStamp());
                String date = Tools.getFormattedDate(timestamp);
                if (timestamp != -1) {
                    date += " (" + Tools.getElapsedTime(timestamp) + ")";
                }
                line.add(date);
                line.add(nodeEvent.getNodeUrl());
                line.add(nodeEvent.getNodeProvider() == null ? "" : nodeEvent
                        .getNodeProvider());
                line.add(nodeEvent.getNodeOwner() == null ? "" : nodeEvent
                        .getNodeOwner());
                formatter.addLine(line);
            }
        }
        return Tools.getStringAsArray(formatter);
    }

    public static String string(NodeSourceView[] nodeSources) {
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(80);
        formatter.setSpace(4);

        List<String> titles = new ArrayList<String>();
        titles.add("SOURCE_NAME");
        titles.add("DESCRIPTION");
        titles.add("ADMINISTRATOR");
        formatter.setTitle(titles);

        formatter.addEmptyLine();

        for (NodeSourceView ns : nodeSources) {
            List<String> line = new ArrayList<String>();
            line.add(ns.getSourceName());
            line.add(ns.getSourceDescription());
            line.add(ns.getNodeSourceAdmin());
            formatter.addLine(line);
        }
        return Tools.getStringAsArray(formatter);
    }

    public static String mBeanInfoAsString(MBeanInfoView[] infoViews) {
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(80);
        formatter.setSpace(4);
        List<String> titles = new ArrayList<String>();
        titles.add("Stats:");
        titles.add("");
        formatter.setTitle(titles);
        formatter.addEmptyLine();
        for (MBeanInfoView infoView : infoViews) {
            List<String> line = new ArrayList<String>();
            line.add("" + infoView.getName());
            line.add("" + infoView.getValue());
            formatter.addLine(line);
        }
        return Tools.getStringAsArray(formatter);
    }

    public static String jobOutputAsString(String id, Map<String, String> output) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("%s output:\n", id));
        for (String key : output.keySet()) {
            buffer.append(String.format("%s : %s\n", key, output.get(key)));
        }
        return buffer.toString();
    }

    public static String jobResultAsString(String id, JobResultView jobResult) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("%s result:\n", id));
        Map<String, TaskResultView> allResults = jobResult.getAllResults();
        for (String taskName : allResults.keySet()) {
            buffer.append(String.format(taskName
                    + " : "
                    + ObjectUtility.object(allResults.get(taskName)
                            .getSerializedValue())) + '\n');
        }
        return buffer.toString();
    }

    public static String jobStateAsString(String id, JobStateView jobState) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(id).append("\tNAME: ").append(jobState.getName())
                .append("\tOWNER: ").append(jobState.getOwner())
                .append("\tSTATUS: ").append(jobState.getJobInfo().getStatus())
                .append("\t#TASKS: ")
                .append(jobState.getJobInfo().getTotalNumberOfTasks())
                .toString();
        // create formatter
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(80);
        // space between column
        formatter.setSpace(4);
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
        formatter.setTitle(list);
        // separator
        formatter.addEmptyLine();
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
            formatter.addLine(list);
        }
        buffer.append('\n');
        buffer.append(Tools.getStringAsArray(formatter));
        return buffer.toString();
    }
    
    public static String schedulerStateAsString(SchedulerStateView schedulerState) {
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(30);
        formatter.setSpace(4);

        List<String> columnNames = new ArrayList<String>();
        columnNames.add("ID");
        columnNames.add("NAME");
        columnNames.add("OWNER");
        columnNames.add("PRIORITY");
        columnNames.add("PROJECT");
        columnNames.add("STATUS");
        columnNames.add("START AT");
        columnNames.add("DURATION");
        formatter.setTitle(columnNames);

        formatter.addEmptyLine();

        List<JobStateView> pendingJobs = Arrays.asList(schedulerState
                .getPendingJobs());
        Collections.sort(pendingJobs);
        List<JobStateView> runningJobs = Arrays.asList(schedulerState
                .getRunningJobs());
        Collections.sort(runningJobs);
        List<JobStateView> finishedJobs = Arrays.asList(schedulerState
                .getFinishedJobs());
        Collections.sort(finishedJobs);

        for (JobStateView js : finishedJobs) {
            formatter.addLine(rowList(js));
        }

        if (runningJobs.size() > 0) {
            formatter.addEmptyLine();
        }
        for (JobStateView js : schedulerState.getRunningJobs()) {
            formatter.addLine(rowList(js));
        }

        if (pendingJobs.size() > 0) {
            formatter.addEmptyLine();
        }
        for (JobStateView js : pendingJobs) {
            formatter.addLine(rowList(js));
        }

        return Tools.getStringAsArray(formatter);
    }

    private static List<String> rowList(JobStateView js) {
        List<String> row = new ArrayList<String>();
        row.add(String.valueOf(js.getId()));
        row.add(js.getName());
        row.add(js.getOwner());
        row.add(js.getPriority().toString());
        row.add(js.getProjectName());
        row.add(js.getJobInfo().getStatus());

        String date = StringUtility.formattedDate(js.getJobInfo()
                .getStartTime());
        if (js.getJobInfo().getStartTime() != -1)
            date += " ("
                    + StringUtility.formattedElapsedTime(js.getJobInfo()
                            .getStartTime()) + ")";
        row.add(date);
        row.add(StringUtility.formattedDuration(js.getJobInfo().getStartTime(),
                js.getJobInfo().getFinishedTime()));

        return row;
    }
    public static String taskOutputAsString(String id, String response) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("%s output:", id));
        buffer.append(String.format("%s", response));
        return buffer.toString();
    }

    public static String taskResultAsString(String id, TaskResultView taskResult) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("%s result:", id));
        buffer.append(ObjectUtility.object(taskResult.getSerializedValue())
                .toString());
        return buffer.toString();
    }
    
    public static String statsAsString(Map<String,String> stats) {
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(80);
        formatter.setSpace(2);
        List<String> columnNames = new ArrayList<String>();
        columnNames.add("");
        columnNames.add("");
        formatter.setTitle(columnNames);
        for (Entry<String, String> e : stats.entrySet()) {
            List<String> row = new ArrayList<String>();
            row.add(e.getKey());
            row.add(e.getValue());
            formatter.addLine(row);
        }
        return Tools.getStringAsArray(formatter);
    }
    
    public static String stackTraceAsString(Throwable error) {
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);
        error.printStackTrace(writer);
        writer.flush();
        return out.toString();
    }

    private StringUtility() {
    }

}
