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
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.SelectionScript;


@Entity
@NamedQueries({ @NamedQuery(name = "deleteSelectionScriptData", query = "delete from SelectionScriptData where taskData.id.jobId = :jobId"),
                @NamedQuery(name = "deleteSelectionScriptDataInBulk", query = "delete from SelectionScriptData where taskData.id.jobId in :jobIdList"),
                @NamedQuery(name = "countSelectionScriptData", query = "select count (*) from SelectionScriptData") })
@Table(name = "SELECTION_SCRIPT_DATA", indexes = { @Index(name = "SELECTION_SCRIPT_DATA_JOB_ID", columnList = "JOB_ID"),
                                                   @Index(name = "SELECTION_SCRIPT_DATA_TASK_ID", columnList = "TASK_ID") })
@BatchSize(size = 100)
public class SelectionScriptData {

    private long id;

    private String scriptEngine;

    private String script;

    private List<Serializable> scriptParameters;

    private boolean selectionScriptDynamic;

    private TaskData taskData;

    static SelectionScriptData createForSelectionScript(SelectionScript script, TaskData taskData) {
        SelectionScriptData scriptData = new SelectionScriptData();
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

    SelectionScript createSelectionScript() throws InvalidScriptException {
        return new SelectionScript(getScript(), getScriptEngine(), parameters(), isSelectionScriptDynamic());
    }

    private Serializable[] parameters() {
        if (scriptParameters != null) {
            return scriptParameters.toArray(new Serializable[scriptParameters.size()]);
        } else {
            return new String[] {};
        }
    }

    protected static void initCommonAttributes(SelectionScriptData scriptData, Script<?> script) {
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

    @Column(name = "PARAMETERS")
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public List<Serializable> getScriptParameters() {
        return scriptParameters;
    }

    public void setScriptParameters(List<Serializable> scriptParameters) {
        this.scriptParameters = scriptParameters;
    }

    @Column(name = "IS_DYNAMIC")
    public boolean isSelectionScriptDynamic() {
        return selectionScriptDynamic;
    }

    public void setSelectionScriptDynamic(boolean selectionScriptDynamic) {
        this.selectionScriptDynamic = selectionScriptDynamic;
    }

}
