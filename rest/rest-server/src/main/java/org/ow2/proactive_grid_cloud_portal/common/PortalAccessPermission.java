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
package org.ow2.proactive_grid_cloud_portal.common;

import java.security.Permission;
import java.util.HashSet;

import org.ow2.proactive.permissions.ClientPermission;

import com.google.common.collect.Sets;


/**
 *
 * This permission allows to set access to portals
 * Portal naming should be a lowercase string with underscore word separation, e.g
 * scheduler, job_planner, automation_dashboard
 *
 */
public class PortalAccessPermission extends ClientPermission {

    boolean allPortals = false;

    HashSet<String> portalsAccess = new HashSet<>();

    /**
     * Construct the permission with portals list.
     *
     * @param portals separated by comma, or * for all portals
     */
    public PortalAccessPermission(String portals) {
        super("portal access");
        portals = portals.trim();
        if (portals == null || portals.isEmpty()) {
            portalsAccess = new HashSet<>();
        } else if (portals.equals("*")) {
            allPortals = true;
        } else {
            portalsAccess = Sets.newHashSet(portals.split("[\\s,]+"));
        }
    }

    /**
     * Checks that all portals of the permission we're checking with
     * are among allowed portals in "this" permission
     */
    @Override
    public boolean implies(Permission p) {
        if (!(p instanceof PortalAccessPermission)) {
            return false;
        }
        PortalAccessPermission clientRequest = (PortalAccessPermission) p;
        if (this.allPortals) {
            return true;
        }
        return portalsAccess.containsAll(clientRequest.portalsAccess);
    }
}
