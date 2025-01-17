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

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.keycloak.adapters.AdapterUtils;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.ow2.proactive.authentication.keycloak.KeycloakException;
import org.ow2.proactive.authentication.keycloak.KeycloakOidcProperties;
import org.ow2.proactive.authentication.keycloak.KeycloakOidcRestClient;
import org.ow2.proactive.authentication.principals.GroupNamePrincipal;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;

import com.google.common.base.Strings;


/**
 * Login Module that performs user authentication and authorization against Keycloak.
 * It uses the OIDC REST endpoints of Keycloak to obtain an access token given a username and password.
 *
 * @author The ActiveEon Team
 * @since ProActive Scheduling 14.0
 */
public abstract class KeycloakLoginModule extends FileLoginModule implements Loggable {

    /** Logger instance */
    private static final Logger LOGGER = Logger.getLogger(KeycloakLoginModule.class.getName());

    /** Keycloak OIDC configuration properties */
    private final KeycloakOidcProperties keycloakProperties = new KeycloakOidcProperties(getKeycloakConfigFileName());

    /** Refresh token in Keycloak access token  */
    private String refreshToken;

    /** Login status */
    private boolean succeeded = false;

    /**
     * Creates a new instance of KeycloakLoginModule
     */
    public KeycloakLoginModule() {
        if (keycloakProperties.isFallbackUserAuth()) {
            checkLoginFile();
            checkGroupFile();
            LOGGER.debug("Using Login file for fall back authentication at: " + loginFile);
            LOGGER.debug("Using Group file for fall back group membership at: " + groupFile);
        } else if (keycloakProperties.isFallbackGroupMembership()) {
            checkGroupFile();
            LOGGER.debug("Using Group file for fall back group membership at: " + groupFile);
        }

        if (keycloakProperties.isFallbackTenantMembership()) {
            checkTenantFile();
            LOGGER.debug("Using Tenant file for fall back tenant membership at: " + tenantFile);
        }
    }

    /**
     * Initialize this <code>KeycloakLoginModule</code>.
     *
     * <p>
     *
     * @param subject
     *            the <code>Subject</code> not to be authenticated.
     *            <p>
     *
     * @param callbackHandler
     *            a <code>CallbackHandler</code> to get the credentials of the
     *            user, must work with <code>NoCallback</code> callbacks.
     *            <p>
     * @param sharedState State shared with other configured LoginModules. <p>
     *
     * @param options Options specified in the login
     *			<code>Configuration</code> for this particular
     *			<code>KeycloakLoginModule</code>.
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {

        this.subject = subject;
        this.callbackHandler = callbackHandler;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Using Keycloak: " + keycloakProperties.getProperty(KeycloakOidcProperties.KEYCLOAK_URL));
        }

        // This is used just for logout
        Iterator<RefreshTokenHolder> iterator = subject.getPrivateCredentials(RefreshTokenHolder.class).iterator();
        if (iterator.hasNext()) {
            refreshToken = iterator.next().refreshToken;
        }

    }

    /**
     * Authenticate the user by getting the username and password from the
     * CallbackHandler.
     * @return Whether the login against KeycloakLoginModule succeeded
     *
     * @exception LoginException
     *                if this KeycloakLoginModule is unable to
     *                perform the authentication.
     */
    @Override
    public boolean login() throws LoginException {

        succeeded = false;

        if (callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available " +
                                     "to garner authentication information from the user");
        }
        try {
            Callback[] callbacks = new Callback[] { new NoCallback() };

            // gets the username, password, group Membership, and group Hierarchy from callback handler
            callbackHandler.handle(callbacks);
            Map<String, Object> params = ((NoCallback) callbacks[0]).get();
            String username = (String) params.get("username");
            String password = (String) params.get("pw");

            params.clear();
            ((NoCallback) callbacks[0]).clear();

            if (Strings.isNullOrEmpty(username)) {
                LOGGER.error("No username has been specified for authentication");
                throw new FailedLoginException("No username has been specified for authentication");
            }
            if (Strings.isNullOrEmpty(password)) {
                LOGGER.error("No password has been specified for authentication");
                throw new FailedLoginException("No password has been specified for authentication");
            }

            succeeded = logUser(username, password);
            return succeeded;

        } catch (IOException | UnsupportedCallbackException e) {
            LOGGER.error(e.getMessage(), e);
            throw new LoginException(e.toString());
        }
    }

    /**
     * Logs in the user via either the FileLoginModule or the KeycloakLoginModule
     *
     * @param username User login
     * @param password User password
     *
     * @return Whether the login succeeded
     */
    private boolean logUser(String username, String password) throws LoginException {

        if (keycloakProperties.isFallbackUserAuth()) {
            try {
                return super.logUser(username, password, null, false);
            } catch (LoginException ex) {
                boolean answer = keycloakLogUser(username, password);
                if (answer && keycloakProperties.isShadowUsers()) {
                    addShadowAccount(null, username);
                } else if (answer) {
                    createAndStoreCredentialFile(null, username, password, false);
                }
                return answer;
            }
        } else {
            return keycloakLogUser(username, password);
        }
    }

    /**
     * Logs in the user via the KeycloakLoginModule
     *
     * @param username User login
     * @param password User password
     *
     * @return Whether the login succeeded
     */
    private boolean keycloakLogUser(String username, String password) {

        try {
            AccessTokenResponse accessTokenResponse = getKeycloakToken(username, password);

            AdapterTokenVerifier.VerifiedTokens tokens = verifyAccessTokenResponse(accessTokenResponse,
                                                                                   keycloakProperties);

            // refreshToken will be saved to private Credentials of Subject for now
            refreshToken = accessTokenResponse.getRefreshToken();

            parseAccessTokenResponse(tokens.getAccessToken(), accessTokenResponse.getToken());

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return false;
        }

        return succeeded;
    }

    /**
     * Calls Keycloak REST client to acquire an access token
     * @param username User login
     * @param password User password
     *
     * @return Keycloak access token
     */
    private AccessTokenResponse getKeycloakToken(String username, String password) {
        return new KeycloakOidcRestClient().getKeycloakToken(keycloakProperties, username, password);
    }

    /**
     * Decrypts the access token using the public key of Keycloak and verify its content (including signature, expiration date etc.)
     *
     * @param keycloakResponse Response acquired from Keycloak
     * @param keycloakProperties Configuration properties of Keycloak
     *
     * @return VerifiedTokens valid tokens
     */
    private AdapterTokenVerifier.VerifiedTokens verifyAccessTokenResponse(AccessTokenResponse keycloakResponse,
            KeycloakOidcProperties keycloakProperties) {

        AdapterTokenVerifier.VerifiedTokens tokens;

        try {
            tokens = AdapterTokenVerifier.verifyTokens(keycloakResponse.getToken(),
                                                       keycloakResponse.getIdToken(),
                                                       keycloakProperties.getKeycloakDeployment());
        } catch (VerificationException e) {
            throw new KeycloakException("Unable to verify Keycloak response. Response contains invalid tokens.");
        }

        return tokens;
    }

    /**
     * Parses the verified tokens and extract the user principal and roles
     *
     * @param accessToken Token to be parsed
     * @param tokenString Token in raw (JSON) format
     */
    protected void parseAccessTokenResponse(AccessToken accessToken, String tokenString) {
        boolean verifyCaller;
        KeycloakDeployment deployment = keycloakProperties.getKeycloakDeployment();
        if (deployment.isUseResourceRoleMappings()) {
            verifyCaller = accessToken.isVerifyCaller(deployment.getResourceName());
        } else {
            verifyCaller = accessToken.isVerifyCaller();
        }
        if (verifyCaller) {
            throw new IllegalStateException("VerifyCaller not supported yet in login module");
        }

        RefreshableKeycloakSecurityContext skSession = new RefreshableKeycloakSecurityContext(deployment,
                                                                                              null,
                                                                                              tokenString,
                                                                                              accessToken,
                                                                                              null,
                                                                                              null,
                                                                                              null);
        String principalName = AdapterUtils.getPrincipalName(deployment, accessToken);
        final Set<String> roles = AdapterUtils.getRolesFromSecurityContext(skSession);

        if (Strings.isNullOrEmpty(principalName)) {
            throw new KeycloakException("Unable to acquire the user name from Keycloak access token. The principalName is null or empty.");
        }

        if (roles == null || roles.isEmpty()) {
            throw new KeycloakException("Unable to acquire the user roles from Keycloak access token. The user roles are null or empty.");
        }

        //Authentication
        subject.getPrincipals().add(new UserNamePrincipal(principalName));
        LOGGER.debug("adding principal '" + principalName);
        subject.getPrivateCredentials().add(tokenString);

        //Authorization
        for (String role : roles) {
            subject.getPrincipals().add(new GroupNamePrincipal(role));
            LOGGER.debug("adding group '" + role + "' to principal '" + principalName + "'");
        }

        //Successful login
        succeeded = true;
        LOGGER.debug("authentication succeeded for user '" + principalName + "'");
    }

    /**
     * @see javax.security.auth.spi.LoginModule#commit()
     */
    @Override
    public boolean commit() {
        // refreshToken will be saved to private Credentials of Subject for now
        if (refreshToken != null) {
            RefreshTokenHolder refreshTokenHolder = new RefreshTokenHolder();
            refreshTokenHolder.refreshToken = refreshToken;
            subject.getPrivateCredentials().add(refreshTokenHolder);
        }
        return succeeded;
    }

    /**
     * Aborts the login operation
     *
     * @return Actual status of the aborted login
     */
    @Override
    public boolean abort() {
        boolean result = succeeded;
        succeeded = false;
        return result;
    }

    /**
     * Logs out the user and invalidates Keycloak access tokens
     *
     * @return Whether the logout is successful
     */
    @Override
    public boolean logout() {
        try {
            new KeycloakOidcRestClient().logout(keycloakProperties, refreshToken);
            Set<Principal> principals = subject.getPrincipals();
            for (Principal principal : principals) {
                if (principal.getClass().equals(UserNamePrincipal.class) ||
                    principal.getClass().equals(GroupNamePrincipal.class)) {
                    subject.getPrincipals().remove(principal);
                }
            }
            Set<Object> credentials = subject.getPrivateCredentials();
            for (Object cred : credentials) {
                subject.getPrivateCredentials().remove(cred);
            }
            succeeded = false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Retrieves Keycloak configuration file name.
     * @return name of the file with Keycloak configuration.
     */
    protected abstract String getKeycloakConfigFileName();

    /**
     * Holds the Keycloak refresh token
     */
    private static class RefreshTokenHolder implements Serializable {
        private String refreshToken;
    }
}
