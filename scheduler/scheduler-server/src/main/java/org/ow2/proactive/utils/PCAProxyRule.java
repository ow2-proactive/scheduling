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
package org.ow2.proactive.utils;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.rewrite.handler.Rule;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.URIUtil;
import org.ow2.proactive.web.WebProperties;


/**
 * PCAProxyRule allows rewriting requests coming from PCA proxyfied applications.
 * It relies on the Referer header to determine where a request comes from
 * It does not do any operation for requests which contains a Referer header not matching any proxified application url
 *
 * For example:
 * A request /static/css/some.css is received with a Referer containing /cloud-automation-service/services/12/endpoints/my-endpoint/
 * This request will be rewritten to
 * /cloud-automation-service/services/12/endpoints/my-endpoint/static/css/some.css
 *
 * if the request is a GET request, the jetty server will issue a redirection (code 302 Found), and request handling will terminate.
 * This allows the client browser to have a correct referer for further requests.
 * If the request is not a GET request, the request handling will continue, using the rewritten uri, thus delegating to cloud-automation-service the request.
 *
 * In the rare case where the referer is incorrect (for example a css stylesheet witch includes another css with absolute path AND redirection strategy did not work),
 * PCAProxyRule maintains a referer cache to keep track of requests which have already been rewritten, and will use this cache to determine the correct referer.
 *
 * @author ActiveEon Team
 * @since 23/07/2020
 */
public class PCAProxyRule extends Rule implements Rule.ApplyURI {

    public static final String originalPathAttribute = "__path";

    private final static String PCA_PROXY_REGEXP = "/cloud-automation-service/services/[0-9]+/endpoints/[^/]+/";

    private final static Pattern pattern = Pattern.compile(PCA_PROXY_REGEXP);

    private static final Logger logger = Logger.getLogger(PCAProxyRule.class);

    private final int maximumCacheSize;

    private final LinkedHashMap<String, String> referrerCache;

    public PCAProxyRule() {
        _terminating = false;
        _handling = false;
        maximumCacheSize = WebProperties.WEB_PCA_PROXY_REWRITE_REFERER_CACHE_SIZE.getValueAsInt();
        referrerCache = new LinkedHashMap<String, String>(100, 0.75f, true) {
            @Override
            public boolean removeEldestEntry(Map.Entry eldest) {
                return size() > maximumCacheSize;
            }
        };
    }

    @Override
    public String matchAndApply(String target, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("Analysing target: " + target);
        }
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (logger.isTraceEnabled()) {
            logger.trace("Referer: " + referer);
        }
        if (referer != null && !referer.isEmpty()) {

            URL refererUrl = null;
            try {
                refererUrl = new URL(referer);
            } catch (Exception e) {
                logger.warn("Invalid URL in referer : " + referer, e);
                return target;
            }
            String refererPath = refererUrl.getPath();
            Matcher matcherReferer = pattern.matcher(refererPath);

            if (matcherReferer.find()) {
                // get the endpoint path, without trailing slash
                String endpointPath = refererPath.substring(matcherReferer.start(), matcherReferer.end() - 1);
                return rewriteTarget(target, endpointPath, request, response);
            } else if (referrerCache.containsKey(refererPath)) {
                // the referer is a direct url which has already been handled and stored in our cache
                // this is typically the case when a css file includes another css file
                // in order to solve this problem, we reuse the previous endpoint stored in the cache and associated with this referer
                String endpointPath = referrerCache.get(refererPath);
                return rewriteTarget(target, endpointPath, request, response);
            }
        }
        return target;
    }

    private String rewriteTarget(String target, String endpointPath, HttpServletRequest request,
            HttpServletResponse response) {
        Matcher matcherRequest = pattern.matcher(target);
        if (logger.isDebugEnabled()) {
            logger.debug("Endpoint found in referer: " + endpointPath);
        }
        if (!matcherRequest.find()) {
            // request target does not contain a pca endpoint, we need to add it
            String newTarget = endpointPath + target;
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Rewrote %s to %s", target, newTarget));
            }
            referrerCache.put(target, endpointPath);
            if (HttpMethod.GET.is(request.getMethod())) {
                redirectGetRequest(target, endpointPath, request, response);
            }
            return newTarget;
        } else {
            logger.trace("Target already contains endpoint");
            if (!target.startsWith(endpointPath)) {
                // endpoint is in the middle of the path (who did this?), let's bring it on top
                // NOTE: we don't store this in the referer cache, and we don't use redirect
                String targetWithoutEndpoint = target.replace(endpointPath, "");
                String newTarget = endpointPath + targetWithoutEndpoint;
                return newTarget;
            }
        }
        return target;
    }

    private void redirectGetRequest(String targetWithoutEndpoint, String endpointPath, HttpServletRequest request,
            HttpServletResponse response) {
        // GET requests should be redirected while preserving all original parameters
        // this allows the application to provide a correct Referer in most cases (thus not relying on the cache mechanism)
        // Other type of requests redirection behaves erratically
        String newUri = null;
        try {
            String oldUri = ((Request) request).getUri().getCompletePath();
            String uriWithoutEndpoint = oldUri.replace(endpointPath, "");
            newUri = endpointPath + uriWithoutEndpoint;

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Relocation URI %s", newUri));
            }

            response.sendRedirect(response.encodeRedirectURL(newUri));
            (request instanceof Request ? (Request) request
                                        : HttpChannel.getCurrentHttpChannel().getRequest()).setHandled(true);
        } catch (IOException e) {
            logger.error(String.format("Error while redirecting %s to %s", targetWithoutEndpoint, newUri), e);
        }
    }

    @Override
    public void applyURI(Request request, String oldURI, String newURI) throws IOException {
        String originalPath = URIUtil.encodePath((String) request.getAttribute(originalPathAttribute));
        if (logger.isTraceEnabled()) {
            logger.trace("originalPath: " + originalPath);
            logger.trace("oldURI: " + oldURI);
            logger.trace("newURI: " + newURI);
        }
        boolean isRedirection = !originalPath.equals(newURI);
        if (isRedirection && oldURI.startsWith(originalPath)) {
            String remaining = oldURI.substring(originalPath.length());
            String uriRewritten = newURI + remaining;
            request.setRequestURI(uriRewritten);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Rewrote URI %s to %s", oldURI, uriRewritten));
            }
            request.setPathInfo(uriRewritten);
        }
    }
}
