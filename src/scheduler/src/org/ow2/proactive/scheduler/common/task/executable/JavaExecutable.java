/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task.executable;

import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Extends this abstract class if you want to create your own java task.<br>
 * A java task is a task representing a java process as a java class.<br>
 * This class provides an {@link #init(Map)} that will get your parameters back for this task.
 * By default, this method does nothing.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public abstract class JavaExecutable extends Executable {

    /**
     * Initialization default method for a java task.
     * By default it does nothing.
     * You can override this method to make your own initialization.
     *
     * @param args a map containing the different parameter names and values.
     */
    @Override
    public void init(Map<String, String> args) throws Exception {
        // TODO : automatic assignation ?
    }

}
