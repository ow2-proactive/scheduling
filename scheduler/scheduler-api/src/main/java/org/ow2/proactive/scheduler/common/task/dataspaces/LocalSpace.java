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
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * LocalSpace is the interface used to access the local file space of the ProActive Node used to executed the Task
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 4.0
 */
@PublicAPI
public interface LocalSpace {

    /**
     * Returns a File object corresponding to the path given as parameter relative to the LocalSpace root
     * @param path path to resolve
     * @return a File object
     * @throws FileSystemException
     */
    File getFile(String path) throws FileSystemException;

    /**
     * Returns a list of File objects found by resolving the pattern given in parameter  <br>
     * The following special characters can be used : <br>
     *  ** matches zero or more directories <br>
     *  * matches zero or more characters<br>
     *  ? matches one character
     *
     *
     * @param pattern pattern to locate files
     * @return a set of File objects
     * @throws FileSystemException
     */
    Set<File> getFiles(String pattern) throws FileSystemException;

    /**
     * Deletes all files found by resolving the given pattern
     * The following special characters can be used : <br>
     *  ** matches zero or more directories <br>
     *  * matches zero or more characters<br>
     *  ? matches one character
     * @param pattern pattern to locate files
     * @throws FileSystemException
     */
    void deleteFiles(String pattern) throws FileSystemException;

    /**
     * Delete the given file or folder(including its content) inside the local space
     * @param file path to the local file (relative to the LocalSpace root) to delete
     * @throws FileSystemException
     */
    void deleteFile(File file) throws FileSystemException;

    /**
     * Returns the root File of the LocalSpace
     * @return root File object
     * @throws FileSystemException
     */
    File getLocalRoot() throws FileSystemException;
}
