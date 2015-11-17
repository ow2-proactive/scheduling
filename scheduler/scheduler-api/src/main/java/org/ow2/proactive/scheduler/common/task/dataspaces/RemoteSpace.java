/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task.dataspaces;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * RemoteSpace is the interface used to access remote file spaces
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 4.0
 */
@PublicAPI
public interface RemoteSpace {

    /**
     * Push the given file or directory(including its content) from the local space to the remote space.
     *
     * Several scenarios can occur :
     * - If the localPath is a file and the remotePath represents a directory, the file will be copied inside the remote directory
     * @param localPath path to the local file or folder
     * @param remotePath path in the RemoteSpace where to put the file or folder
     * @throws FileSystemException
     */
    void pushFile(File localPath, String remotePath) throws FileSystemException;

    /**
     * Push the local files described by the given pattern to the remote space
     *
     *  The following special characters can be used inside the pattern : <br>
     *  ** matches zero or more directories <br>
     *  * matches zero or more characters<br>
     *  ? matches one character
     *
     * @param pattern pattern to locate files inside the LocalSpace
     * @param remotePath path in the RemoteSpace where to put the files
     * @throws FileSystemException
     */
    void pushFiles(String pattern, String remotePath) throws FileSystemException;

    /**
     * Pull the given file or folder(including its content) from the remote space to the local space
     * @param remotePath path to the remote file (relative to the RemoteSpace root)
     * @param localPath path in the LocalSpace where to put the file or folder
     * @return the path to the file or directory pulled
     * @throws FileSystemException
     */
    File pullFile(String remotePath, File localPath) throws FileSystemException;

    /**
     * Pull the remote files described by the given pattern to the local space
     *
     *  The following special characters can be used inside the pattern : <br>
     *  ** matches zero or more directories <br>
     *  * matches zero or more characters<br>
     *  ? matches one character
     *
     * @param pattern pattern to locate files inside the RemoteSpace
     * @param localPath path in the LocalSpace where to put the files
     * @return a set of files pulled
     * @throws FileSystemException
     */
    Set<File> pullFiles(String pattern, String localPath) throws FileSystemException;

    /**
     * Delete the given file or folder(including its content) inside the remote space
     * @param remotePath path to the remote file (relative to the RemoteSpace root) to delete
     * @throws FileSystemException
     */
    void deleteFile(String remotePath) throws FileSystemException;

    /**
     * Delete the remote files described by the given pattern
     *
     *  The following special characters can be used inside the pattern : <br>
     *  ** matches zero or more directories <br>
     *  * matches zero or more characters<br>
     *  ? matches one character
     *
     * @param pattern pattern to locate files inside the RemoteSpace
     * @throws FileSystemException
     */
    void deleteFiles(String pattern) throws FileSystemException;

    /**
     * Returns the root URL of the RemoteSpace
     * @return URL to the space
     */
    String getSpaceURL();

    /**
     * Returns an input stream on the specified remote file. It will throw an exception if the file does not exist.
     * @param remotePath path to the remote file
     * @return an input stream
     * @throws FileSystemException
     */
    InputStream getInputStream(String remotePath) throws FileSystemException;

    /**
     * Returns an output stream on the specified remote file. If the file does not exist, it will be created.
     * @param remotePath path to the remote file
     * @return an output stream
     * @throws FileSystemException
     */
    OutputStream getOutputStream(String remotePath) throws FileSystemException;

}
