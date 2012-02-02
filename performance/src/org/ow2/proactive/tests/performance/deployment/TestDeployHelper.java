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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.ow2.proactive.tests.performance.utils.TestFileUtils;


public abstract class TestDeployHelper {

    public static final String TEST_JVM_OPTION = "org.ow2.proactive.tests.performance=true";

    protected final SchedulingFolder schedulingFolder;

    protected final String javaPath;

    protected final TestProtocolHelper protocolHelper;

    public TestDeployHelper(String javaPath, SchedulingFolder schedulingFolder, InetAddress serverHost,
            String protocol) throws InterruptedException {
        this.javaPath = javaPath;
        this.schedulingFolder = schedulingFolder;
        this.protocolHelper = createProtocolHelper(protocol, schedulingFolder, serverHost, javaPath);
    }

    private TestProtocolHelper createProtocolHelper(String protocol, SchedulingFolder schedulingFolder,
            InetAddress serverHost, String javaPath) throws InterruptedException {
        if (protocol.equalsIgnoreCase("pnp")) {
            return new TestPnpProtocolHelper(javaPath, schedulingFolder, serverHost);
        } else if (protocol.equalsIgnoreCase("pamr")) {
            return new TestPamrProtocolHelper(javaPath, schedulingFolder, serverHost,
                getPamrServedReservedId());
        } else {
            throw new IllegalArgumentException("Test doesn't support protocol " + protocol);
        }
    }

    protected abstract String getPamrServedReservedId();

    public abstract List<String> createServerStartCommand();

    public String prepareForDeployment() throws Exception {
        return protocolHelper.prepareForDeployment();
    }

    public Map<String, String> getClientProActiveProperties() {
        Map<String, String> properties = new LinkedHashMap<String, String>();

        properties.put(CentralPAPropertyRepository.PA_HOME.getName(), schedulingFolder.getRootDirPath());
        properties.put(CentralPAPropertyRepository.LOG4J.getName(), "file:" +
            TestDeployer.getFileName(schedulingFolder.getTestConfigDir(), "/log4j/log4j-client"));

        Map<String, String> protocolSpecificOptions = protocolHelper.getClientProActiveProperties();
        properties.putAll(protocolSpecificOptions);

        return properties;
    }

    public List<String> getClientJavaOptions() {
        List<String> result = new ArrayList<String>();
        result.add("-D" + TEST_JVM_OPTION);
        return result;
    }

    protected String buildSchedulingClasspath() {
        List<String> distLibJars = TestFileUtils.listDirectoryJars(new File(schedulingFolder.getRootDir(),
            "/dist/lib").getAbsolutePath());
        List<String> addonsJars = TestFileUtils.listDirectoryJars(new File(schedulingFolder.getRootDir(),
            "/addons").getAbsolutePath());

        List<String> allJars = new ArrayList<String>(distLibJars);
        allJars.addAll(addonsJars);
        StringBuilder result = new StringBuilder();
        for (String jar : allJars) {
            result.append(jar).append(File.pathSeparatorChar);
        }

        return result.toString();
    }

}
