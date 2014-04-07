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
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.webapp;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * RESTeasy interceptor for logging
 */
@Provider
@SecurityPrecedence
@ServerInterceptor
public class LoggingInterceptor implements PreProcessInterceptor, PostProcessInterceptor {

    @Context
    ServletContext servletContext;
    @Context
    HttpHeaders httpHeaders;
    @Context
    UriInfo ui;

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    public ServerResponse preProcess(HttpRequest request, ResourceMethodInvoker method)
      throws Failure, WebApplicationException {
        if (logger.isDebugEnabled()) {

            String httpMethod = request.getHttpMethod();

            URI uri = ui.getRequestUri();

            String uriPath = uri.getPath();
            if (uri.getQuery() != null) {
                uriPath += "?" + uri.getQuery();
            }
            if (uri.getFragment() != null) {
                uriPath += "#" + uri.getFragment();
            }

            String sessionid = null;
            List<String> headerSessionId = request.getHttpHeaders().getRequestHeader("sessionid");
            if (headerSessionId != null) {
                sessionid = headerSessionId.get(0);
            }
            if (logger.isDebugEnabled()) {
                // log only in debug mode
                logger.debug(sessionid + "|" + httpMethod + "|" + uriPath);
            }
        }
        return null;
    }

    public void postProcess(ServerResponse response) {

        Iterator<Entry<String, List<String>>> it = httpHeaders.getRequestHeaders().entrySet().iterator();

        // get sessionid from headers

        String sessionid = "[NOTSET]";
        List<String> headerSessionId = httpHeaders.getRequestHeaders().get("sessionid");

        if (headerSessionId != null) {
            sessionid = headerSessionId.get(0);
        }

        URI uri = ui.getRequestUri();
        String uriPath = uri.getPath();
        if (uri.getQuery() != null) {
            uriPath += "?" + uri.getQuery();
        }
        if (uri.getFragment() != null) {
            uriPath += "#" + uri.getFragment();
        }

        if (logger.isDebugEnabled()) {
            // in debug mode log anything
            logger.info(sessionid + "|" + uriPath + "|" + response.getStatus() + "|" +
                response.getEntity().toString());
        } else {
            // log errors even in info mode
            if ((logger.isInfoEnabled()) && (response.getStatus() >= 300)) {
                logger.info(sessionid + "|" + uriPath + "|" + response.getStatus() + "|" +
                    response.getEntity().toString());
            }
        }
    }

}