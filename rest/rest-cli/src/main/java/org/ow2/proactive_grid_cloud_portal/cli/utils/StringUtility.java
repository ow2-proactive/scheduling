/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.StringUtils;
import org.ow2.proactive.utils.ObjectArrayFormatter;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive_grid_cloud_portal.cli.json.MBeanInfoView;
import org.ow2.proactive_grid_cloud_portal.cli.json.NodeEventView;
import org.ow2.proactive_grid_cloud_portal.cli.json.NodeSourceView;
import org.ow2.proactive_grid_cloud_portal.cli.json.TopologyView;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskInfoData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.UserJobData;
import org.ow2.proactive_grid_cloud_portal.utils.ObjectUtility;


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

        List<String> titles = new ArrayList<>();
        titles.add("Host");
        titles.add("Distance (µs)");
        titles.add("Host");
        formatter.setTitle(titles);
        formatter.addEmptyLine();

        List<String> line;
        for (String host : hostList) {
            Map<String, String> hostTopology = topology.getDistances().get(host);
            if (hostTopology != null) {
                for (String anotherHost : hostTopology.keySet()) {
                    line = new ArrayList<>();
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
        formatter.setMaxColumnLength(300);
        formatter.setSpace(4);

        List<String> titles = new ArrayList<>();
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
                List<String> line = new ArrayList<>();
                line.add(nodeEvent.getNodeSource());
                line.add(nodeEvent.getHostName());
                line.add(nodeEvent.getNodeState());
                long timestamp = Long.parseLong(nodeEvent.getTimeStamp());
                String date = Tools.getFormattedDate(timestamp);
                if (timestamp != -1) {
                    date += " (" + Tools.getElapsedTime(timestamp) + ")";
                }
                line.add(date);
                line.add(nodeEvent.getNodeUrl());
                line.add(nodeEvent.getNodeProvider() == null ? "" : nodeEvent.getNodeProvider());
                line.add(nodeEvent.getNodeOwner() == null ? "" : nodeEvent.getNodeOwner());
                formatter.addLine(line);
            }
        }
        return Tools.getStringAsArray(formatter);
    }

    public static String string(NodeSourceView[] nodeSources) {
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(80);
        formatter.setSpace(4);

        List<String> titles = new ArrayList<>();
        titles.add("SOURCE_NAME");
        titles.add("DESCRIPTION");
        titles.add("ADMINISTRATOR");
        formatter.setTitle(titles);

        formatter.addEmptyLine();

        for (NodeSourceView ns : nodeSources) {
            List<String> line = new ArrayList<>();
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
        List<String> titles = new ArrayList<>();
        titles.add("Stats:");
        titles.add("");
        formatter.setTitle(titles);
        formatter.addEmptyLine();
        for (MBeanInfoView infoView : infoViews) {
            List<String> line = new ArrayList<>();
            line.add("" + infoView.getName());
            line.add("" + infoView.getValue());
            formatter.addLine(line);
        }
        return Tools.getStringAsArray(formatter);
    }

    public static String jobResultAsString(String id,
            org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobResultData jobResult) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("%s result:\n", id));
        Map<String, TaskResultData> allResults = jobResult.getAllResults();
        for (String taskName : allResults.keySet()) {
            buffer.append(
                    String.format(taskName + " : " +
                        ObjectUtility.object(allResults.get(taskName).getSerializedValue()))).append('\n');
        }
        return buffer.toString();
    }

    public static String jobStateAsString(String id,
            org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStateData jobState) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(id).append("\tNAME: ").append(jobState.getName()).append("\tOWNER: ").append(
                jobState.getOwner()).append("\tSTATUS: ").append(jobState.getJobInfo().getStatus()).append(
                "\t#TASKS: ").append(jobState.getJobInfo().getTotalNumberOfTasks());

        buffer.append('\n');

        Collection<TaskStateData> tasks = jobState.getTasks().values();
        buffer.append(taskStatesAsString(tasks, true));
        return buffer.toString();
    }




    public static String taskStatesAsString(Collection<TaskStateData> tasks, boolean displayTags){
        // create formatter
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(80);
        // space between column
        formatter.setSpace(4);
        // title line
        List<String> list = new ArrayList<>();
        list.add("ID");
        list.add("NAME");
        if (displayTags) {
            list.add("TAG");
        }
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

        // TaskState.setSortingBy(sort);
        // Collections.sort(tasks);
        for (TaskStateData taskState : tasks) {
            list = new ArrayList<>();
            TaskInfoData taskInfo = taskState.getTaskInfo();
            TaskIdData taskId = taskInfo.getTaskId();

            list.add(String.valueOf(taskId.getId()));
            list.add(taskId.getReadableName());
            if (displayTags) {
                list.add((taskState.getTag() != null) ? taskState.getTag() : "");
            }
            list.add((taskState.getIterationIndex() > 0) ? "" + taskState.getIterationIndex() : "");
            list.add((taskState.getReplicationIndex() > 0) ? "" + taskState.getReplicationIndex() : "");
            list.add(taskInfo.getTaskStatus().toString());
            list.add((taskInfo.getExecutionHostName() == null) ? "unknown" : taskInfo.getExecutionHostName());
            list.add(Tools.getFormattedDuration(0, taskInfo.getExecutionDuration()));
            list.add(Tools.getFormattedDuration(taskInfo.getFinishedTime(), taskInfo.getStartTime()));
            list.add("" + taskState.getNumberOfNodesNeeded());
            if (taskState.getMaxNumberOfExecution() - taskInfo.getNumberOfExecutionLeft() < taskState
                    .getMaxNumberOfExecution()) {
                list.add((taskState.getMaxNumberOfExecution() - taskInfo.getNumberOfExecutionLeft() + 1) +
                        "/" + taskState.getMaxNumberOfExecution());
            } else {
                list.add((taskState.getMaxNumberOfExecution() - taskInfo.getNumberOfExecutionLeft()) + "/" +
                        taskState.getMaxNumberOfExecution());
            }
            list.add((taskState.getMaxNumberOfExecutionOnFailure() - taskInfo
                    .getNumberOfExecutionOnFailureLeft()) +
                    "/" + taskState.getMaxNumberOfExecutionOnFailure());
            formatter.addLine(list);
        }

        return Tools.getStringAsArray(formatter);
    }

    public static String jobsAsString(List<UserJobData> jobs) {
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(30);
        formatter.setSpace(4);

        List<String> columnNames = new ArrayList<>();
        columnNames.add("ID");
        columnNames.add("NAME");
        columnNames.add("OWNER");
        columnNames.add("PRIORITY");
        columnNames.add("STATUS");
        columnNames.add("START AT");
        columnNames.add("DURATION");
        formatter.setTitle(columnNames);

        formatter.addEmptyLine();

        for (UserJobData job : jobs) {
            formatter.addLine(rowList(job));
        }

        return Tools.getStringAsArray(formatter);
    }

    public static String credentialsKeysAsString(Set<String> keys) {
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(30);
        formatter.setSpace(4);

        formatter.setTitle(Collections.singletonList("KEY"));

        formatter.addEmptyLine();

        for (String key : keys) {
            formatter.addLine(Collections.singletonList(key));
        }

        return Tools.getStringAsArray(formatter);
    }

    private static List<String> rowList(UserJobData userJobInfo) {
        org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobInfoData jobInfo = userJobInfo.getJobInfo();

        List<String> row = new ArrayList<>();
        row.add(String.valueOf(jobInfo.getJobId().getId()));
        row.add(jobInfo.getJobId().getReadableName());
        row.add(userJobInfo.getJobOwner());
        row.add(jobInfo.getPriority().toString());
        row.add(jobInfo.getStatus().toString());

        long startTime = jobInfo.getStartTime();
        String date = StringUtility.formattedDate(startTime);
        if (startTime != -1)
            date += " (" + StringUtility.formattedElapsedTime(startTime) + ")";
        row.add(date);
        row.add(StringUtility.formattedDuration(startTime, jobInfo.getFinishedTime()));

        return row;
    }


    public static String taskResultsAsString(List<TaskResultData> results){
        StringBuffer buf = new StringBuffer();
        for(TaskResultData currentResult: results){
            buf.append(taskResultAsString(currentResult.getId().getReadableName(), currentResult));
            buf.append("\n");
        }
        return buf.toString();
    }


    public static String taskResultAsString(String id,
            org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData taskResult) {
        return String.format("%s result: %s", id, ObjectUtility.object(taskResult.getSerializedValue())
                .toString());
    }

    public static String statsAsString(Map<String, String> stats) {
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(80);
        formatter.setSpace(2);
        List<String> columnNames = new ArrayList<>();
        columnNames.add("");
        columnNames.add("");
        formatter.setTitle(columnNames);
        for (Entry<String, String> e : stats.entrySet()) {
            List<String> row = new ArrayList<>();
            row.add(e.getKey());
            row.add(e.getValue());
            formatter.addLine(row);
        }
        return Tools.getStringAsArray(formatter);
    }

    private StringUtility() {
    }
}
