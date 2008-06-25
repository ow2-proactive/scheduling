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
package org.ow2.proactive.scheduler.common.task;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * This class represents the different restart mode for a task if an error occurred during its execution.<br>
 *
 * @author The ProActive Team
 * @version 4.0, May 30, 2008
 * @since ProActive 4.0
 */
@PublicAPI
public enum RestartMode implements java.io.Serializable {

    /**
     * The task will be restarted according to its possible resources.
     */
    ANYWHERE("Anywhere"),
    /**
     * The task will be restarted on an other node.
     */
    ELSEWHERE("Elsewhere"),
    /**
     * The task won't be restart.
     */
    NOWHERE("Nowhere");

    private String name;

    /**
     * Implicit constructor of a restart mode.
     *
     * @param name the name of the restart mode.
     */
    RestartMode(String name) {
        this.name = name;
    }

    public static RestartMode getMode(String sMode) {
        if ("elsewhere".equals(sMode)) {
            return ELSEWHERE;
        } else if ("anywhere".equals(sMode)) {
            return ANYWHERE;
        } else {
            return NOWHERE;
        }
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return name;
    }
}
