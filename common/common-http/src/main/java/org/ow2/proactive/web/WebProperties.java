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
package org.ow2.proactive.web;

/**
 * The class defines the name of the properties related
 * to the deployment of the Web server.
 *
 * @author ActiveEon Team
 */
public final class WebProperties {

    public static final String WEB_DEPLOY = "web.deploy";

    public static final String WEB_HTTP_PORT = "web.http.port";

    public static final String WEB_HTTPS = "web.https";

    public static final String WEB_HTTPS_ALLOW_ANY_CERTIFICATE = "web.https.allow_any_certificate";

    public static final String WEB_HTTPS_ALLOW_ANY_HOSTNAME = "web.https.allow_any_hostname";

    public static final String WEB_HTTPS_KEYSTORE = "web.https.keystore";

    public static final String WEB_HTTPS_KEYSTORE_PASSWORD = "web.https.keystore.password"; // NOSONAR

    public static final String WEB_HTTPS_PORT = "web.https.port";

    public static final String WEB_MAX_THREADS = "web.max_threads";

    @Deprecated
    public static final String WEB_PORT = "web.port";

    public static final String WEB_REDIRECT_HTTP_TO_HTTPS = "web.redirect_http_to_https";

    public static final String METADATA_CONTENT_TYPE = "content.type";

    public static final String METADATA_FILE_NAME = "file.name";

    public static final String METADATA_FILE_EXTENSION = "file.extension";

    private WebProperties() {

    }

}
