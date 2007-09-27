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
package org.objectweb.proactive.core.filetransfer;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class handles the forwarding of a file to a remote node. An instance of
 * this class is contained within a FileTransferService object.
 *
 * @author The ProActive Team 06/06 (mleyton)
 *
 */
class FileForwarder {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FILETRANSFER);

    //  Map of Vectors of FileTransferService holding remote FTS to send the file (key of the hash)
    protected HashMap<File, Vector<FileTransferRequest>> servingRequests;

    //  Map of Vectors of FileTransferService holding new remote FTS to send the file (key of the hash)
    protected HashMap<File, Vector<FileTransferRequest>> newRequests;

    //  Map of Vectors of failed FileTransferRequests
    protected HashMap<File, Vector<FileTransferRequest>> failedRequests;
    protected FileTransferService servingFTS;
    protected FileDispatcher dispatcher;

    public FileForwarder(FileTransferService fts) {
        servingRequests = new HashMap<File, Vector<FileTransferRequest>>();
        newRequests = new HashMap<File, Vector<FileTransferRequest>>();
        failedRequests = new HashMap<File, Vector<FileTransferRequest>>();
        servingFTS = fts; //direct reference to the object (not a stub)

        try { //TODO handle the exception
            dispatcher = (FileDispatcher) ProActiveObject.newActive(FileDispatcher.class.getName(),
                    null);
        } catch (Exception e) {
        }
    }

    /**
     * This method is inteded to be called as an Immediate Service.
     * It will enqueue the file transfer information to be served in
     * the next loop of the receiveFile method.
     * @param fti An instance of a file transfer object.
     */
    public synchronized void requestFileTransfer(FileTransferRequest fti) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting file transfer request for:" + fti);
        }
        if (!newRequests.containsKey(fti.getSrcFile())) {
            newRequests.put(fti.getSrcFile(), new Vector<FileTransferRequest>());
        }

        Vector<FileTransferRequest> requests = newRequests.get(fti.getSrcFile());
        requests.add(fti);
    }

    /**
     * This method forwards the blocks for all the pertinent file transfer requests.
     * @param srcFile  A vector of FileTransferInformation to which the blocks should be forwarded.
     * @param flyingBlocks The flyingBlocks that should be forwarded. If the flyingBlocks are futures, this method call will block.
     */

    /*
    private synchronized void forward(FileTransferService dispatcherFile, File srcFile, FileBlock[] blocks) {
        if (!servingRequests.containsKey(srcFile) || blocks.length<=0) {
            return; //nothing to do
        }

        Vector forward = (Vector) servingRequests.get(srcFile);

        for (int i = 0; i < forward.size(); i++) {
            FileTransferRequest fti = (FileTransferRequest) forward.get(i);
            for (int j = 0; j < blocks.length; j++) {
                    dispatcherFile.sendBlock(fti.getDestinationFTS(), blocks[j],fti.getDstFile().getAbsolutePath()); //async call
            }
        }
    }
    */
    protected synchronized void forward(File srcFile, FileBlock block) {
        if (!servingRequests.containsKey(srcFile)) {
            return; //nothing to do
        }

        Vector<FileTransferRequest> forward = servingRequests.get(srcFile);
        for (int i = 0; i < forward.size(); i++) {
            FileTransferRequest ftr = forward.get(i);
            OperationStatus opStat = dispatcher.sendBlock(ftr.getDestinationFTS(),
                    block, ftr.getDstFile()); //async call

            if (opStat.hasException()) { //call failed
                forward.remove(i);
                ftr.setDstFuture(opStat);
                addToHash(failedRequests, srcFile, ftr);
            }
        }
    }

    protected synchronized void forwardWithoutThrowingException(File srcFile,
        FileBlock block) {
        if (!servingRequests.containsKey(srcFile)) {
            return; //nothing to do
        }

        Vector<FileTransferRequest> forward = servingRequests.get(srcFile);
        for (int i = 0; i < forward.size(); i++) {
            FileTransferRequest ftr = forward.get(i);
            dispatcher.sendBlockFileBlockWithoutThrowingException(ftr.getDestinationFTS(),
                block, ftr.getDstFile()); //async call
                                          /*
            if(opStat.hasException()){ //call failed
            forward.remove(i);
            ftr.setDstFuture(opStat);
            addToHash(failedRequests,srcFile, ftr);
            }
            */
        }
    }

    protected synchronized void forward(File srcFile, FileBlock[] block) {
        if (!servingRequests.containsKey(srcFile)) {
            return; //nothing to do
        }

        Vector<FileTransferRequest> forward = servingRequests.get(srcFile);
        for (int i = 0; i < forward.size(); i++) {
            FileTransferRequest ftr = forward.get(i);
            for (int j = 0; j < block.length; j++) {
                dispatcher.sendBlockFileBlockWithoutThrowingException(ftr.getDestinationFTS(),
                    block[j], ftr.getDstFile()); //async call
            }

            /*
            if(opStat.hasException()){ //call failed
                    forward.remove(i);
                    ftr.setDstFuture(opStat);
                    addToHash(failedRequests,srcFile, ftr);
            }
            */
        }
    }

    /**
     * This method is used to send information already stored on the hard drive.
     * @param The File Transfer Engine singleton active object.
     * @param file The file that must be routed.
     */
    protected synchronized void handleNewRequests(File file) {
        if (!newRequests.containsKey(file)) {
            return; //nothing to do
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Sending already saved data for file: " +
                file.getAbsolutePath() + " " + file.length() + "[bytes]");
        }

        servingFTS.closeWrite(file); //close the buffer to generate a flush

        Vector<FileTransferRequest> requests = newRequests.remove(file);
        for (int i = 0; i < requests.size(); i++) {
            FileTransferRequest ftr = requests.get(i);

            if (!ftr.getSrcFile().equals(file)) {
                ftr.setDstFuture(new OperationStatus(
                        new ProActiveException(
                            "Error when sending saved data. Source files do not match:" +
                            ftr.getSrcFile().getAbsolutePath() + " != " +
                            file.getAbsolutePath())));
                addToHash(failedRequests, file, ftr);
                continue;
            }

            //Send the current file
            if (file.length() > 0) {
                OperationStatus opRes = servingFTS.sendFile(ftr.getDestinationFTS(),
                        ftr.getSrcFile(), ftr.getDstFile());
                ProFuture.waitFor(opRes); //wait for the send to finish (possibly with errors)
                if (opRes.hasException()) {
                    ftr.setDstFuture(opRes); //Update the future with the error.
                    addToHash(failedRequests, file, ftr);
                    continue;
                } else {
                    //Open the remote buffer for further appending
                    ftr.getDestinationFTS().canWrite(ftr.getDstFile(), true);
                }
            }

            addToHash(servingRequests, file, ftr);
        }

        servingFTS.getWritingBuffer(file, true); //open the writting buffer for appending
    }

    private void addToHash(HashMap<File, Vector<FileTransferRequest>> hash,
        File file, FileTransferRequest ftr) {
        if (!hash.containsKey(file)) {
            hash.put(file, new Vector<FileTransferRequest>());
        }
        Vector<FileTransferRequest> v = hash.get(file);

        v.add(ftr);
    }

    public synchronized void closeForwardingService(File srcFile) {
        closeForwardingService(srcFile, null);
    }

    public synchronized void closeForwardingService(File srcFile, Exception e) {
        servingFTS.closeWrite(srcFile);

        Vector requests = this.servingRequests.remove(srcFile);
        if (requests == null) {
            return;
        }

        Vector<OperationStatus> opStat = new Vector<OperationStatus>();
        for (int i = 0; i < requests.size(); i++) {
            FileTransferRequest ftr = (FileTransferRequest) requests.get(i);
            FileTransferService remoteFTS = ftr.getDestinationFTS();

            if (e != null) {
                opStat.add(dispatcher.closeForwardingService(remoteFTS,
                        ftr.getDstFile(), e));
                ftr.setDstFuture(new OperationStatus(e));
                addToHash(failedRequests, srcFile, ftr);
            } else {
                opStat.add(dispatcher.closeForwardingService(remoteFTS,
                        ftr.getDstFile()));
            }
        }
        ProFuture.waitForAll(opStat);
    }

    public synchronized void clearNewRequests(File srcFile) {
        this.newRequests.remove(srcFile);
    }

    /**
     * This method removes the specified FileTransferRequest from the new request list.
     * This method is ment to be called as an immediate service.
     * @param ftr The FileTransferRequest that will be removed.
     * @return true if fti was found (and removed) inside the new request list, false otherwise.
     */
    public synchronized OperationStatus getFileTransferRequestStatus(
        FileTransferRequest ftr) {
        //check if the operation was never performed
        Vector<FileTransferRequest> v = newRequests.get(ftr.getSrcFile());
        for (int i = 0; (v != null) && (i < v.size()); i++) {
            if (ftr.equals(v.get(i))) {
                v.remove(i);
                return new OperationStatus(true);
            }
        }
        if ((v != null) && v.isEmpty()) {
            newRequests.remove(ftr.getSrcFile());
        }

        //check if the operation encountered errors
        v = failedRequests.get(ftr.getSrcFile());
        for (int i = 0; (v != null) && (i < v.size()); i++) {
            if (ftr.equals(v.get(i))) {
                v.remove(i);
                return ftr.getOperationFuture();
            }
        }

        if ((v != null) && v.isEmpty()) {
            failedRequests.remove(ftr.getSrcFile());
        }

        return new OperationStatus();
    }
}
