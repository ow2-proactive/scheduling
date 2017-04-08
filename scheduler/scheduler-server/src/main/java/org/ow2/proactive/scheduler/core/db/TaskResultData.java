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
package org.ow2.proactive.scheduler.core.db;

import java.util.Map;

import javax.persistence.*;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.task.TaskResultImpl;


@Entity
@NamedQueries({ @NamedQuery(name = "loadJobResult", query = "select taskResult, " + "task.id, " + "task.taskName, " +
                                                            "task.preciousResult from TaskResultData as taskResult left outer join taskResult.taskRuntimeData as task " +
                                                            "where task.jobData = :job order by task.id, taskResult.resultTime desc"),
                @NamedQuery(name = "loadTasksResultByJobAndTaskName", query = "select id, taskName from TaskData where taskName = :taskName and jobData = :job"),
                @NamedQuery(name = "loadTasksResultByTask", query = "from TaskResultData result where result.taskRuntimeData = :task order by result.resultTime desc"),
                @NamedQuery(name = "loadTasksResults", query = "select taskResult, " + "task.id, " + "task.taskName, " +
                                                               "task.preciousResult from TaskResultData as taskResult join taskResult.taskRuntimeData as task " + "where task.id in (:tasksIds) order by task.id, taskResult.resultTime desc") })
@Table(name = "TASK_RESULT_DATA", indexes = { @Index(name = "TASK_RESULT_DATA_RUNTIME_DATA", columnList = "JOB_ID,TASK_ID") })
public class TaskResultData {

    private long id;

    private TaskData taskRuntimeData;

    private long resultTime;

    private byte[] serializedValue;

    private byte[] serializedException;

    private String previewerClassName;

    private Map<String, byte[]> propagatedVariables;

    private FlowActionData flowAction;

    private TaskLogs logs;

    private Map<String, String> metadata;

    TaskResultImpl toTaskResult(TaskId taskId) {

        TaskResultImpl result = new TaskResultImpl(taskId,
                                                   getSerializedValue(),
                                                   getSerializedException(),
                                                   getLogs(),
                                                   getMetadata(),
                                                   getPropagatedVariables());

        result.setPreviewerClassName(getPreviewerClassName());
        FlowActionData actionData = getFlowAction();
        if (actionData != null) {
            FlowAction action = new FlowAction(actionData.getType());
            action.setDupNumber(actionData.getDupNumber());
            action.setTarget(actionData.getTarget());
            action.setTargetContinuation(actionData.getTargetContinuation());
            action.setTargetElse(actionData.getTargetElse());
            result.setAction(action);
        }

        return result;
    }

    static TaskResultData createTaskResultData(TaskData taskRuntimeData, TaskResultImpl result) {
        TaskResultData resultData = new TaskResultData();
        resultData.setTaskRuntimeData(taskRuntimeData);
        resultData.setLogs(result.getOutput());
        resultData.setPreviewerClassName(result.getPreviewerClassName());
        resultData.setMetadata(result.getMetadata());
        resultData.setPropagatedVariables(result.getPropagatedVariables());
        resultData.setSerializedException(result.getSerializedException());
        resultData.setSerializedValue(result.getSerializedValue());
        resultData.setResultTime(System.currentTimeMillis());

        FlowAction flowAction = result.getAction();
        if (flowAction != null) {
            FlowActionData actionData = new FlowActionData();
            actionData.setDupNumber(flowAction.getDupNumber());
            actionData.setTarget(flowAction.getTarget());
            actionData.setTargetContinuation(flowAction.getTargetContinuation());
            actionData.setTargetElse(flowAction.getTargetElse());
            actionData.setType(flowAction.getType());
            resultData.setFlowAction(actionData);
        }

        return resultData;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TASK_RESULT_DATA_ID_SEQUENCE")
    @SequenceGenerator(name = "TASK_RESULT_DATA_ID_SEQUENCE", sequenceName = "TASK_RESULT_DATA_ID_SEQUENCE")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "LOGS", length = Integer.MAX_VALUE)
    @Lob
    public TaskLogs getLogs() {
        return logs;
    }

    public void setLogs(TaskLogs logs) {
        this.logs = logs;
    }

    @Embedded
    public FlowActionData getFlowAction() {
        return flowAction;
    }

    public void setFlowAction(FlowActionData flowAction) {
        this.flowAction = flowAction;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumns(value = { @JoinColumn(name = "JOB_ID", referencedColumnName = "TASK_ID_JOB"),
                           @JoinColumn(name = "TASK_ID", referencedColumnName = "TASK_ID_TASK") })
    public TaskData getTaskRuntimeData() {
        return taskRuntimeData;
    }

    public void setTaskRuntimeData(TaskData taskRuntimeData) {
        this.taskRuntimeData = taskRuntimeData;
    }

    @Column(name = "RESULT_TIME", nullable = false)
    public long getResultTime() {
        return resultTime;
    }

    public void setResultTime(long resultTime) {
        this.resultTime = resultTime;
    }

    @Column(name = "RESULT_VALUE", length = Integer.MAX_VALUE)
    @Lob
    public byte[] getSerializedValue() {
        return serializedValue;
    }

    public void setSerializedValue(byte[] serializedValue) {
        this.serializedValue = serializedValue;
    }

    @Column(name = "RESULT_EXCEPTION", length = Integer.MAX_VALUE)
    @Lob
    public byte[] getSerializedException() {
        return serializedException;
    }

    public void setSerializedException(byte[] serializedException) {
        this.serializedException = serializedException;
    }

    @Column(name = "PREVIEWER_CLASS", length = 1000)
    public String getPreviewerClassName() {
        return previewerClassName;
    }

    public void setPreviewerClassName(String previewerClassName) {
        this.previewerClassName = previewerClassName;
    }

    @Column(name = "PROPAGATED_VARIABLES", length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Map<String, byte[]> getPropagatedVariables() {
        return propagatedVariables;
    }

    @Column(name = "METADATA", length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public void setPropagatedVariables(Map<String, byte[]> executionVariables) {
        this.propagatedVariables = executionVariables;
    }
}
