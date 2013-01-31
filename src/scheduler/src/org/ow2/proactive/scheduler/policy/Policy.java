/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.ow2.proactive.scheduler.policy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.common.RMState;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;


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
     * Create a new instance of Policy. (must be public)
     * Called by class.forname when instantiating the policy.
     *<br/><br/>
     * The {@link #reloadConfig()} method is called by the scheduler after the creation of this policy instance
     * This empty constructor remains mandatory to enable the core to instantiate the policy but SchedulerCore
     * does not wait for any special or mandatory behavior inside.
     * <br/><br/>
     * This constructor can/should be empty.
     */
    public Policy() {
        //reloadConfig();
    }

    /**
     * Return the tasks that have to be scheduled.
     * The tasks must be in the desired scheduling order.
     * The first task to be schedule must be the first in the returned Vector.
     *
     * @param jobs the list of pending or running job descriptors.
     * @return a vector of every tasks that are ready to be schedule.
     */
    public abstract Vector<EligibleTaskDescriptor> getOrderedTasks(List<JobDescriptor> jobs);

    /**
     * Set the RM state
     *
     * @param RM state
     */
    public final void setRMState(RMState state) {
        this.RMState = state;
    }

    /**
     * Return the configuration as properties (key->value) read in the policy config file.
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
     * Reload the configuration file of the corresponding policy.<br/>
     * This method is called each time a reload action is performed for the policy
     * (ie. {@link Scheduler#reloadPolicyConfiguration()} method is called from client side)<br/>
     * <br/>
     * This method just call {@link #getConfigFile()} method and then, load the content of the returned file
     * in this policy properties.<br/>
     * <br/>
     * Override this method to have a fully custom reload, override only the {@link #getConfigFile()} method to
     * change the properties file location.
     * <br/>
     * This method will reload the whole content of the properties. Every old properties will be erased and new one from file
     * will be set.
     * <br/>
     * <br/>
     * Note : this method is also called once at Scheduler startup.
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
     * Return the configuration file for this policy.<br/>
     * Default configuration file is config/policy/[SimpleClassName].conf.<br/>
     * <br/>
     * In the default behavior, this method is called by {@link #reloadConfig()} method each time a policy reload is performed.<br/>
     * <br/>
     * Override this method to change the default path where to find the configuration file.
     */
    protected File getConfigFile() {
        String relPath = "config/scheduler/policy/" + getClass().getSimpleName() + ".conf";
        return new File(PASchedulerProperties.getAbsolutePath(relPath));
    }

}
