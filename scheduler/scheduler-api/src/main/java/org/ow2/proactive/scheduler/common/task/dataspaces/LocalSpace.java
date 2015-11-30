/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
