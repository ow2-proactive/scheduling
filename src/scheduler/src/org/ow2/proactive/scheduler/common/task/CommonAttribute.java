/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.task.util.BigString;
import org.ow2.proactive.scheduler.common.task.util.BooleanWrapper;
import org.ow2.proactive.scheduler.common.task.util.IntegerWrapper;


/**
 * Definition of the common attributes between job and task.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
@PublicAPI
@MappedSuperclass
@Table(name = "COMMON_ATTRIBUTE")
@AccessType("field")
@Proxy(lazy = false)
public abstract class CommonAttribute implements Serializable {
    /** 
     * Do the job has to cancel when an exception occurs in a task. (default is false) <br />
     * You can override this property inside each task.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
    protected UpdatableProperties<BooleanWrapper> cancelJobOnError = new UpdatableProperties<BooleanWrapper>(
        new BooleanWrapper(false));

    /** 
     * Where will a task be restarted if an error occurred. (default is ANYWHERE)<br />
     * It will be restarted according to the number of execution remaining.<br />
     * You can override this property inside each task.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
    protected UpdatableProperties<RestartMode> restartTaskOnError = new UpdatableProperties<RestartMode>(
        RestartMode.ANYWHERE);

    /**
     * The maximum number of execution for a task (default 1). <br />
     * You can override this property inside each task.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
    protected UpdatableProperties<IntegerWrapper> maxNumberOfExecution = new UpdatableProperties<IntegerWrapper>(
        new IntegerWrapper(1));

    /** Common user informations */
    @OneToMany(cascade = javax.persistence.CascadeType.ALL)
    @Cascade(CascadeType.ALL)
    @LazyCollection(value = LazyCollectionOption.FALSE)
    protected Map<String, BigString> genericInformations = new HashMap<String, BigString>();

    /**
     * To get the cancelOnError property
     *
     * @return the cancelOnError property
     */
    public boolean isCancelJobOnError() {
        return cancelJobOnError.getValue().booleanValue();
    }

    /**
     * Set to true if you want to cancel the job when an exception occurs in a task. (Default is false)
     *
     * @param cancelJobOnError the cancelJobOnError to set
     */
    public void setCancelJobOnError(boolean cancelJobOnError) {
        this.cancelJobOnError.setValue(new BooleanWrapper(cancelJobOnError));
    }

    /**
     * Get the cancelJobOnError updatable property.
     * 
     * @return the cancelJobOnError updatable property.
     */
    public UpdatableProperties<BooleanWrapper> getCancelJobOnErrorProperty() {
        return cancelJobOnError;
    }

    /**
     * Returns the restartTaskOnError state.
     * 
     * @return the restartTaskOnError state.
     */
    public RestartMode getRestartTaskOnError() {
        return restartTaskOnError.getValue();
    }

    /**
     * Sets the restartTaskOnError to the given restartOnError value. (Default is 'ANYWHERE')
     *
     * @param restartOnError the restartOnError to set.
     */
    public void setRestartTaskOnError(RestartMode restartOnError) {
        this.restartTaskOnError.setValue(restartOnError);
    }

    /**
     * Get the restartTaskOnError updatable property.
     * 
     * @return the restartTaskOnError updatable property.
     */
    public UpdatableProperties<RestartMode> getRestartTaskOnErrorProperty() {
        return restartTaskOnError;
    }

    /**
     * Get the number of execution allowed for this task.
     * 
     * @return the number of execution allowed for this task
     */
    public int getMaxNumberOfExecution() {
        return maxNumberOfExecution.getValue().integerValue();
    }

    /**
     * To set the number of execution for this task. (Default is 1)
     *
     * @param numberOfExecution the number of times this task can be executed
     */
    public void setMaxNumberOfExecution(int numberOfExecution) {
        if (numberOfExecution <= 0) {
            throw new IllegalArgumentException(
                "The number of execution must be a non negative integer (>0) !");
        }
        this.maxNumberOfExecution.setValue(new IntegerWrapper(numberOfExecution));
    }

    /**
     * Get the maximum number Of Execution updatable property.
     * 
     * @return the maximum number Of Execution updatable property
     */
    public UpdatableProperties<IntegerWrapper> getMaxNumberOfExecutionProperty() {
        return maxNumberOfExecution;
    }

    /**
     * Returns the genericInformations.<br>
     * These informations are transmitted to the policy that can use it for the scheduling.
     *
     * @return the genericInformations.
     */
    public Map<String, String> getGenericInformations() {
        Map<String, String> tmp = new HashMap<String, String>();
        for (Entry<String, BigString> e : this.genericInformations.entrySet()) {
            tmp.put(e.getKey(), e.getValue().getValue());
        }
        return tmp;
    }

    /**
     * Add an information to the generic field informations field.
     * This information will be given to the scheduling policy.
     *
     * @param key the key in which to store the informations.
     * @param genericInformation the information to store.
     */
    public void addGenericInformation(String key, String genericInformation) {
        if (key != null && key.length() > 255) {
            throw new IllegalArgumentException("Key is too long, it must have 255 chars length max : " + key);
        }
        this.genericInformations.put(key, new BigString(genericInformation));
    }

    /**
     * Set the generic information as a hash map.
     *
     * @param genericInformations the generic information to set.
     */
    public void setGenericInformations(Map<String, String> genericInformations) {
        this.genericInformations = new HashMap<String, BigString>();
        if (genericInformations != null) {
            for (Entry<String, String> e : genericInformations.entrySet()) {
                addGenericInformation(e.getKey(), e.getValue());
            }
        }
    }

}
