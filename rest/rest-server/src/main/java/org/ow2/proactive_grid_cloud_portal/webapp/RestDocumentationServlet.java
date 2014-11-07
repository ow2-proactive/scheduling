/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
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
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.webapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.UnavailableException;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.URLResource;


/**
 * Default Jetty FileResource won't escape curly braces that are used in our REST documentation URLs.
 */
public class RestDocumentationServlet extends DefaultServlet {
    private ResourceEscapingBraces resourceEscapingBraces;

    @Override
    public void init() throws UnavailableException {
        super.init();

        try {
            resourceEscapingBraces = new ResourceEscapingBraces(getServletContext().getResource("/"));
        } catch (Exception e) {
            log("Could not start servlet", e);
            throw new UnavailableException("Could not start servlet " + e);
        }
    }

    @Override
    public Resource getResource(String pathInContext) {
        return resourceEscapingBraces.getResource(pathInContext);
    }

    private class ResourceEscapingBraces extends FileResource {

        public ResourceEscapingBraces(URL url) throws IOException, URISyntaxException {
            super(url);
        }

        public Resource addPath(String path) throws IOException, MalformedURLException {
            URLResource r;
            String url;

            path = org.eclipse.jetty.util.URIUtil.canonicalPath(path);

            if ("/".equals(path))
                return this;
            else if (!isDirectory()) {
                r = (FileResource) super.addPath(path);
            } else {
                if (path == null)
                    throw new MalformedURLException();

                // treat all paths being added as relative
                String rel = path;
                if (path.startsWith("/"))
                    rel = path.substring(1);

                String encodedPath = URIUtil.encodePath(rel);
                encodedPath = encodedPath.replaceAll("\\{", "%7B").replaceAll("\\}", "%7D");
                url = URIUtil.addPaths(_urlString, encodedPath);
                r = (URLResource) Resource.newResource(url);
            }
            return r;
        }
    }

}
