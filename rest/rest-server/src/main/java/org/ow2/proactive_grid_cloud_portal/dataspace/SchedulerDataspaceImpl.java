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
package org.ow2.proactive_grid_cloud_portal.dataspace;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.SchedulerSpaceInterface;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive_grid_cloud_portal.common.Session;
import org.ow2.proactive_grid_cloud_portal.scheduler.exception.NotConnectedRestException;


public class SchedulerDataspaceImpl implements SchedulerSpaceInterface {

    private static final Logger logger = Logger.getLogger(SchedulerDataspaceImpl.class);

    private static RestDataspaceImpl dataspaceRestApi = new RestDataspaceImpl();

    private Session session;

    public SchedulerDataspaceImpl(String sessionId) throws NotConnectedRestException {
        session = dataspaceRestApi.checkSessionValidity(sessionId);
    }

    @Override
    public boolean isFolder(String dataspace, String pathname) throws NotConnectedException, PermissionException {
        try {
            return resolveFile(dataspace, pathname).isFolder();
        } catch (FileSystemException e) {
            logger.debug(String.format("Can't parse the file [%s] in the dataspace [%s].", pathname, dataspace), e);
            return false;
        }
    }

    @Override
    public boolean checkFileExists(String dataspace, String pathname)
            throws NotConnectedException, PermissionException {
        try {
            return resolveFile(dataspace, pathname).exists();
        } catch (FileSystemException e) {
            logger.debug(String.format("Can't parse the file [%s] in the dataspace [%s].", pathname, dataspace), e);
            return false;
        }
    }

    private FileObject resolveFile(String dataspace, String pathname)
            throws NotConnectedException, FileSystemException, PermissionException {
        switch (dataspace) {
            case SchedulerConstants.GLOBALSPACE_NAME:
                return dataspaceRestApi.fileSystem(session).resolveFileInGlobalspace(pathname);
            case SchedulerConstants.USERSPACE_NAME:
                return dataspaceRestApi.fileSystem(session).resolveFileInUserspace(pathname);
            default:
                throw new IllegalArgumentException("Invalid dataspace name: " + dataspace);
        }
    }

}
