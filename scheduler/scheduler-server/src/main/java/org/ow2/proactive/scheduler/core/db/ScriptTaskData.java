package org.ow2.proactive.scheduler.core.db;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.PropertyModifier;
import org.ow2.proactive.scheduler.task.containers.ForkedScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
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

    public static ScriptTaskData createScriptTaskData(TaskData taskData, ForkedScriptExecutableContainer container) {
        Script script = container.getScript();
        return createScriptTaskData(taskData, script, container.getForkEnvironment());
    }

    public static ScriptTaskData createScriptTaskData(TaskData taskData, ScriptExecutableContainer container) {
        Script script = container.getScript();
        return createScriptTaskData(taskData, script, null);
    }

    private static ScriptTaskData createScriptTaskData(TaskData taskData, Script script, ForkEnvironment forkEnvironment) {
        ScriptTaskData scriptTaskData = new ScriptTaskData();

        if (forkEnvironment != null) {
            taskData.setAdditionalClasspath(forkEnvironment.getAdditionalClasspath());
            taskData.setJavaHome(forkEnvironment.getJavaHome());
            taskData.setJvmArguments(forkEnvironment.getJVMArguments());
            taskData.setWorkingDir(forkEnvironment.getWorkingDir());

            if (forkEnvironment.getEnvScript() != null) {
                taskData.setEnvScript(ScriptData.createForScript(forkEnvironment.getEnvScript()));
            }

            if (forkEnvironment.getPropertyModifiers() != null) {
                List<PropertyModifier> modifiers = forkEnvironment.getPropertyModifiers();

                List<EnvironmentModifierData> envModifiers = new ArrayList<>(modifiers.size());
                for (PropertyModifier propertyModifier : modifiers) {
                    envModifiers.add(EnvironmentModifierData.create(propertyModifier, taskData));
                }
                taskData.setEnvModifiers(envModifiers);
            }
        }

        scriptTaskData.setTaskData(taskData);
        scriptTaskData.setScript(ScriptData.createForScript(script));

        return scriptTaskData;
    }

    ScriptExecutableContainer createExecutableContainer() throws InvalidScriptException {
        return new ScriptExecutableContainer(new TaskScript(script.createSimpleScript()));
    }

    ForkedScriptExecutableContainer createForkedExecutableContainer() throws InvalidScriptException {
        return new ForkedScriptExecutableContainer(
                    new TaskScript(script.createSimpleScript()),
                        taskData.createForkEnvironment());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SCRIPT_TASK_DATA_ID_SEQUENCE")
    @SequenceGenerator(name = "SCRIPT_TASK_DATA_ID_SEQUENCE", sequenceName = "SCRIPT_TASK_DATA_ID_SEQUENCE")
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
