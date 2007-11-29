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
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * This class is intended to be used a service active object for performing
 * file transfer operations.
 *
 * This class is not Serializable on purpose.
 *
 * @author The ProActive Team 09/2005 (mleyton)
 */
public class FileTransferService implements ProActiveInternalObject, InitActive,
    FileTransferServiceSend, FileTransferServiceReceive {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FILETRANSFER);
    public final static int DEFAULT_MAX_SIMULTANEOUS_BLOCKS = 8;
    public static final int DEFAULT_BUFFER_SIZE = 512 * 1024; //Bytes
    protected HashMap<File, BufferedOutputStream> writeBufferMap; //Map for storing the opened output sockets

    /**
     * This is an empty constructor for ProActive's MOP. Don't use directly.
     */
    @Deprecated
    public FileTransferService() {
    }

    //@Override
    public void initActivity(Body body) {
        writeBufferMap = new HashMap<File, BufferedOutputStream>();

        //ProActiveObject.setImmediateService("requestFileTransfer", new Class[] { FileTransferRequest.class });
    }

    /* ***************** BEGIN FILETRANSFER SERVICE RECIEVE  ***************************/
    public BooleanWrapper openWrite(File file) {
        return new BooleanWrapper(getWritingBuffer(file, false) != null);
    }

    public BooleanWrapper canWrite(File file, boolean append) {
        return new BooleanWrapper(getWritingBuffer(file, append) != null);
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
     *
     * @param block
     */
    public void saveFileBlock(File dstFile, FileBlock block)
        throws IOException {
        //TODO propagate exception into forwarded files
        BufferedOutputStream bos = getWritingBuffer(dstFile);
        block.saveCurrentBlock(bos);
    }

    public void saveFileBlockWithoutThrowingException(File dstFile,
        FileBlock block) {
        try {
            saveFileBlock(dstFile, block);
        } catch (IOException e) {
            //TODO propagate exception into forwarded files
        }
    }

    protected BufferedOutputStream getWritingBuffer(File f) {
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

    /* ***************** BEGIN FILETRANSFER SERVICE SEND  ***************************/

    /**
     * This method handles the sending of a file.
     * @param ftsRemote The remote FileTransferService object that will receive the file.
     * @param srcFile The local source of the file.
     * @param dstFile The remote destination of the file.
     * @param bsize The size of the blocks the file will be split into.
     * @param numFlyingBlocks The number of simultaneous blocks that will be sent.
     * @return The result status of the operation.
     */
    public OperationStatus sendFile(File srcFile,
        FileTransferServiceReceive ftsRemote, File dstFile, int bsize,
        int numFlyingBlocks) {
        long init = System.currentTimeMillis();
        long numBlocks = 0;

        //Open the local reading buffer
        BufferedInputStream bis;
        try {
            bis = new BufferedInputStream(new FileInputStream(
                        srcFile.getAbsolutePath()), DEFAULT_BUFFER_SIZE);
        } catch (Exception e) {
            //TODO change when moving to Java 1.6
            //return new OperationStatus(new IOException( "Cannot open for sending:" + srcFile.getAbsoluteFile(), e));
            return new OperationStatus(new IOException(
                    "Cannot open for sending:" + srcFile.getAbsoluteFile() +
                    " " + e.getMessage()));
        }

        long totalNumBlocks = Math.round(Math.ceil(
                    (double) srcFile.length() / bsize));
        if (totalNumBlocks == 0) {
            close(bis);
            return new OperationStatus(new IOException(
                    "Cannot send an empty File:" + srcFile.getAbsolutePath()));
        }

        BooleanWrapper bw = ftsRemote.openWrite(dstFile);
        if (bw.booleanValue() != true) {
            close(bis);
            return new OperationStatus(new IOException(
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

                    if (i == (numFlyingBlocks - 1)) { //rendezvous the burst, so the remote AO will not be drowned
                        ftsRemote.saveFileBlock(dstFile, fileBlock);
                    } else { //simply burst
                        ftsRemote.saveFileBlockWithoutThrowingException(dstFile,
                            fileBlock); //remote (async) invocation
                    }
                    numBlocks++;
                } catch (IOException e) {
                    //TODO change when moving to Java 1.6
                    return new OperationStatus(new IOException(
                            "Cannot send file block to:" +
                            ProActiveObject.getActiveObjectNodeUrl(ftsRemote) +
                            e));
                    //                    return new OperationStatus(new IOException(
                    //                            "Cannot send file block to:" +
                    //                            ProActiveObject.getActiveObjectNodeUrl(ftsRemote), e));
                }
            }
        }

        //Handle a rendezvous with last block here
        try {
            fileBlock.loadNextBlock(bis);
            ftsRemote.saveFileBlock(dstFile, fileBlock);
            numBlocks++;
        } catch (IOException e) {
            //TODO change when moving to Java 1.6
            //return new OperationStatus(new IOException("Cannot send File to:" + ProActiveObject.getActiveObjectNodeUrl(ftsRemote), e));
            return new OperationStatus(new IOException("Cannot send File to:" +
                    ProActiveObject.getActiveObjectNodeUrl(ftsRemote) + e));
        }

        //Close the remote/local buffers
        ProFuture.waitFor(ftsRemote.closeWrite(dstFile));
        close(bis);

        if (logger.isDebugEnabled()) {
            long fin = System.currentTimeMillis();
            long delta = (fin - init);
            logger.debug("File " + dstFile.getAbsolutePath() + " sent using " +
                numBlocks + " blocks,  in: " + delta + "[ms]");
        }

        return new OperationStatus();
    }

    protected void close(BufferedInputStream bis) {
        try {
            bis.close();
        } catch (IOException e) {
            //We don't care about closing exceptions
        }
    }

    /**
     * Put this active object back in the local pool
     */
    public void putBackInLocalPool() {
        FileTransferEngine.getFileTransferEngine()
                          .putFTS((FileTransferService) ProActiveObject.getStubOnThis());
    }

    /**
     * Put the FileTransferServiceReceive destination object back on its local pool,
     * and put my self (the source) on my local pool.
     */
    public void putBackInPool(FileTransferServiceReceive ftsDst) {
        putBackInLocalPool();
        ftsDst.putBackInLocalPool();
    }
}
