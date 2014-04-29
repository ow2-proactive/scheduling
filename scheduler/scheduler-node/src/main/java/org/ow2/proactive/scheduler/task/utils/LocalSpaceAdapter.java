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
package org.ow2.proactive.scheduler.task.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.FileSelector;
import org.objectweb.proactive.extensions.dataspaces.exceptions.DataSpacesException;
import org.objectweb.proactive.utils.StackTraceUtil;
import org.ow2.proactive.scheduler.common.task.dataspaces.FileSystemException;
import org.ow2.proactive.scheduler.common.task.dataspaces.LocalSpace;
import org.ow2.proactive.scheduler.task.TaskLauncher;


/**
 * LocalSpaceAdapter default implementation of the LocalSpace interface
 *
 * @author The ProActive Team
 **/
public class LocalSpaceAdapter implements LocalSpace {

    public static final Logger logger = Logger.getLogger(RemoteSpaceAdapter.class);

    private DataSpacesFileObject localDataSpace;

    public LocalSpaceAdapter(DataSpacesFileObject localDataSpace) {
       this.localDataSpace = localDataSpace;
    }

    private File convertToRelative(File absolutePath) throws URISyntaxException, DataSpacesException {
        String relPath = absolutePath.getPath().replace(TaskLauncher.convertDataSpaceToFileIfPossible(localDataSpace, true),"");
        if (relPath.startsWith(File.separator)) {
            relPath = relPath.substring(1);
        }
        return new File(relPath);
    }

    @Override
    public File getFile(String path) throws FileSystemException {
        try {
            DataSpacesFileObject fo = localDataSpace.resolveFile(RemoteSpaceAdapter.stripLeadingSlash(path));
            return new File(TaskLauncher.convertDataSpaceToFileIfPossible(fo, true));
        } catch (Exception e) {
            throw new FileSystemException(StackTraceUtil.getStackTrace(e));
        }
    }

    @Override
    public Set<File> getFiles(String pattern) throws FileSystemException {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking for pattern : " + pattern + " in " + localDataSpace.getRealURI());
        }
        HashSet<File> fileResuls = new HashSet<File>();
        try {
            ArrayList<DataSpacesFileObject> results = null;
            results = RemoteSpaceAdapter.getFilesFromPattern(localDataSpace, pattern);

            for (DataSpacesFileObject res : results) {
                fileResuls.add(new File(TaskLauncher.convertDataSpaceToFileIfPossible(res, true)));
            }
        } catch (Exception e) {
            throw new FileSystemException(StackTraceUtil.getStackTrace(e));
        }
        return fileResuls;
    }

    @Override
    public void deleteFiles(String pattern) throws FileSystemException {
        try {
            ArrayList<DataSpacesFileObject> todelete = RemoteSpaceAdapter.getFilesFromPattern(localDataSpace, pattern);
            for (DataSpacesFileObject dest : todelete) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Deleting "+dest.getRealURI());
                }
                dest.delete();
            }
        } catch (org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException e) {
            throw new FileSystemException(StackTraceUtil.getStackTrace(e));
        }
    }

    @Override
    public void deleteFile(File file) throws FileSystemException {
        try {
            if (file.isAbsolute()) {
                file = convertToRelative(file);
            }
            DataSpacesFileObject todelete = localDataSpace.resolveFile(RemoteSpaceAdapter.stripLeadingSlash(file.getPath()));
            int nb_del = todelete.delete(FileSelector.SELECT_ALL);
        } catch (Exception e) {
            throw new FileSystemException(StackTraceUtil.getStackTrace(e));
        }
    }

    @Override
    public File getLocalRoot() throws FileSystemException {
        try {
            return new File(TaskLauncher.convertDataSpaceToFileIfPossible(localDataSpace, true));
        } catch (Exception e) {
            throw new FileSystemException(StackTraceUtil.getStackTrace(e));
        }
    }
}
