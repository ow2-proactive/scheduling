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
package org.objectweb.proactive.extensions.calcium.environment.multithreaded;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.objectweb.proactive.extensions.calcium.environment.FileServer;
import org.objectweb.proactive.extensions.calcium.environment.RemoteFile;
import org.objectweb.proactive.extensions.calcium.system.HashSum;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;


public class LocalFileServer implements FileServer {
    File rootDir;
    long nextId;
    HashMap<Long, Long> refCount;

    public LocalFileServer() {
        // /tmp/calcium-fileserver/XXXXX/
        this(SkeletonSystemImpl.newRandomNamedDirIn(
                SkeletonSystemImpl.newDirInTmp("calcium-fileserver")));
    }

    public LocalFileServer(File rootDir) {
        if (!rootDir.exists() && !rootDir.mkdirs()) {
            throw new IllegalArgumentException("Can't creat directory: " +
                rootDir);
        }

        if (!rootDir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + rootDir);
        }

        if (!rootDir.canWrite()) {
            throw new IllegalArgumentException("Can not write to: " + rootDir);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("FileServer running in :" + rootDir);
        }

        this.rootDir = rootDir;
        this.nextId = 0;
        this.refCount = new HashMap<Long, Long>();
    }

    /*
     *  PUBLIC INTERFACE METHODS
     */
    public synchronized RemoteFile store(File localFile)
        throws IOException {
        long fileId = getNewId();

        if (logger.isDebugEnabled()) {
            logger.debug("FileServer storing " + localFile + " as " + fileId);
        }

        File dst = new File(rootDir, fileId + ".dat");
        SkeletonSystemImpl.copyFile(localFile, dst);

        return getNewLocalFile(fileId, dst);
    }

    public synchronized RemoteFile store(URL remoteFile)
        throws IOException {
        long fileId = getNewId();
        File dst = new File(rootDir, fileId + ".dat");

        SkeletonSystemImpl.download(remoteFile, dst);

        return getNewLocalFile(fileId, dst);
    }

    public void clean() {
        SkeletonSystemImpl.deleteDirectory(rootDir);
    }

    public synchronized void increaseReference(long fileId) {
        if (!refCount.containsKey(fileId)) {
            throw new IllegalArgumentException(
                "Cannot increase reference count on unstored file:" + fileId);
        }

        Long c = refCount.get(fileId) + 1;
        refCount.put(fileId, c);

        if (logger.isDebugEnabled()) {
            logger.debug("FileServer file " + fileId +
                " refererence increase to:" + c);
        }
    }

    public synchronized void discountReference(long fileId) {
        if (!refCount.containsKey(fileId)) {
            throw new IllegalArgumentException(
                "Cannot decrease reference count on unstored file:" + fileId);
        }

        Long c = refCount.get(fileId) - 1;

        if (c != 0) {
            refCount.put(fileId, c);
        } else { //remove the file from storage
            if (logger.isDebugEnabled()) {
                logger.debug("FileServer file " + fileId +
                    " is deleted (refererence decreases to:" + c + ")");
            }

            refCount.remove(fileId);
            File f = new File(rootDir, fileId + ".dat");
            f.delete();
        }
    }

    /*
     *  PRIVATE METHODS
     */
    private synchronized long getNewId() {
        return nextId++;
    }

    private LocalFile getNewLocalFile(long fileId, File dst)
        throws IOException {
        String md5sum;
        try {
            md5sum = HashSum.md5sum(dst);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Unable to perform hash sum on file: " + dst);
        }

        refCount.put(fileId, new Long(1));

        if (logger.isDebugEnabled()) {
            logger.debug("FileServer stored new file in:" + dst);
        }

        return new LocalFile(dst, fileId, dst.length(), md5sum);
    }
}
