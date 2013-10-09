package org.ow2.proactive.scheduler.core.db;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskLogs;
import org.ow2.proactive.scheduler.common.task.flow.FlowAction;
import org.ow2.proactive.scheduler.task.TaskResultImpl;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;


@Entity
@Table(name = "TASK_RESULT_DATA")
public class TaskResultData {

    private long id;

    private TaskData taskRuntimeData;

    private long resultTime;

    private byte[] serializedValue;

    private byte[] serializedException;

    private String previewerClassName;

    private Map<String, String> propagatedProperties;
    
    private Map<String, byte[]> propagatedVariables;

    private FlowActionData flowAction;

    private TaskLogs logs;

    TaskResultImpl toTaskResult(TaskId taskId, String[] jobClasspath) {

        TaskResultImpl result = new TaskResultImpl(taskId, getSerializedValue(), getSerializedException(),
            getLogs(), getPropagatedProperties(), getPropagatedVariables());

        result.setPreviewerClassName(getPreviewerClassName());
        result.setJobClasspath(jobClasspath);
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
        resultData.setPropagatedProperties(result.getPropagatedProperties());
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

    @Column(name = "PROPAGATED_PROPERTIES", updatable = false)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Map<String, String> getPropagatedProperties() {
        return propagatedProperties;
    }

    public void setPropagatedProperties(Map<String, String> propagatedProperties) {
        this.propagatedProperties = propagatedProperties;
    }

    @Column(name = "PROPAGATED_VARIABLES")
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Map<String, byte[]> getPropagatedVariables() {
        return propagatedVariables;
    }

    public void setPropagatedVariables(Map<String, byte[]> executionVariables) {
        this.propagatedVariables = executionVariables;
    }
}
