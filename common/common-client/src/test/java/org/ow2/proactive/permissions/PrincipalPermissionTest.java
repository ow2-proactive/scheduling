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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;
import org.ow2.proactive.authentication.principals.*;


public class PrincipalPermissionTest {

    @Test
    public void userInGroupImpliesEmptyUserTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));
        perms.add(new PrincipalPermission("permissions", singleton(new GroupNamePrincipal("group0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions", emptySet());

        assertTrue(perms.implies(perm));
    }

    @Test
    public void userImpliesSameUserTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        PrincipalPermission perm = new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0")));
        perms.add(perm);

        assertTrue(perms.implies(perm));
    }

    @Test
    public void userInGroupsImpliesUsersGroupsWithHimTest() throws Exception {

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
    public void userInGroupsImpliesUsersGroupsWithHisGroupTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));
        perms.add(new PrincipalPermission("permissions", singleton(new GroupNamePrincipal("group0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           new HashSet<>(Arrays.asList(new UserNamePrincipal("user0"),
                                                                                       new UserNamePrincipal("user1"),
                                                                                       new GroupNamePrincipal("group0"),
                                                                                       new GroupNamePrincipal("group2"))));
        assertTrue(perms.implies(perm));
    }

    @Test
    public void userImpliesDifferentUserTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user1")));

        assertTrue(!perms.implies(perm));
    }

    @Test
    public void userInGroupImpliesDifferentGroupTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));
        perms.add(new PrincipalPermission("permissions", singleton(new GroupNamePrincipal("group0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           new HashSet<>(Arrays.asList(new GroupNamePrincipal("group1"),
                                                                                       new GroupNamePrincipal("group2"))));

        assertTrue(!perms.implies(perm));
    }

    @Test
    public void userImpliesUsersWithoutHimTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           new HashSet<>(Arrays.asList(new UserNamePrincipal("user1"),
                                                                                       new UserNamePrincipal("user2"))));
        assertTrue(!perms.implies(perm));
    }

    @Test
    public void userImpliesNotSameUserTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions", singleton(new NotUserNamePrincipal("user0")));

        assertTrue(!perms.implies(perm));
    }

    @Test
    public void userImpliesNotDifferentUserTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions", singleton(new NotUserNamePrincipal("user1")));

        assertTrue(perms.implies(perm));
    }

    @Test
    public void usersInGroupImpliesNotSameGroupTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));
        perms.add(new PrincipalPermission("permissions", singleton(new GroupNamePrincipal("group0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           singleton(new NotGroupNamePrincipal("group0")));

        assertTrue(!perms.implies(perm));
    }

    @Test
    public void userInGroupImpliesNotDifferentUsersGroupsTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new UserNamePrincipal("user0"))));
        perms.add(new PrincipalPermission("permissions", singleton(new GroupNamePrincipal("group0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           new HashSet<>(Arrays.asList(new NotUserNamePrincipal("user1"),
                                                                                       new NotUserNamePrincipal("user2"),
                                                                                       new NotGroupNamePrincipal("group1"),
                                                                                       new NotGroupNamePrincipal("group2"))));

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
    public void tokenImpliesSameTokenTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        PrincipalPermission perm = new PrincipalPermission("permissions", singleton(new TokenPrincipal("token0")));
        perms.add(perm);

        assertTrue(perms.implies(perm));
    }

    @Test
    public void tokenImpliesDifferentTokensTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new TokenPrincipal("token0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           new HashSet<>(Arrays.asList(new TokenPrincipal("token1"),
                                                                                       new TokenPrincipal("token2"))));

        assertTrue(!perms.implies(perm));
    }

    @Test
    public void tokenImpliesDifferentTokensWithItTest() throws Exception {

        PrincipalPermissionCollection perms = new PrincipalPermissionCollection();
        perms.add(new PrincipalPermission("permissions", singleton(new TokenPrincipal("token0"))));

        PrincipalPermission perm = new PrincipalPermission("permissions",
                                                           new HashSet<>(Arrays.asList(new TokenPrincipal("token0"),
                                                                                       new TokenPrincipal("token1"))));

        assertTrue(perms.implies(perm));
    }
}
