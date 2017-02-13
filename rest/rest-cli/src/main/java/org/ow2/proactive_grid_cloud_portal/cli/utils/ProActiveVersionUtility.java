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
package org.ow2.proactive_grid_cloud_portal.cli.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.ow2.proactive.http.HttpClientBuilder;
import org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext;
import org.ow2.proactive_grid_cloud_portal.cli.CommonEntryPoint;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;


public final class ProActiveVersionUtility {

    public static final String VERSION_UNDEFINED = "unknown";

    public static void writeProActiveVersion(ApplicationContext currentContext, PrintStream printStream) {
        writeProActiveVersion(currentContext, printStream, false);
    }

    public static void writeProActiveVersionWithBreakEndLine(ApplicationContext currentContext,
            PrintStream printStream) {
        writeProActiveVersion(currentContext, printStream, true);
    }

    private static void writeProActiveVersion(ApplicationContext currentContext, PrintStream printStream,
            boolean breakEndLine) {
        printStream.println("ProActive client version: " + getProActiveClientVersion());
        printStream.println("ProActive server version: " + getProActiveServerVersion(currentContext));

        if (breakEndLine) {
            printStream.println();
        }
    }

    protected static String getProActiveClientVersion() {
        String result = CommonEntryPoint.class.getPackage().getImplementationVersion();

        if (result == null) {
            result = VERSION_UNDEFINED;
        }

        return result;
    }

    protected static String getProActiveServerVersion(ApplicationContext currentContext) {
        int timeout = (int) TimeUnit.SECONDS.toMillis(2);

        RequestConfig config = RequestConfig.custom()
                                            .setConnectTimeout(timeout)
                                            .setConnectionRequestTimeout(timeout)
                                            .setSocketTimeout(timeout)
                                            .build();

        try (CloseableHttpClient httpClient = new HttpClientBuilder().setDefaultRequestConfig(config)
                                                                     .useSystemProperties()
                                                                     .build()) {

            HttpGet getMethod = new HttpGet(currentContext.getResourceUrl("version"));
            HttpResponse response = httpClient.execute(getMethod);

            String jsonObject = handleResponse(response);
            if (jsonObject != null)
                return jsonObject;
        } catch (IOException ignore) {
            // ignore exception, default value will be used
        }

        return VERSION_UNDEFINED;
    }

    protected static String handleResponse(HttpResponse response) throws IOException {
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

        return null;
    }

}
