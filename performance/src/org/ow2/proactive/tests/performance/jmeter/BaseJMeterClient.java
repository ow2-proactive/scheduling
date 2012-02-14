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
package org.ow2.proactive.tests.performance.jmeter;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;


public abstract class BaseJMeterClient extends AbstractJavaSamplerClient {

    private String setupError;

    @Override
    public final SampleResult runTest(JavaSamplerContext context) {
        if (setupError != null) {
            SampleResult errorResult = new SampleResult();
            errorResult.setSuccessful(false);
            errorResult.setResponseMessage(setupError);
            return errorResult;
        } else {
            try {
                return doRunTest(context);
            } catch (Throwable t) {
                SampleResult errorResult = new SampleResult();
                errorResult.setSuccessful(false);
                errorResult.setResponseMessage("Unexpected exception during test execution: " + t);
                logError("Unexpected exception during test execution: " + t, t);
                return errorResult;
            }
        }
    }

    protected abstract SampleResult doRunTest(JavaSamplerContext context) throws Throwable;

    @Override
    public final void setupTest(JavaSamplerContext context) {
        try {
            printParameters(context);

            doSetupTest(context);
        } catch (Throwable t) {
            setupError = "Failed to setup execution: " + t;
            logError(setupError, t);
        }
    }

    protected void logError(String message, Throwable t) {
        getLogger().error(message, t);
    }

    protected void logError(String message) {
        getLogger().error(message);
    }

    protected void logInfo(String message) {
        getLogger().info(message);
    }

    protected abstract void doSetupTest(JavaSamplerContext context) throws Throwable;

    public static boolean getBooleanParameter(JavaSamplerContext context, String name) {
        return Boolean.valueOf(context.getParameter(name));
    }

    public static String getRequiredParameter(JavaSamplerContext context, String name) {
        if (!context.containsParameter(name)) {
            throw new IllegalArgumentException("Parameter '" + name + "' isn't set");
        }
        String value = context.getParameter(name);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Parameter '" + name + "' is empty");
        }
        return value;
    }

    protected void printParameters(JavaSamplerContext context) {
        Arguments args = getDefaultParameters();
        logInfo("Parameters for sampler " + getClass().getName() + ":");
        for (String name : args.getArgumentsAsMap().keySet()) {
            logInfo(name + " = " + context.getParameter(name));
        }
    }

}
