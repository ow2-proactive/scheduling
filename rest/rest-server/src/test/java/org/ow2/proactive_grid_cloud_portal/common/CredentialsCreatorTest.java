package org.ow2.proactive_grid_cloud_portal.common;

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
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class CredentialsCreatorTest {

    private CredentialsCreator credentialsCreator;

    private String authenticationPath;

    @Before
    public void init() throws IOException {
        PASchedulerProperties.SCHEDULER_HOME.updateProperty(".");
        authenticationPath = PASchedulerProperties.SCHEDULER_HOME.getValueAsString() +
            "/config/authentication/";
        setupFolder();
        credentialsCreator = new CredentialsCreator();

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
        Assert.assertTrue(
                Arrays.equals(credentialBytes, IOUtils.toByteArray(new FileInputStream(credentialsFile))));

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

    private void setupFolder() throws IOException {

        FileUtils.deleteDirectory(new File(authenticationPath));
        new File(authenticationPath).mkdirs();

    }

}
