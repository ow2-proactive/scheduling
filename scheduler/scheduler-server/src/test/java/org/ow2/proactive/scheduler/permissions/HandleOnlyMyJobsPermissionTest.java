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

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class HandleOnlyMyJobsPermissionTest {

    @Test
    public void testHandleOnlyMyJobsPermissionFalse() {
        //on security.java.policy-server file > permission org.ow2.proactive.scheduler.permissions.HandleOnlyMyJobsPermission "false";
        //In this case the user belonging to the group with the above configuration have permission (implies returns true) by default.
        HandleOnlyMyJobsPermission permissionDefinedAtJaasConfigurationLevel = new HandleOnlyMyJobsPermission("false");

        assertEquals(permissionDefinedAtJaasConfigurationLevel.implies(new HandleOnlyMyJobsPermission(false)), (true));

        assertEquals(permissionDefinedAtJaasConfigurationLevel.implies(new HandleOnlyMyJobsPermission(true)), (true));

    }

    @Test
    public void testHandleOnlyMyJobsPermissionTrue() {
        //on security.java.policy-server file > permission org.ow2.proactive.scheduler.permissions.HandleOnlyMyJobsPermission "true";
        //In this case the user belonging to the group with the above configuration have no permission (implies returns false)
        //unless the permission is given at runtime with new HandleOnlyMyJobsPermission(true).
        HandleOnlyMyJobsPermission permissionDefinedAtJaasConfigurationLevel = new HandleOnlyMyJobsPermission("true");

        assertEquals(permissionDefinedAtJaasConfigurationLevel.implies(new HandleOnlyMyJobsPermission(false)), (false));

        assertEquals(permissionDefinedAtJaasConfigurationLevel.implies(new HandleOnlyMyJobsPermission(true)), (true));

    }

}
