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
import java.io.Serializable;

import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.util.ProActiveRandom;


/**
 * This class represents a file transfer request. A file transfer
 * request is composed of a source, destination and the result
 * of the operation.
 *
 * @author The ProActive Team 06/06 (mleyton)
 *
 */
public class FileTransferRequest implements Serializable {
    private FileTransferService sourceFTS;
    private FileTransferService destinationFTS;
    private File srcFile;
    private File dstFile;
    private OperationStatus operationFuture;
    private int id;

    /*
    public FileTransferRequest(File srcFile, File dstFile, File futureDstFile,
                    FileTransferService localFTS, FileTransferService remoteFTS) {

            this(srcFile, dstFile, new OperationFuture(futureDstFile), localFTS, remoteFTS);
    }
    */
    public FileTransferRequest(File srcFile, File dstFile,
        OperationStatus futureDstFile, FileTransferService srcFTS,
        FileTransferService dstFTS) {
        this.srcFile = srcFile;
        this.dstFile = dstFile;
        this.operationFuture = futureDstFile;
        this.sourceFTS = srcFTS;
        this.destinationFTS = dstFTS;
        this.id = ProActiveRandom.nextInt();
    }

    public OperationStatus getOperationFuture() {
        return operationFuture;
    }

    public void waitForOperation() {
        ProFuture.waitFor(operationFuture);
    }

    public boolean isAwaited() {
        return ProFuture.isAwaited(operationFuture);
    }

    public File getDstFile() {
        return dstFile;
    }

    public FileTransferService getSourceFTS() {
        return sourceFTS;
    }

    public FileTransferService getDestinationFTS() {
        return destinationFTS;
    }

    public File getSrcFile() {
        return srcFile;
    }

    public void setDstFuture(OperationStatus futureDstFile) {
        this.operationFuture = futureDstFile;
    }

    @Override
    public boolean equals(Object o) {
        FileTransferRequest ftr = (FileTransferRequest) o;
        return getDstFile().equals(ftr.getDstFile()) &&
        getSrcFile().equals(ftr.getSrcFile()) && (getId() == ftr.getId());
    }

    public int getId() {
        return id;
    }

    public void setSourceFTS(FileTransferService sourceFTS) {
        this.sourceFTS = sourceFTS;
    }

    @Override
    public String toString() {
        return getSrcFile().getAbsolutePath() + "->" +
        getDstFile().getAbsolutePath();
    }
}
