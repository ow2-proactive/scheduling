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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.Arrays;

import javax.security.auth.login.LoginException;

import org.apache.commons.io.IOUtils;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.ConnectionException;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;


public class CredentialsCreator {

    public final static CredentialsCreator INSTANCE = new CredentialsCreator();

    private CredentialsCreator() {
    }

    public synchronized void createAndStoreCredentialFile(String username, String password) {
        if (!PASchedulerProperties.SCHEDULER_CREATE_CREDENTIALS_WHEN_LOGIN.getValueAsBoolean()) {
            return;
        }
        try {
            byte[] credentialBytes = createCredentials(username, password);

            saveCredentialsFile(username, credentialBytes);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void saveCredentialsFile(String username, byte[] credentialBytes) {
        if (!PASchedulerProperties.SCHEDULER_CREATE_CREDENTIALS_WHEN_LOGIN.getValueAsBoolean()) {
            return;
        }
        File credentialsfile = new File(PASchedulerProperties.SCHEDULER_HOME.getValueAsString() +
            "/config/authentication/" + username + ".cred");

        if (credentialsfile.exists() && sameCredentialsBytes(credentialBytes, credentialsfile)) {
            return;
        }

        FileOutputStream fos = null;
        try {
            credentialsfile.delete();
            credentialsfile.createNewFile();
            fos = new FileOutputStream(credentialsfile);
            fos.write(credentialBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                }

            }
        }
    }

    private boolean sameCredentialsBytes(byte[] credentialBytes, File credentialsfile) {
        try {
            return Arrays.equals(credentialBytes, IOUtils.toByteArray(new FileInputStream(credentialsfile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] createCredentials(String username, String password)
            throws ConnectionException, LoginException, KeyException {
        String url = PortalConfiguration.SCHEDULER_URL.getValueAsString();
        SchedulerAuthenticationInterface auth = SchedulerConnection.join(url);
        PublicKey pubKey = auth.getPublicKey();
        byte[] privateKey = auth.getPrivateKey();

        Credentials cred = Credentials.createCredentials(new CredData(CredData.parseLogin(username),
            CredData.parseDomain(username), password, privateKey), pubKey);

        byte[] credentialBytes = cred.getBase64();
        return credentialBytes;
    }

}
