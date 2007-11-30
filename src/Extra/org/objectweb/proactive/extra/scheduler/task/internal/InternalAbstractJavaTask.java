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
package org.objectweb.proactive.extra.scheduler.task.internal;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.extra.scheduler.common.task.executable.Executable;


/**
 * Abstract definition of a java task.
 * See also @see TaskDescriptor
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Jul 16, 2007
 * @since ProActive 3.9
 */
public abstract class InternalAbstractJavaTask extends InternalTask {

    /** Serial Version UID  */
    private static final long serialVersionUID = 1340022492862249182L;

    /** Class instance of the class to instantiate. */
    protected Class<?extends Executable> taskClass;

    /** Arguments of the task as a map */
    protected Map<String, Object> args = new HashMap<String, Object>();

    /**
     * ProActive empty constructor
     */
    public InternalAbstractJavaTask() {
    }

    /**
     * Create a new java task descriptor using the Class instance of the class to instantiate.
     *
     * @param taskClass the Class instance of the class to instantiate.
     */
    public InternalAbstractJavaTask(Class<?extends Executable> taskClass) {
        this.taskClass = taskClass;
    }

    /**
     * Get the task Class instance.
     *
     * @return the task Class instance.
     */
    public Class<?extends Executable> getTaskClass() {
        return taskClass;
    }

    /**
     * Set the task Class instance.
     *
     * @param taskClass the task Class instance.
     */
    public void setTaskClass(Class<?extends Executable> taskClass) {
        this.taskClass = taskClass;
    }

    /**
     * Get the task arguments as a map.
     *
     * @return the task arguments.
     */
    public Map<String, Object> getArgs() {
        return args;
    }

    /**
     * Set the task arguments as a map.
     *
     * @param args the task arguments.
     */
    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }
}
