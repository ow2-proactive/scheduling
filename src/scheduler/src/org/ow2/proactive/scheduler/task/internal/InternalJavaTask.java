/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.ow2.proactive.scheduler.task.internal;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.task.ForkedJavaTaskLauncher;
import org.ow2.proactive.scheduler.task.JavaExecutableContainer;
import org.ow2.proactive.scheduler.task.ForkEnvironment;
import org.ow2.proactive.scheduler.task.JavaTaskLauncher;
import org.ow2.proactive.scheduler.task.TaskLauncher;


/**
 * Description of a java task.
 * See also @see AbstractJavaTaskDescriptor
 *
 * @author The ProActive Team
 * @version 3.9, Jul 16, 2007
 * @since ProActive 3.9
 */
public class InternalJavaTask extends InternalTask {

    /** Whether user wants to execute a task in a separate JVM */
    private boolean fork = false;

    /** Environment of a new dedicated JVM */
    private ForkEnvironment forkEnvironment = null;

    /** Options passed to Java (not an application) (example: memory settings or properties) */
    private String javaOptions = null;

    /**
     * ProActive empty constructor
     */
    public InternalJavaTask() {
    }

    /**
     * Create a new Java task descriptor using instantiated java task.
     *
     * @param task the already instantiated java task.
     */
    public InternalJavaTask(JavaExecutableContainer execContainer) {
        this.executableContainer = execContainer;
    }

    /**
     * Create the launcher for this java task Descriptor.
     *
     * @param node the node on which to create the launcher.
     * @return the created launcher as an activeObject.
     */
    public TaskLauncher createLauncher(Node node) throws ActiveObjectCreationException, NodeException {
        JavaTaskLauncher launcher = null;
        if (fork || isWallTime()) {
            if (getPreScript() == null) {
                launcher = (ForkedJavaTaskLauncher) PAActiveObject.newActive(ForkedJavaTaskLauncher.class
                        .getName(), new Object[] { getId() }, node);
            } else {
                launcher = (ForkedJavaTaskLauncher) PAActiveObject.newActive(ForkedJavaTaskLauncher.class
                        .getName(), new Object[] { getId(), getPreScript() }, node);
            }
            ((ForkedJavaTaskLauncher) launcher).setForkEnvironment(forkEnvironment);
        } else {
            if (getPreScript() == null) {
                launcher = (JavaTaskLauncher) PAActiveObject.newActive(JavaTaskLauncher.class.getName(),
                        new Object[] { getId() }, node);
            } else {
                launcher = (JavaTaskLauncher) PAActiveObject.newActive(JavaTaskLauncher.class.getName(),
                        new Object[] { getId(), getPreScript() }, node);
            }
        }
        setExecuterInformations(new ExecuterInformations(launcher, node));
        setKillTaskTimer(launcher);

        return launcher;
    }

    /**
     * @return the fork if the user wants to execute the task in a separate JVM
     */
    public boolean isFork() {
        return fork;
    }

    /**
     * @param fork if the user wants to execute the task in a separate JVM
     */
    public void setFork(boolean fork) {
        this.fork = fork;
    }

    /**
     * @return the forkEnvironment
     */
    public ForkEnvironment getForkEnvironment() {
        return forkEnvironment;
    }

    /**
     * @param forkEnvironment the forkEnvironment to set
     */
    public void setForkEnvironment(ForkEnvironment forkEnvironment) {
        this.forkEnvironment = forkEnvironment;
    }

}
