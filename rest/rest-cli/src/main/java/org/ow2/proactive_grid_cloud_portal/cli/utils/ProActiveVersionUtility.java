/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.cli.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CommonEntryPoint;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public final class ProActiveVersionUtility {

    public static final String VERSION_UNDEFINED = "unknown";

    public static void writeProActiveVersionWithBreakEndLine(ApplicationContext currentContext, PrintStream printStream) {
        writeProActiveVersion(currentContext, printStream, true);
    }

    public static void writeProActiveVersion(ApplicationContext currentContext, PrintStream printStream, boolean breakEndLine) {
        printStream.println("ProActive client version: " + getProActiveClientVersion());
        printStream.println("ProActive server version: " + getProActiveServerVersion(currentContext));

        if (breakEndLine) {
            printStream.println();
        }
    }

    private static String getProActiveClientVersion() {
        String result = CommonEntryPoint.class.getPackage().getImplementationVersion();

        if (result == null) {
            result = VERSION_UNDEFINED;
        }

        return result;
    }

    private static String getProActiveServerVersion(ApplicationContext currentContext) {
        int timeout = (int) TimeUnit.SECONDS.toMillis(2);

        RequestConfig config =
                RequestConfig.custom()
                        .setConnectTimeout(timeout)
                        .setConnectionRequestTimeout(timeout)
                        .setSocketTimeout(timeout).build();

        try(CloseableHttpClient httpClient =
                    HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {

            HttpGet getMethod = new HttpGet(currentContext.getResourceUrl("version"));
            HttpResponse response = httpClient.execute(getMethod);

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    JsonValue jsonValue = Json.parse(EntityUtils.toString(entity));

                    if (jsonValue.isObject()) {
                        JsonObject jsonObject = jsonValue.asObject();
                        return jsonObject.get("rest").asString();
                    }
                }
            }
        } catch (IOException ignore) {
            // ignore exception, default value will be used
        }

        return VERSION_UNDEFINED;
    }

}
