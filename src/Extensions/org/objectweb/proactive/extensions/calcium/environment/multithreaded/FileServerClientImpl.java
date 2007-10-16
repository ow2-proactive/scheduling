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
package org.objectweb.proactive.extensions.calcium.environment.multithreaded;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.objectweb.proactive.extensions.calcium.environment.FileServer;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.environment.RemoteFile;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;


public class FileServerClientImpl implements FileServerClient {
    FileServer fserver;

    public FileServerClientImpl(FileServer fserver) {
        this.fserver = fserver;
    }

    public void commit(long fileId, int refCountDelta) {
        fserver.commit(fileId, refCountDelta);
    }

    public void fetch(RemoteFile rfile, File localDst)
        throws IOException {
        fserver.canFetch(rfile);

        SkeletonSystemImpl.copyFile(rfile.location, localDst);
    }

    public void shutdown() {
        fserver.shutdown();
    }

    public RemoteFile store(File current, int refCount)
        throws IOException {
        RemoteFile rfile = fserver.register();

        SkeletonSystemImpl.copyFile(current, rfile.location);

        //now mark as stored
        return fserver.dataHasBeenStored(rfile, refCount);
    }

    public RemoteFile store(URL current) throws IOException {
        return fserver.registerAndStore(current);
    }
}
