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
package org.ow2.proactive.permissions;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;
import org.ow2.proactive.authentication.principals.*;


public class PrincipalPermissionTest {

    @Test
    public void userInGroupImpliesEmptyPermissionTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));
        perms.add(new PrincipalPermission("permissions", singleton(new GroupNamePrincipal("group0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions", emptySet());

        assertTrue(perms.implies(perm));
    }

    @Test
    public void userImpliesHimselfTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        PrincipalPermission perm = new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0")));
        perms.add(perm);

        assertTrue(perms.implies(perm));
    }

    @Test
    public void userInGroupImpliesHimWithOtherUserAndGroupsTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));
        perms.add(new PrincipalPermission("permissions", singleton(new GroupNamePrincipal("group0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           new HashSet<>(Arrays.asList(new UserNamePrincipal("user0"),
                                                                                       new UserNamePrincipal("user1"),
                                                                                       new GroupNamePrincipal("group1"),
                                                                                       new GroupNamePrincipal("group2"))));
        assertTrue(perms.implies(perm));
    }

    @Test
    public void userInGroupImpliesHimAndHisGroupWithOtherUserAndGroupTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));
        perms.add(new PrincipalPermission("permissions", singleton(new GroupNamePrincipal("group0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           new HashSet<>(Arrays.asList(new UserNamePrincipal("user0"),
                                                                                       new UserNamePrincipal("user1"),
                                                                                       new GroupNamePrincipal("group0"),
                                                                                       new GroupNamePrincipal("group1"))));
        assertTrue(perms.implies(perm));
    }

    @Test
    public void userNotImpliesOtherUserTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user1")));

        assertFalse(perms.implies(perm));
    }

    @Test
    public void userInGroupsNotImpliesOtherGroupsTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));
        perms.add(new PrincipalPermission("permissions", singleton(new GroupNamePrincipal("group0"))));
        perms.add(new PrincipalPermission("permissions", singleton(new GroupNamePrincipal("group1"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           new HashSet<>(Arrays.asList(new GroupNamePrincipal("group2"),
                                                                                       new GroupNamePrincipal("group3"))));

        assertFalse(perms.implies(perm));
    }

    @Test
    public void userNotImpliesOtherUsersTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           new HashSet<>(Arrays.asList(new UserNamePrincipal("user1"),
                                                                                       new UserNamePrincipal("user2"))));
        assertFalse(perms.implies(perm));
    }

    @Test
    public void userNotImpliesExcludedHimTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           singleton(new ExcludedUserNamePrincipal("user0")));

        assertFalse(perms.implies(perm));
    }

    @Test
    public void userImpliesExcludedOtherUserTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           singleton(new ExcludedUserNamePrincipal("user1")));

        assertTrue(perms.implies(perm));
    }

    @Test
    public void userInGroupNotImpliesExcludedHisGroupTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));
        perms.add(new PrincipalPermission("permissions", singleton(new GroupNamePrincipal("group0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           singleton(new ExcludedGroupNamePrincipal("group0")));

        assertFalse(perms.implies(perm));
    }

    @Test
    public void userInGroupImpliesExcludedOtherUsersAndGroupsTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));
        perms.add(new PrincipalPermission("permissions", singleton(new GroupNamePrincipal("group0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           new HashSet<>(Arrays.asList(new ExcludedUserNamePrincipal("user1"),
                                                                                       new ExcludedUserNamePrincipal("user2"),
                                                                                       new ExcludedGroupNamePrincipal("group1"),
                                                                                       new ExcludedGroupNamePrincipal("group2"))));

        assertTrue(perms.implies(perm));
    }

    @Test
    public void tokenImpliesEmptyTokenTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new TokenPrincipal("token0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions", emptySet());

        assertTrue(perms.implies(perm));
    }

    @Test
    public void tokenImpliesItselfTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        PrincipalPermission perm = new PrincipalPermission("permissions", singleton(new TokenPrincipal("token0")));
        perms.add(perm);

        assertTrue(perms.implies(perm));
    }

    @Test
    public void tokenNotImpliesOtherTokensTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new TokenPrincipal("token0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           new HashSet<>(Arrays.asList(new TokenPrincipal("token1"),
                                                                                       new TokenPrincipal("token2"))));

        assertFalse(perms.implies(perm));
    }

    @Test
    public void tokenImpliesItselfWithOtherTokenTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new TokenPrincipal("token0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           new HashSet<>(Arrays.asList(new TokenPrincipal("token0"),
                                                                                       new TokenPrincipal("token1"))));

        assertTrue(perms.implies(perm));
    }
}
