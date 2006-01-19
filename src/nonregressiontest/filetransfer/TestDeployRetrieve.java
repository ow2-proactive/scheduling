package nonregressiontest.filetransfer;

import java.io.File;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import testsuite.test.Assertions;
import testsuite.test.FunctionalTest;


public class TestDeployRetrieve extends FunctionalTest {
    static final long serialVersionUID = 1;
    private static Logger logger = ProActiveLogger.getLogger(
            "nonregressiontest");
    private static String XML_LOCATION = TestAPI.class.getResource(
            "/nonregressiontest/filetransfer/TestDeployRetrieve.xml").getPath();
    ProActiveDescriptor pad;
    File fileTest = new File("/tmp/ProActiveTestFile.dat");
    File fileRetrieved = new File("/tmp/ProActiveTestFileRetrieved.dat");
    File fileDeployed = new File("/tmp/ProActiveTestFileDeployed.dat");

    public TestDeployRetrieve() {
        super("File Transfer at Deployment and Retrieval Time",
            "Tests that both schems work using the ProActive FileTransfer API");
    }

    public boolean postConditions() throws Exception {
    	//depricated when using the assertions patter
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
    	
    	long fileTestSum = TestAPI.checkSum(fileTest);
    	
        if (logger.isDebugEnabled()) {
            logger.debug("Loading descriptor from: " + XML_LOCATION);
        }
        pad = ProActive.getProactiveDescriptor(XML_LOCATION);

        VirtualNode testVNode = pad.getVirtualNode("test");
        testVNode.activate();
        if(logger.isDebugEnabled()){
        	logger.debug("Getting the Node.");
        }
        Node node[]=testVNode.getNodes();
        Assertions.assertTrue(node.length > 0);

        //Checking correc FileTransferDeploy
        if(logger.isDebugEnabled()){
        	logger.debug("Checking the integrity of the test file transfer at deployment time.");
        }
        long fileDeployedSum = TestAPI.checkSum(fileDeployed);
        Assertions.assertTrue(fileTestSum == fileDeployedSum);
        
		
        //Checking correct FileTransferRetrieve
		if(logger.isDebugEnabled()){
        	logger.debug("Retrieving test files");
        }
        File file[] = testVNode.fileTransferRetrieve();
        if(logger.isDebugEnabled()){
        	logger.debug("Retrieved "+file.length+" files from VirtualNode"+testVNode.getName());
        }

        long fileRetrievedSum = TestAPI.checkSum(fileRetrieved);

        if (logger.isDebugEnabled()) {
            logger.debug("CheckSum TestFile  =" + fileTestSum);
            logger.debug("CheckSum RetrieveFile=" + fileRetrievedSum);
            logger.debug("CheckSum Deploy=" + fileDeployedSum);
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
        
        if (fileDeployed.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Deleting old randomly generated file:" +
                		fileDeployed.getName());
            }
            fileDeployed.delete();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        TestDeployRetrieve test = new TestDeployRetrieve();
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
