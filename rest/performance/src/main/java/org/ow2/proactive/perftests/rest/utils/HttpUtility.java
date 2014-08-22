/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.perftests.rest.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpParams;


public class HttpUtility {

    public static final String DFLT_CHARSET = "ISO-8859-1";
    //    private static final String DFLT_CHARSET = "UTF-8";

    private static final int DFLT_CONNECTION_TIMEOUT = 5 * 1000;

    public static final int STATUS_OK = 200;

    private HttpUtility() {
    }

    public static HttpResponseWrapper execute(String session, HttpRequestBase request) throws IOException {
        if (session != null) {
            request.setHeader("sessionid", session);
        }
        try {
            HttpResponse response = threadSafeClient().execute(request);
            HttpResponseWrapper wrapper = new HttpResponseWrapper();
            wrapper.setStatusCode(response.getStatusLine().getStatusCode());
            InputStream content = response.getEntity().getContent();
            if (content != null) {
                wrapper.setContents(IOUtils.toByteArray(content));
            }
            return wrapper;
        } finally {
            request.releaseConnection();
        }
    }

    public static String encode(String unescaped) throws UnsupportedEncodingException {
        return URLEncoder.encode(unescaped, DFLT_CHARSET);
    }

    public static boolean isEmpty(HttpResponseWrapper response) {
        return (response.getContents() == null || response.getContents().length == 0);
    }

    private static HttpClient threadSafeClient() {
        DefaultHttpClient client = new DefaultHttpClient();
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();
        org.apache.http.params.HttpConnectionParams.setConnectionTimeout(params, DFLT_CONNECTION_TIMEOUT);
        client = new DefaultHttpClient(new PoolingClientConnectionManager(mgr.getSchemeRegistry()), params);
        return client;
    }

}
