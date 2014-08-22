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

import org.ow2.proactive.perftests.rest.jmeter.BaseRESTfulClient;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.util.JMeterUtils;


/**
 * BaseRESTfulSchedClient contains set of methods which are used by its child
 * classes to retrieve the available (or known) job-ids. Each child classes
 * queries different information about each of these jobs or subset of these
 * jobs.
 */
public abstract class BaseRESTfulSchedClient extends BaseRESTfulClient {

    public static final String PARAM_CLIENT_REFRESH_TIME = "clientRefreshTime";

    public static final String[] EMPTY_STRING_ARRAY = new String[] {};

    private RESTfulSchedConnection conn;

    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = super.getDefaultParameters();
        RESTfulSchedConnection.addDefaultParameters(arguments);
        return arguments;
    }

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        super.doSetupTest(context);
        conn = new RESTfulSchedConnection(context);
    }

    protected RESTfulSchedConnection getConnection() {
        return conn;
    }

    protected String clientSpecificJobIdKey() {
        return getThreadNum() + "-job-ids";
    }

    protected String[] getAvailableJobIds() {
        String jobIdString = JMeterUtils.getProperty(clientSpecificJobIdKey());
        if (jobIdString == null) {
            return EMPTY_STRING_ARRAY;
        } else {
            return jobIdString.split(" ");
        }
    }

    protected boolean isEmpty(String[] array) {
        return array == null || array.length == 0;
    }
}
