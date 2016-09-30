/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
