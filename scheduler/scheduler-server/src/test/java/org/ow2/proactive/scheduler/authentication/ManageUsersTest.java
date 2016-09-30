/*
 *  *
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.authentication;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.authentication.FileLoginModule;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.authentication.crypto.HybridEncryptionUtil;
import org.ow2.proactive.authentication.crypto.KeyGen;
import org.ow2.tests.ProActiveTest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.security.KeyException;
import java.security.PrivateKey;
import java.util.Map;
import java.util.Properties;

/**
 * Tests creation, modification and deletion of users with ManageUsers tool
 */
public class ManageUsersTest extends ProActiveTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    File loginFile;
    File groupFile;
    private File privateKeyFile;
    private File publicKeyFile;

    PrivateKey privateKey;

    final Map<String, String> users = ImmutableMap.<String, String>builder().
            put("user1", "pwd1").
            put("user2", "pwd2").
            put("admin1", "pwd3").
            put("admin2", "pwd4").
            build();

    final Map<String, String> users2 = ImmutableMap.<String, String>builder().
            put("user1", "pwda").
            put("user2", "pwdb").
            put("admin1", "pwdc").
            put("admin2", "pwdd").
            build();

    final Multimap<String, String> groups = ImmutableMultimap.<String, String>builder().
            putAll("user1", "user").
            putAll("user2", "user", "other").
            putAll("admin1", "admin").
            putAll("admin2", "admin", "other").
            build();

    final Multimap<String, String> groups2 = ImmutableMultimap.<String, String>builder().
            putAll("user1", "user", "other").
            putAll("user2", "user").
            putAll("admin1", "admin", "other").
            putAll("admin2", "admin").
            build();


    @Before
    public void init() throws Exception {
        loginFile = tmpFolder.newFile("login");
        groupFile = tmpFolder.newFile("group");
        privateKeyFile = tmpFolder.newFile("priv.key");
        publicKeyFile = tmpFolder.newFile("pub.key");
        KeyGen.main(new String[]{"-P", publicKeyFile.getAbsolutePath(), "-p", privateKeyFile.getAbsolutePath()});
        privateKey = Credentials.getPrivateKey(privateKeyFile.getAbsolutePath());
    }

    @Test
    public void testUsers() throws Exception {
        createUsers();
        updateUsers();
        deleteUsers();
    }

    private String getGroupString(String user, Multimap<String, String> groupsToUser) {
        StringBuilder answer = new StringBuilder();
        for (String group : groupsToUser.get(user)) {
            answer.append(group).append(",");
        }
        answer.deleteCharAt(answer.length() - 1);
        return answer.toString();
    }

    private void createUsers() throws Exception {
        for (Map.Entry<String, String> user : users.entrySet()) {
            ManageUsers.manageUsers(new String[]{"-" + ManageUsers.CREATE_OPTION,
                    "-" + ManageUsers.LOGIN_OPTION, user.getKey(),
                    "-" + ManageUsers.PWD_OPTION, user.getValue(),
                    "-" + ManageUsers.GROUPS_OPTION, getGroupString(user.getKey(), groups),
                    "-" + ManageUsers.LOGINFILE_OPTION, loginFile.getAbsolutePath(),
                    "-" + ManageUsers.GROUPFILE_OPTION, groupFile.getAbsolutePath(),
                    "-" + ManageUsers.KEYFILE_OPTION, publicKeyFile.getAbsolutePath()
            });
        }
        validateContents(users, groups);
    }

    private void updateUsers() throws Exception {
        for (Map.Entry<String, String> user : users2.entrySet()) {
            ManageUsers.manageUsers(new String[]{"-" + ManageUsers.UPDATE_OPTION,
                    "-" + ManageUsers.LOGIN_OPTION, user.getKey(),
                    "-" + ManageUsers.PWD_OPTION, user.getValue(),
                    "-" + ManageUsers.GROUPS_OPTION, getGroupString(user.getKey(), groups2),
                    "-" + ManageUsers.LOGINFILE_OPTION, loginFile.getAbsolutePath(),
                    "-" + ManageUsers.GROUPFILE_OPTION, groupFile.getAbsolutePath(),
                    "-" + ManageUsers.KEYFILE_OPTION, publicKeyFile.getAbsolutePath()
            });
        }
        validateContents(users2, groups2);
    }

    private void deleteUsers() throws Exception {
        for (Map.Entry<String, String> user : users.entrySet()) {
            ManageUsers.manageUsers(new String[]{"-" + ManageUsers.DELETE_OPTION,
                    "-" + ManageUsers.LOGIN_OPTION, user.getKey(),
                    "-" + ManageUsers.LOGINFILE_OPTION, loginFile.getAbsolutePath(),
                    "-" + ManageUsers.GROUPFILE_OPTION, groupFile.getAbsolutePath(),
                    "-" + ManageUsers.KEYFILE_OPTION, publicKeyFile.getAbsolutePath()
            });
        }
        String loginContent = IOUtils.toString(loginFile.toURI());
        Assert.assertTrue("login file should be empty", loginContent.trim().isEmpty());
        String groupContent = IOUtils.toString(groupFile.toURI());
        Assert.assertTrue("group file should be empty", groupContent.trim().isEmpty());
    }

    private void validateContents(Map<String, String> usersToCheck, Multimap<String, String> groupsToCheck) throws IOException, KeyException {
        Properties props = new Properties();
        try (Reader reader = new FileReader(loginFile)) {
            props.load(reader);
            String groupContent = IOUtils.toString(groupFile.toURI());
            for (Map.Entry<String, String> user : usersToCheck.entrySet()) {
                Assert.assertTrue("login file should contain " + user.getKey(), props.containsKey(user.getKey()));
                String encryptedPassword = (String) props.get(user.getKey());
                String password = HybridEncryptionUtil.decryptBase64String(encryptedPassword, privateKey, FileLoginModule.ENCRYPTED_DATA_SEP);
                Assert.assertEquals("decrypted password for user " + user.getKey() + " should match", user.getValue(), password);
                for (String group : groupsToCheck.get(user.getKey())) {
                    Assert.assertTrue("group file should contain " + user.getKey() + ":" + group, groupContent.contains(user.getKey() + ":" + group));
                }
            }
        }

    }


}
