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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.common.task.util;

/**
 * TaskConstructorTools is used to know if a user executable task (java or ProActive)
 * contains a no parameter constructor. If it is not the case, the executable cannot be launched.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 *
 */
public class TaskConstructorTools {

    /**
     * Return true if the given class contains a no parameter constructor.
     *
     * @param cla the class to check.
     * @return true if the given class contains a no parameter constructor, false if not.
     */
    public static boolean hasEmptyConstructor(Class<?> cla) {
        try {
            cla.getDeclaredConstructor(new Class<?>[] {});
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
