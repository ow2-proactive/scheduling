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

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.ow2.proactive.authentication.iam.IAMRestClient;
import org.ow2.proactive.authentication.iam.JWTUtils;
import org.ow2.proactive.boot.microservices.IAMStarter;
import org.ow2.proactive.boot.microservices.util.IAMConfiguration;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class IAMSessionUtil {

    private static final String IAM_LOGIN_METHOD = "IAMLoginMethod";

    private static final String CREDENTIAL_KEY = "credential";

    public static final boolean IAM_IS_USED = PASchedulerProperties.SCHEDULER_LOGIN_METHOD.getValueAsString()
                                                                                          .endsWith(IAM_LOGIN_METHOD) ||
                                              PAResourceManagerProperties.RM_LOGIN_METHOD.getValueAsString()
                                                                                         .endsWith(IAM_LOGIN_METHOD);

    private static final String IAM_URL = IAMStarter.iamURL;

    private static final String IAM_TICKET_REQUEST = IAMConfiguration.IAM_TICKET_REQUEST;

    private static IAMRestClient iamRestClient = new IAMRestClient();

    private static boolean tokenSignatureEnabled = IAMStarter.config.getBoolean(IAMConfiguration.IAM_TOKEN_SIGNATURE_ENABLED);

    private static boolean tokenEncryptionEnabled = IAMStarter.config.getBoolean(IAMConfiguration.IAM_TOKEN_ENCRYPTION_ENABLED);

    private static String tokenSignatureKey = IAMStarter.config.getString(IAMConfiguration.IAM_TOKEN_SIGNATURE_KEY);

    private static String tokenEncryptionKey = IAMStarter.config.getString(IAMConfiguration.IAM_TOKEN_ENCRYPTION_KEY);

    private static boolean passwordCryptoEnabled = IAMStarter.config.getBoolean(IAMConfiguration.IAM_PASS_CRYPTO_ENABLED);

    private static String passwordSignatureKey = IAMStarter.config.getString(IAMConfiguration.IAM_PASS_SIGNATURE_KEY);

    private static String passwordEncryptionKey = IAMStarter.config.getString(IAMConfiguration.IAM_PASS_ENCRYPTION_KEY);

    private IAMSessionUtil() {

    }

    public static String renewIAMSession(SessionStore sessionStore, String sessionId) throws MalformedClaimException {

        IAMSession iamSession = (IAMSession) sessionStore.get(sessionId);

        String ssoTicket = iamSession.getJwtClaims().getJwtId();

        if (!iamRestClient.isSSOTicketValid(IAM_URL + IAM_TICKET_REQUEST + "/" + ssoTicket)) {

            // Retrieve cyphered user password form the current IAMSession token
            String cypheredPassword = (String) iamSession.getJwtClaims().getClaimValue(CREDENTIAL_KEY);

            // Decypher user password
            String password = JWTUtils.decypherJWT(cypheredPassword,
                                                   passwordCryptoEnabled,
                                                   passwordSignatureKey,
                                                   passwordCryptoEnabled,
                                                   passwordEncryptionKey);

            // Create a new IAMSession using user credentials
            Session newIAMSession = createNewSessionToken(iamSession.getUserName(), password, sessionStore);

            // Remove the old IAMSession
            sessionStore.terminate(sessionId);

            return newIAMSession.getSessionId();

        } else
            return sessionId;
    }

    public static Session createNewSessionToken(String username, String password, SessionStore sessionStore) {

        String sessionToken = iamRestClient.getSSOTicket(IAM_URL + IAM_TICKET_REQUEST, username, password, null, true);

        JwtClaims claims = JWTUtils.parseJWT(sessionToken,
                                             tokenSignatureEnabled,
                                             tokenSignatureKey,
                                             tokenEncryptionEnabled,
                                             tokenEncryptionKey,
                                             IAM_URL,
                                             IAM_URL);

        return sessionStore.create(sessionToken, username, claims);
    }
}
