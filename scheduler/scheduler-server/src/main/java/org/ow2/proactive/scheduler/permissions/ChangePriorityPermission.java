/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
     * @param priority priority
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
