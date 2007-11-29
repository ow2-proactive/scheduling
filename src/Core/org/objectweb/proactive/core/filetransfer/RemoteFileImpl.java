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
package org.objectweb.proactive.core.filetransfer;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.api.ProFileTransfer;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;


public class RemoteFileImpl implements RemoteFile {
    Node node;
    File file;
    FileTransferRequest request;

    public RemoteFileImpl(Node node, File file, FileTransferRequest request) {
        this.node = node;
        this.file = file;
        this.request = request;
    }

    //@Override
    public RemoteFile pull(File localDst) throws IOException {
        waitForFinishedTransfer();

        Node localNode;

        try {
            localNode = NodeFactory.getDefaultNode();
        } catch (NodeException e) {
        	//TODO change when moving to Java 1.6
        	//throw new IOException("Can't get local node", e);
        	throw new IOException("Can't get local node "+ e.getMessage());
        }

        return ProFileTransfer.transfer(node, file, localNode, localDst);
    }

    //@Override
    public RemoteFile push(Node dstNode, File dstFile)
        throws IOException {
        waitForFinishedTransfer();

        return ProFileTransfer.transfer(getRemoteNode(), getRemoteFilePath(),
            dstNode, dstFile);
    }

    //@Override
    public File getRemoteFilePath() {
        return file;
    }

    //@Override
    public Node getRemoteNode() {
        return node;
    }

    //@Override
    public boolean isTransferFinished() {
        return request.getOperationFuture().getException() != null || !request.isAwaited();
    }

    //@Override
    public void waitForFinishedTransfer() throws IOException {
        
        if (request != null) {
            request.waitForOperation();
        }
        
    	
        if (request.getOperationFuture().getException() != null) {
            throw request.getOperationFuture().getException();
        }
    }
}
