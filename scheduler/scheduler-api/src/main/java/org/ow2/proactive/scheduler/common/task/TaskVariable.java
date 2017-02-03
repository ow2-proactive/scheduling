/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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

    public TaskVariable(String name, String value, String model, boolean isJobInherited) {
        super(name, value, model);
        this.jobInherited = isJobInherited;
    }

    public boolean isJobInherited() {
        return jobInherited;
    }

    public void setJobInherited(boolean inherited) {
        this.jobInherited = inherited;
    }

    @Override
    public boolean equals(Object object){
        if (this == object){
            return true;
        }
        if (object == null){
            return false;
        }        
        if (getClass() != object.getClass()){
            return false;
        }

        TaskVariable taskVariable = (TaskVariable) object;
        if (jobInherited != taskVariable.isJobInherited()){
            return false;
        }
        if (getName() == null) {
            if (taskVariable.getName() != null) {
                return false;
            }
        } else if (!getName().equals(taskVariable.getName())) {
            return false;
        }
        if (getValue() == null) {
            if (taskVariable.getValue() != null) {
                return false;
            }
        } else if (!getValue().equals(taskVariable.getValue())) {
            return false;
        }
        if (getModel() == null) {
            if (taskVariable.getModel() != null) {
                return false;
            }
        } else if (!getModel().equals(taskVariable.getModel())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int primeNumber = 31;
        int result = 1;
        result = primeNumber * result + (jobInherited ? 3 : 5);
        result = primeNumber * result + ((getName() == null) ? 0 : getName().hashCode());
        result = primeNumber * result + ((getValue() == null) ? 0 : getValue().hashCode());
        result = primeNumber * result + ((getModel() == null) ? 0 : getModel().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "TaskVariable{" + "name='" + getName() + '\'' + ", value='" + getValue() + '\'' + ", model='" +
               getModel() + '\'' + ", jobInherited=" + jobInherited + '}';
    }

}
