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
package org.ow2.proactive.perftests.rest.jmeter.sched;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;


/**
 * Retrieves the set parameters which are required to connect to Scheduler REST
 * API from the given JavaSamplerContext instance.
 * 
 */
public class RESTfulSchedConnection {

    public static final String PARAM_REST_SCHED_URL = "restSchedulerUrl";
    public static final String PARAM_REST_SCHED_LOGIN = "restSchedulerLogin";
    public static final String PARAM_REST_SCHED_PASSWORD = "restSchedulerPassword";

    public static Arguments addDefaultParameters(Arguments arguments) {
        arguments.addArgument(PARAM_REST_SCHED_URL, "${restSchedulerUrl}");
        arguments.addArgument(PARAM_REST_SCHED_LOGIN, "${restSchedulerLogin}");
        arguments.addArgument(PARAM_REST_SCHED_PASSWORD, "${restSchedulerPassword}");
        return arguments;
    }

    private final JavaSamplerContext context;
    private String resourceUrl;

    public RESTfulSchedConnection(JavaSamplerContext context) {
        this.context = context;
        setResourceUrl();
    }

    public String getUrl() {
        return resourceUrl;
    }

    public String login() {
        return context.getParameter(PARAM_REST_SCHED_LOGIN);
    }

    public String password() {
        return context.getParameter(PARAM_REST_SCHED_PASSWORD);
    }

    private void setResourceUrl() {
        String serverUrl = context.getParameter(PARAM_REST_SCHED_URL);
        serverUrl = (serverUrl.endsWith("/")) ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
        resourceUrl = (serverUrl.endsWith("scheduler")) ? serverUrl : serverUrl + "/scheduler";
    }
}
