/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
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
package org.ow2.proactive.scheduler.task.client;


import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.task.dataspaces.FileSystemException;
import org.ow2.proactive.scheduler.common.task.dataspaces.RemoteSpace;
import org.ow2.proactive.scheduler.rest.ds.DataSpaceClient;
import org.ow2.proactive.scheduler.rest.ds.IDataSpaceClient;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

/**
 * DataSpace api available as a variable during the execution of a task. it is based on the RemoteSpace interface.
 *
 * @author ActiveEon Team
 */
@PublicAPI
public class DataSpaceNodeClient implements RemoteSpace {

    private final SchedulerNodeClient schedulerNodeClient;
    private final DataSpaceClient dataSpaceClient;
    private final IDataSpaceClient.Dataspace space;
    private final String schedulerRestUrl;

    private final RemoteSpace spaceProxy;


    public DataSpaceNodeClient(SchedulerNodeClient schedulerNodeClient, IDataSpaceClient.Dataspace space, String schedulerRestUrl) {
        this.schedulerNodeClient = schedulerNodeClient;
        this.dataSpaceClient = new DataSpaceClient();
        this.space = space;
        this.schedulerRestUrl = schedulerRestUrl;
        switch (space) {
            case GLOBAL:
                spaceProxy = dataSpaceClient.getGlobalSpace();
                break;
            case USER:
                spaceProxy = dataSpaceClient.getUserSpace();
                break;
            default:
                throw new IllegalStateException("Unkown space : " + space);
        }
    }

    /**
     * Connects to the dataspace at the default schedulerRestUrl, using the current user credentials
     *
     * @throws Exception
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
        schedulerNodeClient.connect(url);
        this.dataSpaceClient.init(url, schedulerNodeClient);
    }

    private void renewSession() {
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
