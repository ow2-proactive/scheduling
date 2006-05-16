package org.objectweb.proactive.core.filetransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

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
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.FileWrapper;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;


/**
 * @author The ProActive Team 09/2005
 *
 */
public class FileTransferService implements Serializable,
    ProActiveInternalObject {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FILETRANSFER);
    public final static int DEFAULT_MAX_SIMULTANEOUS_BLOCKS = 8;
	public static final int DEFAULT_BUFFER_SIZE=512*1024;
	
    protected HashMap readBufferMap; //Map for storing the opened reading sockets
    protected HashMap writeBufferMap; //Map for storing the opened output sockets
    
    public FileTransferService() {
    	readBufferMap = new HashMap();
    	writeBufferMap = new HashMap();
    }

    public LongWrapper openRead(File f){
    	
    	try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f.getAbsolutePath()),DEFAULT_BUFFER_SIZE);
			readBufferMap.put(f,bis);
		} catch (FileNotFoundException e) {
			logger.error("Unable to open file: "+f.getAbsolutePath());
			return new LongWrapper(0);
			
		}
		return new LongWrapper(f.length());
		
    }
    
    /**
     * Opens a buffer reader for File f.
     * @param f  The file for which a buffer read will be opened
     * @return Length of the file
     */
    public BooleanWrapper openWrite(File f){
    	try {
    		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f.getAbsolutePath()),DEFAULT_BUFFER_SIZE);
			writeBufferMap.put(f,bos);
		} catch (FileNotFoundException e) {
			logger.error("Unable to open file: "+f.getAbsolutePath());
			return new BooleanWrapper(false);
		}

		return new BooleanWrapper(true);
    }
    
    public void closeRead(File f){
    	BufferedInputStream bis = (BufferedInputStream) readBufferMap.remove(f);
    	try {
    		if(bis!=null) bis.close();
		} catch (IOException e) {}
    	
    }
    
    public void closeWrite(File f){
    	BufferedOutputStream bos = (BufferedOutputStream) writeBufferMap.remove(f);
    	try {
    		if(bos!=null) bos.close();
    	} catch (IOException e) {}
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

        BufferedOutputStream bos = (BufferedOutputStream)writeBufferMap.get(f);
        fileblock.saveCurrentBlock(bos);
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
     * @return A file block loaded from the file and starting from the offest.
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
        BufferedInputStream bis = (BufferedInputStream)readBufferMap.get(f);
        newBlock.loadNextBlock(bis);

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

        //Open the local reading buffer
       	BufferedInputStream bis;
		try {
			bis = new BufferedInputStream(new FileInputStream(srcFile.getAbsoluteFile()),DEFAULT_BUFFER_SIZE);
		} catch (FileNotFoundException e1) {
			logger.error("Can not open for sending:"+srcFile.getAbsoluteFile());
			logger.error(e1.getMessage());
			return new BooleanWrapper(false);
		}

		long totalNumBlocks=Math.round(Math.ceil((double)srcFile.length()/bsize));
        if (totalNumBlocks == 0) {
            logger.error("Can not send an empty File:" +
                srcFile.getAbsolutePath());
            try {bis.close();} catch (IOException e) {}
            return new BooleanWrapper(false);
        }
		
		//Open the remote writting buffer
		BooleanWrapper bw = ftsRemote.openWrite(dstFile);
		if(bw.booleanValue() !=true){
			logger.error("Unable to open remote file for writting"+dstFile.getAbsolutePath());
			try {bis.close();} catch (IOException e) {}
			return new BooleanWrapper(false);
		}
		
        FileBlock fileBlock = new FileBlock(srcFile.getAbsolutePath(), 0, bsize);
        fileBlock.setDstFilename(dstFile.getAbsolutePath());
        while (numBlocks < (totalNumBlocks - 1)) {
            //Exceptions can happen here
            try {
                for (int i = 0;
                        (i < numFlyingBlocks) &&
                        (numBlocks < (totalNumBlocks - 1)); i++) {
                    fileBlock.loadNextBlock(bis);
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

        //Handle a rendevouz with last block here!
        try {
            fileBlock.loadNextBlock(bis);
            ftsRemote.saveFileBlock(fileBlock);
            numBlocks++;
        } catch (IOException e) {
            logger.error("Can not send File to:" + ftsRemote.getHostName());
            logger.error(e.getMessage());
            return new BooleanWrapper(false);
        }

        //Close the remote/local buffers
        ftsRemote.closeWrite(dstFile);
        try { bis.close(); } catch (IOException e) {}
        
        if (logger.isDebugEnabled()) {
            long fin = System.currentTimeMillis();
            long delta = (fin - init);
            logger.debug("File sent using " + numBlocks + " blocks,  in: " +
                delta + "[ms]");
        }

        //TODO Here we could register this FTS AO into a Node or RunTime pool for reuse!
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

        //Open local writting buffer
       	BufferedOutputStream bos;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(dstFile.getAbsoluteFile()),DEFAULT_BUFFER_SIZE);
		} catch (FileNotFoundException e1) {
			logger.error("Can not open local file for writing:"+dstFile.getAbsoluteFile());
			logger.error(e1.getMessage());
			return dstFile;
		}
        
        //Open remote buffer for reading
 		LongWrapper length = ftsRemote.openRead(srcFile);
		if(length.longValue() <= 0){
			logger.error("Unable to open remote file for reading:" +srcFile.getAbsolutePath());
			try {bos.close();} catch (IOException e) {}
			return dstFile;
		}

		long totalNumBlocks = Math.round(Math.ceil((double)length.longValue()/bsize));
		
        FileBlock[] flyingBlocks = new FileBlock[numFlyingBlocks];
        while (numBlocks < totalNumBlocks) {
            int i;

            ProActive.tryWithCatch(IOException.class);
            try {
                for (i = 0;
                        (i < flyingBlocks.length) &&
                        (numBlocks < totalNumBlocks); i++) {
                    flyingBlocks[i] = ftsRemote.getFileBlock(srcFile.getAbsolutePath(),
                            bsize * numBlocks, bsize); //async call
                    numBlocks++;
                }
            } catch (IOException e) {
                logger.error("Error, unable to get File:" +
                    srcFile.getAbsolutePath() + " from " +
                    ftsRemote.getHostName());
                return dstFile;
            } finally {
                ProActive.removeTryWithCatch(); //Wait for exceptions here
            }

            //here we sync (wait-by-necessity)                
            for (int j = 0; j < i; j++) {
                flyingBlocks[j].setDstFilename(dstFile.getAbsolutePath());
                flyingBlocks[j].saveCurrentBlock(bos);
            }
        }

        //close local and remote buffers
        ftsRemote.closeRead(srcFile);
        try {bos.close();} catch (IOException e) {}
        
        if (logger.isDebugEnabled()) {
            long fin = System.currentTimeMillis();
            long delta = (fin - init);
            logger.debug("File received using " + numBlocks + " blocks,  in: " +
                delta + "[ms]");
        }

        //TODO Here we could register this FTS AO into a Node or RunTime pool for reuse!
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
