/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.task.launcher.dataspace;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;


/**
 * Information about a file, that is used to select files during the
 * traversal of a hierarchy.
 */
public class FileSelectInfo {
    private DataSpacesFileObject baseFolder;
    private DataSpacesFileObject file;
    private int depth;

    /**
     * Returns the base folder of the traversal.
     *
     * @return FileObject representing the base folder.
     */
    public DataSpacesFileObject getBaseFolder() {
        return baseFolder;
    }

    void setBaseFolder(final DataSpacesFileObject baseFolder) {
        this.baseFolder = baseFolder;
    }

    /**
     * Returns the file (or folder) to be considered.
     *
     * @return The FileObject.
     */
    public DataSpacesFileObject getFile() {
        return file;
    }

    void setFile(final DataSpacesFileObject file) {
        this.file = file;
    }

    /**
     * Returns the depth of the file relative to the base folder.
     *
     * @return The depth of the file relative to the base folder.
     */
    int getDepth() {
        return depth;
    }

    public void setDepth(final int depth) {
        this.depth = depth;
    }
}
