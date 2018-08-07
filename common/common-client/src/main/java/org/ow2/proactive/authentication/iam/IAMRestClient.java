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
package org.ow2.proactive.authentication.iam;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.ow2.proactive.http.CommonHttpClientBuilder;


public class IAMRestClient {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(IAMRestClient.class.getName());

    public String getSSOTicket(String url, String username, String password, String ticketMarker) {

        String ssoTicket = null;

        try (CloseableHttpClient httpClient = new CommonHttpClientBuilder().allowAnyCertificate(true).build()) {

            HttpPost httpPost = new HttpPost(url);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));

            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if ((httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) ||
                (null == httpResponse.getEntity())) {
                throw new IAMException("Failed to acquire SSO ticket for the user" + username);
            }

            String htmlResponse = EntityUtils.toString(httpResponse.getEntity());
            Document doc = Jsoup.parse(htmlResponse);
            String htmlSsoTicket = doc.getElementsByAttribute(ticketMarker).first().attributes().get(ticketMarker);

            if (htmlSsoTicket != null) {

                Pattern pattern = Pattern.compile("(TGT-\\d+-\\S+)");
                Matcher matcher = pattern.matcher(htmlSsoTicket);

                if (matcher.find()) {
                    ssoTicket = matcher.group(1);
                }
            }

            LOG.debug("SSO ticket successfully generated for user " + username + "\n Token: " + ssoTicket);

        } catch (Exception e) {
            throw new IAMException("Authentication failed: Error occurred while acquiring SSO ticket for the user " +
                                   username, e);
        }

        return ssoTicket;
    }

    public String getServiceToken(String url, String service) {

        String token;

        try (CloseableHttpClient httpClient = new CommonHttpClientBuilder().allowAnyCertificate(true).build()) {

            HttpPost httpPost = new HttpPost(url);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("service", service));
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            if ((httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) ||
                (null == httpResponse.getEntity()))
                throw new IAMException("Failed to acquire authentication token for the service " + service);

            token = EntityUtils.toString(httpResponse.getEntity());

            LOG.debug("Service token successfully generated for service " + service + "\n Token: " + token);

        } catch (Exception e) {
            throw new IAMException("Authentication failed: Error occurred while acquiring authentication token for the service " +
                                   service, e);
        }

        return token;
    }
}
