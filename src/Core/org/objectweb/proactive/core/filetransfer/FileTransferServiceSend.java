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


public interface FileTransferServiceSend {

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
        int numFlyingBlocks);

    public OperationStatus sendFile(File srcFile,
        FileTransferServiceReceive remoteFTS, File dstFile);

    //public OperationStatus sendFile(RemoteFile srcFile, FileTransferServiceReceive dstFTS, File dstFile);
}
