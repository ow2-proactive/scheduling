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
package functionaltests.execremote;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingServiceDeployer;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.nodesource.NodeSource;
import org.ow2.proactive.resourcemanager.utils.TargetType;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.tests.FunctionalTest;

import functionaltests.RMTHelper;


public final class TestExecRemote extends FunctionalTest {
    private static final String simpleScriptContent = "";
    private static final String erroneousSimpleScriptContent = "var a = null; a.toString();";
    private static final String selectionScriptContent = "selected = true; print(selected);";

    //@org.junit.Ignore("SCHEDULING-1587")
    @org.junit.Test
    public void action() throws Exception {
        try {
            internalAction();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void internalAction() throws Exception {
        final String miscDir = System.getProperty("pa.rm.home") + File.separator + "samples" +
            File.separator + "scripts" + File.separator + "misc" + File.separator;
        boolean isLinux = System.getProperty("os.name").toLowerCase().startsWith("linux") ||
            System.getProperty("os.name").toLowerCase().startsWith("mac");
        final String valueToEcho = "111";

//        RMTHelper.defaultNodesNumber = 1;
        RMTHelper.createLocalNodeSource();
        RMTHelper
                .waitForNodeSourceEvent(RMEventType.NODESOURCE_CREATED, NodeSource.LOCAL_INFRASTRUCTURE_NAME);

        // Wait until all nodes are added and free
        HashSet<String> nodesUrls = new HashSet<String>();
        String hostname = null;
        for (int i = 0; i < RMTHelper.defaultNodesNumber; i++) {
            RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_ADDED);
            RMNodeEvent nodeEvent = RMTHelper.waitForAnyNodeEvent(RMEventType.NODE_STATE_CHANGED);
            nodesUrls.add(nodeEvent.getNodeUrl());
            hostname = nodeEvent.getHostName();
        }
        {
            RMTHelper.log("Test 1 - Execute SimpleScript");
            SimpleScript script = new SimpleScript(TestExecRemote.erroneousSimpleScriptContent, "javascript");
            List<ScriptResult<Object>> results = RMTHelper.getResourceManager().executeScript(script,
                    TargetType.NODE_URL.toString(), nodesUrls);
            Assert.assertNotNull("The list of results must not be null", results);
            Assert.assertFalse("The results must not be empty", results.size() == 0);
            for (ScriptResult<Object> res : results) {
                Throwable exception = res.getException();
                Assert.assertNotNull(
                        "There should be an exception since the script is deliberately erroneous", exception);
            }
        }
        {
            RMTHelper.log("Test 2 - Execute SelectionScript");
            SelectionScript script = new SelectionScript(TestExecRemote.selectionScriptContent, "javascript");
            List<ScriptResult<Boolean>> results = RMTHelper.getResourceManager().executeScript(script,
                    TargetType.NODE_URL.toString(), nodesUrls);
            Assert.assertNotNull("The list of results must not be null", results);
            Assert.assertFalse("The results must not be empty", results.size() == 0);
            for (ScriptResult<Boolean> res : results) {
                Assert.assertTrue("The selection script must return true", res.getResult());
                String output = res.getOutput();
                Assert.assertTrue("The script output must contain the printed value",
                        TestExecRemote.selectionScriptContent.contains(output));
            }
        }
        {
            File sFile = new File(miscDir + "processBuilder.js");
            RMTHelper.log("Test 4 - Test " + sFile);

            String[] cmd = (isLinux) ? new String[] { "/bin/bash", "-c", "echo " + valueToEcho }
                    : new String[] { "cmd.exe", "/c", "@(echo " + valueToEcho + ")" };
            SimpleScript script = new SimpleScript(sFile, cmd);
            List<ScriptResult<Object>> results = RMTHelper.getResourceManager().executeScript(script,
                    TargetType.NODE_URL.toString(), nodesUrls);
            Assert.assertNotNull("The list of results must not be null", results);
            Assert.assertFalse("The results must not be empty", results.size() == 0);
            for (ScriptResult<Object> res : results) {
                String output = res.getOutput();
                Assert.assertTrue("The script output must contains " + valueToEcho, output
                        .contains(valueToEcho));
            }
        }
        {
            File sFile = new File(miscDir + "processBuilderWithDS.js");
            RMTHelper.log("Test 5 - Test " + sFile);
            // Create test temporary file
            String testFilename = "test.txt";
            File tempDir = new File("testExecRemote");
            tempDir.mkdir();
            tempDir.deleteOnExit();
            File testFile = new File(tempDir, testFilename);
            testFile.createNewFile();
            testFile.deleteOnExit();
            // Write a string into the file that will be the output of the script result        				
            BufferedWriter out = new BufferedWriter(new FileWriter(testFile));
            out.write(valueToEcho);
            out.close();
            // Generate the remote command execute in the remote localspace
            DSHelper dsHelper = new DSHelper();
            try {
                // Start DS				
                String dsurl = dsHelper.startDS(tempDir.getAbsolutePath());
                String[] cmd = (isLinux) ? new String[] { dsurl, "/bin/bash", "-c", "more " + testFilename }
                        : new String[] { dsurl, "cmd.exe", "/c", "more", testFilename };
                // Execute the script
                SimpleScript script = new SimpleScript(sFile, cmd);
                List<ScriptResult<Object>> results = RMTHelper.getResourceManager().executeScript(script,
                        TargetType.NODE_URL.toString(), nodesUrls);
                Assert.assertNotNull("The list of results must not be null", results);
                Assert.assertFalse("The results must not be empty", results.size() == 0);
                for (ScriptResult<Object> res : results) {
                    Throwable exception = res.getException();
                    if (exception != null) {
                        RMTHelper.log("An exception occured while executing the script remotely:");
                        exception.printStackTrace(System.out);
                    }

                    String output = res.getOutput();

                    Assert.assertNotNull("Output must not be null", output);
                    Assert.assertTrue("The script output must contains " + valueToEcho, output
                            .contains(valueToEcho));
                }
            } finally {
                dsHelper.stopDS();
                tempDir.deleteOnExit();
            }
        }
        {
            RMTHelper.log("Test 6 - Execute script on a specified nodesource name");
            SimpleScript script = new SimpleScript(TestExecRemote.simpleScriptContent, "javascript");
            HashSet<String> targets = new HashSet<String>(1);
            targets.add(NodeSource.LOCAL_INFRASTRUCTURE_NAME);
            List<ScriptResult<Object>> results = RMTHelper.getResourceManager().executeScript(script,
                    TargetType.NODESOURCE_NAME.toString(), targets);
            Assert.assertNotNull("The list of results must not be null", results);
            Assert.assertEquals("The size of result list must equal to size of nodesource", results.size(),
                    RMTHelper.defaultNodesNumber);
            for (ScriptResult<Object> res : results) {
                Throwable exception = res.getException();
                if (exception != null) {
                    RMTHelper.log("An exception occured while executing the script remotely:");
                    exception.printStackTrace(System.out);
                }
                Assert.assertNull("No exception must occur", exception);
            }
        }
        {
            RMTHelper.log("Test 7 - Execute script with hostname as target");
            SimpleScript script = new SimpleScript(TestExecRemote.simpleScriptContent, "javascript");
            HashSet<String> targets = new HashSet<String>(1);
            targets.add(hostname);
            List<ScriptResult<Object>> results = RMTHelper.getResourceManager().executeScript(script,
                    TargetType.HOSTNAME.toString(), targets);
            Assert.assertNotNull("The list of results must not be null", results);
            Assert
                    .assertEquals(
                            "The size of result list must equal to 1, if a hostname is specified a single node must be selected",
                            results.size(), 1);
            for (ScriptResult<Object> res : results) {
                Throwable exception = res.getException();
                if (exception != null) {
                    RMTHelper.log("An exception occured while executing the script remotely:");
                    exception.printStackTrace(System.out);
                }
                Assert.assertNull("No exception must occur", exception);
            }
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

            SpaceInstanceInfo inSpaceInfo = new SpaceInstanceInfo(0xcafe, config);
            localNamingService.registerApplication(0xcafe, Collections.singleton(inSpaceInfo));
            return namingServiceDeployer.getNamingServiceURL();
        }

        public void stopDS() throws Exception {
            try {
                if (this.namingServiceDeployer != null) {
                    this.namingServiceDeployer.getLocalNamingService().unregisterApplication(0xcafe);
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
