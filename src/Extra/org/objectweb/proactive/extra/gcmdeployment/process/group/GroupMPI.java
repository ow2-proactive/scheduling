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
