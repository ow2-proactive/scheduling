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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.ws.http.HTTPException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jasig.cas.client.util.CommonUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.ow2.proactive.boot.microservices.iam.exceptions.IAMException;
import org.ow2.proactive.http.CommonHttpClientBuilder;


public class IAMRestClient {

    /**
     * An attribute (key) used to designate the service for which a (service) token is generated
     */
    private static final String SERVICE_KEY = "service";

    /**
     * An attribute (key) used to designate the user login
     */
    private static final String USER_KEY = "username";

    /**
     * An attribute (key) used to designate the user login
     */
    private static final String SECRET_KEY = "password";

    /**
     * An attribute (key) used to generate SSO ticket as a JWT (Json Web Token)
     */
    private static final String TOKEN_KEY = "token";

    /**
     * The attribute value set to 'true' when SSO ticket is generated as a JWT
     */
    private static final String TOKEN_VALUE = "true";

    /**
     * Pattern used to extract SSO ticket generated as a TGT (Ticket-Granting-Ticket)
     * Example: TGT-1-cS39DsDtjIXUV4-zVF23dYys5GpfSIGPmWU7vdA8tzM0aI7FB4H52EG6BLPADMik3zA-activeeon
     */
    private static final Pattern pattern = Pattern.compile("(TGT-\\d+-\\S+)");

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(IAMRestClient.class.getName());

    public String getSSOTicket(String url, String username, char[] password, String ticketMarker, boolean asJWT) {

        String ssoTicket = null;

        try (CloseableHttpClient httpClient = new CommonHttpClientBuilder().allowAnyCertificate(true).build()) {

            HttpPost httpPost = new HttpPost(url);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(USER_KEY, username));
            params.add(new BasicNameValuePair(SECRET_KEY, String.valueOf(password)));

            if (asJWT) {
                params.add(new BasicNameValuePair(TOKEN_KEY, TOKEN_VALUE));
            }

            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if ((httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) ||
                (null == httpResponse.getEntity())) {

                throw new IAMException("Failed to acquire SSO ticket for the user '" + username,
                                       new HTTPException(httpResponse.getStatusLine().getStatusCode()));
            }

            LOG.debug("IAM response: " + httpResponse);

            String iamResponse = EntityUtils.toString(httpResponse.getEntity());

            if (!asJWT) {
                Document doc = Jsoup.parse(iamResponse);
                String htmlSsoTicket = doc.getElementsByAttribute(ticketMarker).first().attributes().get(ticketMarker);

                if (htmlSsoTicket != null) {

                    Matcher matcher = pattern.matcher(htmlSsoTicket);

                    if (matcher.find()) {
                        ssoTicket = matcher.group(1);
                    }
                }
            } else {
                ssoTicket = iamResponse;
            }

            CommonUtils.assertNotNull(ssoTicket, "Unable to extract SSO ticket form IAM response. SSO ticket is null.");

            LOG.debug("SSO ticket successfully generated for user " + username + "\n Token: " + ssoTicket);

        } catch (Exception e) {
            throw new IAMException("Error occurred while acquiring SSO ticket for the user " + username, e);
        }

        return ssoTicket;
    }

    public String getServiceToken(String url, String service) {

        String token;

        try (CloseableHttpClient httpClient = new CommonHttpClientBuilder().allowAnyCertificate(true).build()) {

            HttpPost httpPost = new HttpPost(url);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(SERVICE_KEY, service));
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if ((httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) ||
                (null == httpResponse.getEntity()))
                throw new IAMException("Failed to acquire token for the service " + service,
                                       new HTTPException(httpResponse.getStatusLine().getStatusCode()));

            token = EntityUtils.toString(httpResponse.getEntity());

            LOG.debug("Service token successfully generated for service " + service + "\n Token: " + token);

        } catch (Exception e) {
            throw new IAMException("Error occurred while acquiring authentication token for the service " + service, e);
        }

        return token;
    }

    public boolean isSSOTicketValid(String url) {

        try (CloseableHttpClient httpClient = new CommonHttpClientBuilder().allowAnyCertificate(true).build()) {

            return httpClient.execute(new HttpGet(url)).getStatusLine().getStatusCode() == HttpStatus.SC_OK;

        } catch (Exception e) {
            throw new IAMException("Error occurred while validating SSO ticket.", e);
        }
    }

    public boolean deleteSSOTicket(String url) {

        try (CloseableHttpClient httpClient = new CommonHttpClientBuilder().allowAnyCertificate(true).build()) {

            return httpClient.execute(new HttpDelete(url)).getStatusLine().getStatusCode() == HttpStatus.SC_OK;

        } catch (Exception e) {
            throw new IAMException("Error occurred while destroying SSO ticket.", e);
        }
    }

    public String validateUserCredentials(String url, String username, char[] password) {

        try (CloseableHttpClient httpClient = new CommonHttpClientBuilder().allowAnyCertificate(true).build()) {

            HttpPost httpPost = new HttpPost(url);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(USER_KEY, username));
            params.add(new BasicNameValuePair(SECRET_KEY, String.valueOf(password)));

            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if ((httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) ||
                (null == httpResponse.getEntity()))
                throw new IAMException("Failed to validate credentials of user '" + username,
                                       new HTTPException(httpResponse.getStatusLine().getStatusCode()));

            return EntityUtils.toString(httpResponse.getEntity());

        } catch (Exception e) {
            throw new IAMException("Error occurred while validating credentials of user [" + username + "]", e);
        }
    }
}
