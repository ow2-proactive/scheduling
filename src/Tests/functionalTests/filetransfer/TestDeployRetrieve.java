/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.filetransfer;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.filetransfer.RemoteFile;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;


/**
 * Tests that both schemes work using the ProActive FileTransfer API
 */
public class TestDeployRetrieve extends FunctionalTest {
    private static Logger logger = ProActiveLogger.getLogger("functionalTests");
    private static String XML_LOCATION = TestAPI.class.getResource(
            "/functionalTests/filetransfer/TestDeployRetrieve.xml").getPath();
    ProActiveDescriptor pad;
    File fileTest = new File("/tmp/ProActiveTestFile.dat");
    File fileRetrieved = new File("/tmp/ProActiveTestFileRetrieved.dat");
    File fileDeployed = new File("/tmp/ProActiveTestFileDeployed.dat");
    File fileRetrieved2 = new File("/tmp/ProActiveTestFileRetrieved2.dat");
    File fileDeployed2 = new File("/tmp/ProActiveTestFileDeployed2.dat");
    static int testblocksize = org.objectweb.proactive.core.filetransfer.FileBlock.DEFAULT_BLOCK_SIZE;
    static int testflyingblocks = org.objectweb.proactive.core.filetransfer.FileTransferService.DEFAULT_MAX_SIMULTANEOUS_BLOCKS;
    static int filesize = 2;

    //Descriptor variables
    String jvmProcess = "localJVM";
    String hostName = "localhost";

    @Before
    public void initTest() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating " + filesize + "Mb random test file in /tmp");
        }

        //creates a new 2MB test file
        TestAPI.createRandomContentFile(fileTest, filesize);

        try {
            hostName = ProActiveInet.getInstance().getInetAddress().getHostName();
        } catch (Exception e) {
            hostName = "localhost";
        }
    }

    @After
    public void endTest() throws Exception {
        if (pad != null) {
            pad.killall(false);
        }

        cleanIfNecessary(this.fileTest);
        cleanIfNecessary(this.fileDeployed);
        cleanIfNecessary(this.fileDeployed2);
        cleanIfNecessary(this.fileRetrieved2);
        cleanIfNecessary(this.fileRetrieved);
    }

    @Test
    public void action() throws Exception {
        long fileTestSum = TestAPI.checkSum(fileTest);

        if (logger.isDebugEnabled()) {
            logger.debug("Loading descriptor from: " + XML_LOCATION);
        }

        // We save the current state of the schema validation and set it to false for this example
        String validatingProperyOld = PAProperties.SCHEMA_VALIDATION.getValue();
        System.setProperty("schema.validation", "false");

        VariableContract vc = new VariableContract();
        vc.setVariableFromProgram("HOST_NAME", hostName, VariableContractType.DescriptorDefaultVariable);

        pad = PADeployment.getProactiveDescriptor(XML_LOCATION, vc);

        // we restore the old state of the schema validation
        System.setProperty("schema.validation", validatingProperyOld);

        VirtualNode testVNode = pad.getVirtualNode("test");
        long initDeployment = System.currentTimeMillis();
        testVNode.activate();
        if (logger.isDebugEnabled()) {
            logger.debug("Getting the Node.");
        }

        Node[] node = testVNode.getNodes();
        long finitDeployment = System.currentTimeMillis();

        assertTrue(node.length > 0);
        if (logger.isDebugEnabled()) {
            logger.debug("Deployed " + node.length + " node from GCMVirtualNode " + testVNode.getName() +
                " in " + (finitDeployment - initDeployment) + "[ms]");
        }

        //Checking correct FileTransferDeploy
        if (logger.isDebugEnabled()) {
            logger.debug("Checking the integrity of the test file transfer at deployment time.");
        }
        long fileDeployedSum = TestAPI.checkSum(fileDeployed);
        assertTrue(fileTestSum == fileDeployedSum);

        //Checking correct FileTransferRetrieve
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving test files");
        }
        long initRetrieve = System.currentTimeMillis();

        List<RemoteFile> list = testVNode.getVirtualNodeInternal().fileTransferRetrieve(); //async
        for (RemoteFile rfile : list) {
            rfile.waitFor(); //sync here
        }

        long finitRetrieve = System.currentTimeMillis();

        if (logger.isDebugEnabled()) {
            logger.debug("Retrieved " + list.size() + " files from GCMVirtualNode " + testVNode.getName() +
                " in " + (finitRetrieve - initRetrieve) + "[ms]");
        }

        assertTrue(list.size() == 2);

        fileRetrieved = new File(fileRetrieved.getAbsoluteFile() + "-" +
            node[0].getNodeInformation().getName());
        fileRetrieved2 = new File(fileRetrieved2.getAbsoluteFile() + "-" +
            node[0].getNodeInformation().getName());

        long fileRetrievedSum = TestAPI.checkSum(fileRetrieved);

        if (logger.isDebugEnabled()) {
            logger.debug("CheckSum TestFile  =" + fileTestSum);
            logger.debug("CheckSum RetrieveFile=" + fileRetrievedSum);
            logger.debug("CheckSum Deploy=" + fileDeployedSum);
        }

        assertTrue(fileTestSum == fileRetrievedSum);
    }

    /**
     * Cleans test files
     */
    private void cleanIfNecessary(File f) {
        if (f.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Deleting old randomly generated file:" + f.getName());
            }
            f.delete();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 4) {
            filesize = Integer.parseInt(args[0]);
            testblocksize = Integer.parseInt(args[1]);
            testflyingblocks = Integer.parseInt(args[2]);
            XML_LOCATION = args[3];
        } else if (args.length != 0) {
            System.out
                    .println("Use with arguments: filesize[mb] fileblocksize[bytes] maxflyingblocks xmldescriptorpath");
        }

        TestDeployRetrieve test = new TestDeployRetrieve();
        test.jvmProcess = "remoteJVM";

        try {
            System.out.println("InitTest");
            test.initTest();
            System.out.println("Action");
            test.action();
            System.out.println("endTest");
            test.endTest();
            System.out.println("The end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
