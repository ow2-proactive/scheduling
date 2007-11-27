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
package org.objectweb.proactive.filetransfer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.filetransfer.FileBlock;
import org.objectweb.proactive.core.filetransfer.FileTransferEngine;
import org.objectweb.proactive.core.filetransfer.FileTransferRequest;
import org.objectweb.proactive.core.filetransfer.FileTransferService;
import org.objectweb.proactive.core.filetransfer.FileTransferServiceReceive;
import org.objectweb.proactive.core.filetransfer.FileTransferServiceSend;
import org.objectweb.proactive.core.filetransfer.OperationStatus;
import org.objectweb.proactive.core.filetransfer.RemoteFile;
import org.objectweb.proactive.core.filetransfer.RemoteFileImpl;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
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

    protected static Node getLocalNode() throws IOException {
        Node localNode;

        try {
            localNode = NodeFactory.getDefaultNode();
        } catch (NodeException e) {
            throw new IOException("Can't get local node", e);
        }

        return localNode;
    }

    public static List<RemoteFile> push(File[] srcFile, Node dstNode,
        File[] dstFile) throws IOException {
        return transfer(getLocalNode(), srcFile, dstNode, dstFile);
    }

    public static RemoteFile push(File srcFile, Node dstNode, File dstFile)
        throws IOException {
        return transfer(getLocalNode(), srcFile, dstNode, dstFile);
    }

    public static RemoteFile pull(Node srcNode, File srcFile, File dstFile)
        throws IOException {
        return transfer(srcNode, srcFile, getLocalNode(), dstFile);
    }

    public static List<RemoteFile> pull(Node srcNode, File[] srcFile,
        File[] dstFile) throws IOException {
        return transfer(srcNode, srcFile, getLocalNode(), dstFile);
    }

    public static RemoteFile transfer(Node srcNode, File srcFile, Node dstNode,
        File dstFile) throws IOException {
        List<RemoteFile> rfiles = transfer(srcNode, new File[] { srcFile },
                dstNode, new File[] { dstFile });

        return rfiles.get(0);
    }

    public static List<RemoteFile> transfer(Node srcNode, File[] srcFile,
        Node dstNode, File[] dstFile) throws IOException {
        return transfer(srcNode, srcFile, dstNode, dstFile,
            FileBlock.DEFAULT_BLOCK_SIZE,
            FileTransferService.DEFAULT_MAX_SIMULTANEOUS_BLOCKS);
    }

    public static List<RemoteFile> transfer(Node srcNode, File[] srcFile,
        Node dstNode, File[] dstFile, int bsize, int numFlyingBlocks)
        throws IOException {
        if (srcFile.length != dstFile.length) {
            throw new IOException(
                "Error, number destination and source file lists do not match in length");
        }

        Node localNode = getLocalNode();

        ArrayList<RemoteFile> rfile = new ArrayList<RemoteFile>(srcFile.length);

        // local side verifications
        for (int i = 0; i < srcFile.length; i++) {
            //Case srcNode is local
            if (FileTransferEngine.nodeEquals(srcNode, localNode)) {
                if (!srcFile[i].canRead()) {
                    logger.error("Can't read or doesn't exist: " +
                        srcFile[i].getAbsoluteFile());
                    throw new IOException("Can't read or doesn't exist: " +
                        srcFile[i].getAbsoluteFile());
                }
            }

            //Case dstNode is local
            if (FileTransferEngine.nodeEquals(dstNode, localNode)) {
                if (dstFile[i].exists() && !dstFile[i].canWrite()) {
                    logger.error("Can't write to file: " +
                        dstFile[i].getAbsoluteFile());
                    throw new IOException("Can't overrite existant file: " +
                        dstFile[i].getAbsoluteFile());
                }
            }

            //Case srcNode == dstNode
            if (FileTransferEngine.nodeEquals(srcNode, dstNode)) {
                if (dstFile[i].equals(srcFile[i])) {
                    logger.error(
                        "Can't copy, src and destination are the same: " +
                        srcFile[i].getAbsolutePath());
                }
            }
        }

        return internalTransfer(srcNode, srcFile, dstNode, dstFile, bsize,
            numFlyingBlocks);
    }

    protected static List<RemoteFile> internalTransfer(Node srcNode,
        File[] srcFile, Node dstNode, File[] dstFile, int bsize,
        int numFlyingBlocks) throws IOException {
        FileTransferServiceSend ftsSrc;
        FileTransferServiceReceive ftsDst;

        try {
            ftsSrc = FileTransferEngine.getFileTransferEngine(srcNode).getFTS();
            ftsDst = FileTransferEngine.getFileTransferEngine(dstNode).getFTS();
        } catch (Exception e) {
            throw new IOException("Unable to connect or use ProActive Node: " +
                srcNode + " -> " + dstNode, e);
        }

        ArrayList<RemoteFile> rfile = new ArrayList<RemoteFile>(srcFile.length);

        // Alternative: ask the remote AO to send the file to us futureFile = ftsRemote.sendFile(ftsLocal, srcFile, dstFile, bsize, numFlyingBlocks);
        for (int i = 0; i < srcFile.length; i++) {
            // OperationStatus status = ftsDst.receiveFile(ftsSrc, srcFile[i], dstFile[i], bsize, numFlyingBlocks);
            OperationStatus status = ftsSrc.sendFile(srcFile[i], ftsDst,
                    dstFile[i], bsize, numFlyingBlocks);

            FileTransferRequest request = new FileTransferRequest(srcFile[i],
                    dstFile[i], status, ftsSrc, ftsDst);

            rfile.add(new RemoteFileImpl(dstNode, dstFile[i], request));
        }

        return rfile;
    }
}
