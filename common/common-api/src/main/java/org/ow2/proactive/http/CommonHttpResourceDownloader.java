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
package org.ow2.proactive.http;

import io.github.pixee.security.BoundedLineReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.ow2.proactive.utils.FileUtils;


/**
 * @author ActiveEon Team
 * @since 28/11/2017
 */
public class CommonHttpResourceDownloader {

    private static final Logger logger = Logger.getLogger(CommonHttpResourceDownloader.class);

    private static final Integer CONNECTION_POOL_SIZE = 5;

    private static final String CONTENT_DISPOSITIOB_REGEXP = "(?i)^.*filename=\"([^\"]+)\".*$";

    private static CommonHttpResourceDownloader instance = null;

    private CloseableHttpClient client;

    public static CommonHttpResourceDownloader getInstance() {
        if (instance == null) {
            instance = new CommonHttpResourceDownloader();
        }
        return instance;
    }

    private CommonHttpResourceDownloader() {

    }

    public String getResource(String sessionId, String url, boolean insecure) throws IOException {

        CommonHttpClientBuilder builder = new CommonHttpClientBuilder().maxConnections(CONNECTION_POOL_SIZE)
                                                                       .useSystemProperties()
                                                                       .insecure(insecure);
        try (CloseableHttpClient client = builder.build()) {
            CloseableHttpResponse response = createAndExecuteRequest(sessionId, url, client);
            return readContent(response.getEntity().getContent());
        }
    }

    public UrlContent getResourceContent(String sessionId, String url, boolean insecure) throws IOException {

        CommonHttpClientBuilder builder = new CommonHttpClientBuilder().maxConnections(CONNECTION_POOL_SIZE)
                                                                       .useSystemProperties()
                                                                       .insecure(insecure);
        try (CloseableHttpClient client = builder.build()) {
            CloseableHttpResponse response = createAndExecuteRequest(sessionId, url, client);

            Header contentDispositionHeader = response.getFirstHeader("Content-Disposition");
            String filename;
            if (contentDispositionHeader != null &&
                contentDispositionHeader.getValue().matches(CONTENT_DISPOSITIOB_REGEXP)) {
                filename = contentDispositionHeader.getValue().replaceFirst(CONTENT_DISPOSITIOB_REGEXP, "$1");
            } else {
                filename = FileUtils.getFileNameWithExtension(new URL(url));
            }
            return new UrlContent(readContent(response.getEntity().getContent()), filename);
        }
    }

    public ResponseContent getResponse(String sessionId, String url, boolean insecure) throws IOException {
        CommonHttpClientBuilder builder = new CommonHttpClientBuilder().maxConnections(CONNECTION_POOL_SIZE)
                                                                       .useSystemProperties()
                                                                       .insecure(insecure);
        try (CloseableHttpClient client = builder.build()) {
            HttpGet request = new HttpGet(url);

            if (sessionId != null) {
                request.setHeader("sessionid", sessionId);
            }
            HttpResponse response = client.execute(request);
            String content = EntityUtils.toString(response.getEntity());
            int code = response.getStatusLine().getStatusCode();
            return new ResponseContent(content, code);
        }
    }

    private CloseableHttpResponse createAndExecuteRequest(String sessionId, String url, CloseableHttpClient client)
            throws IOException {
        HttpGet request = new HttpGet(url);

        if (sessionId != null) {
            request.setHeader("sessionid", sessionId);
        }
        CloseableHttpResponse response = client.execute(request);

        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException(String.format("Cannot access resource %s: code %d", url, status.getStatusCode()));
        }
        return response;
    }

    private String readContent(InputStream input) throws IOException {
        try (BufferedReader buf = new BufferedReader(new InputStreamReader(input))) {
            StringBuilder builder = new StringBuilder();
            String tmp;

            while ((tmp = BoundedLineReader.readLine(buf, 5_000_000)) != null) {
                builder.append(tmp).append("\n");
            }

            return builder.toString();
        }
    }

    public static class UrlContent {
        private final String content;

        private final String fileName;

        public UrlContent(String content, String fileName) {
            this.content = content;
            this.fileName = fileName;
        }

        public String getContent() {
            return content;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public static class ResponseContent {
        private final String content;

        private final int code;

        public ResponseContent(String content, int code) {
            this.content = content;
            this.code = code;
        }

        public String getContent() {
            return content;
        }

        public int getCode() {
            return code;
        }
    }
}
