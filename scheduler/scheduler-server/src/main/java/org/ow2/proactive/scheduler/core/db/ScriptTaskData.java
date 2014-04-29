package org.ow2.proactive.scheduler.core.db;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.script.ScriptExecutableContainer;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.TaskScript;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@Entity
@Table(name = "SCRIPT_TASK_DATA")
public class ScriptTaskData {

    private long id;
    private TaskData taskData;
    private ScriptData script;

    static ScriptTaskData createScriptTaskData(TaskData taskData, ForkedScriptExecutableContainer container) {
        Script script = container.getScript();
        return createScriptTaskData(taskData, script);
    }

    static ScriptTaskData createScriptTaskData(TaskData taskData, ScriptExecutableContainer container) {
        Script script = container.getScript();
        return createScriptTaskData(taskData, script);
    }

    private static ScriptTaskData createScriptTaskData(TaskData taskData, Script script) {
        ScriptTaskData scriptTaskData = new ScriptTaskData();
        scriptTaskData.setTaskData(taskData);
        scriptTaskData.setScript(ScriptData.createForScript(script));
        return scriptTaskData;
    }

    ScriptExecutableContainer createExecutableContainer() throws InvalidScriptException {
        return new ScriptExecutableContainer(new TaskScript(script.createSimpleScript()));
    }

    ForkedScriptExecutableContainer createForkedExecutableContainer() throws InvalidScriptException {
        return new ForkedScriptExecutableContainer(new TaskScript(script.createSimpleScript()));
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

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumns(value = { @JoinColumn(name = "JOB_ID", referencedColumnName = "TASK_ID_JOB"),
            @JoinColumn(name = "TASK_ID", referencedColumnName = "TASK_ID_TASK") })
    public TaskData getTaskData() {
        return taskData;
    }

    public void setTaskData(TaskData taskData) {
        this.taskData = taskData;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "TASK_SCRIPT_ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public ScriptData getScript() {
        return script;
    }

    public void setScript(ScriptData script) {
        this.script = script;
    }

}
