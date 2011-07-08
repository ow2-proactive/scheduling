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
package org.ow2.proactive.scheduler.common.job;

import java.net.MalformedURLException;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.task.CommonAttribute;


/**
 * Definition of a job for the user.
 * You can create a job by using this class. Job will be used to set some properties,
 * and give it the different tasks to run.
 * <p>
 * Here's a definition of the different parts of a job :<br>
 * {@link #setName(String)} will be used to identified the job.<br>
 * {@link #setDescription(String)} to set a short description of your job.<br>
 * {@link #setPriority(JobPriority)} to set the priority for the job, see {@link JobPriority} for more details.<br>
 * {@link #setCancelJobOnError(boolean)} used if you want to abort the job if an exception occurred in at least one of the task.<br>
 * {@link #setLogFile(String)} allow you to save the output of the job tasks in a specific file.<br>
 * <p>
 * Once the job created, you can submit it to the scheduler using the UserSchedulerInterface.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
@MappedSuperclass
@Table(name = "JOB")
@AccessType("field")
@Proxy(lazy = false)
public abstract class Job extends CommonAttribute {

    /** Name of the job */
    @Column(name = "NAME")
    protected String name = SchedulerConstants.JOB_DEFAULT_NAME;

    /** Execution environment for this job */
    @Cascade(CascadeType.ALL)
    @OneToOne(fetch = FetchType.EAGER, targetEntity = JobEnvironment.class)
    protected JobEnvironment environment = new JobEnvironment();

    /** Short description of this job */
    @Column(name = "DESCRIPTION", length = Integer.MAX_VALUE)
    @Lob
    protected String description = "No description";

    /** Project name for this job */
    @Column(name = "PROJECT_NAME")
    protected String projectName = "Not Assigned";

    /** Job priority */
    @Column(name = "PRIORITY", columnDefinition = "integer")
    protected JobPriority priority = JobPriority.NORMAL;

    @Column(name = "INPUT_SPACE")
    protected String inputSpace = null;
    @Column(name = "OUTPUT_SPACE")
    protected String outputSpace = null;

    /** ProActive Empty Constructor */
    public Job() {
    }

    /**
     * To get the type of this job
     *
     * @return the type of this job
     */
    public abstract JobType getType();

    /**
     * To get the id
     *
     * @return the id
     */
    public abstract JobId getId();

    /**
     * To get the description
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * To set a short description for the job.
     *
     * @param description the description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * To get the name of the job.
     *
     * @return the name of the job.
     */
    public String getName() {
        return name;
    }

    /**
     * To set the name of the job.
     *
     * @param name the name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * To get the priority of the job.
     *
     * @return the priority of the job.
     */
    public JobPriority getPriority() {
        return priority;
    }

    /**
     * To set the priority of the job. (Default is 'NORMAL')
     *
     * @param priority the priority to set.
     */
    public void setPriority(JobPriority priority) {
        this.priority = priority;
    }

    /**
     * Returns the project Name.
     * 
     * @return the project Name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Sets the project Name to the given projectName value.
     *
     * @param projectName the project Name to set.
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Return the environment for this job
     *
     * @see JobEnvironment
     * @return the environment for this job
     */
    public JobEnvironment getEnvironment() {
        return environment;
    }

    /**
     * Set the environment for this job.
     * @param environment the environment to set
     */
    public void setEnvironment(JobEnvironment environment) {
        this.environment = environment;
    }

    /**
     * Get the input Space
     *
     * @return the input Space
     */
    public String getInputSpace() {
        return inputSpace;
    }

    /**
     * Set the input Space value to the given inputSpace value
     *
     * @param inputSpace the input Space to set
     * @throws MalformedURLException
     */
    public void setInputSpace(String inputSpace) {
        this.inputSpace = inputSpace;
    }

    /**
     * Get the output Space
     *
     * @return the output Space
     */
    public String getOutputSpace() {
        return outputSpace;
    }

    /**
     * Set the output Space value to the given outputSpace value
     *
     * @param outputDataSpaceURL the outputDataSpaceURL to set
     */
    public void setOutputSpace(String outputSpace) {
        this.outputSpace = outputSpace;
    }
}
