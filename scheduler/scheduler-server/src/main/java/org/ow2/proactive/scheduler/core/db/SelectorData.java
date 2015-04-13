package org.ow2.proactive.scheduler.core.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;
import org.ow2.proactive.scheduler.common.task.dataspaces.FileSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;


@Entity
@Table(name = "DS_SELECTOR_DATA")
public class SelectorData {

    private static final String INPUT_TYPE = "input";

    private static final String OUTPUT_TYPE = "output";

    private long id;

    private String[] includes;

    private String[] excludes;

    private TaskData taskData;

    private String accessMode;

    private String type;

    private boolean caseSensitive;

    static SelectorData createForInputSelector(InputSelector selector, TaskData task) {
        SelectorData selectorData = new SelectorData();
        selectorData.setAccessMode(selector.getMode().name());
        selectorData.setType(INPUT_TYPE);
        selectorData.setFileSelector(selector.getInputFiles());
        selectorData.setTaskData(task);
        return selectorData;
    }

    static SelectorData createForOutputSelector(OutputSelector selector, TaskData task) {
        SelectorData selectorData = new SelectorData();
        selectorData.setAccessMode(selector.getMode().name());
        selectorData.setType(OUTPUT_TYPE);
        selectorData.setFileSelector(selector.getOutputFiles());
        selectorData.setTaskData(task);
        return selectorData;
    }

    private void setFileSelector(FileSelector selector) {
        setCaseSensitive(selector.isCaseSensitive());
        setIncludes(selector.getIncludes());
        setExcludes(selector.getExcludes());
    }

    InputSelector createInputSelector() {
        if (!type.equals(INPUT_TYPE)) {
            throw new IllegalStateException("Not input selector: " + type);
        }
        FileSelector fileSelector = new FileSelector(getIncludes(), getExcludes());
        fileSelector.setCaseSensitive(isCaseSensitive());
        InputSelector selector = new InputSelector(fileSelector, InputAccessMode.valueOf(getAccessMode()));
        return selector;
    }

    OutputSelector createOutputSelector() {
        if (!type.equals(OUTPUT_TYPE)) {
            throw new IllegalStateException("Not output selector: " + type);
        }
        FileSelector fileSelector = new FileSelector(getIncludes(), getExcludes());
        fileSelector.setCaseSensitive(isCaseSensitive());
        OutputSelector selector = new OutputSelector(fileSelector, OutputAccessMode.valueOf(getAccessMode()));
        return selector;
    }

    @Transient
    boolean isInput() {
        if (type == null) {
            throw new IllegalStateException();
        }
        return type.equals(INPUT_TYPE);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SELECTOR_DATA_ID_SEQUENCE")
    @SequenceGenerator(name = "SELECTOR_DATA_ID_SEQUENCE", sequenceName = "SELECTOR_DATA_ID_SEQUENCE")
    @Column(name = "ID")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    @Column(name = "ACCESS_MODE", length = 30)
    public String getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(String accessMode) {
        this.accessMode = accessMode;
    }

    @Column(name = "TYPE", nullable = false, length = 10)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "INCLUDES")
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public String[] getIncludes() {
        return includes;
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    @Column(name = "EXCLUDES")
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    public String[] getExcludes() {
        return excludes;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

    @Column(name = "CASE_SENSITIVE")
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

}
