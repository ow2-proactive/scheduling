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
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.job.JobVariable;


/**
 * 
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 7.20
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskVariable extends JobVariable implements Serializable {

    private boolean jobInherited = false;

    public TaskVariable() {
        //Empty constructor
    }

    public TaskVariable(String name, String value) {
        this(name, value, null, false);
    }

    public TaskVariable(String name, String value, String model, boolean isJobInherited) {
        super(name, value, model);
        this.jobInherited = isJobInherited;
    }

    public TaskVariable(String name, String value, String model, String description, String group, boolean advanced,
            boolean hidden, boolean isJobInherited) {
        super(name, value, model, description, group, advanced, hidden);
        this.jobInherited = isJobInherited;
    }

    public boolean isJobInherited() {
        return jobInherited;
    }

    public void setJobInherited(boolean inherited) {
        this.jobInherited = inherited;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        TaskVariable that = (TaskVariable) o;

        return isJobInherited() == that.isJobInherited();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isJobInherited() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TaskVariable{" + "name='" + getName() + '\'' + ", value='" + getValue() + '\'' + ", model='" +
               getModel() + '\'' + ", description='" + getDescription() + '\'' + ", group='" + getGroup() + '\'' +
               ", advanced=" + isAdvanced() + ", hidden=" + isHidden() + ", jobInherited=" + jobInherited + '}';
    }
}
