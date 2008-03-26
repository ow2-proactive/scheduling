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

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class provides reference that can access a remote FileServer
 * on the execution environment, from the execution nodes.
 *
 * @author The ProActive Team
 */
public interface FileServerClient {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_SYSTEM);

    /**
     * Stores a file on the FileServer.
     *
     * @param current The local location of the file
     * @param refCount The number of references on this file
     * @return A {@link StoredFile StoredFile} object representing the file on the FileServer.
     * @throws IOException if an error is detected
     */
    public StoredFile store(File current, int refCount) throws IOException;

    /**
     * Retrieves a file from the FileServer
     *
     * @param rfile The reference of the file on the FileServer
     * @param localDst The local destination of the file.
     * @throws IOException if an error is detected.
     */
    public void fetch(StoredFile rfile, File localDst) throws IOException;

    /**
     * Stores a file reachable through an URL on the FileServer.
     *
     * @see #store(File, int)
     */
    public StoredFile store(URL current) throws IOException;

    /**
     * Commits a reference count modification
     *
     * @param fileId The file's unique id on the remote server
     * @param refCountDelta The reference count difference on the server. Can be a positive or negative value.
     */
    public void commit(long fileId, int refCountDelta);

    /**
     * Shuts down the file server.
     */
    public void shutdown();
}
