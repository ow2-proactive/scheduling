package org.ow2.proactive.scheduler.core.db;

import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;
import org.ow2.proactive.scheduler.task.nativ.NativeExecutableContainer;
import org.ow2.proactive.scripting.GenerationScript;
import org.ow2.proactive.scripting.InvalidScriptException;


@Entity
@Table(name = "NATIVE_TASK_DATA")
public class NativeTaskData {

    private long id;

    private TaskData taskData;

    private List<String> command;

    private String workingDir;

    private ScriptData generationScript;

    static NativeTaskData createNativeTaskData(TaskData taskData, NativeExecutableContainer container) {
        NativeTaskData nativeTaskData = new NativeTaskData();
        nativeTaskData.setTaskData(taskData);
        nativeTaskData.setWorkingDir(container.getWorkingDir());
        if (container.getCommand() != null) {
            nativeTaskData.setCommand(Arrays.asList(container.getCommand()));
        }
        if (container.getGenerationScript() != null) {
            nativeTaskData.setGenerationScript(ScriptData.createForScript(container.getGenerationScript()));
        }
        return nativeTaskData;
    }

    NativeExecutableContainer createExecutableContainer() throws InvalidScriptException {
        GenerationScript script;
        if (generationScript == null) {
            script = null;
        } else {
            script = generationScript.createGenerationScript();
        }
        String[] command;
        if (getCommand() == null) {
            command = null;
        } else {
            command = getCommand().toArray(new String[getCommand().size()]);
        }
        NativeExecutableContainer container = new NativeExecutableContainer(command, script, getWorkingDir());
        return container;
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
    @JoinColumns(value = { @JoinColumn(name = "JOB_ID", referencedColumnName = "TASK_ID_JOB"),
            @JoinColumn(name = "TASK_ID", referencedColumnName = "TASK_ID_TASK") })
    public TaskData getTaskData() {
        return taskData;
    }

    public void setTaskData(TaskData taskData) {
        this.taskData = taskData;
    }

    @Column(name = "WORK_DIR", length = Integer.MAX_VALUE)
    @Lob
    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    @Column(name = "COMMAND", updatable = false)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public List<String> getCommand() {
        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "GEN_SCRIPT_ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public ScriptData getGenerationScript() {
        return generationScript;
    }

    public void setGenerationScript(ScriptData generationScript) {
        this.generationScript = generationScript;
    }

}
