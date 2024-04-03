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
package org.ow2.proactive.authentication.keycloak;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.OAuth2ErrorRepresentation;
import org.keycloak.util.JsonSerialization;
import org.ow2.proactive.http.CommonHttpClientBuilder;

import com.google.common.base.Strings;


public class KeycloakOidcRestClient {

    /** Logger instance */
    private static final Logger LOGGER = Logger.getLogger(KeycloakOidcRestClient.class.getName());

    /**
     * An attribute (key) that designates the client application for which an access token is generated (which is ProActive).
     * This key is used in the body of the access token REST request
     */
    private static final String CLIENT_ID_KEY = "client_id";

    /**
     * An attribute (key) that designates the secret of the client application for which an access token is generated (which is ProActive).
     * This key is used in the body of the access token REST request
     */
    private static final String CLIENT_SECRET_KEY = "client_secret";

    /**
     * An attribute (key) that designates the grant type provided to obtain an access token.
     * This key is used in the body of the access token REST request
     */
    private static final String GRANT_TYPE_KEY = "grant_type";

    /**
     * An attribute (key) that designates the value of the grant type.
     * This value is used in the body of the access token REST request
     */
    private static final String GRANT_TYPE_VALUE = "password";

    /**
     * An attribute (key) used to designate the user login
     */
    private static final String USER_KEY = "username";

    /**
     * An attribute (key) used to designate the user login
     */
    private static final String SECRET_KEY = "password";

    /**
     * Acquires an access token from Keycloak.
     * @param keycloakProperties Keycloak deployment properties
     * @param username user login
     * @param password user password
     * @return Keycloak access token response
     */
    public AccessTokenResponse getKeycloakToken(KeycloakOidcProperties keycloakProperties, String username,
            String password) {

        String keycloakUrl = keycloakProperties.getProperty(KeycloakOidcProperties.KEYCLOAK_URL);
        String keycloakRealm = keycloakProperties.getProperty(KeycloakOidcProperties.KEYCLOAK_REALM);
        String clientID = keycloakProperties.getProperty(KeycloakOidcProperties.KEYCLOAK_PROACTIVE_CLIENT_ID);
        String clientSecret = keycloakProperties.getProperty(KeycloakOidcProperties.KEYCLOAK_PROACTIVE_CLIENT_SECRET);
        String scope = keycloakProperties.getProperty(KeycloakOidcProperties.KEYCLOAK_PROACTIVE_SCOPE);
        boolean allowAnyCertificate = Boolean.parseBoolean(keycloakProperties.getProperty(KeycloakOidcProperties.ALLOW_ANY_CERTIFICATE));

        if (Strings.isNullOrEmpty(keycloakUrl) || Strings.isNullOrEmpty(keycloakRealm) ||
            Strings.isNullOrEmpty(clientID) || Strings.isNullOrEmpty(clientSecret)) {
            throw new KeycloakException("Keycloak parameters are not properly set. Please check that none of them is null or empty.");
        }

        AccessTokenResponse accessTokenResponse;

        try (CloseableHttpClient httpClient = new CommonHttpClientBuilder().allowAnyCertificate(allowAnyCertificate)
                                                                           .build()) {

            String tokenUrl = keycloakProperties.getKeycloakDeployment().getTokenUrl();

            HttpPost httpPost = new HttpPost(tokenUrl);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(GRANT_TYPE_KEY, GRANT_TYPE_VALUE));
            params.add(new BasicNameValuePair(USER_KEY, username));
            params.add(new BasicNameValuePair(SECRET_KEY, password));
            params.add(new BasicNameValuePair(CLIENT_ID_KEY, clientID));
            params.add(new BasicNameValuePair(CLIENT_SECRET_KEY, clientSecret));

            if (!Strings.isNullOrEmpty(scope)) {
                params.add(new BasicNameValuePair(OAuth2Constants.SCOPE, scope));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse httpResponse = httpClient.execute(httpPost);
            if ((httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) ||
                (null == httpResponse.getEntity())) {
                throw new KeycloakException("Failed to acquire an access token for the user '" + username);
            }

            String keycloakResponse = EntityUtils.toString(httpResponse.getEntity());
            if (Strings.isNullOrEmpty(keycloakResponse)) {
                throw new KeycloakException("Unable to acquire token from Keycloak. Response is null or empty.");
            }
            accessTokenResponse = JsonSerialization.readValue(keycloakResponse, AccessTokenResponse.class);

            LOGGER.debug("Keycloak response: " + keycloakResponse);

        } catch (IOException e) {
            throw new KeycloakException("Unable to parse Keycloak response. Response does not correspond to the class AccessTokenResponse.");
        }

        return accessTokenResponse;
    }

    /**
     * Logs out the user from Keycloak and invalidates the refresh tokens
     *
     * @param keycloakProperties Keycloak deployment properties
     * @param refreshToken Refresh token to be invalidated
     */
    public void logout(KeycloakOidcProperties keycloakProperties, String refreshToken) {

        KeycloakDeployment deployment = keycloakProperties.getKeycloakDeployment();
        boolean allowAnyCertificate = Boolean.parseBoolean(keycloakProperties.getProperty(KeycloakOidcProperties.ALLOW_ANY_CERTIFICATE));
        String clientID = keycloakProperties.getProperty(KeycloakOidcProperties.KEYCLOAK_PROACTIVE_CLIENT_ID);
        String clientSecret = keycloakProperties.getProperty(KeycloakOidcProperties.KEYCLOAK_PROACTIVE_CLIENT_SECRET);

        if (Strings.isNullOrEmpty(clientID) || Strings.isNullOrEmpty(clientSecret)) {
            throw new KeycloakException("Keycloak client credentials are not properly set. Please check that none of them is null or empty.");
        }

        try (CloseableHttpClient httpClient = new CommonHttpClientBuilder().allowAnyCertificate(allowAnyCertificate)
                                                                           .build()) {

            URI logoutUrl = deployment.getLogoutUrl().clone().build();

            HttpPost httpPost = new HttpPost(logoutUrl);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(CLIENT_ID_KEY, clientID));
            params.add(new BasicNameValuePair(CLIENT_SECRET_KEY, clientSecret));
            params.add(new BasicNameValuePair(OAuth2Constants.REFRESH_TOKEN, refreshToken));

            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse httpResponse = httpClient.execute(httpPost);
            int status = httpResponse.getStatusLine().getStatusCode();
            if ((status != HttpStatus.SC_NO_CONTENT)) {
                StringBuilder errorBuilder = new StringBuilder("Logout of refreshToken failed. Invalid status: " +
                                                               status);
                String response = EntityUtils.toString(httpResponse.getEntity());
                if (status == HttpStatus.SC_BAD_REQUEST && response != null) {
                    OAuth2ErrorRepresentation errorRep = JsonSerialization.readValue(response,
                                                                                     OAuth2ErrorRepresentation.class);
                    errorBuilder.append(", OAuth2 error. Error: ")
                                .append(errorRep.getError())
                                .append(", Error description: ")
                                .append(errorRep.getErrorDescription());
                }
                // Should do something better than throwing an error if logout failed. Perhaps update of refresh tokens on existing subject...
                throw new KeycloakException(errorBuilder.toString());
            }
        } catch (IOException ioe) {
            LOGGER.warn(ioe.getMessage());
        }
    }
}
