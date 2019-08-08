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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.task.util.IntegerWrapper;
import org.ow2.proactive.scheduler.common.task.util.LongWrapper;
import org.ow2.proactive.scheduler.common.util.VariableSubstitutor;


/**
 * Definition of the common attributes between job and task.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class CommonAttribute implements Serializable {

    /** The key for specifying start at time as generic information */
    public static final String GENERIC_INFO_START_AT_KEY = "START_AT";

    /** The default value of defining how long to wait before restart task in error (zero or negative value means restart immediately) */
    public static final Long DEFAULT_TASK_RETRY_DELAY = -1l;

    /**
     * Define where will a task be restarted if an error occurred (default is ANYWHERE).
     * <p>
     * It will be restarted according to the number of execution remaining.
     * <p>
     * You can override this property inside each task.
     */
    protected UpdatableProperties<RestartMode> restartTaskOnError = new UpdatableProperties<RestartMode>(RestartMode.ANYWHERE);

    /** Specify how long to wait before restart the task if an error occurred. */
    protected UpdatableProperties<LongWrapper> taskRetryDelay = new UpdatableProperties<LongWrapper>(new LongWrapper(DEFAULT_TASK_RETRY_DELAY));

    /**
     * The maximum number of execution for a task (default 1).
     * <p>
     * You can override this property inside each task.
     */
    protected UpdatableProperties<IntegerWrapper> maxNumberOfExecution = new UpdatableProperties<IntegerWrapper>(new IntegerWrapper(1));

    /** Common user informations */
    protected Map<String, String> genericInformation = new HashMap<String, String>();

    protected Map<String, String> unresolvedGenericInformation = new LinkedHashMap<>();

    /**
     * OnTaskError defines the behavior happening when a task fails.
     */
    protected UpdatableProperties<OnTaskError> onTaskError = new UpdatableProperties<>(OnTaskError.NONE);

    /**
     * Set onTaskError property value.
     * @param onTaskError A OnTaskError instance.
     * @throws IllegalArgumentException If set to null.
     */
    public void setOnTaskError(OnTaskError onTaskError) {
        if (onTaskError == null) {
            throw new IllegalArgumentException("OnTaskError cannot be set to null.");
        }
        this.onTaskError.setValue(onTaskError);
    }

    /**
     * Get the OnTaskError UpdatableProperties.
     * @return Reference to the UpdatableProperties instance hold by this class.
     */
    public UpdatableProperties<OnTaskError> getOnTaskErrorProperty() {
        return this.onTaskError;
    }

    /**
     * Get how long to wait before restart the task if an error occurred.
     *
     * @return delay to restart a task in error
     */
    public Long getTaskRetryDelay() {
        return taskRetryDelay.getValue().getLongValue();
    }

    /**
     * Get taskRetryDelay UpdatableProperties
     * @return taskRetryDelay UpdatableProperties
     */
    public UpdatableProperties<LongWrapper> getTaskRetryDelayProperty() {
        return this.taskRetryDelay;
    }

    /**
     * Set how long to wait before restart the task if an error occurred.
     *
     * @param taskRetryDelay delay to restart a task in error
     */
    public void setTaskRetryDelay(long taskRetryDelay) {
        this.taskRetryDelay.setValue(new LongWrapper(taskRetryDelay));
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
        return maxNumberOfExecution.getValue().getIntegerValue();
    }

    /**
     * To set the number of execution for this task. (Default is 1)
     *
     * @param numberOfExecution the number of times this task can be executed
     */
    public void setMaxNumberOfExecution(int numberOfExecution) {
        if (numberOfExecution <= 0) {
            throw new IllegalArgumentException("The number of execution must be a non negative integer (>0) !");
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
     * Returns generic information.
     * <p>
     * These information are transmitted to the policy that can use it for the scheduling.
     *
     * @return generic information.
     */
    public Map<String, String> getGenericInformation() {
        Set<Entry<String, String>> entries = this.genericInformation.entrySet();
        Map<String, String> result = new HashMap<>(entries.size());
        for (Entry<String, String> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Returns the generic information without variable replacements.
     *
     * @return unresolved generic information.
     */
    public Map<String, String> getUnresolvedGenericInformation() {
        Set<Entry<String, String>> entries = this.unresolvedGenericInformation.entrySet();
        Map<String, String> result = new LinkedHashMap<>(entries.size());
        for (Entry<String, String> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Add an information to the generic informations map field.
     * This information will be given to the scheduling policy.
     *
     * @param key the key in which to store the informations.
     * @param genericInformation the information to store.
     */
    public void addGenericInformation(String key, String genericInformation) {
        if (key != null && key.length() > 255) {
            throw new IllegalArgumentException("Key is too long, it must have 255 chars length max : " + key);
        }
        this.genericInformation.put(key, genericInformation);
    }

    /**
     * Add a map of generic informations into the generic informations map field.
     *
     * @param genericInformations the generic informations map to add.
     */
    public void addGenericInformations(Map<String, String> genericInformations) {
        if (genericInformations != null) {
            Set<Entry<String, String>> entries = genericInformations.entrySet();
            for (Entry<String, String> entry : entries) {
                addGenericInformation(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Set the generic information as a hash map.
     *
     * @param genericInformation the generic information to set.
     */
    public void setGenericInformation(Map<String, String> genericInformation) {
        if (genericInformation != null) {
            this.genericInformation = genericInformation;
        } else {
            this.genericInformation = new HashMap<>();
        }

    }

    /**
     * Set the generic information without variable replacement.
     *
     * @param unresolvedGenericInformation the raw generic information to set.
     */
    public void setUnresolvedGenericInformation(Map<String, String> unresolvedGenericInformation) {
        if (unresolvedGenericInformation != null) {
            this.unresolvedGenericInformation = unresolvedGenericInformation;
        } else {
            this.unresolvedGenericInformation = new HashMap<>();
        }

    }

    protected static Map<String, String> applyReplacementsOnGenericInformation(Map<String, String> genericInformation,
            Map<String, Serializable> variables) {
        return VariableSubstitutor.filterAndUpdate(genericInformation, variables);
    }

}
