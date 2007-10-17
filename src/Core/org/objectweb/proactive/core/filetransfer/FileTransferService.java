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
package org.objectweb.proactive.core.filetransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.LongWrapper;


/**
 * This class is intented to be used a service active object for performing
 * file transfer operations.
 *
 * This class is not serializable on purpose.
 *
 * @author The ProActive Team 09/2005 (mleyton)
 *
 */
public class FileTransferService implements ProActiveInternalObject {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FILETRANSFER);
    public final static int DEFAULT_MAX_SIMULTANEOUS_BLOCKS = 8;
    public static final int DEFAULT_BUFFER_SIZE = 512 * 1024; //Bytes
    protected HashMap<File, BufferedInputStream> readBufferMap; //Map for storing the opened reading sockets
    protected HashMap<File, BufferedOutputStream> writeBufferMap; //Map for storing the opened output sockets
    protected FileForwarder forwardFile;

    public FileTransferService() {
        readBufferMap = new HashMap<File, BufferedInputStream>();
        writeBufferMap = new HashMap<File, BufferedOutputStream>();
        forwardFile = new FileForwarder(this);
    }

    public int setImmediateSevices() {
        ProActiveObject.setImmediateService("requestFileTransfer",
            new Class[] { FileTransferRequest.class });
        ProActiveObject.setImmediateService("getFileTransferRequestStatus",
            new Class[] { FileTransferRequest.class });
        return 0; // synchronous call
    }

    public LongWrapper getFileLength(File f) {
        return new LongWrapper(f.length());
    }

    public BooleanWrapper canRead(File f) {
        return new BooleanWrapper(f.canRead());
    }

    public BooleanWrapper openWrite(File file) {
        return new BooleanWrapper(getWritingBuffer(file, false) != null);
    }

    public BooleanWrapper canWrite(File file, boolean append) {
        return new BooleanWrapper(getWritingBuffer(file, append) != null);
    }

    private BufferedOutputStream getWritingBuffer(File f) {
        return getWritingBuffer(f, false);
    }

    protected synchronized BufferedOutputStream getWritingBuffer(File f,
        boolean append) {
        if (!writeBufferMap.containsKey(f)) {
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
                            f.getAbsolutePath(), append), DEFAULT_BUFFER_SIZE);
                writeBufferMap.put(f, bos);
            } catch (FileNotFoundException e) {
                logger.error("Unable to open file: " + f.getAbsolutePath());
                return null;
            }
        }

        return writeBufferMap.get(f);
    }

    private synchronized BufferedInputStream getReadingBuffer(File f) {
        if (!readBufferMap.containsKey(f)) {
            try {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
                            f.getAbsolutePath()), DEFAULT_BUFFER_SIZE);
                readBufferMap.put(f, bis);
            } catch (FileNotFoundException e) {
                logger.error("Unable to open file: " + f.getAbsolutePath());
                return null;
            }
        }

        return readBufferMap.get(f);
    }

    public synchronized void closeRead(File f) {
        BufferedInputStream bis = readBufferMap.remove(f);
        try {
            if (bis != null) {
                bis.close();
            }
        } catch (IOException e) {
        }
    }

    public synchronized BooleanWrapper closeWrite(File f) {
        BufferedOutputStream bos = writeBufferMap.remove(f);
        try {
            if (bos != null) {
                bos.close();
            }
        } catch (IOException e) {
            return new BooleanWrapper(false);
        }

        return new BooleanWrapper(true);
    }

    /**
     * This method will save the specified FileBlock.
     * @param block
     */
    public void saveFileBlock(File dstFile, FileBlock block)
        throws IOException {
        BufferedOutputStream bos = getWritingBuffer(dstFile);
        block.saveCurrentBlock(bos);
    }

    public void saveFileBlockWithoutThrowingException(File dstFile,
        FileBlock block) {
        BufferedOutputStream bos = getWritingBuffer(dstFile);
        try {
            block.saveCurrentBlock(bos);
        } catch (IOException e) {
            //ignore exception
        }
    }

    public void saveFileBlockAndForward(File dstFile, FileBlock block)
        throws IOException {
        this.forwardFile.handleNewRequests(dstFile);
        this.forwardFile.forward(dstFile, block);

        BufferedOutputStream bos = getWritingBuffer(dstFile);
        block.saveCurrentBlock(bos);
    }

    public void saveFileBlockAndForwardWithoutThrowingException(File dstFile,
        FileBlock block) {
        this.forwardFile.handleNewRequests(dstFile);
        this.forwardFile.forward(dstFile, block);

        BufferedOutputStream bos = getWritingBuffer(dstFile);
        try {
            block.saveCurrentBlock(bos);
        } catch (IOException e) {
            //ignore exception
        }
    }

    /**
     * This method will load a FileBlock from a file, starting from the specified offset.
     * @param file The file to read from
     * @param offset The offset of the file
     * @param bsize The size of the block to read.
     * @return A file block loaded from the file and starting from the offest.
     */
    public FileBlock getFileBlock(File file, long offset, int bsize) {
        FileBlock newBlock = new FileBlock(offset, bsize);
        BufferedInputStream bis = getReadingBuffer(file);
        try {
            newBlock.loadNextBlock(bis);
        } catch (IOException e) {
            newBlock.setException(e);
        }
        return newBlock;
    }

    /**
     * This method handles the sending of a file.
     * @param ftsRemote The remote FileTransferService object that will receive the file.
     * @param srcFile The local source of the file.
     * @param dstFile The remote destination of the file.
     * @param bsize The size of the blocks the file will be split into.
     * @param numFlyingBlocks The number of simultaneous blocks that will be sent.
     * @return The result status of the operation.
     */
    public OperationStatus sendFile(FileTransferService ftsRemote,
        File srcFile, File dstFile, int bsize, int numFlyingBlocks) {
        long init = System.currentTimeMillis();
        long numBlocks = 0;

        //Open the local reading buffer
        BufferedInputStream bis = getReadingBuffer(srcFile);
        if (bis == null) {
            return new OperationStatus(new ProActiveException(
                    "Can not open for sending:" + srcFile.getAbsoluteFile()));
        }

        long totalNumBlocks = Math.round(Math.ceil(
                    (double) srcFile.length() / bsize));
        if (totalNumBlocks == 0) {
            closeRead(srcFile);
            return new OperationStatus(new ProActiveException(
                    "Can not send an empty File:" + srcFile.getAbsolutePath()));
        }

        BooleanWrapper bw = ftsRemote.openWrite(dstFile);
        if (bw.booleanValue() != true) {
            closeRead(srcFile);
            return new OperationStatus(new ProActiveException(
                    "Unable to open remote file for writting: " +
                    dstFile.getAbsolutePath()));
        }

        FileBlock fileBlock = new FileBlock(0, bsize);
        while (numBlocks < (totalNumBlocks - 1)) {
            for (int i = 0;
                    (i < numFlyingBlocks) &&
                    (numBlocks < (totalNumBlocks - 1)); i++) {
                try {
                    fileBlock.loadNextBlock(bis);

                    if (i == (numFlyingBlocks - 1)) { //rendevouz the burst, so the remote AO will not be drowned
                        ftsRemote.saveFileBlockAndForward(dstFile, fileBlock);
                    } else { //simply burst
                        ftsRemote.saveFileBlockAndForwardWithoutThrowingException(dstFile,
                            fileBlock); //remote (async) invocation
                    }
                    numBlocks++;
                } catch (IOException e) {
                    ftsRemote.closeForwardingService(dstFile, e);
                    return new OperationStatus(new ProActiveException(
                            "Can not send file block to:" +
                            ProActiveObject.getActiveObjectNodeUrl(ftsRemote), e));
                }
            }
        }

        //Handle a rendevouz with last block here!
        try {
            fileBlock.loadNextBlock(bis);
            ftsRemote.saveFileBlock(dstFile, fileBlock);
            numBlocks++;
        } catch (IOException e) {
            ftsRemote.closeForwardingService(dstFile, e);
            return new OperationStatus(new ProActiveException(
                    "Can not send File to:" +
                    ProActiveObject.getActiveObjectNodeUrl(ftsRemote), e));
        }

        //Close the remote/local buffers
        ProFuture.waitFor(ftsRemote.closeWrite(dstFile));
        closeRead(srcFile);
        ftsRemote.closeForwardingService(dstFile); //sync-call

        if (logger.isDebugEnabled()) {
            long fin = System.currentTimeMillis();
            long delta = (fin - init);
            logger.debug("File " + dstFile.getAbsolutePath() + " sent using " +
                numBlocks + " blocks,  in: " + delta + "[ms]");
        }

        return new OperationStatus();
    }

    public void closeForwardingService(File dstFile) {
        this.forwardFile.closeForwardingService(dstFile);
    }

    public void closeForwardingService(File dstFile, Exception e) {
        this.forwardFile.closeForwardingService(dstFile, e);
    }

    /**
     * This method handles the reception of a file.
     * @param ftsRemote The remote FileTransferService object that will send the file.
     * @param srcFile The remote source of the file.
     * @param dstFile The local destination of the file.
     * @param bsize The size of the blocks the file will be split into.
     * @param numFlyingBlocks The number of simultaneous blocks that will be received.
     * @return The result status of the operation.
     */
    public OperationStatus receiveFile(FileTransferService ftsRemote,
        File srcFile, File dstFile, int bsize, int numFlyingBlocks) {
        long numBlocks = 0;
        long init = System.currentTimeMillis();

        LongWrapper length = ftsRemote.getFileLength(srcFile);
        if (length.longValue() <= 0) {
            closeWrite(dstFile);
            OperationStatus fc = new OperationStatus(new Exception(
                        "Unable to open remote file for reading:" +
                        srcFile.getAbsolutePath()));
            return fc;
        }

        long totalNumBlocks = Math.round(Math.ceil(
                    (double) length.longValue() / bsize));
        FileBlock[] flyingBlocks = new FileBlock[numFlyingBlocks];
        while (numBlocks < totalNumBlocks) {
            int i;

            for (i = 0;
                    (i < flyingBlocks.length) && (numBlocks < totalNumBlocks);
                    i++) {
                flyingBlocks[i] = ftsRemote.getFileBlock(srcFile,
                        bsize * numBlocks, bsize); //async call
                numBlocks++;
            }

            forwardFile.handleNewRequests(dstFile);
            forwardFile.forward(dstFile, flyingBlocks);

            //here we sync (wait-by-necessity)                
            for (int j = 0; j < i; j++) {
                if (flyingBlocks[j].hasException()) {
                    Exception e = flyingBlocks[j].getException();
                    forwardFile.closeForwardingService(dstFile, e);
                    return new OperationStatus(e);
                }
                try {
                    saveFileBlock(dstFile, flyingBlocks[j]);
                } catch (IOException e) {
                    forwardFile.closeForwardingService(dstFile, e);
                    return new OperationStatus(e);
                }
            }
        }

        //close remote and local buffers
        ftsRemote.closeRead(srcFile); //async-call
        closeWrite(dstFile);

        forwardFile.closeForwardingService(dstFile); //sync-call

        if (logger.isDebugEnabled()) {
            long fin = System.currentTimeMillis();
            long delta = (fin - init);
            logger.debug("File " + dstFile.getAbsolutePath() +
                " received using " + numBlocks + " blocks,  in: " + delta +
                "[ms]");
        }

        return new OperationStatus();
    }

    /*
    public void putInThePool(FileTransferService me) {
    FileTransferEngine.getFileTransferEngine().putFTS(me);
    }
    */
    public OperationStatus sendFile(FileTransferService remoteFTS,
        File srcFile, File dstFile) {
        return sendFile(remoteFTS, srcFile, dstFile,
            FileBlock.DEFAULT_BLOCK_SIZE, DEFAULT_MAX_SIMULTANEOUS_BLOCKS);
    }

    /**
     * This method is ment to be used as an immediate service method to enque new file transfer objects.
     * @param fti
     */
    public void requestFileTransfer(FileTransferRequest fti) {
        forwardFile.requestFileTransfer(fti);
    }

    public OperationStatus getFileTransferRequestStatus(FileTransferRequest fti) {
        return forwardFile.getFileTransferRequestStatus(fti);
    }

    /**
     * This method will handle a file transfer request. If the file
     * concerned in the operation is still in transit, then this method
     * will try to active file transfer forwarding. If the file transfer
     * forwarding is not used, then this method will handle the file
     * transfer using a normal file transfer operation.
     *
     * @param meFTS The active object that was called upon this method.
     * @param ftr The file transfer request to perform.
     * @param parentOpStat The future of the result operation of the parent. If
     * the parent operation fails, this implies this operation will also fail.
     * @return The result of the operation.
     */
    public OperationStatus submitFileTransferRequest(
        FileTransferService meFTS, FileTransferRequest ftr,
        OperationStatus parentOpStat) {
        FileTransferService srcFTS = ftr.getSourceFTS();

        OperationStatus opstat = null;

        //if the future is allready here, don't bother making a request for the file transfer
        if (ProFuture.isAwaited(parentOpStat)) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "File transfer request will be handled using forwarding");
            }
            srcFTS.requestFileTransfer(ftr); //immediate service call
            ProFuture.waitFor(parentOpStat); //we wait for the original file transfer to finish
            if (parentOpStat.hasException()) { //if the original file transfer had an error we propagate it
                srcFTS.getFileTransferRequestStatus(ftr); //we need to remove the desubmit the ftr
                return parentOpStat;
            }

            opstat = srcFTS.getFileTransferRequestStatus(ftr); //immediate service call
            if (opstat.hasException()) {
                return opstat; //error was encountered
            }
        }

        //If the file was not handled by the forwarding AO, then we take care of it here.
        if ((opstat == null) || opstat.isPending()) {
            if (logger.isDebugEnabled()) {
                logger.debug("File transfer request will be handled directly");
            }

            String myNodeURL = ProActiveObject.getActiveObjectNodeUrl(meFTS);
            String srcNodeURL = ProActiveObject.getActiveObjectNodeUrl(ftr.getSourceFTS());

            //If the file is not here, we get a FTS AO in the source node.
            if (!myNodeURL.equals(srcNodeURL)) {
                try {
                    srcFTS = FileTransferEngine.getFileTransferEngine()
                                               .getFTS(srcNodeURL);
                    opstat = srcFTS.sendFile(ftr.getDestinationFTS(),
                            ftr.getSrcFile(), ftr.getDstFile());
                    ftr.setSourceFTS(srcFTS);
                } catch (Exception e) {
                    return new OperationStatus(new ProActiveException(
                            "Unable to create File Transfer Service on node: " +
                            srcNodeURL, e));
                }
            } else { //If the file is in this node, we send it from this Active Object
                opstat = sendFile(ftr.getDestinationFTS(), ftr.getSrcFile(),
                        ftr.getDstFile());
                ftr.setSourceFTS(meFTS);
            }
        }
        return opstat;
    }

    /**
     * Gets a checksum on the specified file
     * @param file The file to be checksumed.
     * @return The checksum.
     * @throws IOException
     */
    public long checkSum(File file) throws IOException {
        // Compute Adler-32 checksum
        CheckedInputStream cis = new CheckedInputStream(new FileInputStream(
                    file.getAbsoluteFile()), new Adler32());
        byte[] tempBuf = new byte[1024 * 1024]; //1MB loops
        while (cis.read(tempBuf) >= 0)
            ;

        return cis.getChecksum().getValue();
    }

    /** Returns a hello world string with the hostname and the time.
     *  It is used for debugging.
     * @return a String with the hostname and the time*/
    public String sayHello() {
        String address = "Unknow";
        try {
            address = URIBuilder.getLocalAddress().getHostName();
        } catch (UnknownHostException e) { /* address will be Unknown */
        }

        return "Hello World from " + address + " at " +
        new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
    }
}
