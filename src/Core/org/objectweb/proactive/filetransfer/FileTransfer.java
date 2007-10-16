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
package org.objectweb.proactive.filetransfer;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.filetransfer.FileBlock;
import org.objectweb.proactive.core.filetransfer.FileTransferEngine;
import org.objectweb.proactive.core.filetransfer.FileTransferRequest;
import org.objectweb.proactive.core.filetransfer.FileTransferService;
import org.objectweb.proactive.core.filetransfer.OperationStatus;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class provides a standard entry point for API FileTransfer tools.
 *
 * @author The ProActive Team (mleyton)
 * @since ProActive 3.0.2 (Feb 2006)
 */
public class FileTransfer {
    static Logger logger = ProActiveLogger.getLogger(Loggers.FILETRANSFER);

    /**
     * Pulls a file from a remote node. This method behaves asynchronously.
     * @param node The remote ProActive node.
     * @param srcFile The source file in the remote node.
     * @param dstFile The destination file in the local node.
     * @return A FileWrapper instance containing a File object. This return value is a future,
     * and to wait on it simply call the FileWrapper.getFiles() method.
     * @throws IOException Problem with permissions, files not found, etc.
     * @throws ProActiveException Problems with communication like node unreachable, etc.
     */
    public static FileVector pullFile(Node node, File srcFile, File dstFile)
        throws IOException, ProActiveException {
        return pullFile(node, srcFile, dstFile, FileBlock.DEFAULT_BLOCK_SIZE,
            FileTransferService.DEFAULT_MAX_SIMULTANEOUS_BLOCKS);
    }

    public static FileVector pullFile(Node node, File srcFile, File dstFile,
        int bsize, int numFlyingBlocks) throws IOException, ProActiveException {
        File[] src = new File[1];
        File[] dst = new File[1];
        src[0] = srcFile;
        dst[0] = dstFile;

        return pullFiles(node, src, dst, bsize, numFlyingBlocks);
    }

    /**
     * This method behaves like pullFile(Node, File, File), with the difference that it transfers multiple files.
     * When performing a FileWrapper.getFiles() on the returned object, the wait-by-necessity mechanism will block
     * the calling thread until all files have been pulled.
     */
    public static FileVector pullFiles(Node node, File[] srcFile, File[] dstFile)
        throws IOException {
        return pullFiles(node, srcFile, dstFile, FileBlock.DEFAULT_BLOCK_SIZE,
            FileTransferService.DEFAULT_MAX_SIMULTANEOUS_BLOCKS);
    }

    public static FileVector pullFiles(Node node, File[] srcFile,
        File[] dstFile, int bsize, int numFlyingBlocks)
        throws IOException {
        if (srcFile.length != dstFile.length) {
            throw new IOException(
                "Error, number destination and source file lists do not match in length");
        }

        FileVector fileWrapper = new FileVector();
        if (srcFile.length == 0) {
            return fileWrapper;
        }

        for (int i = 0; i < srcFile.length; i++) {
            // local verification
            if (dstFile[i].exists() && !dstFile[i].canWrite()) {
                logger.error("Can't write to " + dstFile[i].getAbsoluteFile());
                throw new IOException("Can't write to " +
                    dstFile[i].getAbsoluteFile());
            }
        }
        try {
            FileTransferService ftsRemote = FileTransferEngine.getFileTransferEngine()
                                                              .getFTS(node);
            FileTransferService ftsLocal = FileTransferEngine.getFileTransferEngine()
                                                             .getFTS();

            // We could ask the remote AO to send the file to us
            // futureFile = ftsRemote.sendFile(ftsLocal, srcFile, dstFile,
            // bsizem, numFlyingBlocks);
            for (int i = 0; i < srcFile.length; i++) {
                OperationStatus futureFile = ftsLocal.receiveFile(ftsRemote,
                        srcFile[i], dstFile[i], bsize, numFlyingBlocks);
                FileTransferRequest ftr = new FileTransferRequest(srcFile[i],
                        dstFile[i], futureFile, ftsRemote, ftsLocal);
                fileWrapper.add(ftr);
            }

            //ftsLocal.putInThePool(ftsLocal);
            //TODO put the ftsRemote back into the pool
            return fileWrapper;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Unable to connect or use ProActive Node: " +
                node);
        }
    }

    /**
     * Pushs a file from the local node to a remote node. This method behaves asynchronously.
     * @param node The remote ProActive node.
     * @param srcFile The source file in the local node.
     * @param dstFile The destination file in the remote node.
     * @return A future of a BooleanWrapper. Accessing this variable will cause the block of the calling thread
     * until the file has been fully received at the remote node.
     * @throws IOException Problem with permissions, files not found, etc.
     * @throws ProActiveException Problems with communication like node unreachable, etc.
     */
    public static FileVector pushFile(Node node, File srcFile, File dstFile)
        throws IOException {
        return pushFile(node, srcFile, dstFile, FileBlock.DEFAULT_BLOCK_SIZE,
            FileTransferService.DEFAULT_MAX_SIMULTANEOUS_BLOCKS);
    }

    public static FileVector pushFile(Node node, File srcFile, File dstFile,
        int bsize, int numFlyingBlocks) throws IOException {
        File[] src = new File[1];
        File[] dst = new File[1];
        src[0] = srcFile;
        dst[0] = dstFile;

        return pushFiles(node, src, dst, bsize, numFlyingBlocks);
    }

    /**
     * This method behaves like pushFile(Node, File, File),  with the difference that it transfers multiple files.
     * Accessing the future BooleanWrapper will block the thread, until all files have been pushed to the remote node.
     */
    public static FileVector pushFiles(Node node, File[] srcFile, File[] dstFile)
        throws IOException {
        return pushFiles(node, srcFile, dstFile, FileBlock.DEFAULT_BLOCK_SIZE,
            FileTransferService.DEFAULT_MAX_SIMULTANEOUS_BLOCKS);
    }

    public static FileVector pushFiles(Node node, File[] srcFile,
        File[] dstFile, int bsize, int numFlyingBlocks)
        throws IOException {
        if (srcFile.length != dstFile.length) {
            throw new IOException(
                "Error, destination and source file lists do not match in length");
        }

        FileVector fileVector = new FileVector();

        if (srcFile.length == 0) {
            return fileVector;
        }

        for (int i = 0; i < srcFile.length; i++) {
            // local verification
            if (!srcFile[i].canRead()) {
                logger.error("Can't read or doesn't exist: " +
                    srcFile[i].getAbsoluteFile());
                throw new IOException("Can't read or doesn't exist: " +
                    srcFile[i].getAbsoluteFile());
            }
        }
        try {
            FileTransferService ftsLocal = FileTransferEngine.getFileTransferEngine()
                                                             .getFTS();
            FileTransferService ftsRemote = FileTransferEngine.getFileTransferEngine()
                                                              .getFTS(node);

            // We ask the local AO to send the file to the remote AO
            for (int i = 0; i < srcFile.length; i++) {
                OperationStatus f = ftsLocal.sendFile(ftsRemote, srcFile[i],
                        dstFile[i], bsize, numFlyingBlocks); // this call is asynchronous
                fileVector.add(new FileTransferRequest(srcFile[i], dstFile[i],
                        f, ftsLocal, ftsRemote));
            }

            //ftsLocal.putInThePool(ftsLocal);
            //TODO put the ftsRemote back into the pool
            return fileVector;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Unable to connect or use ProActive Node: " +
                node);
        }
    }

    static public FileVector pushFile(Node node, FileVector srcFile, File dst)
        throws IOException, ProActiveException {
        File[] f = new File[1];
        f[0] = dst;

        return pushFiles(node, srcFile, f);
    }

    /**
     * Pushes a File to the node, while the file is being transferred from a previous operation.
     * This methods is HIGHLY EXPERIMENTAL, and should be used with caution.
     *
     * @param node The node to where the file will be transferred/
     * @param srcFile The source files which correspond to the result of another file transfer operation.
     * @param dst The destination names of the files.
     * @return A vector holding the futures of the files being transferred. The futures will be updated once this operation is finished.
     * @throws ProActiveException If the srcFile vector and the dst array length do not match.
     */
    static public FileVector pushFiles(Node node, FileVector srcFile, File[] dst)
        throws ProActiveException {
        if (srcFile.size() != dst.length) {
            throw new ProActiveException(
                "Error, destination and source file lists do not match in length");
        }

        FileTransferService ftsLocal = FileTransferEngine.getFileTransferEngine()
                                                         .getFTS();
        FileTransferService ftsRemote = FileTransferEngine.getFileTransferEngine()
                                                          .getFTS(node);

        //For each file, send
        Vector requests = srcFile.getFilesRequest();
        FileVector fileWrapper = new FileVector();
        for (int i = 0; i < requests.size(); i++) {
            FileTransferRequest ftr = (FileTransferRequest) requests.get(i);

            /* The source file is the previous operation's destination.
             * The future of the previous operation is passed to know
             * when the previous operation has finished.
             */
            FileTransferRequest newFTR = new FileTransferRequest(ftr.getDstFile(),
                    dst[i], null, ftr.getDestinationFTS(), ftsRemote);
            OperationStatus future = ftsLocal.submitFileTransferRequest(ftsLocal,
                    newFTR, ftr.getOperationFuture()); //async call
            newFTR.setDstFuture(future); //we keep a future on the remote file
            fileWrapper.add(newFTR);
        }

        return fileWrapper;
    }
}
