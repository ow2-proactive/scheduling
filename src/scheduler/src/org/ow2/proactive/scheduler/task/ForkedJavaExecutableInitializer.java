/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package org.ow2.proactive.scheduler.task;

import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.task.JavaExecutableInitializer;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncherInitializer;


/**
 * ForkedJavaExecutableInitializer is the class used to store context of forked java executable initialization
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 */
public class ForkedJavaExecutableInitializer extends JavaExecutableInitializer {

    /**  */
    private static final long serialVersionUID = 21L;

    /** Environment of a new dedicated JVM */
    private ForkEnvironment forkEnvironment = null;

    /** Launcher initializer containing scripts */
    private TaskLauncherInitializer taskLauncherInitializer;

    /** Java executable container for the forked java task */
    private JavaExecutableContainer javaExecutableContainer;

    /**
     * Create a new instance of ForkedJavaExecutableInitializer
     *
     * @param execInitializer the parent initializer in order to decorate this one
     */
    public ForkedJavaExecutableInitializer(JavaExecutableInitializer execInitializer) {
        this.serializedArguments = execInitializer.getSerializedArguments();
        this.nodes = execInitializer.getNodes();
    }

    /**
     * Get the forkEnvironment
     *
     * @return the forkEnvironment
     */
    public ForkEnvironment getForkEnvironment() {
        return forkEnvironment;
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
     * Get the taskLauncherInitializer
     *
     * @return the taskLauncherInitializer
     */
    public TaskLauncherInitializer getJavaTaskLauncherInitializer() {
        return taskLauncherInitializer;
    }

    /**
     * Set the taskLauncherInitializer value to the given taskLauncherInitializer value
     *
     * @param taskLauncherInitializer the taskLauncherInitializer to set
     */
    public void setJavaTaskLauncherInitializer(TaskLauncherInitializer taskLauncherInitializer) {
        this.taskLauncherInitializer = taskLauncherInitializer;
    }

    /**
     * Get the javaExecutableContainer
     *
     * @return the javaExecutableContainer
     */
    public JavaExecutableContainer getJavaExecutableContainer() {
        return javaExecutableContainer;
    }

    /**
     * Set the javaExecutableContainer value to the given javaExecutableContainer value
     *
     * @param javaExecutableContainer the javaExecutableContainer to set
     */
    public void setJavaExecutableContainer(JavaExecutableContainer javaExecutableContainer) {
        this.javaExecutableContainer = javaExecutableContainer;
    }

}
