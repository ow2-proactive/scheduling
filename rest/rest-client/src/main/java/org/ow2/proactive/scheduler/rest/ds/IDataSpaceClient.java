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
package org.ow2.proactive.scheduler.rest.ds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.task.dataspaces.RemoteSpace;
import org.ow2.proactive_grid_cloud_portal.common.FileType;
import org.ow2.proactive_grid_cloud_portal.dataspace.dto.ListFile;


public interface IDataSpaceClient {

    enum Dataspace {
        USER("user"),
        GLOBAL("global");

        private String value;

        Dataspace(String value) {
            this.value = value;
        }

        String value() {
            return value;
        }
    }

    /**
     * Creates a new file or folder in the specified dataspace <i>dataspace</i>.
     *
     * @param source the remote source used to identify the type of file to create and its location.
     * @return {@code true} if the creation has succeeded, {@code false} otherwise.
     *
     * @throws NotConnectedException if the client is not logged in or the session has expired
     * @throws PermissionException   if the user does not have permission to upload the file to
     *                               the specified location in the server
     */
    boolean create(IRemoteSource source) throws NotConnectedException, PermissionException;

    boolean download(IRemoteSource source, ILocalDestination destination)
            throws NotConnectedException, PermissionException;

    boolean upload(ILocalSource source, IRemoteDestination destination)
            throws NotConnectedException, PermissionException;

    /**
     * Returns a {@link ListFile} type object which contains the names of files
     * and directories in the specified location of the <i>dataspace</i>.
     *
     * @throws NotConnectedException
     *             if the client is not logged in or the session has expired
     * @throws PermissionException
     *             if the user does not have permission to upload the file to
     *             the specified location in the server
     */
    ListFile list(IRemoteSource source) throws NotConnectedException, PermissionException;

    /**
     * Deletes the specified directory or the file from the <i>dataspace</i>.
     *
     * @throws NotConnectedException
     *             if the client is not logged in or the session has expired
     * @throws PermissionException
     *             if the user does not have permission to upload the file to
     *             the specified location in the server
     */
    boolean delete(IRemoteSource source) throws NotConnectedException, PermissionException;

    /**
     * Returns the metadata map of the specified file in the <i>dataspace</i>.
     *
     * @return an instace of {@link Map}
     * @throws NotConnectedException
     *             if the client is not logged in or the session has expired
     * @throws PermissionException
     *             if the user does not have permission to upload the file to
     *             the specified location in the server
     */
    Map<String, String> metadata(IRemoteSource source) throws NotConnectedException, PermissionException;

    /**
     * Returns a {@link RemoteSpace} implementation instance which represents
     * the <i>globalspace</i>.
     *
     * @return an instance of {@link RemoteSpace}
     */
    RemoteSpace getGlobalSpace();

    /**
     * Returns a {@link RemoteSpace} implementation instance which represents
     * the <i>userspace</i>
     *
     * @return an instance of {@link RemoteSpace}
     */
    RemoteSpace getUserSpace();

    interface IRemoteSource {
        Dataspace getDataspace();

        String getPath();

        List<String> getIncludes();

        List<String> getExcludes();

        FileType getType();
    }

    interface ILocalDestination {
        void readFrom(InputStream is, String encoding) throws IOException;
    }

    interface ILocalSource {
        void writeTo(OutputStream outputStream) throws IOException;

        String getEncoding() throws IOException;
    }

    interface IRemoteDestination {
        Dataspace getDataspace();

        String getPath();
    }

}
