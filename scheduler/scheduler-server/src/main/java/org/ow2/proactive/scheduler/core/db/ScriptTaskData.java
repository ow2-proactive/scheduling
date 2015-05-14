package org.ow2.proactive.scheduler.core.db;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.PropertyModifier;
import org.ow2.proactive.scheduler.task.script.ForkedScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.script.ScriptExecutableContainer;
import org.ow2.proactive.scripting.InvalidScriptException;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.TaskScript;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "SCRIPT_TASK_DATA")
public class ScriptTaskData {

    private long id;
    private TaskData taskData;
    private ScriptData script;
    private String workingDir;

    private String javaHome;
    private List<String> jvmArguments;
    private List<String> additionalClasspath;
    private ScriptData envScript;
    private List<EnvironmentModifierData> envModifiers;


    static ScriptTaskData createScriptTaskData(TaskData taskData, ForkedScriptExecutableContainer container) {
        Script script = container.getScript();
        return createScriptTaskData(taskData, script, container.getForkEnvironment());
    }

    static ScriptTaskData createScriptTaskData(TaskData taskData, ScriptExecutableContainer container) {
        Script script = container.getScript();
        return createScriptTaskData(taskData, script, null);
    }

    private static ScriptTaskData createScriptTaskData(TaskData taskData, Script script, ForkEnvironment forkEnvironment) {
        ScriptTaskData scriptTaskData = new ScriptTaskData();
        scriptTaskData.setTaskData(taskData);
        scriptTaskData.setScript(ScriptData.createForScript(script));

        if (forkEnvironment != null) {
            scriptTaskData.setJavaHome(forkEnvironment.getJavaHome());
            scriptTaskData.setWorkingDir(forkEnvironment.getWorkingDir());
            scriptTaskData.setJvmArguments(forkEnvironment.getJVMArguments());
            scriptTaskData.setAdditionalClasspath(forkEnvironment.getAdditionalClasspath());
            if (forkEnvironment.getEnvScript() != null) {
                scriptTaskData.setEnvScript(ScriptData.createForScript(forkEnvironment.getEnvScript()));
            }
            if (forkEnvironment.getPropertyModifiers() != null) {
                List<EnvironmentModifierData> envModifiers = new ArrayList<EnvironmentModifierData>();
                for (PropertyModifier propertyModifier : forkEnvironment.getPropertyModifiers()) {
                    envModifiers.add(EnvironmentModifierData.create(propertyModifier, scriptTaskData));
                }
                scriptTaskData.setEnvModifiers(envModifiers);
            }
            scriptTaskData.setWorkingDir(forkEnvironment.getWorkingDir());
        }

        return scriptTaskData;
    }

    ScriptExecutableContainer createExecutableContainer() throws InvalidScriptException {
        return new ScriptExecutableContainer(new TaskScript(script.createSimpleScript()));
    }

    ForkedScriptExecutableContainer createForkedExecutableContainer() throws InvalidScriptException {
        ForkEnvironment forkEnv = new ForkEnvironment();
        forkEnv.setJavaHome(javaHome);
        forkEnv.setWorkingDir(workingDir);

        List<String> additionalClasspath = getAdditionalClasspath();
        if (additionalClasspath != null) {
            for (String classpath : additionalClasspath) {
                forkEnv.addAdditionalClasspath(classpath);
            }
        }

        List<String> jvmArguments = getJvmArguments();
        if (jvmArguments != null) {
            for (String jvmArg : jvmArguments) {
                forkEnv.addJVMArgument(jvmArg);
            }
        }

        List<EnvironmentModifierData> envModifiers = getEnvModifiers();

        if (envModifiers != null) {
            for (EnvironmentModifierData envModifier : envModifiers) {
                if (envModifier.getAppendChar() != 0) {
                    forkEnv.addSystemEnvironmentVariable(envModifier.getName(), envModifier.getValue(),
                            envModifier.getAppendChar());
                } else {
                    forkEnv.addSystemEnvironmentVariable(envModifier.getName(), envModifier.getValue(),
                            envModifier.isAppend());
                }
            }
        }
        if (envScript != null) {
            forkEnv.setEnvScript(envScript.createSimpleScript());
        }
        return new ForkedScriptExecutableContainer(new TaskScript(script.createSimpleScript()), forkEnv);
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

    @Column(name = "WORKING_DIR")
    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    @Column(name = "JAVA_HOME", length = Integer.MAX_VALUE)
    @Lob
    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    @Column(name = "JVM_ARGUMENTS")
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @org.hibernate.annotations.Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public List<String> getJvmArguments() {
        return jvmArguments;
    }

    public void setJvmArguments(List<String> jvmArguments) {
        this.jvmArguments = jvmArguments;
    }

    @Column(name = "CLASSPATH")
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @org.hibernate.annotations.Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public List<String> getAdditionalClasspath() {
        return additionalClasspath;
    }

    public void setAdditionalClasspath(List<String> additionalClasspath) {
        this.additionalClasspath = additionalClasspath;
    }

    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @OneToOne
    @JoinColumn(name = "ENV_SCRIPT_ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public ScriptData getEnvScript() {
        return envScript;
    }

    public void setEnvScript(ScriptData envScript) {
        this.envScript = envScript;
    }

    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @OneToMany(mappedBy = "taskData")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public List<EnvironmentModifierData> getEnvModifiers() {
        return envModifiers;
    }

    public void setEnvModifiers(List<EnvironmentModifierData> envModifiers) {
        this.envModifiers = envModifiers;
    }
}
