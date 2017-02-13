/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common.task.dataspaces;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;


/**
 * RemoteSpace is the interface used to access remote file spaces
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 4.0
 */
@PublicAPI
public interface RemoteSpace {

    /**
     * List the content of the given remote directory, using a glob pattern
     * <p>
     * The following special characters can be used inside the pattern : <br>
     * ** matches zero or more directories <br>
     * * matches zero or more characters<br>
     * ? matches one character
     *
     * @param remotePath path in the RemoteSpace where files should be listed. Use "." for remote root path.
     * @param pattern    pattern to locate files
     * @return a list of remote paths matching the pattern
     * @throws FileSystemException
     * @see "https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)"
     */
    List<String> listFiles(String remotePath, String pattern) throws FileSystemException;

    /**
     * Push the given file or directory(including its content) from the local space to the remote space.
     *
     * When pushing a file or directory, the remotePath must contain the target new file or directory
     * @param localPath path to the local file or directory
     * @param remotePath path in the RemoteSpace where to put the file or folder. Use "." for remote root path.
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
     * @param localDirectory local directory used as base
     * @param pattern pattern to locate files inside the localDirectory
     * @param remotePath path in the RemoteSpace where to put the files. Use "." for remote root path.
     * @throws FileSystemException
     * @see "https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)"
     */
    void pushFiles(File localDirectory, String pattern, String remotePath) throws FileSystemException;

    /**
     * Pull the given file or folder(including its content) from the remote space to the local space
     *
     * @param remotePath path to the remote file (relative to the RemoteSpace root)
     * @param localPath path in the local file system where to put the file or folder
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
     * @param remotePath path in the RemoteSpace used as base for the pattern (e.g. "/")
     * @param pattern pattern to locate files inside the RemoteSpace
     * @param localPath path in the local file system where to put the files
     * @return a set of files pulled
     * @throws FileSystemException
     * @see "https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)"
     */
    Set<File> pullFiles(String remotePath, String pattern, File localPath) throws FileSystemException;

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
     * @param remotePath path in the RemoteSpace used as base for the pattern (e.g. "/")
     * @param pattern pattern to locate files inside the RemoteSpace
     * @throws FileSystemException
     * @see "https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)"
     */
    void deleteFiles(String remotePath, String pattern) throws FileSystemException;

    /**
     * Returns the URLs of the RemoteSpace
     * @return URL to the space
     */
    List<String> getSpaceURLs() throws NotConnectedException, PermissionException, FileSystemException;

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
