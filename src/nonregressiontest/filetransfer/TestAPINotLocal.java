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

import testsuite.test.FunctionalTest;


public class TestAPINotLocal extends FunctionalTest {
    static final long serialVersionUID = 1;
    private static Logger logger = ProActiveLogger.getLogger(
            "nonregressiontest");
    static String XML_LOCATION = TestAPINotLocal.class.getResource(
            "/nonregressiontest/filetransfer/TestAPINotLocal.xml").getPath();
    ProActiveDescriptor pad;
    File fileTest = new File("/tmp/ProActiveTestFile.dat");
    File filePushed = new File("/tmp/ProActiveTestPushed.dat");
    File filePulled = new File("/tmp/ProActiveTestPulled.dat");
    
    static int testblocksize= org.objectweb.proactive.core.filetransfer.FileBlock.DEFAULT_BLOCK_SIZE;
    static int testflyingblocks=org.objectweb.proactive.core.filetransfer.FileTransferService.DEFAULT_MAX_SIMULTANEOUS_BLOCKS;
    static int filesize=10;
    public TestAPINotLocal() {
        super("File Transfer API Not Locally: File Push and File Pull",
            "Tests the two main methods of the File Transfer API between different machines.");
    }

    public boolean postConditions() throws Exception {
        long fileTestSum = TestAPI.checkSum(fileTest);
        long filePulledSum = TestAPI.checkSum(filePulled);

        if (logger.isDebugEnabled()) {
            logger.debug("CheckSum TestFile  =" + fileTestSum);
            logger.debug("CheckSum PulledFile=" + filePulledSum);
        }

        return (fileTestSum == filePulledSum);
     }

    public void initTest() throws Exception {
        cleanIfNecessary();

        if (logger.isDebugEnabled()) {
            logger.debug("Creating "+filesize+"Mb random test file in /tmp");
        }

        //creates a new 10MB test file
        TestAPI.createRandomContentFile(fileTest.getAbsolutePath(), filesize);
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

        //BooleanWrapper bw =org.objectweb.proactive.tools.FileTransfer.pushFile(testnode[0], fileTest, filePushed);
        BooleanWrapper bw =org.objectweb.proactive.core.filetransfer.FileTransferService.pushFile(testnode[0], fileTest, filePushed,testblocksize,testflyingblocks);
        bw.booleanValue(); //sync by wait-by-neccessity
        
        //filePulled = org.objectweb.proactive.tools.FileTransfer.pullFile(testnode[0],filePushed, filePulled);
        FileWrapper fw = org.objectweb.proactive.core.filetransfer.FileTransferService.pullFile(testnode[0],filePushed, filePulled,testblocksize,testflyingblocks);
        File f[] = fw.getFiles(); //wait-by-neccessity
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
    public static void main(String[] args) throws Exception{
    	
    	if(args.length==4){
    		filesize=Integer.parseInt(args[0]);
    		testblocksize=Integer.parseInt(args[1]);
    		testflyingblocks=Integer.parseInt(args[2]);
    		XML_LOCATION=args[3];
    	}
    	else if(args.length !=0){
    		System.out.println("Use with arguments: filesize[mb] fileblocksize[bytes] maxflyingblocks xmldescriptorpath");
    	}
    	
    	TestAPINotLocal test = new TestAPINotLocal();
    	
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
        
        System.exit(0);
    }
}
