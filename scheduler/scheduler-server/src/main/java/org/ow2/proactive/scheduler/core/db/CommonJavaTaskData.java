package org.ow2.proactive.scheduler.core.db;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

import org.ow2.proactive.scheduler.common.task.util.ByteArrayWrapper;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;


@MappedSuperclass
public class CommonJavaTaskData implements Serializable {

    private long id;

    private TaskData taskData;

    protected Map<String, byte[]> searializedArguments;

    private String userExecutableClassName;

    protected void initProperties(TaskData taskData, ScriptExecutableContainer container) {
        setTaskData(taskData);

        Map<String, ByteArrayWrapper> args = container.getSerializedArguments();

        if (args != null) {
            Map<String, byte[]> convertedArgs = new HashMap<>(args.size());
            for (Map.Entry<String, ByteArrayWrapper> argEntry : args.entrySet()) {
                convertedArgs.put(argEntry.getKey(), argEntry.getValue().getByteArray());
            }
            setSearializedArguments(convertedArgs);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "C_JAVA_TASK_DATA_ID_SEQUENCE")
    @SequenceGenerator(name = "C_JAVA_TASK_DATA_ID_SEQUENCE", sequenceName = "C_JAVA_TASK_DATA_ID_SEQUENCE")
    @Column(name = "ID")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumns(value = { @JoinColumn(name = "JOB_ID", referencedColumnName = "TASK_ID_JOB"),
            @JoinColumn(name = "TASK_ID", referencedColumnName = "TASK_ID_TASK") })
    @OnDelete(action = OnDeleteAction.CASCADE)
    public TaskData getTaskData() {
        return taskData;
    }

    public void setTaskData(TaskData taskData) {
        this.taskData = taskData;
    }

    @Column(name = "CLASS_NAME", nullable = false)
    @Lob
    public String getUserExecutableClassName() {
        return userExecutableClassName;
    }

    public void setUserExecutableClassName(String userExecutableClassName) {
        this.userExecutableClassName = userExecutableClassName;
    }

    @Column(name = "ARGUMENTS")
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public Map<String, byte[]> getSearializedArguments() {
        return searializedArguments;
    }

    public void setSearializedArguments(Map<String, byte[]> searializedArguments) {
        this.searializedArguments = searializedArguments;
    }

}
