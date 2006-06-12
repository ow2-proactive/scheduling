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

import org.objectweb.proactive.core.ProActiveException;


/**
 * This class acts as a dispatcher of file blocks. It is ment
 * to improve performance, by performing a rendevouz on a local
 * active object instead of a remote one.
 *
 * @author The ProActive Team 06/06 (mleyton)
 *
 */
public class FileDispatcher {
    public FileDispatcher() {
    }

    public OperationStatus sendBlock(FileTransferService remoteFTS,
        FileBlock fileBlock, File dstFile) {
        try {
            remoteFTS.saveFileBlockAndForward(dstFile, fileBlock);
        } catch (IOException e) {
            return new OperationStatus(new ProActiveException(
                    "Can't forward block!", e));
        }

        return new OperationStatus();
    }

    public void sendBlockFileBlockWithoutThrowingException(
        FileTransferService remoteFTS, FileBlock fileBlock, File dstFile) {
        remoteFTS.saveFileBlockAndForwardWithoutThrowingException(dstFile,
            fileBlock);
    }

    public OperationStatus closeForwardingService(FileTransferService fts,
        File srcFile, Exception e) {
        fts.closeForwardingService(srcFile, e);
        return new OperationStatus();
    }

    public OperationStatus closeForwardingService(FileTransferService fts,
        File srcFile) {
        fts.closeForwardingService(srcFile);
        return new OperationStatus();
    }
}
