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
package org.ow2.proactive.perftests.rest.jmeter.rm;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;


/**
 * Retrieves the set of parameters which are required to connect to RM REST API
 * from the JavaSamplerContext instance.
 * 
 */
public class RESTfulRMConnection {
    public static final String PARAM_REST_RM_URL = "restRmUrl";
    public static final String PARAM_REST_RM_LOGIN = "restRmLogin";
    public static final String PARAM_REST_RM_PASSWORD = "restRmPassword";

    public static Arguments addDefaultParamters(Arguments args) {
        args.addArgument(PARAM_REST_RM_URL, "${restRmUrl}");
        args.addArgument(PARAM_REST_RM_LOGIN, "${restRmLogin}");
        args.addArgument(PARAM_REST_RM_PASSWORD, "${restRmPassword}");
        return args;
    }

    private final JavaSamplerContext context;
    private String resourceUrl;

    public RESTfulRMConnection(JavaSamplerContext context) {
        this.context = context;
        setResourceUrl();
    }

    public String getUrl() {
        return resourceUrl;
    }

    public String getLogin() {
        return context.getParameter(PARAM_REST_RM_LOGIN);
    }

    public String getPassword() {
        return context.getParameter(PARAM_REST_RM_PASSWORD);
    }

    private void setResourceUrl() {
        String serverUrl = context.getParameter(PARAM_REST_RM_URL);
        serverUrl = (serverUrl.endsWith("/")) ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
        this.resourceUrl = (serverUrl.endsWith("rm")) ? serverUrl : serverUrl + "";
    }

}
