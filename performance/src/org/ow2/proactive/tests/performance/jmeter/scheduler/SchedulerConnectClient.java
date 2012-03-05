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
package org.ow2.proactive.tests.performance.jmeter.scheduler;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.tests.performance.jmeter.scheduler.BaseJMeterSchedulerClient;
import org.ow2.proactive.tests.performance.jmeter.scheduler.SchedulerConnectionParameters;


public class SchedulerConnectClient extends BaseJMeterSchedulerClient {

    private static final long LOGIN_TIMEOUT = 5 * 60000;

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context) throws Throwable {
        SchedulerConnectionParameters parameters = new SchedulerConnectionParameters(context);
        String url = parameters.getSchedulerUrl();
        String login = parameters.getSchedulerLogin();
        String password = parameters.getSchedulerPassword();

        SchedulerAuthenticationInterface auth = SchedulerConnection.waitAndJoin(url, LOGIN_TIMEOUT);
        Credentials cred = Credentials.createCredentials(new CredData(CredData.parseLogin(login), CredData
                .parseDomain(login), password), auth.getPublicKey());

        SampleResult result = new SampleResult();
        result.sampleStart();
        Scheduler scheduler = auth.login(cred);
        result.sampleEnd();
        result.setSuccessful(true);
        scheduler.disconnect();

        return result;
    }

}
