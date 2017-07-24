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
package org.ow2.proactive.scheduler.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.KeyException;
import java.security.PrivateKey;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
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
import org.ow2.tests.ProActiveTestClean;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;


/**
 * Tests creation, modification and deletion of users with ManageUsers tool
 */
public class ManageUsersTest extends ProActiveTestClean {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    File loginFile;

    File groupFile;

    File sourceLoginFile;

    File sourceGroupFile;

    private File privateKeyFile;

    private File publicKeyFile;

    PrivateKey privateKey;

    final List<String> sourceUsers1 = ImmutableList.of("user1:pwd1",
                                                       "user2:pwd2",
                                                       "userà:pwdà",
                                                       "admin1:pwd3",
                                                       "admin2:pwd4",
                                                       "usernopwd:",
                                                       ":nologin",
                                                       "usernogroup:toobad");

    final List<String> sourceUsers2 = ImmutableList.of("user1:pwda",
                                                       "user2:pwdb",
                                                       "userà:pwdé",
                                                       "admin1:pwdc",
                                                       "admin2:pwdd",
                                                       "usernopwd:",
                                                       ":nologin",
                                                       "usernogroup:toobad");

    final List<String> sourceGroups1 = ImmutableList.of("user1:user",
                                                        "user2:user",
                                                        "user2:other",
                                                        "userà:groupà",
                                                        "admin1:admin",
                                                        "admin2:admin",
                                                        "admin2:other",
                                                        "useremptygroup:",
                                                        ":nologin",
                                                        "userwithoutcredentials:admin");

    final List<String> sourceGroups2 = ImmutableList.of("user1:user",
                                                        "user1:other",
                                                        "user2:user",
                                                        "userà:groupé",
                                                        "admin1:admin",
                                                        "admin1:other",
                                                        "admin2:admin",
                                                        "useremptygroup:",
                                                        ":nologin",
                                                        "userwithoutcredentials:admin");

    final Map<String, String> users = ImmutableMap.<String, String> builder()
                                                  .put("user1", "pwd1")
                                                  .put("user2", "pwd2")
                                                  .put("userà", "pwdà")
                                                  .put("admin1", "pwd3")
                                                  .put("admin2", "pwd4")
                                                  .build();

    final Map<String, String> users2 = ImmutableMap.<String, String> builder()
                                                   .put("user1", "pwda")
                                                   .put("user2", "pwdb")
                                                   .put("userà", "pwdé")
                                                   .put("admin1", "pwdc")
                                                   .put("admin2", "pwdd")
                                                   .build();

    final Multimap<String, String> groups = ImmutableMultimap.<String, String> builder()
                                                             .putAll("user1", "user")
                                                             .putAll("user2", "user", "other")
                                                             .putAll("userà", "groupà")
                                                             .putAll("admin1", "admin")
                                                             .putAll("admin2", "admin", "other")
                                                             .build();

    final Multimap<String, String> groups2 = ImmutableMultimap.<String, String> builder()
                                                              .putAll("user1", "user", "other")
                                                              .putAll("user2", "user")
                                                              .putAll("userà", "groupé")
                                                              .putAll("admin1", "admin", "other")
                                                              .putAll("admin2", "admin")
                                                              .build();

    @Before
    public void init() throws Exception {
        loginFile = tmpFolder.newFile("login");
        groupFile = tmpFolder.newFile("group");
        sourceLoginFile = tmpFolder.newFile("sourcelogin");
        sourceGroupFile = tmpFolder.newFile("sourcegroup");

        privateKeyFile = tmpFolder.newFile("priv.key");
        publicKeyFile = tmpFolder.newFile("pub.key");
        KeyGen.main(new String[] { "-P", publicKeyFile.getAbsolutePath(), "-p", privateKeyFile.getAbsolutePath() });
        privateKey = Credentials.getPrivateKey(privateKeyFile.getAbsolutePath());
    }

    @Test
    public void testUsers() throws Exception {
        createUsers();
        updateUsers();
        deleteUsers();
    }

    @Test
    public void testBulkLoadUsers() throws Exception {
        createBulkUsers();
        updateBulkUsers();
        deleteUsers();
    }

    @Test
    public void testBulkLoadUsersPartialFiles() throws Exception {
        createBulkUsers();
        updateBulkUsersLoginFile();
        updateBulkUsersGroupFile();
        validateContents(users2, groups2);
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
            ManageUsers.manageUsers("-" + ManageUsers.CREATE_OPTION,
                                    "-" + ManageUsers.LOGIN_OPTION,
                                    user.getKey(),
                                    "-" + ManageUsers.PWD_OPTION,
                                    user.getValue(),
                                    "-" + ManageUsers.GROUPS_OPTION,
                                    getGroupString(user.getKey(), groups),
                                    "-" + ManageUsers.LOGINFILE_OPTION,
                                    loginFile.getAbsolutePath(),
                                    "-" + ManageUsers.GROUPFILE_OPTION,
                                    groupFile.getAbsolutePath(),
                                    "-" + ManageUsers.KEYFILE_OPTION,
                                    publicKeyFile.getAbsolutePath());
        }
        validateContents(users, groups);
    }

    private void createBulkUsers() throws Exception {
        FileUtils.writeLines(sourceLoginFile, sourceUsers1);
        FileUtils.writeLines(sourceGroupFile, sourceGroups1);
        ManageUsers.manageUsers("-" + ManageUsers.CREATE_OPTION,
                                "-" + ManageUsers.SOURCE_LOGINFILE_OPTION,
                                sourceLoginFile.getAbsolutePath(),
                                "-" + ManageUsers.SOURCE_GROUPFILE_OPTION,
                                sourceGroupFile.getAbsolutePath(),
                                "-" + ManageUsers.LOGINFILE_OPTION,
                                loginFile.getAbsolutePath(),
                                "-" + ManageUsers.GROUPFILE_OPTION,
                                groupFile.getAbsolutePath(),
                                "-" + ManageUsers.KEYFILE_OPTION,
                                publicKeyFile.getAbsolutePath());

        validateContents(users, groups);
    }

    private void updateUsers() throws Exception {
        for (Map.Entry<String, String> user : users2.entrySet()) {
            ManageUsers.manageUsers("-" + ManageUsers.UPDATE_OPTION,
                                    "-" + ManageUsers.LOGIN_OPTION,
                                    user.getKey(),
                                    "-" + ManageUsers.PWD_OPTION,
                                    user.getValue(),
                                    "-" + ManageUsers.GROUPS_OPTION,
                                    getGroupString(user.getKey(), groups2),
                                    "-" + ManageUsers.LOGINFILE_OPTION,
                                    loginFile.getAbsolutePath(),
                                    "-" + ManageUsers.GROUPFILE_OPTION,
                                    groupFile.getAbsolutePath(),
                                    "-" + ManageUsers.KEYFILE_OPTION,
                                    publicKeyFile.getAbsolutePath());
        }
        validateContents(users2, groups2);
    }

    private void updateBulkUsers() throws Exception {
        FileUtils.writeLines(sourceLoginFile, sourceUsers2);
        FileUtils.writeLines(sourceGroupFile, sourceGroups2);
        ManageUsers.manageUsers("-" + ManageUsers.UPDATE_OPTION,
                                "-" + ManageUsers.SOURCE_LOGINFILE_OPTION,
                                sourceLoginFile.getAbsolutePath(),
                                "-" + ManageUsers.SOURCE_GROUPFILE_OPTION,
                                sourceGroupFile.getAbsolutePath(),
                                "-" + ManageUsers.LOGINFILE_OPTION,
                                loginFile.getAbsolutePath(),
                                "-" + ManageUsers.GROUPFILE_OPTION,
                                groupFile.getAbsolutePath(),
                                "-" + ManageUsers.KEYFILE_OPTION,
                                publicKeyFile.getAbsolutePath());

        validateContents(users2, groups2);
    }

    private void updateBulkUsersLoginFile() throws Exception {
        FileUtils.writeLines(sourceLoginFile, sourceUsers2);
        ManageUsers.manageUsers("-" + ManageUsers.UPDATE_OPTION,
                                "-" + ManageUsers.SOURCE_LOGINFILE_OPTION,
                                sourceLoginFile.getAbsolutePath(),
                                "-" + ManageUsers.LOGINFILE_OPTION,
                                loginFile.getAbsolutePath(),
                                "-" + ManageUsers.GROUPFILE_OPTION,
                                groupFile.getAbsolutePath(),
                                "-" + ManageUsers.KEYFILE_OPTION,
                                publicKeyFile.getAbsolutePath());
    }

    private void updateBulkUsersGroupFile() throws Exception {
        FileUtils.writeLines(sourceGroupFile, sourceGroups2);
        ManageUsers.manageUsers("-" + ManageUsers.UPDATE_OPTION,
                                "-" + ManageUsers.SOURCE_GROUPFILE_OPTION,
                                sourceGroupFile.getAbsolutePath(),
                                "-" + ManageUsers.LOGINFILE_OPTION,
                                loginFile.getAbsolutePath(),
                                "-" + ManageUsers.GROUPFILE_OPTION,
                                groupFile.getAbsolutePath(),
                                "-" + ManageUsers.KEYFILE_OPTION,
                                publicKeyFile.getAbsolutePath());
    }

    private void deleteUsers() throws Exception {
        for (Map.Entry<String, String> user : users.entrySet()) {
            ManageUsers.manageUsers("-" + ManageUsers.DELETE_OPTION,
                                    "-" + ManageUsers.LOGIN_OPTION,
                                    user.getKey(),
                                    "-" + ManageUsers.LOGINFILE_OPTION,
                                    loginFile.getAbsolutePath(),
                                    "-" + ManageUsers.GROUPFILE_OPTION,
                                    groupFile.getAbsolutePath(),
                                    "-" + ManageUsers.KEYFILE_OPTION,
                                    publicKeyFile.getAbsolutePath());
        }
        String loginContent = IOUtils.toString(loginFile.toURI());
        System.out.println("Login file content after deletion:");
        System.out.println(loginContent);
        Assert.assertTrue("login file should be empty", loginContent.trim().isEmpty());
        String groupContent = IOUtils.toString(groupFile.toURI());
        System.out.println("Group file content after deletion:");
        System.out.println(groupContent);
        Assert.assertTrue("group file should be empty", groupContent.trim().isEmpty());
    }

    private void validateContents(Map<String, String> usersToCheck, Multimap<String, String> groupsToCheck)
            throws IOException, KeyException {
        Properties props = new Properties();
        try (Reader reader = new InputStreamReader(new FileInputStream(loginFile))) {
            props.load(reader);
            String groupContent = IOUtils.toString(groupFile.toURI());
            for (Map.Entry<String, String> user : usersToCheck.entrySet()) {
                Assert.assertTrue("login file should contain " + user.getKey(), props.containsKey(user.getKey()));
                String encryptedPassword = (String) props.get(user.getKey());
                String password = HybridEncryptionUtil.decryptBase64String(encryptedPassword,
                                                                           privateKey,
                                                                           FileLoginModule.ENCRYPTED_DATA_SEP);
                Assert.assertEquals("decrypted password for user " + user.getKey() + " should match",
                                    user.getValue(),
                                    password);
                for (String group : groupsToCheck.get(user.getKey())) {
                    Assert.assertTrue("group file should contain " + user.getKey() + ":" + group,
                                      groupContent.contains(user.getKey() + ":" + group));
                }
            }
        }

    }

}
