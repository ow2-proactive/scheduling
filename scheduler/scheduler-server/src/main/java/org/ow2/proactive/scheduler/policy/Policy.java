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
package org.ow2.proactive.scheduler.policy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.utils.NodeSet;


/**
 * Policy interface for the scheduler.
 * Must be implemented in order to be used as a policy in the scheduler core.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public abstract class Policy implements Serializable {

    protected static final Logger logger = Logger.getLogger(Policy.class);

    /**
     * Resources manager state. Can be used in an inherit policy to be aware
     * of resources informations like total nodes number, used nodes, etc.
     * Can be null the first time the {@link #getOrderedTasks(List)} method is called.
     */
    protected RMState RMState = null;

    /** Config properties */
    protected Properties configProperties = null;

    /**
     * Create a new instance of Policy (must be public).
     * Called by class.forname when instantiating the policy.
     * <p>
     * The {@link #reloadConfig()} method is called by the scheduler after the creation of this policy instance
     * This empty constructor remains mandatory to enable the core to instantiate the policy but SchedulerCore
     * does not wait for any special or mandatory behavior inside.
     * <p>
     * This constructor can/should be empty.
     */
    public Policy() {
        //reloadConfig();
    }

    /**
     * Return the tasks that have to be scheduled.
     * The tasks must be in the desired scheduling order.
     * The first task to be schedule must be the first in the returned list.
     * The list will be modified by the scheduling loop, so it may be necessary to copy the list before returning it
     *
     * @param jobs the list of pending or running job descriptors.
     * @return a linked list of every tasks that are ready to be scheduled.
     */
    public abstract LinkedList<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobs);

    /**
     * After the selection process, overriding this method allows to do some filtering on the task scheduled
     * This is useful, for example, when stateless selection scripts cannot completely determine if a node is eligible for execution.
     * Here the scheduling policy can make further decisions and discard the unsuited task.
     *
     * @param selectedNodes set of nodes which were selected to execute the task
     * @param task         task scheduled
     */
    public boolean isTaskExecutable(NodeSet selectedNodes, EligibleTaskDescriptor task) {
        return true;
    }

    /**
     * Set the RM state
     *
     * @param state resource manager state
     */
    public final void setRMState(RMState state) {
        this.RMState = state;
    }

    /**
     * Return the configuration as properties (key-&gt;value) read in the policy config file.
     * The returned value can be null or empty if no property has been loaded.
     *
     * @return the configuration read in the policy config file.
     */
    protected final Properties getConfigurationProperties() {
        return configProperties;
    }

    /**
     * Return the value of the given property key.
     * The returned value can be null if property does not exist.
     *
     * @return the value of the given property key or null if not found.
     */
    protected final String getProperty(final String name) {
        if (configProperties == null) {
            return null;
        }
        return configProperties.getProperty(name);
    }

    /**
     * Reload the configuration file of the corresponding policy.
     * This method is called each time a reload action is performed for the policy
     * (ie. {@link Scheduler#reloadPolicyConfiguration()} method is called from client side)
     * <p>
     * This method just call {@link #getConfigFile()} method and then, load the content of the returned file
     * in this policy properties.
     * <p>
     * Override this method to have a fully custom reload, override only the {@link #getConfigFile()} method to
     * change the properties file location.
     * <p>
     * This method will reload the whole content of the properties. Every old properties will be erased and new one from file
     * will be set.
     * <p>
     * Note: this method is also called once at Scheduler startup.
     *
     * @return true if the configuration has been successfully reload, false otherwise.
     *
     * @see Properties#load(java.io.InputStream) Properties.load(java.io.InputStream) for more details
     * 		about the structure and content of property file
     */
    public boolean reloadConfig() {
        configProperties = new Properties();
        try {
            FileInputStream fis = new FileInputStream(getConfigFile());
            configProperties.load(fis);
            fis.close();
            return true;
        } catch (IOException ioe) {
            logger.warn("Cannot read policy configuration file", ioe);
            return false;
        }
    }

    /**
     * Return the configuration file for this policy.
     * Default configuration file is config/policy/[SimpleClassName].conf.
     * <p>
     * In the default behavior, this method is called by {@link #reloadConfig()} method each time a policy reload is performed.
     * <p>
     * Override this method to change the default path where to find the configuration file.
     */
    protected File getConfigFile() {
        String relPath = "config/scheduler/policy/" + getClass().getSimpleName() + ".conf";
        return new File(PASchedulerProperties.getAbsolutePath(relPath));
    }

}
