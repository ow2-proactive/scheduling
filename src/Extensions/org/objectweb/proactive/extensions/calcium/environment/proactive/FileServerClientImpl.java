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
package org.objectweb.proactive.extensions.calcium.environment.proactive;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.objectweb.proactive.api.ProFileTransfer;
import org.objectweb.proactive.core.filetransfer.RemoteFile;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.calcium.environment.FileServer;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.environment.StoredFile;


public class FileServerClientImpl implements FileServerClient,
    java.io.Serializable {
    Node node;
    FileServer fserver;

    public FileServerClientImpl(Node node, FileServer fserver) {
        this.node = node;
        this.fserver = fserver;
    }

    public void commit(long fileId, int refCountDelta) {
        logger.debug(fserver.getClass());
        fserver.commit(fileId, refCountDelta);
    }

    public void fetch(StoredFile rfile, File localDst)
        throws IOException {
        fserver.canFetch(rfile);

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Pulling file:" + rfile.location + " -> " +
                    localDst);
            }
            RemoteFile fetchedFile = ProFileTransfer.pull(node, rfile.location,
                    localDst);
            fetchedFile.waitForFinishedTransfer();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Unable to fetch remote file: " + rfile);
        }
    }

    public StoredFile store(File current, int refCount)
        throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Storing data for file:" + current);
        }
        StoredFile rfile = fserver.register();

        try {
            //SkeletonSystemImpl.copyFile(localFile, dst);
            RemoteFile sentFile = ProFileTransfer.push(current, node,
                    rfile.location);
            sentFile.waitForFinishedTransfer();
        } catch (Exception e) {
            //If exception happens, then unstore the file.
            fserver.unregister(rfile.fileId);
            e.printStackTrace();
            throw new IOException("Unable to store file on File Server: src=" +
                current);
        }

        //now mark as stored
        return fserver.dataHasBeenStored(rfile, refCount);
    }

    public StoredFile store(URL current) throws IOException {
        return fserver.registerAndStore(current);
    }

    public void shutdown() {
        fserver.shutdown();
    }
}
