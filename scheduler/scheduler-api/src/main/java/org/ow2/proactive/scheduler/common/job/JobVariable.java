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
package org.ow2.proactive.scheduler.common.job;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * @author The ProActive Team
 * @since ProActive Scheduling 7.24
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public class JobVariable implements Serializable {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String value;

    @XmlAttribute
    private String model;

    @XmlAttribute
    private String description;

    @XmlAttribute
    private String group;

    @XmlAttribute
    private boolean advanced;

    @XmlAttribute
    private boolean hidden;

    public JobVariable() {
        //Empty constructor
    }

    public JobVariable(String name, String value) {
        this(name, value, null);
    }

    public JobVariable(String name, String value, String model) {
        this(name, value, model, null, null, false, false);
    }

    public JobVariable(String name, String value, String model, String description, String group, boolean advanced,
            boolean hidden) {
        this.name = name;
        this.value = value;
        this.model = model;
        this.description = description;
        this.group = group;
        this.advanced = advanced;
        this.hidden = hidden;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isAdvanced() {
        return advanced;
    }

    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        JobVariable that = (JobVariable) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
            return false;
        if (getValue() != null ? !getValue().equals(that.getValue()) : that.getValue() != null)
            return false;
        if (getModel() != null ? !getModel().equals(that.getModel()) : that.getModel() != null)
            return false;
        if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null)
            return false;
        if (getGroup() != null ? !getGroup().equals(that.getGroup()) : that.getGroup() != null)
            return false;
        return isAdvanced() == that.isAdvanced() && isHidden() == that.isHidden();
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
        result = 31 * result + (getModel() != null ? getModel().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getGroup() != null ? getGroup().hashCode() : 0);
        result = 31 * result + (isAdvanced() ? 1 : 0);
        result = 31 * result + (isHidden() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JobVariable{" + "name='" + name + '\'' + ", value='" + value + '\'' + ", model='" + model + '\'' +
               ", description='" + description + '\'' + ", group='" + group + '\'' + ", advanced=" + advanced +
               ", hidden=" + hidden + '}';
    }
}
