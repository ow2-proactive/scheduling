/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package functionaltests.execremote;

import functionaltests.monitor.RMMonitorEventReceiver;
import functionaltests.utils.RMFunctionalTest;
import functionaltests.utils.RMTHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingServiceDeployer;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.utils.TargetType;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;


public final class TestExecRemote extends RMFunctionalTest {
    private static final String simpleScriptContent = "";
    private static final String erroneousSimpleScriptContent = "var a = null; a.toString();";
    private static final String selectionScriptContent = "selected = true; print(selected);";

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void action() throws Exception {
        final String miscDir = System.getProperty("pa.rm.home") + File.separator + "samples" +
            File.separator + "scripts" + File.separator + "misc" + File.separator;
        boolean isLinux = OperatingSystem.getOperatingSystem().equals(OperatingSystem.unix);
        final String valueToEcho = "111";
        String nsName = "TestExecRemote";

        rmHelper.createNodeSource(nsName);

        RMInitialState state = ((RMMonitorEventReceiver) rmHelper.getResourceManager()).getInitialState();
        String hostname = state.getNodesEvents().get(0).getHostName();
        HashSet<String> nodesUrls = new HashSet<>();
        for (RMNodeEvent ne : state.getNodesEvents()) {
            nodesUrls.add(ne.getNodeUrl());
        }

        simpleScript(nodesUrls);
        selectionScript(nodesUrls);
        processBuilderScript(miscDir, isLinux, valueToEcho, nodesUrls);
        processBuilderWithDSScript(miscDir, isLinux, valueToEcho, nodesUrls);
        scriptOnNodeSource(nsName, nodesUrls);
        scriptOnHost(hostname);
    }

    private void scriptOnNodeSource(String nsName, HashSet<String> nodesUrls) throws Exception {
        RMTHelper.log("Test 6 - Execute script on a specified nodesource name");
        SimpleScript script = new SimpleScript(TestExecRemote.simpleScriptContent, "javascript");
        HashSet<String> targets = new HashSet<>(1);
        targets.add(nsName);

        List<ScriptResult<Object>> results = rmHelper.getResourceManager().executeScript(script,
                TargetType.NODESOURCE_NAME.toString(), targets);

        assertEquals("The size of result list must equal to size of nodesource", nodesUrls.size(),
                results.size());
        for (ScriptResult<Object> res : results) {
            Throwable exception = res.getException();
            if (exception != null) {
                RMTHelper.log("An exception occured while executing the script remotely:");
                exception.printStackTrace(System.out);
            }
            assertNull("No exception must occur", exception);
        }
    }

    private void simpleScript(HashSet<String> nodesUrls) throws Exception {
        RMTHelper.log("Test 1 - Execute SimpleScript");
        SimpleScript script = new SimpleScript(TestExecRemote.erroneousSimpleScriptContent, "javascript");

        List<ScriptResult<Object>> results = rmHelper.getResourceManager().executeScript(script,
                TargetType.NODE_URL.toString(), nodesUrls);

        assertFalse("The results must not be empty", results.size() == 0);
        for (ScriptResult<Object> res : results) {
            Throwable exception = res.getException();
            assertNotNull("There should be an exception since the script is deliberately erroneous",
                    exception);
        }
    }

    private void selectionScript(HashSet<String> nodesUrls) throws Exception {
        RMTHelper.log("Test 2 - Execute SelectionScript");
        SelectionScript script = new SelectionScript(TestExecRemote.selectionScriptContent, "javascript");

        List<ScriptResult<Boolean>> results = rmHelper.getResourceManager().executeScript(script,
                TargetType.NODE_URL.toString(), nodesUrls);

        assertFalse("The results must not be empty", results.size() == 0);
        for (ScriptResult<Boolean> res : results) {
            assertTrue("The selection script must return true", res.getResult());
            String output = res.getOutput();
            assertTrue("The script output must contain the printed value", output.contains("true"));
        }
    }

    private void processBuilderScript(String miscDir, boolean isLinux, String valueToEcho,
            HashSet<String> nodesUrls) throws Exception {
        File sFile = new File(miscDir + "processBuilder.groovy");
        RMTHelper.log("Test 4 - Test " + sFile);

        String[] cmd = (isLinux) ? new String[] { "/bin/bash", "-c", "echo " + valueToEcho } : new String[] {
                "cmd.exe", "/c", "@(echo " + valueToEcho + ")" };
        SimpleScript script = new SimpleScript(sFile, cmd);

        List<ScriptResult<Object>> results = rmHelper.getResourceManager().executeScript(script,
                TargetType.NODE_URL.toString(), nodesUrls);

        assertFalse("The results must not be empty", results.size() == 0);
        for (ScriptResult<Object> res : results) {
            String output = res.getOutput();
            assertTrue("The script output must contains " + valueToEcho, output.contains(valueToEcho));
        }
    }

    private void processBuilderWithDSScript(String miscDir, boolean isLinux, String valueToEcho,
            HashSet<String> nodesUrls) throws Exception {
        File sFile = new File(miscDir + "processBuilderWithDS.groovy");
        RMTHelper.log("Test 5 - Test " + sFile);

        // Create test temporary file
        File tempDir = tmpFolder.newFolder("testExecRemote");

        String testFilename = "test.txt";
        FileUtils.write(new File(tempDir, testFilename), valueToEcho);

        // Generate the remote command execute in the remote localspace
        DSHelper dsHelper = new DSHelper();

        try {
            // Start DS
            String dsurl = dsHelper.startDS(tempDir.getAbsolutePath());
            String[] cmd = (isLinux) ? new String[] { dsurl, "/bin/cat", testFilename } : new String[] {
                    dsurl, "cmd.exe", "/c", "more", testFilename };
            // Execute the script
            SimpleScript script = new SimpleScript(sFile, cmd);
            List<ScriptResult<Object>> results = rmHelper.getResourceManager().executeScript(script,
                    TargetType.NODE_URL.toString(), nodesUrls);

            assertFalse("The results must not be empty", results.size() == 0);
            for (ScriptResult<Object> res : results) {
                Throwable exception = res.getException();
                if (exception != null) {
                    RMTHelper.log("An exception occured while executing the script remotely:");
                    exception.printStackTrace(System.out);
                }

                String output = res.getOutput();

                assertTrue("The script output must contains " + valueToEcho, output.contains(valueToEcho));
            }
        } finally {
            dsHelper.stopDS();
        }
    }

    private void scriptOnHost(String hostname) throws Exception {
        RMTHelper.log("Test 7 - Execute script with hostname as target");
        SimpleScript script = new SimpleScript(TestExecRemote.simpleScriptContent, "javascript");
        HashSet<String> targets = new HashSet<>(1);
        targets.add(hostname);

        List<ScriptResult<Object>> results = rmHelper.getResourceManager().executeScript(script,
                TargetType.HOSTNAME.toString(), targets);

        assertEquals(
                "The size of result list must equal to 1, if a hostname is specified a single node must be "
                    + "selected", results.size(), 1);
        for (ScriptResult<Object> res : results) {
            Throwable exception = res.getException();
            if (exception != null) {
                RMTHelper.log("An exception occured while executing the script remotely:");
                exception.printStackTrace(System.out);
            }
            assertNull("No exception must occur", exception);
        }
    }

    private class DSHelper {
        private NamingServiceDeployer namingServiceDeployer;
        private FileSystemServerDeployer inputDataserverDeployer;

        public String startDS(final String rootDir) throws Exception {
            // Start Naming Service
            this.namingServiceDeployer = new NamingServiceDeployer();
            NamingService localNamingService = this.namingServiceDeployer.getLocalNamingService();
            this.inputDataserverDeployer = new FileSystemServerDeployer("root", rootDir, true, true);

            InputOutputSpaceConfiguration config = InputOutputSpaceConfiguration
                    .createOutputSpaceConfiguration(this.inputDataserverDeployer.getVFSRootURL(), null, null,
                            PADataSpaces.DEFAULT_IN_OUT_NAME);

            SpaceInstanceInfo inSpaceInfo = new SpaceInstanceInfo(Long.toString(0xcafe), config);
            localNamingService.registerApplication(Long.toString(0xcafe), Collections.singleton(inSpaceInfo));
            return namingServiceDeployer.getNamingServiceURL();
        }

        public void stopDS() throws Exception {
            try {
                if (this.namingServiceDeployer != null) {
                    this.namingServiceDeployer.getLocalNamingService().unregisterApplication(Long.toString(0xcafe));
                }
            } finally {
                try {
                    if (this.namingServiceDeployer != null) {
                        this.namingServiceDeployer.terminate();
                    }
                } finally {
                    if (this.inputDataserverDeployer != null) {
                        this.inputDataserverDeployer.terminate();
                    }
                }
            }
        }
    }
}
