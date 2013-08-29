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
package org.ow2.proactive.tests.performance.jmeter.rm;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.tests.performance.rm.TestRMProxy;


public class RMConnectionParameters {

    public static final String PARAM_RM_URL = "rmUrl";

    public static final String PARAM_RM_LOGIN = "rmLogin";

    public static final String PARAM_RM_PASSWORD = "rmPassword";

    public static Arguments getDefaultParameters(Arguments args) {
        args.addArgument(PARAM_RM_URL, "${rmUrl}");
        args.addArgument(PARAM_RM_LOGIN, "${rmLogin}");
        args.addArgument(PARAM_RM_PASSWORD, "${rmPassword}");
        return args;
    }

    private final JavaSamplerContext context;

    public RMConnectionParameters(JavaSamplerContext context) {
        this.context = context;
    }

    public String getRmUrl() {
        return context.getParameter(PARAM_RM_URL);
    }

    public String getRmLogin() {
        return context.getParameter(PARAM_RM_LOGIN);
    }

    public String getRmPassword() {
        return context.getParameter(PARAM_RM_PASSWORD);
    }

    public TestRMProxy connectWithProxyUserInterface() throws Exception {
        String url = getRmUrl();
        String login = getRmLogin();
        String password = getRmPassword();

        TestRMProxy rmProxy = PAActiveObject.newActive(TestRMProxy.class, new Object[] {});
        rmProxy.init(url, new CredData(CredData.parseLogin(login), CredData.parseDomain(login), password));
        return rmProxy;
    }
}
