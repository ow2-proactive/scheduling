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

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * 
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 7.24
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public class JobVariable implements Serializable {

    private String name;

    private String value;

    private String model;

    public JobVariable() {
        //Empty constructor
    }

    public JobVariable(String name, String value) {
        this(name, value, null);
    }

    public JobVariable(String name, String value, String model) {
        this.name = name;
        this.value = value;
        this.model = model;
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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }

        JobVariable jobVariable = (JobVariable) object;
        if (name == null) {
            if (jobVariable.name != null) {
                return false;
            }
        } else if (!name.equals(jobVariable.name)) {
            return false;
        }
        if (value == null) {
            if (jobVariable.value != null) {
                return false;
            }
        } else if (!value.equals(jobVariable.value)) {
            return false;
        }
        if (model == null) {
            if (jobVariable.model != null) {
                return false;
            }
        } else if (!model.equals(jobVariable.model)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int primeNumber = 31;
        int result = 1;
        result = primeNumber * result + ((name == null) ? 0 : name.hashCode());
        result = primeNumber * result + ((value == null) ? 0 : value.hashCode());
        result = primeNumber * result + ((model == null) ? 0 : model.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "JobVariable{" + "name='" + name + '\'' + ", value='" + value + '\'' + ", model='" + model + '\'' + '}';
    }

}
