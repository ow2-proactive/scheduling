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

import java.util.AbstractMap;

import org.apache.commons.configuration2.Configuration;
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

    public static final Boolean IAM_IS_USED = PASchedulerProperties.SCHEDULER_LOGIN_METHOD.getValueAsString()
                                                                                          .endsWith(IAM_LOGIN_METHOD) ||
                                              PAResourceManagerProperties.RM_LOGIN_METHOD.getValueAsString()
                                                                                         .endsWith(IAM_LOGIN_METHOD);

    private Configuration config;

    private String iamURL;

    private boolean tokenSignatureEnabled;

    private boolean tokenEncryptionEnabled;

    private String tokenSignatureKey;

    private String tokenEncryptionKey;

    private boolean passwordCryptoEnabled;

    private String passwordSignatureKey;

    private String passwordEncryptionKey;

    public IAMSessionUtil() {

        config = IAMStarter.getConfiguration();

        iamURL = IAMStarter.getIamURL();

        tokenSignatureEnabled = config.getBoolean(IAMConfiguration.IAM_TOKEN_SIGNATURE_ENABLED);

        tokenEncryptionEnabled = config.getBoolean(IAMConfiguration.IAM_TOKEN_ENCRYPTION_ENABLED);

        tokenSignatureKey = config.getString(IAMConfiguration.IAM_TOKEN_SIGNATURE_KEY);

        tokenEncryptionKey = config.getString(IAMConfiguration.IAM_TOKEN_ENCRYPTION_KEY);

        passwordCryptoEnabled = config.getBoolean(IAMConfiguration.IAM_PASS_CRYPTO_ENABLED);

        passwordSignatureKey = config.getString(IAMConfiguration.IAM_PASS_SIGNATURE_KEY);

        passwordEncryptionKey = config.getString(IAMConfiguration.IAM_PASS_ENCRYPTION_KEY);
    }

    public boolean tokenIsValid(JwtClaims jwtClaims) {
        try {
            String ssoTicket = jwtClaims.getJwtId();
            return new IAMRestClient().isSSOTicketValid(iamURL + IAMConfiguration.IAM_TICKET_REQUEST + "/" + ssoTicket);
        } catch (MalformedClaimException e) {
            throw new IllegalStateException("SSO token contains wrong session id (jti)");
        }
    }

    public char[] getTokenPassword(JwtClaims jwtClaims) {
        String cypheredPassword = (String) jwtClaims.getClaimValue(CREDENTIAL_KEY);
        return JWTUtils.decypherJWT(cypheredPassword,
                                    passwordCryptoEnabled,
                                    passwordSignatureKey,
                                    passwordCryptoEnabled,
                                    passwordEncryptionKey)
                       .toCharArray();
    }

    public AbstractMap.SimpleEntry<String, JwtClaims> createNewSessionToken(String username, char[] password) {

        String sessionToken = new IAMRestClient().getSSOTicket(iamURL + IAMConfiguration.IAM_TICKET_REQUEST,
                                                               username,
                                                               new String(password),
                                                               null,
                                                               true);

        JwtClaims claims = JWTUtils.parseJWT(sessionToken,
                                             tokenSignatureEnabled,
                                             tokenSignatureKey,
                                             tokenEncryptionEnabled,
                                             tokenEncryptionKey,
                                             iamURL,
                                             iamURL);

        return new AbstractMap.SimpleEntry(sessionToken, claims);
    }
}
