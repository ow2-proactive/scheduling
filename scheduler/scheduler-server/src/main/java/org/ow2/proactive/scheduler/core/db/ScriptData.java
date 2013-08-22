package org.ow2.proactive.scheduler.core.db;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scripting.GenerationScript;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;


@Entity
@Table(name = "SCRIPT_DATA")
@BatchSize(size = 100)
public class ScriptData {

    private long id;

    private String scriptEngine;

    private String script;

    private List<String> scriptParameters;

    private boolean selectionScriptDynamic;

    private String flowScriptActionType;

    private String flowScriptTarget;

    private String flowScriptTargetElse;

    private String flowScriptTargetContinuation;

    private TaskData taskData;

    static ScriptData createForSelectionScript(SelectionScript script, TaskData taskData) {
        ScriptData scriptData = new ScriptData();
        initCommonAttributes(scriptData, script);
        scriptData.setSelectionScriptDynamic(script.isDynamic());
        scriptData.setTaskData(taskData);
        return scriptData;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = { @JoinColumn(name = "JOB_ID", referencedColumnName = "TASK_ID_JOB"),
            @JoinColumn(name = "TASK_ID", referencedColumnName = "TASK_ID_TASK") })
    public TaskData getTaskData() {
        return taskData;
    }

    public void setTaskData(TaskData taskData) {
        this.taskData = taskData;
    }

    static ScriptData createForScript(Script<?> script) {
        ScriptData scriptData = new ScriptData();
        initCommonAttributes(scriptData, script);
        return scriptData;
    }

    static ScriptData createForFlowScript(FlowScript script) {
        ScriptData scriptData = new ScriptData();
        initCommonAttributes(scriptData, script);
        scriptData.setFlowScriptActionType(script.getActionType());
        scriptData.setFlowScriptTarget(script.getActionTarget());
        scriptData.setFlowScriptTargetContinuation(script.getActionContinuation());
        scriptData.setFlowScriptTargetElse(script.getActionTargetElse());
        return scriptData;
    }

    FlowScript createFlowScript() throws InvalidScriptException {
        if (flowScriptActionType == null) {
            throw new DatabaseManagerException("Flow script action type is null");
        }

        if (flowScriptActionType.equals(FlowActionType.CONTINUE.toString())) {
            return FlowScript.createContinueFlowScript();
        } else if (flowScriptActionType.equals(FlowActionType.IF.toString())) {
            return FlowScript.createIfFlowScript(getScript(), getScriptEngine(), getFlowScriptTarget(),
                    getFlowScriptTargetElse(), getFlowScriptTargetContinuation());
        } else if (flowScriptActionType.equals(FlowActionType.LOOP.toString())) {
            return FlowScript.createLoopFlowScript(getScript(), getScriptEngine(), getFlowScriptTarget());
        }
        if (flowScriptActionType.equals(FlowActionType.REPLICATE.toString())) {
            return FlowScript.createReplicateFlowScript(getScript(), getScriptEngine());
        } else {
            throw new DatabaseManagerException("Invalid flow script action: " + flowScriptActionType);
        }
    }

    SelectionScript createSelectionScript() throws InvalidScriptException {
        return new SelectionScript(getScript(), getScriptEngine(), parameters(), isSelectionScriptDynamic());
    }

    GenerationScript createGenerationScript() throws InvalidScriptException {
        return new GenerationScript(script, scriptEngine, parameters());
    }

    SimpleScript createSimpleScript() throws InvalidScriptException {
        return new SimpleScript(script, scriptEngine, parameters());
    }

    private String[] parameters() {
        if (scriptParameters != null) {
            return scriptParameters.toArray(new String[scriptParameters.size()]);
        } else {
            return new String[] {};
        }
    }

    protected static void initCommonAttributes(ScriptData scriptData, Script<?> script) {
        scriptData.setScript(script.getScript());
        scriptData.setScriptEngine(script.getEngineName());
        if (script.getParameters() != null) {
            scriptData.setScriptParameters(Arrays.asList(script.getParameters()));
        }
    }

    @Id
    @GeneratedValue
    @Column(name = "ID")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "ENGINE", nullable = false)
    public String getScriptEngine() {
        return scriptEngine;
    }

    public void setScriptEngine(String scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    @Column(name = "SCRIPT", length = Integer.MAX_VALUE)
    @Lob
    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Column(name = "PARAMETERS")
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public List<String> getScriptParameters() {
        return scriptParameters;
    }

    public void setScriptParameters(List<String> scriptParameters) {
        this.scriptParameters = scriptParameters;
    }

    @Column(name = "IS_DYNAMIC")
    public boolean isSelectionScriptDynamic() {
        return selectionScriptDynamic;
    }

    public void setSelectionScriptDynamic(boolean selectionScriptDynamic) {
        this.selectionScriptDynamic = selectionScriptDynamic;
    }

    @Column(name = "FLOW_ACTION_TYPE")
    public String getFlowScriptActionType() {
        return flowScriptActionType;
    }

    public void setFlowScriptActionType(String flowScriptActionType) {
        this.flowScriptActionType = flowScriptActionType;
    }

    @Column(name = "FLOW_TARGET")
    public String getFlowScriptTarget() {
        return flowScriptTarget;
    }

    public void setFlowScriptTarget(String flowScriptTarget) {
        this.flowScriptTarget = flowScriptTarget;
    }

    @Column(name = "FLOW_TARGET_ELSE")
    public String getFlowScriptTargetElse() {
        return flowScriptTargetElse;
    }

    public void setFlowScriptTargetElse(String flowScriptTargetElse) {
        this.flowScriptTargetElse = flowScriptTargetElse;
    }

    @Column(name = "FLOW_TARGET_CONTINUE")
    public String getFlowScriptTargetContinuation() {
        return flowScriptTargetContinuation;
    }

    public void setFlowScriptTargetContinuation(String flowScriptTargetContinuation) {
        this.flowScriptTargetContinuation = flowScriptTargetContinuation;
    }

}
