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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.ow2.proactive.http.CommonHttpClientBuilder;


public class IAMRestClient {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(IAMRestClient.class.getName());

    private static CloseableHttpClient httpClient;

    private IAMRestClient() {

    }

    public static String getSSOTicket(String url, String username, String password, String ticketHeader)
            throws IOException {

        String ticket = null;

        httpClient = new CommonHttpClientBuilder().allowAnyCertificate(true).build();

        HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));

        httpPost.setEntity(new UrlEncodedFormEntity(params));

        HttpResponse httpResponse = httpClient.execute(httpPost);

        if ((httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) ||
            (null == httpResponse.getEntity()))
            throw new IAMException("Authentication failed");

        Header[] headers = httpResponse.getHeaders(ticketHeader);

        if (headers[0] != null) {

            Pattern pattern = Pattern.compile("(TGT-\\d+-\\S+)");
            Matcher matcher = pattern.matcher(headers[0].toString());

            if (matcher.find()) {
                ticket = matcher.group(1);
            } else
                throw new IAMException("TGT not found in IAM response");
        }

        logger.debug("Token successfully generated for user " + username + "\n Token: " + ticket);

        httpClient.close();

        return ticket;
    }

    public static String getServiceToken(String url, String service) throws IOException {

        httpClient = new CommonHttpClientBuilder().allowAnyCertificate(true).build();

        HttpPost httpPost = new HttpPost(url);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("service", service));
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        HttpResponse httpResponse = httpClient.execute(httpPost);

        if ((httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) || (null == httpResponse.getEntity()))
            throw new IAMException("Authentication failed");

        String token = EntityUtils.toString(httpResponse.getEntity());

        logger.debug("Token successfully generated for service " + service + "\n Token: " + token);

        httpClient.close();

        return token;
    }
}
