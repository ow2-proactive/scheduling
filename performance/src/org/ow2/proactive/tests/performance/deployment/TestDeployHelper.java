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
package org.ow2.proactive.tests.performance.deployment;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.tests.performance.utils.TestFileUtils;


public abstract class TestDeployHelper {

    public static final String TEST_JVM_OPTION_NAME = "org.ow2.proactive.tests.performance";

    public static final String TEST_JVM_OPTION = TEST_JVM_OPTION_NAME + "=true";

    protected final HostTestEnv serverHostEnv;

    protected final TestProtocolHelper protocolHelper;

    static final String[] requiredJARs = { "script-js.jar", "gson-2.1.jar", "jruby-engine.jar",
            "jython-engine.jar", "commons-logging-1.1.1.jar", "ProActive_Scheduler-core.jar",
            "ProActive_SRM-common.jar", "ProActive_ResourceManager.jar", "ProActive_Scheduler-worker.jar",
            "ProActive_Scheduler-matsci.jar", "ProActive_Scheduler-mapreduce.jar",
            "commons-httpclient-3.1.jar", "commons-codec-1.3.jar", "ProActive.jar" };

    public TestDeployHelper(HostTestEnv serverHostEnv, String protocol) throws InterruptedException {
        this.serverHostEnv = serverHostEnv;
        this.protocolHelper = createProtocolHelper(protocol, serverHostEnv);
    }

    private TestProtocolHelper createProtocolHelper(String protocol, HostTestEnv serverHostEnv)
            throws InterruptedException {
        if (protocol.equalsIgnoreCase("pnp")) {
            return new TestPnpProtocolHelper(serverHostEnv);
        } else if (protocol.equalsIgnoreCase("pamr")) {
            return new TestPamrProtocolHelper(serverHostEnv, getPamrServedReservedId());
        } else if (protocol.equalsIgnoreCase("rmi")) {
            return new TestRMIProtocolHelper(serverHostEnv);
        } else {
            throw new IllegalArgumentException("Test doesn't support protocol " + protocol);
        }
    }

    protected abstract String getPamrServedReservedId();

    public abstract List<String> createServerStartCommand();

    public String prepareForDeployment() throws Exception {
        return protocolHelper.prepareForDeployment();
    }

    public Map<String, String> getClientJavaProperties(TestEnv env) {
        Map<String, String> properties = new LinkedHashMap<String, String>();

        properties.put(CentralPAPropertyRepository.PA_HOME.getName(), env.getSchedulingFolder()
                .getRootDirPath());
        properties.put(CentralPAPropertyRepository.LOG4J.getName(), "file:" +
            new File(env.getSchedulingFolder().getTestConfigDir(), "/log4j/log4j-client").getAbsolutePath());

        Map<String, String> protocolSpecificOptions = protocolHelper.getClientProActiveProperties();
        properties.putAll(protocolSpecificOptions);

        properties.put(TEST_JVM_OPTION_NAME, "true");

        return properties;
    }

    protected String buildSchedulingClasspath() {
        TestEnv localEnv = TestEnv.getLocalEnvUsingSystemProperties();

        List<String> distLibJars = new ArrayList<String>();
        for (String jar : requiredJARs) {
            distLibJars.add(localEnv.getSchedulingFolder().getRootDir() + "/dist/lib/" + jar);
        }
        List<String> addonsJars = TestFileUtils.listDirectoryJars(new File(localEnv.getSchedulingFolder()
                .getRootDir(), "/addons").getAbsolutePath());

        List<String> allJars = new ArrayList<String>(distLibJars);
        allJars.addAll(addonsJars);
        StringBuilder result = new StringBuilder();
        for (String jar : allJars) {
            jar = localEnv.convertFileNameForEnv(jar, serverHostEnv.getEnv());
            result.append(jar).append(File.pathSeparatorChar);
        }

        return result.toString();
    }

}
