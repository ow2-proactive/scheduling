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
package org.ow2.proactive.authentication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;


/**
 * An active object responsible for authentication.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public abstract class AuthenticationImpl implements Authentication, RunActive {

    /** Activation is used to control authentication during scheduling initialization */
    private volatile boolean activated = false;

    /**
     * Defines login method
     * 
     * @return a string which represents the login method.
     */
    protected abstract String getLoginMethod();

    /**
     * Path to the private key file for used for authentication
     */
    protected String privateKeyPath;

    /**
     * Path to the private key file for used for authentication
     */
    protected String publicKeyPath;

    /**
     * Empty constructor
     */
    public AuthenticationImpl() {
    }

    /**
     * Default constructor
     * <p>
     * Loads jaas.config and stores it in global system property,
     * also locates keypair used for authentication:
     * public key is used to encrypt credentials to make the old deprecated API still compatible,
     * private key is used to decrypt credentials in the new API.
     * 
     * @param jaasPath path to the jaas configuration file
     * @param privPath path to the private key file
     * @param pubPath path to the public key file
     * 
     */
    public AuthenticationImpl(String jaasPath, String privPath, String pubPath) {
        File jaasFile = new File(jaasPath);
        if (jaasFile.exists() && !jaasFile.isDirectory()) {
            System.setProperty("java.security.auth.login.config", jaasPath);
        } else {
            throw new RuntimeException("Could not find Jaas configuration at: " + jaasPath);
        }

        File privFile = new File(privPath);
        if (privFile.exists() && !privFile.isDirectory()) {
            this.privateKeyPath = privPath;
        } else {
            throw new RuntimeException("Could not find private key file at: " + privPath);
        }

        File pubFile = new File(pubPath);
        if (pubFile.exists() && !pubFile.isDirectory()) {
            this.publicKeyPath = pubPath;
        } else {
            throw new RuntimeException("Could not find public key file at: " + pubPath);
        }
    }

    /**
     * Performs login.
     * 
     * @param cred encrypted username and password
     * @return the name of the user logged
     * @throws LoginException if username or password is incorrect.
     */
    public Subject authenticate(Credentials cred) throws LoginException {

        if (activated == false) {
            throw new LoginException("Authentication active object is not activated.");
        }

        CredData credentials = null;
        try {
            credentials = cred.decrypt(privateKeyPath);
        } catch (KeyException e) {
            throw new LoginException("Could not decrypt credentials: " + e);
        }
        String username = credentials.getLogin();
        String password = credentials.getPassword();

        if (username == null || username.equals("")) {
            throw new LoginException("Bad user name (user is null or empty)");
        }

        try {
            // Verify that this user//password can connect to this existing scheduler
            getLogger().info(username + " is trying to connect");

            Map<String, Object> params = new HashMap<>(4);
            //user name to check
            params.put("username", username);
            //password to check
            params.put("pw", password);

            //Load LoginContext according to login method defined in jaas.config
            LoginContext lc = new LoginContext(getLoginMethod(), new NoCallbackHandler(params));

            lc.login();
            getLogger().info("User " + username + " logged successfully");

            return lc.getSubject();
        } catch (LoginException e) {
            getLogger().info(e.getMessage());
            //Nature of exception is hidden for user, we don't want to inform
            //user about the reason of non authentication
            throw new LoginException("Authentication failed");
        }
    }

    /**
     * Request this AuthenticationImpl's public key.
     * <p>
     * The public key provided by this method can be used to create encrypted credentials with
     * {@link org.ow2.proactive.authentication.crypto.Credentials#createCredentials(String, String, PublicKey)}.
     * The private key corresponding to this public key will be used for decryption.
     * 
     * @return this AuthenticationImpl's public key
     * @throws LoginException the key could not be retrieved
     */
    public PublicKey getPublicKey() throws LoginException {
        if (activated == false) {
            throw new LoginException("Authentication active object is not activated.");
        }
        try {
            return Credentials.getPublicKey(this.publicKeyPath);
        } catch (KeyException e) {
            getLogger().error("", e);
            throw new LoginException("Could not retrieve public key");
        }
    }

    public byte[] getPrivateKey() throws LoginException {
        if (activated == false) {
            throw new LoginException("Authentication active object is not activated.");
        }
        try {
            return Files.readAllBytes(Paths.get(this.privateKeyPath));
        } catch (IOException e) {
            getLogger().error("", e);
            throw new LoginException("Could not retrieve private key");
        }
    }

    /**
     * @see org.ow2.proactive.authentication.Authentication#isActivated()
     */
    @ImmediateService
    public BooleanWrapper isActivated() {
        return new BooleanWrapper(activated);
    }

    /**
     * Activates or desactivates authentication active object
     * 
     * @param activated the status of the desired activated state.
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    /**
     * Terminates the active object
     * 
     * @return true if the object has been terminated.
     */
    public boolean terminate() {
        PAActiveObject.terminateActiveObject(false);
        getLogger().info("Authentication service is now shutdown!");
        return true;
    }

    /**
     * Method controls the execution of every request.
     * Tries to keep this active object alive in case of any exception.
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            Request request = null;
            try {
                request = service.blockingRemoveOldest();
                if (request != null) {
                    try {
                        service.serve(request);
                    } catch (Throwable e) {
                        getLogger().error("Cannot serve request: " + request, e);
                    }
                }
            } catch (InterruptedException e) {
                getLogger().warn("runActivity interrupted", e);
            }

        }
    }

}
