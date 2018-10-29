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
package org.ow2.proactive.boot.microservices.iam.util;

import java.util.AbstractMap;

import org.apache.commons.configuration2.Configuration;
import org.jose4j.jwt.JwtClaims;
import org.ow2.proactive.boot.microservices.iam.IAMStarter;


public class IAMSessionUtil {

    private static final String PA_REST_SERVICE = "PARestAPI";

    private Configuration config;

    private String iamURL;

    private boolean tokenSignatureEnabled;

    private boolean tokenEncryptionEnabled;

    private String tokenSignatureKey;

    private String tokenEncryptionKey;

    private boolean isJWTSession;

    private String ticketMarker;

    /**
     * constructor that initializes all parameters needed by IAMSession
     */
    public IAMSessionUtil() {

        config = IAMStarter.getConfiguration();

        iamURL = IAMStarter.getIamURL();

        tokenSignatureEnabled = config.getBoolean(IAMConfiguration.IAM_TOKEN_SIGNATURE_ENABLED);

        tokenEncryptionEnabled = config.getBoolean(IAMConfiguration.IAM_TOKEN_ENCRYPTION_ENABLED);

        tokenSignatureKey = config.getString(IAMConfiguration.IAM_TOKEN_SIGNATURE_KEY);

        tokenEncryptionKey = config.getString(IAMConfiguration.IAM_TOKEN_ENCRYPTION_KEY);

        isJWTSession = config.getBoolean(IAMConfiguration.PA_REST_SESSION_AS_JWT);

        ticketMarker = config.getString(IAMConfiguration.SSO_TICKET_MARKER);
    }

    /**
     * indicates whether the format of IAM sessions is JWT (Json Web Token) or not.
     *
     * @return boolean indicating whether IAM sessions are generated as JWT.
     * @since version 8.3.0
     */
    public boolean isJWTSession() {
        return isJWTSession;
    }

    /**
     * deletes IAM sessions by destroying SSO tickets registered in IAM microservice.
     *
     * @param ssoTicket SSO ticket to be destroyed
     * @return boolean indicating whether the SSO ticket is destroyed
     * @since version 8.3.0
     */
    public boolean deleteSession(String ssoTicket) {
        return new IAMRestClient().deleteSSOTicket(iamURL + IAMConfiguration.IAM_TICKET_REQUEST + "/" + ssoTicket);
    }

    /**
     * indicates whether the IAM session is still valid.
     *
     * @param ssoTicket SSO ticket of IAMSession to be validated
     * @return boolean indicating whether IAM session is valid.
     * @since version 8.3.0
     */
    public boolean sessionIsValid(String ssoTicket) {
        return new IAMRestClient().isSSOTicketValid(iamURL + IAMConfiguration.IAM_TICKET_REQUEST + "/" + ssoTicket);
    }

    /**
     * creates a new IAM session given user credentials.
     *
     * @param username user login
     * @param password user password
     * @return a key-value pair, the key represents the session ID, and the value are JWT claims (when the session token is generated as a JWT).
     * @since version 8.3.0
     */
    public AbstractMap.SimpleEntry<String, JwtClaims> createNewSessionToken(String username, char[] password) {

        String sessionToken = new IAMRestClient().getSSOTicket(iamURL + IAMConfiguration.IAM_TICKET_REQUEST,
                                                               username,
                                                               password,
                                                               ticketMarker,
                                                               isJWTSession);

        JwtClaims claims = isJWTSession ? JWTUtils.parseJWT(sessionToken,
                                                            tokenSignatureEnabled,
                                                            tokenSignatureKey,
                                                            tokenEncryptionEnabled,
                                                            tokenEncryptionKey,
                                                            iamURL,
                                                            iamURL)
                                        : null;

        return new AbstractMap.SimpleEntry(sessionToken, claims);

    }

    /**
     * creates a service token in the context of a IAM session (that is identified by its SSO ticket).
     * creating such as service token automatically renews IAM sessions (and its SSO ticket, accordingly)
     *
     * @param ssoTicket SSO ticket that identifies IAM session
     * @return a service token
     * @since version 8.3.0
     */
    public String createServiceToken(String ssoTicket) {
        return new IAMRestClient().getServiceToken(iamURL + IAMConfiguration.IAM_TICKET_REQUEST + "/" + ssoTicket,
                                                   PA_REST_SERVICE);
    }
}
