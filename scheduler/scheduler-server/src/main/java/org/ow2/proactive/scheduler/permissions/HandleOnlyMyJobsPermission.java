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

import org.ow2.proactive.permissions.ClientPermission;


/**
 *
 * This permission allows to handle other users jobs or not
 *
 *
 */
public class HandleOnlyMyJobsPermission extends ClientPermission {

    private final static String DESCRIPTION = "Handle only my jobs";

    private boolean handleOnlyMyJobsPermissionAllowed = true;

    /**
     * Construct the permission with specified authorization string.
     *
     * @param handleOnlyMyJobsPermissionAllowed string that represents a boolean
     */
    public HandleOnlyMyJobsPermission(String handleOnlyMyJobsPermissionAllowed) {
        super(DESCRIPTION);
        this.handleOnlyMyJobsPermissionAllowed = "true".equalsIgnoreCase(handleOnlyMyJobsPermissionAllowed);
    }

    /**
     * Construct the permission with specified authorization boolean.
     *
     * @param handleOnlyMyJobsPermissionAllowed specified authorization boolean
     */
    public HandleOnlyMyJobsPermission(boolean handleOnlyMyJobsPermissionAllowed) {
        super(DESCRIPTION);
        this.handleOnlyMyJobsPermissionAllowed = handleOnlyMyJobsPermissionAllowed;
    }

    /**
     * check that the given permission matches with this permission
     */
    @Override
    public boolean implies(Permission p) {
        if (!(p instanceof HandleOnlyMyJobsPermission)) {
            return false;
        }
        HandleOnlyMyJobsPermission fsp = (HandleOnlyMyJobsPermission) p;
        //check incoming permission and permission given by the security file
        return !handleOnlyMyJobsPermissionAllowed || fsp.handleOnlyMyJobsPermissionAllowed;
    }
}
