package nonregressiontest.filetransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.FileWrapper;

import testsuite.test.Assertions;
import testsuite.test.FunctionalTest;


public class TestAPI extends FunctionalTest {
    static final long serialVersionUID = 1;
    private static Logger logger = ProActiveLogger.getLogger(
            "nonregressiontest");
    private static String XML_LOCATION = TestAPI.class.getResource(
            "/nonregressiontest/filetransfer/TestAPI.xml").getPath();
    ProActiveDescriptor pad;
    File fileTest = new File("/tmp/ProActiveTestFile.dat");
    File filePushed = new File("/tmp/ProActiveTestPushed.dat");
    File filePulled = new File("/tmp/ProActiveTestPulled.dat");
    FileWrapper filePulledWrapper;

    public TestAPI() {
        super("File Transfer API: File Push and File Pull",
            "Tests the two main methods of the File Transfer API.");
    }

    public boolean postConditions() throws Exception {
        long fileTestSum = checkSum(fileTest);
        long filePulledSum = checkSum(filePulled);
        long filePushedSum = checkSum(filePushed);

        if (logger.isDebugEnabled()) {
            logger.debug("CheckSum TestFile  =" + fileTestSum);
            logger.debug("CheckSum PushedFile=" + filePushedSum);
            logger.debug("CheckSum PulledFile=" + filePulledSum);
        }

        return (fileTestSum == filePulledSum) &&
        (fileTestSum == filePulledSum);
    }

    public void initTest() throws Exception {
        cleanIfNecessary();

        if (logger.isDebugEnabled()) {
            logger.debug("Creating 10Mb random test file in /tmp");
        }

        //creates a new 10MB test file
        createRandomContentFile(fileTest.getAbsolutePath(), 10);
    }

    public void endTest() throws Exception {
        if (pad != null) {
            pad.killall(false);
        }

        cleanIfNecessary();
    }

    public void action() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Loading descriptor from: " + XML_LOCATION);
        }

        pad = ProActive.getProactiveDescriptor(XML_LOCATION);

        VirtualNode testVNode = pad.getVirtualNode("test");
        testVNode.activate();
        Node[] testnode = testVNode.getNodes();
        BooleanWrapper bw = org.objectweb.proactive.tools.FileTransfer.pushFile(testnode[0],
            fileTest, filePushed);
        Assertions.assertTrue(bw.booleanValue());
		
        filePulledWrapper = org.objectweb.proactive.tools.FileTransfer.pullFile(testnode[0],fileTest, filePulled);
        File pulled[]= filePulledWrapper.getFiles();
        Assertions.assertTrue(pulled.length==1);
        Assertions.assertTrue(pulled[0].equals(filePulled));
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
            if (logger.isDebugEnabled()) {
                logger.debug("Deleting old test file:" + filePushed.getName());
            }
            filePushed.delete();
        }

        if (filePulled.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Deleting old file:" + filePulled.getName());
            }
            filePulled.delete();
        }

        if (fileTest.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Deleting old randomly generated file:" +
                    fileTest.getName());
            }
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
            System.out.println("Result=" + test.postConditions());
            System.out.println("endTest");
            test.endTest();
            System.out.println("The end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
