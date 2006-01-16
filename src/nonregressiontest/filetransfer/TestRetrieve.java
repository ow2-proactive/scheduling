package nonregressiontest.filetransfer;

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import testsuite.test.Assertions;
import testsuite.test.FunctionalTest;


public class TestRetrieve extends FunctionalTest {
    static final long serialVersionUID = 1;
    private static Logger logger = ProActiveLogger.getLogger(
            "nonregressiontest");
    private static String XML_LOCATION = TestAPI.class.getResource(
            "/nonregressiontest/filetransfer/TestRetrieve.xml").getPath();
    ProActiveDescriptor pad;
    File fileTest = new File("/tmp/ProActiveTestFile.dat");
    File fileRetrieved = new File("/tmp/ProActiveTestFileRetrieved.dat");

    public TestRetrieve() {
        super("File Transfer API: File Push and File Pull",
            "Tests the two main methods of the File Transfer API.");
    }

    public boolean postConditions() throws Exception {
        return true;
    }

    public void initTest() throws Exception {
        cleanIfNecessary();

        if (logger.isDebugEnabled()) {
            logger.debug("Creating 2Mb random test file in /tmp");
        }

        //creates a new 2MB test file
        TestAPI.createRandomContentFile(fileTest.getAbsolutePath(), 2);
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
        File file[] = testVNode.fileTransferRetrieve();
        
        if(logger.isDebugEnabled()){
        	logger.debug("Retrieved "+file.length+" files from VirtualNode"+testVNode.getName());
        }

        long fileTestSum = TestAPI.checkSum(fileTest);
        long fileRetrievedSum = TestAPI.checkSum(fileRetrieved);

        if (logger.isDebugEnabled()) {
            logger.debug("CheckSum TestFile  =" + fileTestSum);
            logger.debug("CheckSum RetrieveFile=" + fileRetrievedSum);
        }

        Assertions.assertTrue(fileTestSum == fileRetrievedSum);
    }

    /**
     * Cleans test files

     */
    private void cleanIfNecessary() {
        if (fileRetrieved.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Deleting old test file:" + fileRetrieved.getName());
            }
            fileRetrieved.delete();
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
        TestRetrieve test = new TestRetrieve();
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
