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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


public class HandleJobsWithGenericInformationPermissionTest {

    @Test
    public void testHandleOnlyMyJobsPermissionFalseEmptyGenericInformationEmptyRule() {
        // on security.java.policy-server file > permission
        // org.ow2.proactive.scheduler.permissions.HandleJobsWithGenericInformationPermission
        // "";
        HandleJobsWithGenericInformationPermission permissionDefinedAtJaasConfigurationLevel = new HandleJobsWithGenericInformationPermission("");

        Map<String, String> genericInformation = new HashMap<String, String>();

        assertEquals(permissionDefinedAtJaasConfigurationLevel.implies(new HandleJobsWithGenericInformationPermission(genericInformation)),
                     (false));

    }

    @Test
    public void testHandleOnlyMyJobsPermissionFalseGenericInformationEmptyRule() {
        // on security.java.policy-server file > permission
        // org.ow2.proactive.scheduler.permissions.HandleJobsWithGenericInformationPermission
        // "";
        HandleJobsWithGenericInformationPermission permissionDefinedAtJaasConfigurationLevel = new HandleJobsWithGenericInformationPermission("");

        Map<String, String> genericInformation = new HashMap<String, String>();
        genericInformation.put("GROUP", "admin");

        assertEquals(permissionDefinedAtJaasConfigurationLevel.implies(new HandleJobsWithGenericInformationPermission(genericInformation)),
                     (false));

    }

    @Test
    public void testHandleOnlyMyJobsPermissionFalseEmptyGenericInformation() {
        // on security.java.policy-server file > permission
        // org.ow2.proactive.scheduler.permissions.HandleJobsWithGenericInformationPermission
        // "GROUP=guests";
        HandleJobsWithGenericInformationPermission permissionDefinedAtJaasConfigurationLevel = new HandleJobsWithGenericInformationPermission("GROUP=guests");

        Map<String, String> genericInformation = new HashMap<String, String>();

        assertEquals(permissionDefinedAtJaasConfigurationLevel.implies(new HandleJobsWithGenericInformationPermission(genericInformation)),
                     (false));

    }

    @Test
    public void testHandleOnlyMyJobsPermissionFalseWrongGenericInformation() {
        // on security.java.policy-server file > permission
        // org.ow2.proactive.scheduler.permissions.HandleJobsWithGenericInformationPermission
        // "GROUP=guests";
        HandleJobsWithGenericInformationPermission permissionDefinedAtJaasConfigurationLevel = new HandleJobsWithGenericInformationPermission("GROUP=guests");

        Map<String, String> genericInformation = new HashMap<String, String>();
        genericInformation.put("GROUP", "admin");

        assertEquals(permissionDefinedAtJaasConfigurationLevel.implies(new HandleJobsWithGenericInformationPermission(genericInformation)),
                     (false));

    }

    @Test
    public void testHandleOnlyMyJobsPermissionTrueCorrectGenericInformation() {
        // on security.java.policy-server file > permission
        // org.ow2.proactive.scheduler.permissions.HandleJobsWithGenericInformationPermission
        // "GROUP=guests";
        HandleJobsWithGenericInformationPermission permissionDefinedAtJaasConfigurationLevel = new HandleJobsWithGenericInformationPermission("GROUP=guests");

        Map<String, String> genericInformation = new HashMap<String, String>();
        genericInformation.put("GROUP", "guests");

        assertEquals(permissionDefinedAtJaasConfigurationLevel.implies(new HandleJobsWithGenericInformationPermission(genericInformation)),
                     (true));

    }

    @Test
    public void testHandleOnlyMyJobsPermissionFalseMultipleGenericInformation() {
        // on security.java.policy-server file > permission
        // org.ow2.proactive.scheduler.permissions.HandleJobsWithGenericInformationPermission
        // "GROUP=guests,TYPE=entry";
        HandleJobsWithGenericInformationPermission permissionDefinedAtJaasConfigurationLevel = new HandleJobsWithGenericInformationPermission("GROUP=guests,TYPE=entry");

        Map<String, String> genericInformation = new HashMap<String, String>();
        genericInformation.put("GROUP", "guests");

        assertEquals(permissionDefinedAtJaasConfigurationLevel.implies(new HandleJobsWithGenericInformationPermission(genericInformation)),
                     (false));

    }

    @Test
    public void testHandleOnlyMyJobsPermissionTrueMultipleGenericInformation() {
        // on security.java.policy-server file > permission
        // org.ow2.proactive.scheduler.permissions.HandleJobsWithGenericInformationPermission
        // "GROUP=guests,TYPE=entry";
        HandleJobsWithGenericInformationPermission permissionDefinedAtJaasConfigurationLevel = new HandleJobsWithGenericInformationPermission("GROUP=guests,TYPE=entry");

        Map<String, String> genericInformation = new HashMap<String, String>();
        genericInformation.put("GROUP", "guests");
        genericInformation.put("TYPE", "entry");
        genericInformation.put("SOME_OTHER", "random");

        assertEquals(permissionDefinedAtJaasConfigurationLevel.implies(new HandleJobsWithGenericInformationPermission(genericInformation)),
                     (true));

    }

}
