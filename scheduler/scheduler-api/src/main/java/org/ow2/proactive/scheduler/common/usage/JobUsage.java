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
package org.ow2.proactive.scheduler.common.usage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Job information for accounting / usage purpose.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.4
 */
@PublicAPI
@XmlRootElement
public class JobUsage implements Serializable {
    private final String owner;

    private final String project;

    private final String jobId;

    private final String jobName;

    private final long jobDuration;

    private List<TaskUsage> taskUsages = new ArrayList<TaskUsage>();

    public JobUsage(String owner, String project, String jobId, String jobName, long jobDuration) {
        this.owner = owner;
        this.project = project;
        this.jobId = jobId;
        this.jobName = jobName;
        this.jobDuration = jobDuration;
    }

    public void add(TaskUsage taskUsage) {
        taskUsages.add(taskUsage);
    }

    public String getJobId() {
        return jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public String getOwner() {
        return owner;
    }

    public String getProject() {
        return project;
    }

    public long getJobDuration() {
        return jobDuration;
    }

    public List<TaskUsage> getTaskUsages() {
        return taskUsages;
    }
}
