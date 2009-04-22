/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.task.launcher;

import java.io.Serializable;

import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scripting.Script;


/**
 * TaskLauncherInitializer is used to initialize the different task launcher.<br>
 * It contains every information that can be used by the launchers. It's a kind of contract
 * so that each launcher must use its required information coming from this class. 
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class TaskLauncherInitializer implements Serializable {

    /** The task identification */
    private TaskId taskId;
    /** The content of the security policy file to be sent to forked launcher */
    private Script<?> pre;
    /** The script executed before the task */
    private Script<?> post;
    /** The script executed after the task */
    private String forkedPolicyContent;
    /** The walltime defined for the task (it is considered as defined if it is > 0) */
    private long walltime;
    /** Environment of a new dedicated JVM */
    private ForkEnvironment forkEnvironment;

    /**
     * Get the taskId
     *
     * @return the taskId
     */
    public TaskId getTaskId() {
        return taskId;
    }

    /**
     * Set the taskId value to the given taskId value
     * 
     * @param taskId the taskId to set
     */
    public void setTaskId(TaskId taskId) {
        this.taskId = taskId;
    }

    /**
     * Get the pre-script
     *
     * @return the pre-script
     */
    public Script<?> getPreScript() {
        return pre;
    }

    /**
     * Set the pre-script value to the given pre value
     * 
     * @param pre the pre-script to set
     */
    public void setPreScript(Script<?> pre) {
        this.pre = pre;
    }

    /**
     * Get the post-script
     *
     * @return the post-script
     */
    public Script<?> getPostScript() {
        return post;
    }

    /**
     * Set the post-script value to the given post value
     * 
     * @param post the post-script to set
     */
    public void setPostScript(Script<?> post) {
        this.post = post;
    }

    /**
     * Get the forked Policy Content
     *
     * @return the forked Policy Content
     */
    public String getForkedPolicyContent() {
        return forkedPolicyContent;
    }

    /**
     * Set the forked Policy Content value to the given forkedPolicyContent value
     * 
     * @param forkedPolicyContent the forked Policy Content to set
     */
    public void setForkedPolicyContent(String forkedPolicyContent) {
        this.forkedPolicyContent = forkedPolicyContent;
    }

    /**
     * Set the walltime value to the given walltime value
     * 
     * @param walltime the walltime to set
     */
    public void setWalltime(long walltime) {
        this.walltime = walltime;
    }

    /**
     * Get the walltime of the task
     *
     * @return the walltime of the task
     */
    public long getWalltime() {
        return walltime;
    }

    /**
     * Set the forkEnvironment value to the given forkEnvironment value
     * 
     * @param forkEnvironment the forkEnvironment to set
     */
    public void setForkEnvironment(ForkEnvironment forkEnvironment) {
        this.forkEnvironment = forkEnvironment;
    }

    /**
     * Get the forkEnvironment
     *
     * @return the forkEnvironment
     */
    public ForkEnvironment getForkEnvironment() {
        return forkEnvironment;
    }

}
