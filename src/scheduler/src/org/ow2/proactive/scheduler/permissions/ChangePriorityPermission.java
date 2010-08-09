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
package org.ow2.proactive.scheduler.permissions;

import java.security.Permission;
import java.util.Arrays;

import org.ow2.proactive.permissions.ClientPermission;


/**
 *
 * This permission allows to set the priorities for jobs
 *
 *
 */
public class ChangePriorityPermission extends ClientPermission {

    /**  */
    private static final long serialVersionUID = 21L;
    int[] priorities;

    /**
     * Construct the permission with priorities list.
     *
     * @param priorities separated by comma
     */
    public ChangePriorityPermission(String priorities) {
        super("change priority");
        String[] split = priorities.split("[\\s,]+");
        this.priorities = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            this.priorities[i] = Integer.parseInt(split[i]);
        }
        Arrays.sort(this.priorities);
    }

    /**
     * Construct the permission with specified priority.
     *
     * @param allowed priority
     */
    public ChangePriorityPermission(int priority) {
        super("change priority");
        this.priorities = new int[1];
        priorities[0] = priority;
    }

    /**
     * Checks that all priorities of the permission we're checking with
     * are among priorities in "this" permission
     */
    @Override
    public boolean implies(Permission p) {
        if (!(p instanceof ChangePriorityPermission)) {
            return false;
        }
        // checking that all priorities of the permission we're checking with
        // are among priorities in "this" permission
        ChangePriorityPermission pp = (ChangePriorityPermission) p;
        for (int pr : pp.priorities) {
            if (Arrays.binarySearch(this.priorities, pr) < 0) {
                return false;
            }
        }
        return true;
    }
}
