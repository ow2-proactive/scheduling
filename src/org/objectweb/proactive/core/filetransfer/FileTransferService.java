package org.objectweb.proactive.core.filetransfer;

/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */

import org.apache.log4j.Logger;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;


/**
 * @author The ProActive Team 09/2005
 *
 */
public class FileTransferService implements Serializable,
    ProActiveInternalObject {
	protected static Logger logger = ProActiveLogger.getLogger(Loggers.FILETRANSFER);
    protected static int DEFAULT_MAX_SIMULTANEOUS_BLOCKS = 5;
    private java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    
    public FileTransferService() {
    }

    /**
     * This method will save the specified FileBlock.
     * @param fileblock
     */
    public void saveFileBlock(FileBlock fileblock)
        throws IOException {
        //remote verification
        File f = new File(fileblock.getDstFilename());
        if (f.exists() && !f.canWrite()) {
            logger.error("Can't write to: " + fileblock.getDstFilename());
            throw new IOException("Can't write to: " +
                fileblock.getDstFilename());
        }
    	//logger.debug("saveFileBlock:"+sayHello());
    	//logger.debug(fileblock.getClass());
    	
        fileblock.saveCurrentBlock();
    }

    /**
     * This method will load a FileBlock from a file, starting from the specified offset.
     * @param filename
     * @param offset
     * @return
     * @throws IOException
     */
    public FileBlock getFileBlock(String filename, long offset)
        throws IOException {
        //remote verification
        File f = new File(filename);
        if (!f.canRead()) {
            logger.error("Can't read or doesn't exist: " + filename);
            throw new IOException("Can't read or doesn't exist: " + filename);
        }

        FileBlock newBlock = new FileBlock(filename, offset);
        newBlock.loadNexBlock();
        
        return newBlock;
    }

    private File sendFile(FileTransferService ftsRemote, File srcFile,
        File dstFile) throws IOException {
    	
    	long init=System.currentTimeMillis();
    	long numBlocks=0;
    	
        FileBlock fileBlock = new FileBlock(srcFile.getAbsolutePath());
        fileBlock.setDstFilename(dstFile.getAbsolutePath());

        Boolean[] sent_blocks = new Boolean[DEFAULT_MAX_SIMULTANEOUS_BLOCKS];
        while (fileBlock.hasNextBlock()) {
            int i;
            
            //Exceptions can happen here
            for (i = 0; (i < sent_blocks.length) && fileBlock.hasNextBlock();
                    i++) {
                fileBlock.loadNexBlock();
                numBlocks++;
                ftsRemote.saveFileBlock(fileBlock); //remote (async) invocation (not yet since it's void an throws Exception)
            }
            //Wait for exceptions here
        }

        if(logger.isDebugEnabled()){
        	long fin=System.currentTimeMillis();
        	long delta = (fin-init);
        	logger.debug("File sent using "+numBlocks+" blocks,  in: "+ delta + "[ms]");
        }
        
        //TODO Here We could register this FTS AO into a Node or RunTime pool for reuse!
        return dstFile;
    }
    
    
    private File receiveFile(FileTransferService ftsRemote, File srcFile,
            File dstFile) throws IOException {
        	
        	long init=System.currentTimeMillis();
        	long numBlocks=1;
        	
        	FileBlock fileBlock= ftsRemote.getFileBlock(srcFile.getAbsolutePath(),0);
        	fileBlock.setDstFilename(dstFile.getAbsolutePath());
        	fileBlock.saveCurrentBlock();
        	long totalNumBlocks=fileBlock.getNumberOfBlocks();
        	long blockSize=fileBlock.getBlockSize();

        	FileBlock[] flyingBlocks = new FileBlock[DEFAULT_MAX_SIMULTANEOUS_BLOCKS];
            while (numBlocks < totalNumBlocks) {
                int i;
                
                for (i = 0; (i < flyingBlocks.length) && numBlocks < totalNumBlocks; i++) {
                	flyingBlocks[i]= ftsRemote.getFileBlock(srcFile.getAbsolutePath(),blockSize*numBlocks); //async
                	//fileBlock= ftsRemote.getFileBlock(srcFile.getAbsolutePath(),fileBlock.getOffset());
                    numBlocks++;
                    
                }
                
                //here we sync (wait-by-necessity)                
                for (int j = 0; j < i; j++) {
                	flyingBlocks[j].setDstFilename(dstFile.getAbsolutePath()); 
                	flyingBlocks[j].saveCurrentBlock();
                }
            }

            if(logger.isDebugEnabled()){
            	long fin=System.currentTimeMillis();
            	long delta = (fin-init);
            	logger.debug("File received using "+numBlocks+" blocks,  in: "+ delta + "[ms]");
            }
            
            //TODO Here We could register this FTS AO into a Node or RunTime pool for reuse!
            return dstFile;
        }

    public static File pullFile(Node node, File srcFile, File dstFile)
        throws IOException, ProActiveException {
        if (logger.isDebugEnabled()) {
            logger.debug("Pulling file: "+node.getNodeInformation().getHostName()+"@" + srcFile.getAbsolutePath() +
                " -> local@" + dstFile.getAbsolutePath());
        }

        //local verification
        if (dstFile.exists() && !dstFile.canWrite()) {
            logger.error("Can't write to " + dstFile.getAbsoluteFile());
            throw new IOException("Can't write to " +
                dstFile.getAbsoluteFile());
        }

        File futureFile = null;
        try {
            //TODO Possible optimization is to keep these AO in pools.
            FileTransferService ftsRemote = (FileTransferService) ProActive.newActive(FileTransferService.class.getName(),
                    null, node);

            FileTransferService ftsLocal = (FileTransferService) ProActive.newActive(FileTransferService.class.getName(),
                    null);

            if(logger.isDebugEnabled()){
            	logger.debug("Local FTS: "+ftsLocal.sayHello());
            	logger.debug("Remote FTS: "+ftsRemote.sayHello());
            }
            
            //We ask the remote AO to send the file to us
            //futureFile = ftsRemote.sendFile(ftsLocal, srcFile, dstFile); //this call is asynchronous
            futureFile = ftsLocal.receiveFile(ftsRemote, srcFile, dstFile); //alternative
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProActiveException(
                "Unable to connect or use ProActive Node: " + node);
        }

        return futureFile;
    }

    public static void pushFile(Node node, File srcFile, File dstFile)
        throws IOException, ProActiveException {
        if (logger.isDebugEnabled()) {
            logger.debug("Pushing file: local@" + srcFile.getAbsolutePath() +
                " -> "+node.getNodeInformation().getHostName()+"@" + dstFile.getAbsolutePath());
        }

        //local verification
        if (!srcFile.canRead()) {
            logger.error("Can't read or doesn't exist: " +
                srcFile.getAbsoluteFile());
            throw new IOException("Can't read or doesn't exist: " +
                srcFile.getAbsoluteFile());
        }

        try {
            //TODO Possible optimization is to keep these AO in pools.
            FileTransferService ftsRemote = (FileTransferService) ProActive.newActive(FileTransferService.class.getName(),
                    null, node);

            FileTransferService ftsLocal = (FileTransferService) ProActive.newActive(FileTransferService.class.getName(),
                    null);

            if(logger.isDebugEnabled()){
            	logger.debug("Local FTS: "+ftsLocal.sayHello());
            	logger.debug("Remote FTS: "+ftsRemote.sayHello());
            }
            //We ask the local AO to send the file to the remote AO
            ftsLocal.sendFile(ftsRemote, srcFile, dstFile); //this call is asynchronous
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProActiveException(
                "Unable to connect or use ProActive Node: " + node);
        }
    }
    
    /** The Active Object creates and returns information on its location
     * @return a StringWrapper which is a Serialized version, for asynchrony */
    public String sayHello() {
        return "Hello World from " + getHostName() + " at " +
            new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
    }

    /** finds the name of the local machine */
    static String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().toString();
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    public static void main(String[] args) throws IOException {
        String filenameSrcA = "/home/mleyton/test/Test";
        String filenameSrcB = "/home/mleyton/test/Test-out";
        String filenamePushed = "/home/mleyton/test/TestPushed.ps";
        String filenamePulled = "/home/mleyton/test/TestPulled.ps";

        File fileSrcA = new File(filenameSrcA);

        File fileSrcB = new File(filenameSrcB);

        //clean if necessary
        File filePushed = new File(filenamePushed);
        if (filePushed.exists()) {
            System.out.println("Deleting old file:" + filePushed.getName());
            filePushed.delete();
        }

        //clean if necessary
        File filePulled = new File(filenamePulled);
        if (filePulled.exists()) {
            System.out.println("Deleting old file:" + filePulled.getName());
            filePulled.delete();
        }

        try {
            ProActiveDescriptor pad = ProActive.getProactiveDescriptor(
                    "/home/mleyton/test/SimpleDescriptor.xml");

            VirtualNode testVNode = pad.getVirtualNode("test");
            testVNode.activate();
            Node[] testnode = testVNode.getNodes();

            pushFile(testnode[0], fileSrcA, filePushed);
            pullFile(testnode[0], fileSrcB, filePulled);

            pad.killall(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}
