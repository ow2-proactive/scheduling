package org.ow2.proactive.scheduler.core.db;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.PropertyModifier;
import org.ow2.proactive.scheduler.task.ExecutableContainer;
import org.ow2.proactive.scheduler.task.forked.ForkedJavaExecutableContainer;


@Entity
@Table(name = "FORKED_JAVA_TASK_DATA")
public class ForkedJavaTaskData extends CommonJavaTaskData {

    private static final long serialVersionUID = 60L;

    private String javaHome;

    private String workingDir;

    private List<String> jvmArguments;

    private List<String> additionalClasspath;

    private ScriptData envScript;

    private List<EnvironmentModifierData> envModifiers;

    ExecutableContainer createExecutableContainer() throws Exception {
        ForkedJavaExecutableContainer container = new ForkedJavaExecutableContainer(
            getUserExecutableClassName(), getSearializedArguments());

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

        container.setForkEnvironment(forkEnv);

        return container;
    }

    static ForkedJavaTaskData createForkedJavaTaskData(TaskData task, ForkedJavaExecutableContainer container) {
        ForkedJavaTaskData taskData = new ForkedJavaTaskData();
        taskData.initProperties(task, container);

        ForkEnvironment forkEnv = container.getForkEnvironment();
        if (forkEnv != null) {
            taskData.setJavaHome(forkEnv.getJavaHome());
            taskData.setWorkingDir(forkEnv.getWorkingDir());
            taskData.setJvmArguments(forkEnv.getJVMArguments());
            taskData.setAdditionalClasspath(forkEnv.getAdditionalClasspath());
            if (forkEnv.getEnvScript() != null) {
                taskData.setEnvScript(ScriptData.createForScript(forkEnv.getEnvScript()));
            }
            if (forkEnv.getPropertyModifiers() != null) {
                List<EnvironmentModifierData> envModifiers = new ArrayList<EnvironmentModifierData>();
                for (PropertyModifier propertyModifier : forkEnv.getPropertyModifiers()) {
                    envModifiers.add(EnvironmentModifierData.create(propertyModifier, taskData));
                }
                taskData.setEnvModifiers(envModifiers);
            }
        }
        return taskData;
    }

    @Column(name = "WORK_DIR", length = Integer.MAX_VALUE)
    @Lob
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
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public List<String> getJvmArguments() {
        return jvmArguments;
    }

    public void setJvmArguments(List<String> jvmArguments) {
        this.jvmArguments = jvmArguments;
    }

    @Column(name = "CLASSPATH")
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public List<String> getAdditionalClasspath() {
        return additionalClasspath;
    }

    public void setAdditionalClasspath(List<String> additionalClasspath) {
        this.additionalClasspath = additionalClasspath;
    }

    @Cascade(CascadeType.ALL)
    @OneToOne
    @JoinColumn(name = "ENV_SCRIPT_ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public ScriptData getEnvScript() {
        return envScript;
    }

    public void setEnvScript(ScriptData envScript) {
        this.envScript = envScript;
    }

    @Cascade(CascadeType.ALL)
    @OneToMany(mappedBy = "taskData")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public List<EnvironmentModifierData> getEnvModifiers() {
        return envModifiers;
    }

    public void setEnvModifiers(List<EnvironmentModifierData> envModifiers) {
        this.envModifiers = envModifiers;
    }

}
