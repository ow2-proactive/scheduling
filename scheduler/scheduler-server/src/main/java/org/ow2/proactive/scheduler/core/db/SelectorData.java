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

import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.InputSelector;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode;
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputSelector;
import org.ow2.proactive.scheduler.core.db.types.PatternType;


@Entity
@NamedQueries({ @NamedQuery(name = "deleteSelectorData", query = "delete from SelectorData where taskData.id.jobId = :jobId"), })
@Table(name = "DS_SELECTOR_DATA", indexes = { @Index(name = "DS_SELECTOR_DATA_JOB_ID", columnList = "JOB_ID"),
                                              @Index(name = "DS_SELECTOR_DATA_TASK_ID", columnList = "TASK_ID") })
public class SelectorData {

    private static final String INPUT_TYPE = "input";

    private static final String OUTPUT_TYPE = "output";

    private long id;

    private Set<String> includes;

    private Set<String> excludes;

    private TaskData taskData;

    private String accessMode;

    private String type;

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
        setIncludes(selector.getIncludes());
        setExcludes(selector.getExcludes());
    }

    InputSelector createInputSelector() {
        if (!type.equals(INPUT_TYPE)) {
            throw new IllegalStateException("Not input selector: " + type);
        }

        FileSelector fileSelector = new FileSelector(getIncludes(), getExcludes());
        InputSelector selector = new InputSelector(fileSelector, InputAccessMode.valueOf(getAccessMode()));

        return selector;
    }

    OutputSelector createOutputSelector() {
        if (!type.equals(OUTPUT_TYPE)) {
            throw new IllegalStateException("Not output selector: " + type);
        }

        FileSelector fileSelector = new FileSelector(getIncludes(), getExcludes());
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
    @Type(type = "org.ow2.proactive.scheduler.core.db.types.PatternType", parameters = @Parameter(name = PatternType.CLASS_NAME, value = "java.lang.Object"))
    public Set<String> getIncludes() {
        return includes;
    }

    public void setIncludes(Set<String> includes) {
        this.includes = includes;
    }

    @Column(name = "EXCLUDES")
    @Type(type = "org.ow2.proactive.scheduler.core.db.types.PatternType", parameters = @Parameter(name = PatternType.CLASS_NAME, value = "java.lang.Object"))
    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

}
