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

import static org.hamcrest.CoreMatchers.instanceOf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * 
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.9
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskVariable  {

    private String name;

    private String value;

    private boolean jobInherited;

    private String model;

    /** ProActive default constructor */
    public TaskVariable() {
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

    public boolean isJobInherited() {
        return jobInherited;
    }

    public void setJobInherited(boolean inherited) {
        this.jobInherited = inherited;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
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
        if (name == null) {
            if (taskVariable.name != null){
                return false;
            }
        } else if (!name.equals(taskVariable.name)){
            return false;
        }
        if (value == null) {
            if (taskVariable.value != null){
                return false;
            }
        } else if (!value.equals(taskVariable.value)){
            return false;
        }
        if (model == null) {
            if (taskVariable.model != null){
                return false;
            }
        } else if (!model.equals(taskVariable.model)){
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
    final int primeNumber = 31;
    int result = 1;
    result = primeNumber * result + (jobInherited ? 3 : 5);
    result = primeNumber * result + ((name == null) ? 0 : name.hashCode());
    result = primeNumber * result + ((value == null) ? 0 : value.hashCode());
    result = primeNumber * result + ((model == null) ? 0 : model.hashCode());
    return result;
    }  

}
