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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package nonregressiontest.filetransfer;

import java.io.File;
import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.VariableContract;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.filetransfer.FileVector;

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
    File fileRetrieved2 = new File("/tmp/ProActiveTestFileRetrieved2.dat");
    File fileDeployed2 = new File("/tmp/ProActiveTestFileDeployed2.dat");
    static int testblocksize= org.objectweb.proactive.core.filetransfer.FileBlock.DEFAULT_BLOCK_SIZE;
    static int testflyingblocks=org.objectweb.proactive.core.filetransfer.FileTransferService.DEFAULT_MAX_SIMULTANEOUS_BLOCKS;
    static int filesize=2;
    
    
    //Descriptor variables
    String jvmProcess = "localJVM";
    String hostName = "localhost";
    
    public TestDeployRetrieve() {
        super("File Transfer at Deployment and Retrieval Time",
            "Tests that both schems work using the ProActive FileTransfer API");
    }

    @Override
	public boolean postConditions() throws Exception {
    	//depricated when using the assertions patter
        return true;
    }

    @Override
	public void initTest() throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("Creating "+filesize+"Mb random test file in /tmp");
        }

        //creates a new 2MB test file
        TestAPI.createRandomContentFile(fileTest.getAbsolutePath(), filesize);
        
        try {
        	hostName= java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            hostName= "localhost";
        }
        
    }

    @Override
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

    @Override
	public void action() throws Exception {
    	
    	long fileTestSum = TestAPI.checkSum(fileTest);
    	
        if (logger.isDebugEnabled()) {
            logger.debug("Loading descriptor from: " + XML_LOCATION);
        }
        
        // We save the current state of the schema validation and set it to false for this example
        String validatingProperyOld = System.getProperty("schema.validation");
        System.setProperty("schema.validation", "false");
        
        VariableContract vc = new VariableContract();
        vc.setVariableFromProgram("JVM_PROCESS", jvmProcess ,VariableContractType.DescriptorDefaultVariable);
        vc.setVariableFromProgram("HOST_NAME", hostName ,VariableContractType.DescriptorDefaultVariable);
    
        pad = ProActive.getProactiveDescriptor(XML_LOCATION, vc);
        
        // we restore the old state of the schema validation
        System.setProperty("schema.validation", validatingProperyOld);

        VirtualNode testVNode = pad.getVirtualNode("test");
        testVNode.setFileTransferParams(testblocksize,testflyingblocks);
        long initDeployment=System.currentTimeMillis();
        testVNode.activate();
        if(logger.isDebugEnabled()){
        	logger.debug("Getting the Node.");
        }
        
        Node node[]=testVNode.getNodes();
        long finitDeployment=System.currentTimeMillis();
        
        Assertions.assertTrue(node.length > 0);
        if(logger.isDebugEnabled()){
        	logger.debug("Deployed "+node.length+" node from VirtualNode "+testVNode.getName()+" in "+(finitDeployment-initDeployment)+"[ms]");
        }


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
		long initRetrieve=System.currentTimeMillis();
        FileVector fileVector = testVNode.fileTransferRetrieve(); //async
        fileVector.waitForAll(); //sync here
        long finitRetrieve=System.currentTimeMillis();
        
        if(logger.isDebugEnabled()){
        	logger.debug("Retrieved "+fileVector.size()+" files from VirtualNode "+testVNode.getName()+" in "+(finitRetrieve-initRetrieve)+"[ms]");
        }
        
        Assertions.assertTrue(fileVector.size()==2);
        
        fileRetrieved = new File(fileRetrieved.getAbsoluteFile()+"-"+node[0].getNodeInformation().getName());
        fileRetrieved2 = new File(fileRetrieved2.getAbsoluteFile()+"-"+node[0].getNodeInformation().getName());
        
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
    private void cleanIfNecessary(File f) {
    	if (f.exists()) {
	        if (logger.isDebugEnabled()) {
	            logger.debug("Deleting old randomly generated file:" +
	            		f.getName());
	        }
	        f.delete();
    	}
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
    	
      	if(args.length==4){
    		filesize=Integer.parseInt(args[0]);
    		testblocksize=Integer.parseInt(args[1]);
    		testflyingblocks=Integer.parseInt(args[2]);
    		XML_LOCATION=args[3];
    	}
    	else if(args.length !=0){
    		System.out.println("Use with arguments: filesize[mb] fileblocksize[bytes] maxflyingblocks xmldescriptorpath");
    	}
      	
        TestDeployRetrieve test = new TestDeployRetrieve();
        test.jvmProcess="remoteJVM";
        
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
