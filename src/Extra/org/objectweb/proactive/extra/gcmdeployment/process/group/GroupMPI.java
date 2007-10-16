/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.gcmdeployment.process.group;

import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.PathElement;


public class GroupMPI extends AbstractGroup {
    private String mpiFileName;
    private String hostsFileName;
    private String hostsNumber;
    private PathElement localPath;
    private PathElement remotePath;
    private String mpiCommandOptions;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return Returns the mpiFileName.
     */
    public String getMpiFileName() {
        return mpiFileName;
    }

    /**
     * @param mpiFileName The mpiFileName to set.
     */
    public void setMpiFileName(String mpiFileName) {
        this.mpiFileName = mpiFileName;
    }

    /**
     * @return Returns the hostsFileName.
     */
    public String getHostsFileName() {
        return hostsFileName;
    }

    /**
     * @param hostsFileName The hostsFileName to set.
     */
    public void setHostsFileName(String hostsFileName) {
        this.hostsFileName = hostsFileName;
    }

    /**
     * @return Returns the hostsNumber.
     */
    public String getHostsNumber() {
        return hostsNumber;
    }

    /**
     * @param hostsNumber The hostsNumber to set.
     */
    public void setHostsNumber(String hostsNumber) {
        this.hostsNumber = hostsNumber;
    }

    /**
     * @return Returns the localPath.
     */
    public PathElement getLocalPath() {
        return localPath;
    }

    /**
     * @param path The localPath to set.
     */
    public void setLocalPath(PathElement path) {
        this.localPath = path;
    }

    /**
     * @return Returns the remotePath.
     */
    public PathElement getRemotePath() {
        return remotePath;
    }

    /**
     * @param path The remotePath to set.
     */
    public void setRemotePath(PathElement path) {
        this.remotePath = path;
    }

    /**
     * @param mpiCommandOptions The mpiCommandOptions to set.
     */
    public void setMpiCommandOptions(String mpiCommandOptions) {
        this.mpiCommandOptions = mpiCommandOptions;
    }

    /**
     * @return Returns the mpiCommandOptions.
     */
    public String getMpiCommandOptions() {
        return this.mpiCommandOptions;
    }
}
