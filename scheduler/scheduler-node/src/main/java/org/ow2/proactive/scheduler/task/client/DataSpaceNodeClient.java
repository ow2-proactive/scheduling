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
package org.ow2.proactive.scheduler.task.client;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.task.dataspaces.FileSystemException;
import org.ow2.proactive.scheduler.common.task.dataspaces.RemoteSpace;
import org.ow2.proactive.scheduler.rest.ds.DataSpaceClient;
import org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient;


/**
 * DataSpace api available as a variable during the execution of a task. it is based on the RemoteSpace interface.
 *
 * @author ActiveEon Team
 */
@PublicAPI
public class DataSpaceNodeClient implements RemoteSpace, Serializable {

    private final SchedulerNodeClient schedulerNodeClient;

    private transient DataSpaceClient dataSpaceClient;

    private final IDataSpaceClient.Dataspace space;

    private final String schedulerRestUrl;

    private transient RemoteSpace spaceProxy;

    public DataSpaceNodeClient(SchedulerNodeClient schedulerNodeClient, IDataSpaceClient.Dataspace space,
            String schedulerRestUrl) {
        this.schedulerNodeClient = schedulerNodeClient;
        this.space = space;
        this.schedulerRestUrl = schedulerRestUrl;
    }

    /**
     * Initialize dataSpaceClient and spaceProxy when connect is called. This late initialization will garantee
     * that this object upon restore (from a Serialized source) will be able to reconstruct the needed objects
     * to work properly.
     */
    private void lazyInit() {
        this.dataSpaceClient = new DataSpaceClient();
        switch (space) {
            case GLOBAL:
                spaceProxy = dataSpaceClient.getGlobalSpace();
                break;
            case USER:
                spaceProxy = dataSpaceClient.getUserSpace();
                break;
            default:
                throw new IllegalStateException("Unknown space : " + space);
        }
    }

    /**
     * Test if this object was previously initialized. Use lazy initialization to reconstruct this object
     * after deserialization.
     *
     * @see this.lazyInit
     *
     * @return true if this object has been initialized, false otherwise.
     */
    private boolean isInitialized() {
        if (dataSpaceClient != null && spaceProxy != null)
            return true;
        return false;
    }

    /**
     * Connects to the dataspace at the default schedulerRestUrl, using the current user credentials
     *
     * @throws Exception FileSystemException as required by @see RemoteSpace interface.
     */
    public void connect() throws Exception {
        connect(schedulerRestUrl);
    }

    /**
     * Connects to the dataspace at the specified schedulerRestUrl, using the current user credentials
     *
     * @param url schedulerRestUrl of the scheduler
     * @throws Exception
     */
    public void connect(String url) throws Exception {
        lazyInit();
        schedulerNodeClient.connect(url);
        this.dataSpaceClient.init(url, schedulerNodeClient);
    }

    private void renewSession() throws FileSystemException {
        if (!isInitialized())
            throw new FileSystemException("Client not connected, call connect() before using the scheduler client");
        try {
            schedulerNodeClient.renewSession();
        } catch (NotConnectedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> listFiles(String remotePath, String pattern) throws FileSystemException {
        renewSession();
        return spaceProxy.listFiles(remotePath, pattern);
    }

    @Override
    public void pushFile(File localPath, String remotePath) throws FileSystemException {
        renewSession();
        spaceProxy.pushFile(localPath, remotePath);
    }

    @Override
    public void pushFiles(File localDirectory, String pattern, String remotePath) throws FileSystemException {
        renewSession();
        spaceProxy.pushFiles(localDirectory, pattern, remotePath);
    }

    @Override
    public File pullFile(String remotePath, File localPath) throws FileSystemException {
        renewSession();
        return spaceProxy.pullFile(remotePath, localPath);
    }

    @Override
    public Set<File> pullFiles(String remotePath, String pattern, File localPath) throws FileSystemException {
        renewSession();
        return spaceProxy.pullFiles(remotePath, pattern, localPath);
    }

    @Override
    public void deleteFile(String remotePath) throws FileSystemException {
        renewSession();
        spaceProxy.deleteFile(remotePath);
    }

    @Override
    public void deleteFiles(String remotePath, String pattern) throws FileSystemException {
        renewSession();
        spaceProxy.deleteFiles(remotePath, pattern);
    }

    @Override
    public List<String> getSpaceURLs() throws NotConnectedException, PermissionException, FileSystemException {
        renewSession();
        return spaceProxy.getSpaceURLs();
    }

    @Override
    public InputStream getInputStream(String remotePath) throws FileSystemException {
        renewSession();
        return spaceProxy.getInputStream(remotePath);
    }

    @Override
    public OutputStream getOutputStream(String remotePath) throws FileSystemException {
        renewSession();
        return spaceProxy.getOutputStream(remotePath);
    }
}
