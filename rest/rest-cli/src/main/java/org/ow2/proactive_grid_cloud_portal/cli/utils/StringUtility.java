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
package org.ow2.proactive_grid_cloud_portal.cli.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.StringUtils;
import org.ow2.proactive.utils.ObjectArrayFormatter;
import org.ow2.proactive.utils.ObjectByteConverter;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive_grid_cloud_portal.cli.json.MBeanInfoView;
import org.ow2.proactive_grid_cloud_portal.cli.json.NodeEventView;
import org.ow2.proactive_grid_cloud_portal.cli.json.NodeSourceView;
import org.ow2.proactive_grid_cloud_portal.cli.json.TopologyView;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStatusData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskIdData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskInfoData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStateData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskStatusData;
import org.ow2.proactive_grid_cloud_portal.scheduler.dto.UserJobData;
import org.ow2.proactive_grid_cloud_portal.utils.ObjectUtility;


public final class StringUtility {

    private StringUtility() {
    }

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

        List<String> titles = new ArrayList<>(3);
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
                    line = new ArrayList<>(3);
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

        List<String> titles = new ArrayList<>(7);
        titles.add("SOURCE_NAME");
        titles.add("HOST_NAME");
        titles.add("STATE");
        titles.add("SINCE");
        titles.add("LOCKED");
        titles.add("LOCKED_BY");
        titles.add("LOCK_TIME");
        titles.add("URL");
        titles.add("PROVIDER");
        titles.add("USED_BY");
        formatter.setTitle(titles);

        formatter.addEmptyLine();

        if (nodeEvents != null) {
            for (NodeEventView nodeEvent : nodeEvents) {
                List<String> line = new ArrayList<>(10);
                line.add(nodeEvent.getNodeSource());
                line.add(nodeEvent.getHostName());
                line.add(nodeEvent.getNodeState());
                line.add(getStateChangeTime(nodeEvent));
                line.add(nodeEvent.isLocked());
                line.add(toStringNullable(nodeEvent.getNodeLocker()));
                line.add(getLockTime(nodeEvent));
                line.add(nodeEvent.getNodeUrl());
                line.add(toStringNullable(nodeEvent.getNodeProvider()));
                line.add(toStringNullable(nodeEvent.getNodeOwner()));
                formatter.addLine(line);
            }
        }
        return Tools.getStringAsArray(formatter);
    }

    private static String getStateChangeTime(NodeEventView nodeEvent) {
        long timestamp = Long.parseLong(nodeEvent.getTimeStamp());
        String stateChangeTime = Tools.getFormattedDate(timestamp);
        if (timestamp != -1) {
            stateChangeTime += " (" + Tools.getElapsedTime(timestamp) + ")";
        }
        return stateChangeTime;
    }

    private static String getLockTime(NodeEventView nodeEvent) {
        long lockTime = Long.parseLong(nodeEvent.getLockTime());

        if (lockTime == -1) {
            return "";
        }

        return Tools.getFormattedDate(lockTime) + " (" + Tools.getElapsedTime(lockTime) + ")";
    }

    private static String getLockState(NodeEventView nodeEvent) {
        String lockState = nodeEvent.isLocked();

        if (Boolean.TRUE.toString().equalsIgnoreCase(lockState)) {
            lockState += " (by " + nodeEvent.getNodeLocker() + " since ";
            lockState += Tools.getFormattedDate(Long.parseLong(nodeEvent.getLockTime()));
            lockState += ")";
        }

        return lockState;
    }

    public static String string(NodeSourceView[] nodeSources) {
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(80);
        formatter.setSpace(4);

        List<String> titles = new ArrayList<>(3);
        titles.add("SOURCE_NAME");
        titles.add("DESCRIPTION");
        titles.add("ADMINISTRATOR");
        formatter.setTitle(titles);

        formatter.addEmptyLine();

        for (NodeSourceView ns : nodeSources) {
            List<String> line = new ArrayList<>(3);
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
        List<String> titles = new ArrayList<>(2);
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
            org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobResultData jobResult)
            throws IOException, ClassNotFoundException {
        StringBuilder buffer = new StringBuilder();
        buffer.append(String.format("%s result:\n", id));
        Map<String, TaskResultData> allResults = jobResult.getAllResults();
        for (Map.Entry<String, TaskResultData> entry : allResults.entrySet()) {
            buffer.append(String.format(entry.getKey() + " : " +
                                        ObjectUtility.object(ObjectByteConverter.base64StringToByteArray(entry.getValue()
                                                                                                              .getSerializedValue()))))
                  .append('\n');
        }
        return buffer.toString();
    }

    public static String jobStateAsString(String id,
            org.ow2.proactive_grid_cloud_portal.scheduler.dto.JobStateData jobState) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(id)
              .append("\tNAME: ")
              .append(jobState.getName())
              .append("\tOWNER: ")
              .append(jobState.getOwner())
              .append("\tTENANT: ")
              .append(jobState.getTenant())
              .append("\tDOMAIN: ")
              .append(jobState.getDomain())
              .append("\tSTATUS: ")
              .append(jobState.getJobInfo().getStatus())
              .append("\t#TASKS: ")
              .append(jobState.getJobInfo().getTotalNumberOfTasks());

        buffer.append('\n');

        Collection<TaskStateData> tasks = jobState.getTasks().values();
        buffer.append(taskStatesAsString(tasks, true));
        return buffer.toString();
    }

    public static String taskStatesAsString(Collection<TaskStateData> tasks, boolean displayTags) {
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
        formatter.addEmptyLine();

        for (TaskStateData taskState : tasks) {
            list = new ArrayList<>();
            TaskInfoData taskInfo = taskState.getTaskInfo();
            TaskIdData taskId = taskInfo.getTaskId();

            list.add(String.valueOf(taskId.getId()));
            list.add(taskId.getReadableName());
            if (displayTags) {
                list.add(toStringNullable(taskState.getTag()));
            }
            list.add((taskState.getIterationIndex() > 0) ? "" + taskState.getIterationIndex() : "");
            list.add((taskState.getReplicationIndex() > 0) ? "" + taskState.getReplicationIndex() : "");
            list.add(taskInfo.getTaskStatus().toString());
            list.add(toStringNullable(taskInfo.getExecutionHostName(), "unknown"));

            if (taskInfo.getTaskStatus() == TaskStatusData.IN_ERROR) {
                list.add(Tools.getFormattedDuration(taskInfo.getInErrorTime(), taskInfo.getStartTime()));
            } else {
                list.add(Tools.getFormattedDuration(0, taskInfo.getExecutionDuration()));
            }

            list.add(Tools.getFormattedDuration(taskInfo.getFinishedTime(), taskInfo.getStartTime()));
            list.add("" + taskState.getNumberOfNodesNeeded());

            list.add((taskState.getMaxNumberOfExecution() - taskInfo.getNumberOfExecutionLeft()) + "/" +
                     taskState.getMaxNumberOfExecution());
            list.add((taskState.getMaxNumberOfExecutionOnFailure() - taskInfo.getNumberOfExecutionOnFailureLeft()) +
                     "/" + taskState.getMaxNumberOfExecutionOnFailure());

            formatter.addLine(list);
        }

        return Tools.getStringAsArray(formatter);
    }

    public static String jobsAsString(List<UserJobData> jobs) {
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(30);
        formatter.setSpace(4);

        List<String> columnNames = new ArrayList<>(7);
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

        if (userJobInfo.getJobInfo().getStatus() == JobStatusData.IN_ERROR) {
            row.add(StringUtility.formattedDuration(startTime, jobInfo.getInErrorTime()));
        } else {
            row.add(StringUtility.formattedDuration(startTime, jobInfo.getFinishedTime()));
        }

        return row;
    }

    public static String taskResultsAsString(List<TaskResultData> results) throws IOException, ClassNotFoundException {
        StringBuffer buf = new StringBuffer();
        for (TaskResultData currentResult : results) {
            buf.append(taskResultAsString(currentResult.getId().getReadableName(), currentResult));
            buf.append("\n");
        }
        return buf.toString();
    }

    public static String taskResultAsString(String id,
            org.ow2.proactive_grid_cloud_portal.scheduler.dto.TaskResultData taskResult)
            throws IOException, ClassNotFoundException {
        return String.format("%s result: %s",
                             id,
                             ObjectUtility.object(ObjectByteConverter.base64StringToByteArray(taskResult.getSerializedValue()))
                                          .toString());
    }

    public static String statsAsString(Map<String, String> stats) {
        ObjectArrayFormatter formatter = new ObjectArrayFormatter();
        formatter.setMaxColumnLength(80);
        formatter.setSpace(2);
        List<String> columnNames = new ArrayList<>(2);
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

    private static String toStringNullable(String obj) {
        return toStringNullable(obj, "");
    }

    private static String toStringNullable(String obj, String defaultValue) {
        if (obj == null) {
            return defaultValue;
        }

        return obj;
    }

}
