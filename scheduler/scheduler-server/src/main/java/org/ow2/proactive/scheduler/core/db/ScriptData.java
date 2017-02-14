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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;
import org.ow2.proactive.db.DatabaseManagerException;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SimpleScript;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;


@Entity
@NamedQueries({ @NamedQuery(name = "deleteScriptData", query = "delete from ScriptData where taskData.id.jobId = :jobId") })
@Table(name = "SCRIPT_DATA", indexes = { @Index(name = "SCRIPT_DATA_JOB_ID", columnList = "JOB_ID"),
                                         @Index(name = "SCRIPT_DATA_TASK_ID", columnList = "TASK_ID") })
@BatchSize(size = 100)
public class ScriptData {

    private long id;

    private String scriptEngine;

    private String script;

    private List<Serializable> scriptParameters;

    private String flowScriptActionType;

    private String flowScriptTarget;

    private String flowScriptTargetElse;

    private String flowScriptTargetContinuation;

    private TaskData taskData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = { @JoinColumn(name = "JOB_ID", referencedColumnName = "TASK_ID_JOB"),
                           @JoinColumn(name = "TASK_ID", referencedColumnName = "TASK_ID_TASK") })
    public TaskData getTaskData() {
        return taskData;
    }

    public void setTaskData(TaskData taskData) {
        this.taskData = taskData;
    }

    static ScriptData createForScript(Script<?> script, TaskData taskData) {
        ScriptData scriptData = new ScriptData();
        initCommonAttributes(scriptData, script);
        scriptData.setTaskData(taskData);
        return scriptData;
    }

    static ScriptData createForFlowScript(FlowScript script, TaskData taskData) {
        ScriptData scriptData = new ScriptData();
        initCommonAttributes(scriptData, script);
        scriptData.setFlowScriptActionType(script.getActionType());
        scriptData.setFlowScriptTarget(script.getActionTarget());
        scriptData.setFlowScriptTargetContinuation(script.getActionContinuation());
        scriptData.setFlowScriptTargetElse(script.getActionTargetElse());
        scriptData.setTaskData(taskData);
        return scriptData;
    }

    FlowScript createFlowScript() throws InvalidScriptException {
        if (flowScriptActionType == null) {
            throw new DatabaseManagerException("Flow script action type is null");
        }

        if (flowScriptActionType.equals(FlowActionType.CONTINUE.toString())) {
            return FlowScript.createContinueFlowScript();
        } else if (flowScriptActionType.equals(FlowActionType.IF.toString())) {
            return FlowScript.createIfFlowScript(getScript(),
                                                 getScriptEngine(),
                                                 getFlowScriptTarget(),
                                                 getFlowScriptTargetElse(),
                                                 getFlowScriptTargetContinuation());
        } else if (flowScriptActionType.equals(FlowActionType.LOOP.toString())) {
            return FlowScript.createLoopFlowScript(getScript(), getScriptEngine(), getFlowScriptTarget());
        }
        if (flowScriptActionType.equals(FlowActionType.REPLICATE.toString())) {
            return FlowScript.createReplicateFlowScript(getScript(), getScriptEngine());
        } else {
            throw new DatabaseManagerException("Invalid flow script action: " + flowScriptActionType);
        }
    }

    SimpleScript createSimpleScript() throws InvalidScriptException {
        return new SimpleScript(script, scriptEngine, parameters());
    }

    private Serializable[] parameters() {
        if (scriptParameters != null) {
            return scriptParameters.toArray(new Serializable[scriptParameters.size()]);
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SCRIPT_DATA_ID_SEQUENCE")
    @SequenceGenerator(name = "SCRIPT_DATA_ID_SEQUENCE", sequenceName = "SCRIPT_DATA_ID_SEQUENCE")
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

    @Column(name = "PARAMETERS", length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public List<Serializable> getScriptParameters() {
        return scriptParameters;
    }

    public void setScriptParameters(List<Serializable> scriptParameters) {
        this.scriptParameters = scriptParameters;
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
