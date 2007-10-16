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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.filetransfer.FileTransfer;
import org.objectweb.proactive.filetransfer.FileVector;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertTrue;

/**
 * Tests the two main methods of the File Transfer API
 */
public class TestAPI extends FunctionalTest {
    static final long serialVersionUID = 1;
    private static Logger logger = ProActiveLogger.getLogger("functionalTests");
    private static String XML_LOCATION = TestAPI.class.getResource(
            "/functionalTests/filetransfer/TestAPI.xml").getPath();

    //private static String XML_LOCATION = TestAPI.class.getResource("/functionalTests/filetransfer/TestAPINotLocal.xml").getPath();
    private static int FILE_SIZE = 16; //MB
    ProActiveDescriptor pad;
    File fileTest = new File("/tmp/ProActiveTestFile.dat");
    File filePushed = new File("/tmp/ProActiveTestPushed.dat");
    File filePulled = new File("/tmp/ProActiveTestPulled.dat");
    File fileFuturePushed = new File("/tmp/ProActiveTestFuturePushed.dat");
    FileVector filePulledWrapper;

    @Before
    public void initTest() throws Exception {
        cleanIfNecessary();

        if (logger.isDebugEnabled()) {
            logger.debug("Creating " + FILE_SIZE +
                "MB random test file in /tmp");
        }

        //creates a new 10MB test file
        createRandomContentFile(fileTest.getAbsolutePath(), FILE_SIZE);
    }

    @After
    public void endTest() throws Exception {
        if (pad != null) {
            pad.killall(false);
        }

        cleanIfNecessary();
    }

    @Test
    public void action() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Loading descriptor from: " + XML_LOCATION);
        }

        pad = ProDeployment.getProactiveDescriptor(XML_LOCATION);

        VirtualNode testVNode = pad.getVirtualNode("test");
        VirtualNode testVNodePush = pad.getVirtualNode("testPush");

        testVNode.activate();
        testVNodePush.activate();

        Node[] testnode = testVNode.getNodes();
        Node[] testnodePush = testVNodePush.getNodes();

        FileVector fw = FileTransfer.pushFile(testnode[0], fileTest, filePushed);
        assertTrue(fw.getFile(0).equals(filePushed)); //wait-by-necessity

        filePulledWrapper = FileTransfer.pullFile(testnode[0], filePushed,
                filePulled);

        //Thread.sleep(1000);
        //filePulledWrapper.waitForAll(); //sync line
        //System.out.println("Finished wiating");
        FileVector pushedWhilePulling = FileTransfer.pushFile(testnodePush[0],
                filePulledWrapper, fileFuturePushed);

        assertTrue(filePulledWrapper.size() == 1);
        assertTrue(filePulledWrapper.getFile(0).equals(filePulled)); //wait-by-necessity

        assertTrue(pushedWhilePulling.size() == 1);
        assertTrue(pushedWhilePulling.getFile(0).equals(fileFuturePushed)); //wait-by-necessity

        long fileTestSum = checkSum(fileTest);
        long filePulledSum = checkSum(filePulled);
        long filePushedSum = checkSum(filePushed);
        long fileFuturePushedSum = checkSum(fileFuturePushed);

        if (logger.isDebugEnabled()) {
            logger.debug("CheckSum TestFile              =" + fileTestSum);
            logger.debug("CheckSum PushedFile            =" + filePushedSum);
            logger.debug("CheckSum PulledFile            =" + filePulledSum);
            logger.debug("CheckSum PushedFileWhilePulling=" +
                fileFuturePushedSum);
        }

        assertTrue(fileTestSum == filePushedSum);
        assertTrue(fileTestSum == filePulledSum);
        assertTrue(fileTestSum == fileFuturePushedSum);
    }

    /**
     * Gets a checksum on the specified file
     * @param file The file to be checksumed.
     * @return
     * @throws IOException
     */
    static long checkSum(File file) throws IOException {
        // Compute Adler-32 checksum
        CheckedInputStream cis = new CheckedInputStream(new FileInputStream(
                    file.getAbsoluteFile()), new Adler32());
        byte[] tempBuf = new byte[1024 * 1024]; //1MB loops
        while (cis.read(tempBuf) >= 0)
            ;

        return cis.getChecksum().getValue();
    }

    /**
     * Creates a File with random content of specified MB size.
     * @param path  The path of the File.
     * @param size  The desired size of the file in MB.
     * @return
     * @throws IOException
     */
    static void createRandomContentFile(String path, int size)
        throws IOException {
        SecureRandom psrg = new SecureRandom();
        byte[] b = new byte[1024 * 1024]; //1 * MB
        psrg.nextBytes(b);

        FileOutputStream fos = new FileOutputStream(path, false);
        for (int i = 0; i < size; i++) //size times

            fos.write(b, 0, b.length); //not really random, but good enough for this test
        fos.flush();
        fos.close();
    }

    /**
     * Cleans test files
     *
     */
    private void cleanIfNecessary() {
        if (filePushed.exists()) {
            filePushed.delete();
        }

        if (filePulled.exists()) {
            filePulled.delete();
        }

        if (fileFuturePushed.exists()) {
            fileFuturePushed.delete();
        }

        if (fileTest.exists()) {
            fileTest.delete();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        TestAPI test = new TestAPI();
        try {
            System.out.println("InitTest");
            test.initTest();
            System.out.println("Action");
            test.action();
            System.out.println("postConditions");
            System.out.println("endTest");
            test.endTest();
            System.out.println("The end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
