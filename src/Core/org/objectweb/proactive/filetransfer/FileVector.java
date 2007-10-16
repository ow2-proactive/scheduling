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
import java.io.Serializable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.filetransfer.FileTransferRequest;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class contains a result of a file transfer operation.
 * It can be used to block until the operation has finished,
 * or it can be passed to other file transfer operations
 * using file transfer forwarding.
 *
 * @author The ProActive Team (mleyton)
 *
 * Created on Feb 18, 2006
 */
public class FileVector implements Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FILETRANSFER);
    Vector fileList; //Contains futures of FileTransferRequests

    public FileVector() {
        fileList = new Vector();
    }

    /**
     * Waits for all the pending futures inside this file vector.
     * If the files are still being transferred, then this method will block
     * until the transfers have finished.
     */
    public void waitForAll() {
        for (int i = 0; i < fileList.size(); i++) {
            FileTransferRequest request = ((FileTransferRequest) fileList.get(i));
            request.waitForOperation();
        }
    }

    /**
     * This method will return a File object representing the result of the File Transfer operation (push or pull).
     * If the file transfer operation has not finished, then this method will block until the file is sent/received.
     *
     * Note that if the returned value of this method is passed as a parameter to other push or pull operations, then
     * these operations will not be aware that the file is the result of another operation.  To avoid this
     * issue, the FileVector object must be passed as parameter to other file transfer operations.
     *
     * @param i The index of the file inside this vector
     * @return The File object that represents the path on a remote machine
     * @throws Exception An error if the file transfer operation did not succeed.
     */
    public File getFile(int i) throws Exception {
        FileTransferRequest ftr = (FileTransferRequest) fileList.get(i);
        ftr.waitForOperation();
        if (ftr.getOperationFuture().hasException()) {
            throw ftr.getOperationFuture().getException();
        }
        return ftr.getDstFile();
    }

    /**
     * Returns the current size of the vector
     * @return The size of the vector.
     */
    public int size() {
        return fileList.size();
    }

    /**
     * This method returns the file transfer requests.
     * It is mainly used in the internal implementation of the
     * file transfer mechanism
     *
     * @return The vector containing the file requests.
     */
    protected Vector getFilesRequest() {
        return fileList;
    }

    protected void add(FileTransferRequest ftr) {
        fileList.add(ftr);
    }

    protected void add(Vector newFileReq) {
        for (int i = 0; i < newFileReq.size(); i++) {
            add((FileTransferRequest) newFileReq.get(i));
        }
    }

    public void add(FileVector fw) {
        add(fw.getFilesRequest());
    }
}
