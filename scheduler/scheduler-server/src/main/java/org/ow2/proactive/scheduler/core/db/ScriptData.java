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
import java.net.MalformedURLException;
import java.net.URL;
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
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.SchedulerVars;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SimpleScript;


@Entity
@NamedQueries({ @NamedQuery(name = "deleteScriptDataInBulk", query = "delete from ScriptData where taskData.id.jobId in :jobIdList"),
                @NamedQuery(name = "countScriptData", query = "select count (*) from ScriptData") })
@BatchSize(size = 100)
@Table(name = "SCRIPT_DATA", indexes = { @Index(name = "SCRIPT_DATA_JOB_ID", columnList = "JOB_ID"),
                                         @Index(name = "SCRIPT_DATA_TASK_ID_JOB_ID", columnList = "TASK_ID,JOB_ID"),
                                         @Index(name = "SCRIPT_DATA_TASK_ID", columnList = "TASK_ID") })
public class ScriptData {

    private long id;

    private String scriptEngine;

    private String script;

    private String url;

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

    FlowScript createFlowScriptByScript() throws InvalidScriptException {
        if (flowScriptActionType.equals(FlowActionType.CONTINUE.toString())) {
            return FlowScript.createContinueFlowScript();
        } else if (flowScriptActionType.equals(FlowActionType.IF.toString())) {
            return FlowScript.createIfFlowScript(getScript(),
                                                 getScriptEngine(),
                                                 getFlowScriptTarget(),
                                                 getFlowScriptTargetElse(),
                                                 getFlowScriptTargetContinuation(),
                                                 parameters());
        } else if (flowScriptActionType.equals(FlowActionType.LOOP.toString())) {
            return FlowScript.createLoopFlowScript(getScript(), getScriptEngine(), getFlowScriptTarget(), parameters());
        }
        if (flowScriptActionType.equals(FlowActionType.REPLICATE.toString())) {
            return FlowScript.createReplicateFlowScript(getScript(), getScriptEngine(), parameters());
        } else {
            throw new DatabaseManagerException("Invalid flow script action: " + flowScriptActionType);
        }
    }

    FlowScript createFlowScriptByURL() throws InvalidScriptException {
        URL inputUrl;
        try {
            inputUrl = getScriptUrl(url);
        } catch (MalformedURLException e) {
            throw new InvalidScriptException(e);
        }

        if (flowScriptActionType.equals(FlowActionType.CONTINUE.toString())) {
            return FlowScript.createContinueFlowScript();
        } else if (flowScriptActionType.equals(FlowActionType.IF.toString())) {
            return FlowScript.createIfFlowScript(inputUrl,
                                                 getScriptEngine(),
                                                 getFlowScriptTarget(),
                                                 getFlowScriptTargetElse(),
                                                 getFlowScriptTargetContinuation(),
                                                 parameters());
        } else if (flowScriptActionType.equals(FlowActionType.LOOP.toString())) {
            return FlowScript.createLoopFlowScript(inputUrl, getScriptEngine(), getFlowScriptTarget(), parameters());
        }
        if (flowScriptActionType.equals(FlowActionType.REPLICATE.toString())) {
            return FlowScript.createReplicateFlowScript(inputUrl, getScriptEngine(), parameters());
        } else {
            throw new DatabaseManagerException("Invalid flow script action: " + flowScriptActionType);
        }
    }

    FlowScript createFlowScript() throws InvalidScriptException {
        if (flowScriptActionType == null) {
            throw new DatabaseManagerException("Flow script action type is null");
        }

        if (script == null && url != null) {
            return createFlowScriptByURL();
        } else {
            return createFlowScriptByScript();
        }
    }

    SimpleScript createSimpleScript() throws InvalidScriptException {
        if (script == null && url != null) {
            try {
                return new SimpleScript(getScriptUrl(url), scriptEngine, parameters());
            } catch (MalformedURLException e) {
                throw new InvalidScriptException(e);
            }
        } else {
            return new SimpleScript(script, scriptEngine, parameters());
        }
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
        if (script.getScriptUrl() != null) {
            scriptData.setURL(getDBUrl(script.getScriptUrl()));
        }
        if (script.getParameters() != null) {
            scriptData.setScriptParameters(Arrays.asList(script.getParameters()));
        }
    }

    static String getDBUrl(URL scriptUrl) {
        String answer = scriptUrl.toExternalForm();
        String catalogUrl = PASchedulerProperties.CATALOG_REST_URL.getValueAsString();
        if (PASchedulerProperties.STORE_CATALOG_REF_IN_DB.getValueAsBoolean() && catalogUrl != null &&
            answer.contains(catalogUrl)) {
            return answer.replace(catalogUrl, "$" + SchedulerVars.PA_CATALOG_REST_URL.name());
        }
        return answer;
    }

    static URL getScriptUrl(String dbUrl) throws MalformedURLException {
        String catalogUrl = PASchedulerProperties.CATALOG_REST_URL.getValueAsString();
        if (PASchedulerProperties.STORE_CATALOG_REF_IN_DB.getValueAsBoolean() &&
            dbUrl.contains("$" + SchedulerVars.PA_CATALOG_REST_URL.name())) {
            if (catalogUrl == null) {
                throw new IllegalStateException("When " + PASchedulerProperties.STORE_CATALOG_REF_IN_DB.getKey() +
                                                " is set to true, " + PASchedulerProperties.CATALOG_REST_URL.getKey() +
                                                " must be configured manually.");
            }
            return new URL(dbUrl.replace("$" + SchedulerVars.PA_CATALOG_REST_URL.name(), catalogUrl));
        }
        return new URL(dbUrl);
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

    @Column(name = "ENGINE")
    public String getScriptEngine() {
        return scriptEngine;
    }

    public void setScriptEngine(String scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    @Column(name = "SCRIPT", length = Integer.MAX_VALUE)
    @Lob
    @Type(type = "org.hibernate.type.MaterializedClobType")
    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Column(name = "URL", length = Integer.MAX_VALUE)
    @Lob
    @Type(type = "org.hibernate.type.MaterializedClobType")
    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
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
