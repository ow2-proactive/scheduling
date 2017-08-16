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

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class CredentialsCreatorTest {

    private CredentialsCreator credentialsCreator;

    private String authenticationPath;

    @Before
    public void init() throws IOException {
        assumeTrue(OperatingSystem.getOperatingSystem() != OperatingSystem.windows);

        PASchedulerProperties.SCHEDULER_HOME.updateProperty(".");
        authenticationPath = PASchedulerProperties.SCHEDULER_HOME.getValueAsString() + "/" + "config" + "/" +
                             "authentication" + "/";
        setupFolder();
        credentialsCreator = CredentialsCreator.INSTANCE;
    }

    @Test
    public void testSaveCredentialsFile() throws FileNotFoundException, IOException {
        PASchedulerProperties.SCHEDULER_CREATE_CREDENTIALS_WHEN_LOGIN.updateProperty("true");
        String username = "user1";
        byte[] credentialBytes = new byte[1];
        credentialBytes[0] = 'a';
        credentialsCreator.saveCredentialsFile(username, credentialBytes);
        File credentialsFile = new File(authenticationPath + username + ".cred");
        Assert.assertTrue(credentialsFile.exists());
        Assert.assertTrue(Arrays.equals(credentialBytes, IOUtils.toByteArray(new FileInputStream(credentialsFile))));

    }

    @Test
    public void testSaveCredentialsFileOff() {
        PASchedulerProperties.SCHEDULER_CREATE_CREDENTIALS_WHEN_LOGIN.updateProperty("false");
        String username = "user1";
        byte[] credentialBytes = new byte[1];
        credentialsCreator.saveCredentialsFile(username, credentialBytes);
        File f = new File(authenticationPath + username + ".cred");
        Assert.assertFalse(f.exists());
    }

    @Test
    public void testCreateAndStoreCredentialFileOff() {
        PASchedulerProperties.SCHEDULER_CREATE_CREDENTIALS_WHEN_LOGIN.updateProperty("false");
        String username = "user1";
        String password = "pass1";
        credentialsCreator.createAndStoreCredentialFile(username, password);
        File f = new File(authenticationPath + username + ".cred");
        Assert.assertFalse(f.exists());
    }

    @Test
    public void testSaveCredentialsFileNoDifference() throws FileNotFoundException, IOException, InterruptedException {
        PASchedulerProperties.SCHEDULER_CREATE_CREDENTIALS_WHEN_LOGIN.updateProperty("true");
        String username = "user1";
        byte[] credentialBytes = new byte[1];
        credentialBytes[0] = 'a';
        credentialsCreator.saveCredentialsFile(username, credentialBytes);

        File credentialsFile = new File(authenticationPath + username + ".cred");
        Assert.assertTrue(credentialsFile.exists());
        Assert.assertTrue(Arrays.equals(credentialBytes, IOUtils.toByteArray(new FileInputStream(credentialsFile))));
        long lastModified = credentialsFile.lastModified();

        credentialsCreator.saveCredentialsFile(username, credentialBytes);
        Assert.assertEquals(lastModified, new File(authenticationPath + username + ".cred").lastModified());

    }

    @Test
    public void testSaveCredentialsFileDifferentBytes()
            throws FileNotFoundException, IOException, InterruptedException {
        PASchedulerProperties.SCHEDULER_CREATE_CREDENTIALS_WHEN_LOGIN.updateProperty("true");
        String username = "user1";
        byte[] credentialBytes = new byte[1];
        credentialBytes[0] = 'a';
        credentialsCreator.saveCredentialsFile(username, credentialBytes);

        File credentialsFile = new File(authenticationPath + username + ".cred");
        Assert.assertTrue(credentialsFile.exists());
        Assert.assertTrue(Arrays.equals(credentialBytes, IOUtils.toByteArray(new FileInputStream(credentialsFile))));
        long lastModified = credentialsFile.lastModified();

        credentialsCreator.saveCredentialsFile(username, credentialBytes);
        Assert.assertEquals(lastModified, new File(authenticationPath + username + ".cred").lastModified());

        byte[] credentialBytes2 = new byte[1];
        credentialBytes2[0] = 'b';

        Thread.sleep(1000);

        credentialsCreator.saveCredentialsFile(username, credentialBytes2);
        Assert.assertNotEquals(lastModified, new File(authenticationPath + username + ".cred").lastModified());
    }

    private void setupFolder() throws IOException {

        FileUtils.deleteDirectory(new File(authenticationPath));
        new File(authenticationPath).mkdirs();

    }

}
