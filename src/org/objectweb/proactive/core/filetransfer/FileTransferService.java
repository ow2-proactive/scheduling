package org.objectweb.proactive.core.filetransfer;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

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
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.FileWrapper;


/**
 * @author The ProActive Team 09/2005
 *
 */
public class FileTransferService implements Serializable,
    ProActiveInternalObject {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FILETRANSFER);
    public final static int DEFAULT_MAX_SIMULTANEOUS_BLOCKS = 8;
    private java.text.DateFormat dateFormat = new java.text.SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss");

    public FileTransferService() {
    }

    /**
     * This method will save the specified FileBlock.
     * @param fileblock
     */
    public void saveFileBlock(FileBlock fileblock) throws IOException {
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

    public void saveFileBlockWithoutThrowingException(FileBlock fileblock) {
        try {
            saveFileBlock(fileblock);
        } catch (IOException e) {
        }
    }

    /**
     * This method will load a FileBlock from a file, starting from the specified offset.
     * @param filename
     * @param offset
     * @return
     * @throws IOException
     */
    public FileBlock getFileBlock(String filename, long offset, int bsize)
        throws IOException {
        //remote verification
        File f = new File(filename);
        if (!f.canRead()) {
            logger.error("Can't read or doesn't exist: " + filename);
            throw new IOException("Can't read or doesn't exist: " + filename);
        }

        FileBlock newBlock = new FileBlock(filename, offset, bsize);
        newBlock.loadNexBlock();

        return newBlock;
    }

    public BooleanWrapper sendFiles(FileTransferService ftsRemote,
        File[] srcFile, File[] dstFile, int bsize, int numFlyingBlocks) {
        if (srcFile.length != dstFile.length) {
            logger.error(
                "Error, destination and source file lists do not match in length");
            return new BooleanWrapper(false);
        }
        String remoteHostName="";
        if (logger.isDebugEnabled()){
        	remoteHostName=ftsRemote.getHostName();
        }
        
        BooleanWrapper bw;
        for (int i = 0; i < srcFile.length; i++) {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending file: local@" +
                    srcFile[i].getAbsolutePath() + " -> " +
                    remoteHostName + "@" +
                    dstFile[i].getAbsolutePath());
            }

            bw = sendFile(ftsRemote, srcFile[i], dstFile[i], bsize,
                    numFlyingBlocks);
            //sync, only send one file at a time    		
            if (!bw.booleanValue()) {
                return new BooleanWrapper(false);
            }
        }

        return new BooleanWrapper(true);
    }

    private BooleanWrapper sendFile(FileTransferService ftsRemote,
        File srcFile, File dstFile, int bsize, int numFlyingBlocks) {
        long init = System.currentTimeMillis();
        long numBlocks = 0;

        FileBlock fileBlock = new FileBlock(srcFile.getAbsolutePath(), 0, bsize);
        fileBlock.setDstFilename(dstFile.getAbsolutePath());
        long totalNumBlocks = fileBlock.getNumberOfBlocks();

        if (totalNumBlocks == 0) {
            logger.error("Can not send an empty File:" +
                srcFile.getAbsolutePath());
            return new BooleanWrapper(false);
        }

        while (numBlocks < (totalNumBlocks - 1)) {
            //Exceptions can happen here
            try {
                for (int i = 0;
                        (i < numFlyingBlocks) &&
                        (numBlocks < (totalNumBlocks - 1)); i++) {
                    fileBlock.loadNexBlock();
                    //logger.debug("Num Block: "+numBlocks+"/"+fileBlock.getNumberOfBlocks()+" offset="+fileBlock.getOffset());
                    if (i == (numFlyingBlocks - 1)) { //rendevouz the burst, so the remote AO will not be clogged
                        ftsRemote.saveFileBlock(fileBlock);
                    } else { //simply burst
                        ftsRemote.saveFileBlockWithoutThrowingException(fileBlock); //remote (async) invocation
                    }
                    numBlocks++;
                }
            } catch (IOException ioe) {
                logger.error("Can not send File to:" + ftsRemote.getHostName());
                logger.error(ioe.getMessage());
                return new BooleanWrapper(false);
            }
        }

        //Handle a rendevous with last block here!
        try {
            fileBlock.loadNexBlock();
            ftsRemote.saveFileBlock(fileBlock);
            numBlocks++;
        } catch (IOException e) {
            logger.error("Can not send File to:" + ftsRemote.getHostName());
            logger.error(e.getMessage());
            return new BooleanWrapper(false);
        }

        if (logger.isDebugEnabled()) {
            long fin = System.currentTimeMillis();
            long delta = (fin - init);
            logger.debug("File sent using " + numBlocks + " blocks,  in: " +
                delta + "[ms]");
        }

        //TODO Here We could register this FTS AO into a Node or RunTime pool for reuse!
        return new BooleanWrapper(true);
    }

    public FileWrapper receiveFiles(FileTransferService ftsRemote,
        File[] srcFile, File[] dstFile, int bsize, int numFlyingBlocks) {
        if (srcFile.length != dstFile.length) {
            logger.error(
                "Error, destination and source file lists do not match in length");
        }

        String remoteHostName="";
        if (logger.isDebugEnabled()){
        	remoteHostName=ftsRemote.getHostName();
        }
        
        FileWrapper fw = new FileWrapper();
        for (int i = 0; i < srcFile.length; i++) {
            if (logger.isDebugEnabled()) {
                logger.debug("Receiving file: " +
                	remoteHostName + "@" +
                    srcFile[i].getAbsolutePath() + " -> local@" +
                    dstFile[i].getAbsolutePath());
            }
            
            //sync, same thread execute
            File f = receiveFile(ftsRemote, srcFile[i], dstFile[i],
                    bsize, numFlyingBlocks);

            //ProActive.waitFor(f);
            fw.addFile(f);
        }
        return fw;
    }

    private File receiveFile(FileTransferService ftsRemote, File srcFile,
        File dstFile, int bsize, int numFlyingBlocks) {
        long init = System.currentTimeMillis();
        long numBlocks = 0;

        FileBlock fileBlock;
        //FileWrapper fileWrapper = new FileWrapper();
        //fileWrapper.addFile(dstFile);

        try {
            fileBlock = ftsRemote.getFileBlock(srcFile.getAbsolutePath(), 0,
                    bsize);
            numBlocks++;
        } catch (IOException e) {
            logger.error("Error, unable to get File:" +
                srcFile.getAbsolutePath() + " from " + ftsRemote.getHostName());
            //return fileWrapper;
            return dstFile;
        }

        fileBlock.setDstFilename(dstFile.getAbsolutePath());
        fileBlock.saveCurrentBlock();
        long totalNumBlocks = fileBlock.getNumberOfBlocks();
        long blockSize = fileBlock.getBlockSize();

        FileBlock[] flyingBlocks = new FileBlock[numFlyingBlocks];
        while (numBlocks < totalNumBlocks) {
            int i;

            ProActive.tryWithCatch(IOException.class);
            try {
                for (i = 0;
                        (i < flyingBlocks.length) &&
                        (numBlocks < totalNumBlocks); i++) {
                    flyingBlocks[i] = ftsRemote.getFileBlock(srcFile.getAbsolutePath(),
                            blockSize * numBlocks, bsize); //async call
                    numBlocks++;
                }
            } catch (IOException e) {
                logger.error("Error, unable to get File:" +
                    srcFile.getAbsolutePath() + " from " +
                    ftsRemote.getHostName());
//              return fileWrapper;
                return dstFile;
            } finally {
                ProActive.removeTryWithCatch(); //Wait for exceptions here
            }

            //here we sync (wait-by-necessity)                
            for (int j = 0; j < i; j++) {
                flyingBlocks[j].setDstFilename(dstFile.getAbsolutePath());
                flyingBlocks[j].saveCurrentBlock();
            }
        }

        if (logger.isDebugEnabled()) {
            long fin = System.currentTimeMillis();
            long delta = (fin - init);
            logger.debug("File received using " + numBlocks + " blocks,  in: " +
                delta + "[ms]");
        }

        //TODO Here We could register this FTS AO into a Node or RunTime pool for reuse!
        //return fileWrapper;
        return dstFile;
    }

    public static FileWrapper pullFile(Node node, File srcFile, File dstFile)
        throws IOException, ProActiveException {
        return pullFile(node, srcFile, dstFile, FileBlock.DEFAULT_BLOCK_SIZE,
            DEFAULT_MAX_SIMULTANEOUS_BLOCKS);
    }

    public static FileWrapper pullFile(Node node, File srcFile, File dstFile,
        int bsize, int numFlyingBlocks) throws IOException, ProActiveException {
        File[] src = new File[1];
        File[] dst = new File[1];
        src[0] = srcFile;
        dst[0] = dstFile;

        return pullFiles(node, src, dst, bsize, numFlyingBlocks);
    }

    public static FileWrapper pullFiles(Node node, File[] srcFile,
        File[] dstFile) throws IOException, ProActiveException {
        return pullFiles(node, srcFile, dstFile, FileBlock.DEFAULT_BLOCK_SIZE,
            DEFAULT_MAX_SIMULTANEOUS_BLOCKS);
    }

    public static FileWrapper pullFiles(Node node, File[] srcFile,
        File[] dstFile, int bsize, int numFlyingBlocks)
        throws IOException, ProActiveException {
    	
        if (srcFile.length != dstFile.length) {
            throw new ProActiveException(
                "Error, destination and source file lists do not match in length");
        }
    	if(srcFile.length==0) return new FileWrapper();
    	
        for (int i = 0; i < srcFile.length; i++) {

            //local verification
            if (dstFile[i].exists() && !dstFile[i].canWrite()) {
                logger.error("Can't write to " + dstFile[i].getAbsoluteFile());
                throw new IOException("Can't write to " +
                    dstFile[i].getAbsoluteFile());
            }
        }
        try {
            //TODO Possible optimization is to keep these AO in pools.
            FileTransferService ftsRemote = (FileTransferService) ProActive.newActive(FileTransferService.class.getName(),
                    null, node);

            FileTransferService ftsLocal = (FileTransferService) ProActive.newActive(FileTransferService.class.getName(),
                    null);

            //We ask the remote AO to send the file to us
            //futureFile = ftsRemote.sendFiles(ftsLocal, srcFile, dstFile, bsizem, numFlyingBlocks); 
            return ftsLocal.receiveFiles(ftsRemote, srcFile, dstFile, bsize,
                numFlyingBlocks);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProActiveException(
                "Unable to connect or use ProActive Node: " + node);
        }
    }

    public static BooleanWrapper pushFile(Node node, File srcFile, File dstFile)
        throws IOException, ProActiveException {
        return pushFile(node, srcFile, dstFile, FileBlock.DEFAULT_BLOCK_SIZE,
            DEFAULT_MAX_SIMULTANEOUS_BLOCKS);
    }

    public static BooleanWrapper pushFile(Node node, File srcFile,
        File dstFile, int bsize, int numFlyingBlocks)
        throws IOException, ProActiveException {
        File[] src = new File[1];
        File[] dst = new File[1];
        src[0] = srcFile;
        dst[0] = dstFile;

        return pushFiles(node, src, dst, bsize, numFlyingBlocks);
    }

    public static BooleanWrapper pushFiles(Node node, File[] srcFile,
        File[] dstFile) throws IOException, ProActiveException {
        return pushFiles(node, srcFile, dstFile, FileBlock.DEFAULT_BLOCK_SIZE,
            DEFAULT_MAX_SIMULTANEOUS_BLOCKS);
    }

    public static BooleanWrapper pushFiles(Node node, File[] srcFile,
        File[] dstFile, int bsize, int numFlyingBlocks)
        throws IOException, ProActiveException {
        if (srcFile.length != dstFile.length) {
            throw new ProActiveException(
                "Error, destination and source file lists do not match in length");
        }
        
        if(srcFile.length==0) return new BooleanWrapper(true);

        for (int i = 0; i < srcFile.length; i++) {

            //local verification
            if (!srcFile[i].canRead()) {
                logger.error("Can't read or doesn't exist: " +
                    srcFile[i].getAbsoluteFile());
                throw new IOException("Can't read or doesn't exist: " +
                    srcFile[i].getAbsoluteFile());
            }
        }
        try {
            //Could be improoved using a pool
            //Node localnode=NodeFactory.getDefaultNode();
            //FileTransferService ftsRemote= node.getFileTransferServiceFromPool();
            //FileTransferService ftsLocal = localnode.getFileTransferServiceFromPool();
            //if(ftsRemote ==null ) throw new Exception("Unable to get remote File transfer service");
            //if(ftsLocal ==null ) throw new Exception("Unable to get local File transfer service");
            FileTransferService ftsLocal = (FileTransferService) ProActive.newActive(FileTransferService.class.getName(),
                    null);
            FileTransferService ftsRemote = (FileTransferService) ProActive.newActive(FileTransferService.class.getName(),
                    null, node);

            //We ask the local AO to send the file to the remote AO
            return ftsLocal.sendFiles(ftsRemote, srcFile, dstFile, bsize,
                numFlyingBlocks); //this call is asynchronous
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
    public String getHostName() {
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
