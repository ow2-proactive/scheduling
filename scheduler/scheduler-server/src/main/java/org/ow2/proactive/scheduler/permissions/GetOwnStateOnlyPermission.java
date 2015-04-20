/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 * This permission allows to get the full state or not
 *
 *
 */
public class GetOwnStateOnlyPermission extends ClientPermission {

    private static final long serialVersionUID = 62L;

    private boolean fullStateAllowed = true;

    /**
     * Construct the permission with specified authorization string.
     *
     * @param fullState string that represents a boolean
     */
    public GetOwnStateOnlyPermission(String fullState) {
        super("get full state");
        this.fullStateAllowed = "true".equalsIgnoreCase(fullState);
    }

    /**
     * Construct the permission with specified authorization boolean.
     *
     * @param fullState specified authorization boolean
     */
    public GetOwnStateOnlyPermission(boolean fullState) {
        super("get full state");
        this.fullStateAllowed = fullState;
    }

    /**
     * check that the given permission matches with this permission
     */
    @Override
    public boolean implies(Permission p) {
        if (!(p instanceof GetOwnStateOnlyPermission)) {
            return false;
        }
        GetOwnStateOnlyPermission fsp = (GetOwnStateOnlyPermission) p;
        //check incoming permission and permission given by the security file
        return !fullStateAllowed || fsp.fullStateAllowed;
    }
}
