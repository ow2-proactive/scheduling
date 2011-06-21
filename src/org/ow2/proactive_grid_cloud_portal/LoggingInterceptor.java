package org.ow2.proactive_grid_cloud_portal;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
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

    private static final Logger logger = LoggerFactory.getLogger(SchedulerLoggers.PREFIX + ".rest");

    public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure,
            WebApplicationException {
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