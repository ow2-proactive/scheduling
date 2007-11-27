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
package org.objectweb.proactive.extensions.calcium.environment;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.environment.StoredFile;
import org.objectweb.proactive.extensions.calcium.system.HashSum;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;


public class FileServer {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_SYSTEM);
    private File rootDir;
    long nextId;
    HashMap<Long, StoredFile> unstored;
    HashMap<Long, Long> stored;

    /**
     * Empty constructor for ProActive  MOP.
     *
     * This constructor does not instantiate the fields of this class!
     * Please invoke initFileServer(...) before using this object!
     */
    public FileServer() {
    }

    public void initFileServer() {
        if (this.rootDir != null) {
            return; //already instantiated this object
        }

        File fsRoot = SkeletonSystemImpl.newDirInTmp("calcium-fileserver");
        File rootDir = SkeletonSystemImpl.newRandomNamedDirIn(fsRoot);

        initFileServer(rootDir);
    }

    public void initFileServer(File rootDir) {
        if (this.rootDir != null) {
            return; //already instantiated this object
        }

        SkeletonSystemImpl.checkWritableDirectory(rootDir);
        this.rootDir = rootDir;

        if (logger.isDebugEnabled()) {
            logger.debug("FileServer running in :" + rootDir);
        }

        this.nextId = 0;

        this.unstored = new HashMap<Long, StoredFile>();
        this.stored = new HashMap<Long, Long>();
    }

    public synchronized StoredFile register() throws IOException {
        long fileId = getNewId();

        File dst = new File(rootDir, fileId + ".dat");

        if (logger.isDebugEnabled()) {
            logger.debug("FileServer registering " + dst);
        }

        StoredFile rfile = new StoredFile(dst, fileId, dst.length());
        unstored.put(fileId, rfile);

        return rfile;
    }

    public synchronized StoredFile dataHasBeenStored(StoredFile rfile, int count)
        throws IOException {
        if (count <= 0) {
            throw new IllegalArgumentException(
                "Illegal initial reference count:" + count);
        }

        if (!unstored.containsKey(rfile.fileId)) {
            throw new IllegalArgumentException(
                "RemoteFile is not marked as unstored" + rfile.fileId);
        }

        if (stored.containsKey(rfile.fileId)) {
            throw new IllegalArgumentException(
                "RemoteFile is already marked as stored" + rfile.fileId);
        }

        StoredFile rf = unstored.remove(rfile.fileId);

        if (!rfile.equals(rf)) {
            throw new IllegalArgumentException("RemoteFile was modified " +
                rfile);
        }

        stored.put(rfile.fileId, new Long(count));

        try {
            rf.md5sum = HashSum.md5sum(rf.location);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IOException("Can't perform md5sum");
        }

        return rf;
    }

    public synchronized void canFetch(StoredFile rfile)
        throws IOException {
        if (!stored.containsKey(rfile.fileId)) {
            throw new IllegalArgumentException("RemoteFile in stored list: " +
                rfile.fileId);
        }

        if (!rfile.location.exists()) {
            throw new IOException("File doest not exist: " + rfile.location);
        }

        if (!rfile.location.isFile()) {
            throw new IOException("File is not a file: " + rfile.location);
        }

        if (!rfile.location.canRead()) {
            throw new IOException("Can't read file: " + rfile.location);
        }
    }

    public synchronized StoredFile registerAndStore(URL remoteURL)
        throws IOException {
        long fileId = getNewId();
        File dst = new File(rootDir, fileId + ".dat");

        SkeletonSystemImpl.download(remoteURL, dst);

        StoredFile rfile = new StoredFile(dst, fileId, dst.length());
        try {
            rfile.md5sum = HashSum.md5sum(rfile.location);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IOException("Can't perform md5sum");
        }

        return rfile;
    }

    public void shutdown() {
        if (logger.isDebugEnabled()) {
            logger.debug("Shutting down File Server. Cleaning root directory:" +
                this.rootDir);
        }
        SkeletonSystemImpl.deleteDirectory(this.rootDir);
    }

    public synchronized void commit(long fileId, int delta) {
        if (!stored.containsKey(fileId)) {
            throw new IllegalArgumentException(
                "Cannot change reference count on if file is not stored:" +
                fileId);
        }

        logger.debug("FileServer commiting id=" + fileId + " delta=" + delta);

        Long c = stored.get(fileId) + delta;

        if (c > 0) {
            stored.put(fileId, c);
        } else { //remove the file from storage
            if (logger.isDebugEnabled()) {
                logger.debug("FileServer file " + fileId +
                    " is deleted (refererence decreased to:" + c + ")");
            }

            stored.remove(fileId);
            File f = new File(rootDir, fileId + ".dat");
            f.delete();
        }
    }

    public synchronized void unregister(long fileId) {
        if (unstored.containsKey(fileId)) {
            unstored.remove(fileId);
        }
    }

    private synchronized long getNewId() {
        return nextId++;
    }
}
