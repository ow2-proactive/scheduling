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
package org.objectweb.proactive.extensions.scheduler.common.job;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * This is the different job priorities.
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Jun 11, 2007
 * @since ProActive 3.9
 */
@PublicAPI
public enum JobPriority implements java.io.Serializable {
    /** Lowest priority : administrator only */
    IDLE("Idle", 0),
    /** Lowest priority */
    LOWEST("Lowest", 1),
    /** Low priority */
    LOW("Low", 2),
    /** Normal Priority */
    NORMAL("Normal", 3),
    /** High priority : administrator only */
    HIGH("High", 4),
    /** Highest priority : administrator only */
    HIGHEST("Highest", 5);
    /** Name of the priority */
    private String name;

    /** Priority representing by an integer */
    private int priority;

    /**
     * Implicit constructor of job priority.
     *
     * @param name the name of the priority.
     * @param priority the integer representing the priority.
     */
    JobPriority(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Return the integer representing the priority.
     *
     * @return the integer representing the priority.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Get the priority associated with the given name.
     *
     * @param name the name of the priority to find as a string.
     * @return the job priority corresponding to the string or the NORMAL priority if not found.
     */
    public static JobPriority findPriority(String name) {
        if (name.equalsIgnoreCase(IDLE.toString())) {
            return IDLE;
        }

        if (name.equalsIgnoreCase(LOWEST.toString())) {
            return LOWEST;
        }

        if (name.equalsIgnoreCase(LOW.toString())) {
            return LOW;
        }

        if (name.equalsIgnoreCase(HIGH.toString())) {
            return HIGH;
        }

        if (name.equalsIgnoreCase(HIGHEST.toString())) {
            return HIGHEST;
        }

        return NORMAL;
    }
}
