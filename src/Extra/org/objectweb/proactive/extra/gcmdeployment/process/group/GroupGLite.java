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

import java.util.LinkedList;
import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.PathElement;


public class GroupGLite extends AbstractGroup {
    private String fileName;
    private String filePath;
    private String gLiteCommandPath;
    private int jobNodeNumber;
    private String jobType;
    private String jobJobType;
    private String jobExecutable;
    private String stdout;
    private String stdin;
    private String stderr;
    private String jobOutputStorageElement;
    private String jobVO;
    private String jobRetryCount;
    private String jobMyProxyServer;
    private String jobDataAccessProtocol;
    private String jobStorageIndex;
    private String jobEnvironment;
    private String jobRequirements;
    private String jobRank;
    private String jobFuzzyRank;
    private String netServer;
    private String configFile;
    private boolean jdlRemote;
    private String remoteFilePath;
    private boolean configFileOption;
    private String jobArgument;
    private LinkedList jobInputSB;
    private LinkedList jobOutputSB;
    private String dataCatalog;

    @Override
    public List<String> internalBuildCommands() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return Returns the fileName.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return Returns the filePath.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param nodeValue The filePath to set.
     */
    public void setFilePath(String nodeValue) {
        this.filePath = nodeValue;
    }

    /**
     * @return Returns the command_path.
     */
    public String getGLiteCommandPath() {
        return gLiteCommandPath;
    }

    /**
     * @param commandPath The command_path to set.
     */
    public void setGLiteCommandPath(String commandPath) {
        this.gLiteCommandPath = commandPath;
    }

    /**
    *
    * @return number of desired CPUs (just useful if jobType = mpich)
    */
    public int getJobNodeNumber() {
        return jobNodeNumber;
    }

    /**
     *
     * @param jobNodeNumber number of desired CPUs (just useful if jobType = mpich)
     */
    public void setJobNodeNumber(int jobNodeNumber) {
        this.jobNodeNumber = jobNodeNumber;
    }

    /**
     * @return  type (so far,just "Job" is supported)
     */
    public String getJobType() {
        return jobType;
    }

    /**
     * @param jobType type (so far,just "Job" is supported)
     */
    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    /**
     *
     * @return jobtype (so far, just "normal" and "mpich" are supported)
     */
    public String getJobJobType() {
        return jobJobType;
    }

    /**
     *
     * @param jobJobType jobtype (so far, just "normal" and "mpich" are supported)
     */
    public void setJobJobType(String jobJobType) {
        this.jobJobType = jobJobType;
    }

    /**
     * @return Executable command (usually absolute java command)
     */
    public String getJobExecutable() {
        return jobExecutable;
    }

    /**
     * @param jobExecutable Executable command (usually absolute java command)
     */
    public void setJobExecutable(String jobExecutable) {
        this.jobExecutable = jobExecutable;
    }

    /**
     * @return output filename (must also figure in the OutputSandbox to be usefull)
     */
    public String getStdout() {
        return stdout;
    }

    /**
     * @param stdout output filename (must also figure in the OutputSandbox to be usefull)
     */
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    /**
     * @return input filename
     */
    public String getStdin() {
        return stdin;
    }

    /**
     * @param stdin input filename
     */
    public void setStdin(String stdin) {
        this.stdin = stdin;
    }

    /**
     * @return stderr filename  (must also figure in the OutputSandbox to be useful)
     */
    public String getStderr() {
        return stderr;
    }

    /**
     * @param stderr stderr filename  (must also figure in the OutputSandbox to be useful)
     */
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    /**
     * @return output se  (URL of the Storage Element where the user wants to store the output data)
     */
    public String getJobOutputStorageElement() {
        return jobOutputStorageElement;
    }

    /**
     * @param jobOutputSE output se (URL of the Storage Element where the user wants to store the output data).
     */
    public void setJobOutputStorageElement(String jobOutputSE) {
        this.jobOutputStorageElement = jobOutputSE;
    }

    /**
     * @return Virtual Organization name
     */
    public String getJobVO() {
        return jobVO;
    }

    /**
     * @param jobVO Virtual Organization name
     */
    public void setJobVO(String jobVO) {
        this.jobVO = jobVO;
    }

    /**
     * @return maximum number of deep job re-submissions to be done in case of failure due to some grid component (i.e. not to the job itself).
     */
    public String getJobRetryCount() {
        return jobRetryCount;
    }

    /**
     * @param jobRetryCount maximum number of deep job re-submissions to be done in case of failure due to some grid component (i.e. not to the job itself).
     */
    public void setJobRetryCount(String jobRetryCount) {
        this.jobRetryCount = jobRetryCount;
    }

    /**
     * @return hostname of a MyProxy server where the user has registered her/his long-term proxy certificate.
     */
    public String getJobMyProxyServer() {
        return jobMyProxyServer;
    }

    /**
     * @param jobMyProxyServer hostname of a MyProxy server where the user has registered her/his long-term proxy certificate.
     */
    public void setJobMyProxyServer(String jobMyProxyServer) {
        this.jobMyProxyServer = jobMyProxyServer;
    }

    /**
     * @return string or list of strings representing the protocol or the list of protocols that the application is able to "speak" for accessing files listed in InputData on a given SE.
     */
    public String getJobDataAccessProtocol() {
        return jobDataAccessProtocol;
    }

    /**
     * @param jobDataAccessProtocol string or list of strings representing the protocol or the list of protocols that the application is able to "speak" for accessing files listed in InputData on a given SE.
     */
    public void setJobDataAccessProtocol(String jobDataAccessProtocol) {
        this.jobDataAccessProtocol = jobDataAccessProtocol;
    }

    /**
     * @return attribute kept for backward compatibility and will be soon deprecated. Use DataRequirements attribute 3.14 instead.
     */
    public String getJobStorageIndex() {
        return jobStorageIndex;
    }

    /**
     * @param jobStorageIndex attribute kept for backward compatibility and will be soon deprecated. Use DataRequirements attribute 3.14 instead.
     */
    public void setJobStorageIndex(String jobStorageIndex) {
        this.jobStorageIndex = jobStorageIndex;
    }

    /**
     * @return list of string representing environment settings that have to be performed on the execution machine and are needed by the job to run properly.
     */
    public String getJobEnvironment() {
        return jobEnvironment;
    }

    /**
     * @param nodeValue list of string representing environment settings that have to be performed on the execution machine and are needed by the job to run properly.
     */
    public void setJobEnvironment(String nodeValue) {
        this.jobEnvironment = nodeValue;
    }

    /**
     * @return list of string representing environment settings that have to be performed on the execution machine and are needed by the job to run properly.
     */
    public String getJobRequirements() {
        return jobRequirements;
    }

    /**
     * @param jobRequirements Boolean ClassAd expression that uses C-like operators. It represents job requirements on resources. The Requirements expression can contain attributes that describe the

     */
    public void setJobRequirements(String jobRequirements) {
        this.jobRequirements = jobRequirements;
    }

    /**
     * @return ClassAd Floating-Point expression that states how to rank CEs that have already met the Requirements expression.
     */
    public String getJobRank() {
        return jobRank;
    }

    /**
     * @param jobRank ClassAd Floating-Point expression that states how to rank CEs that have already met the Requirements expression.
     */
    public void setJobRank(String jobRank) {
        this.jobRank = jobRank;
    }

    /**
     * @return a Boolean (true/false) attribute that enables fuzziness in the ranking computation.
     */
    public String getJobFuzzyRank() {
        return jobFuzzyRank;
    }

    /**
     * @param jobFuzzyRank a Boolean (true/false) attribute that enables fuzziness in the ranking computation.
     */
    public void setJobFuzzyRank(String jobFuzzyRank) {
        this.jobFuzzyRank = jobFuzzyRank;
    }

    /**
     * @return Returns the netServer.
     */
    public String getNetServer() {
        return netServer;
    }

    /**
     * @param netServer The netServer to set.
     */
    public void setNetServer(String netServer) {
        this.netServer = netServer;
    }

    /**
     * @return Returns the configFile.
     */
    public String getConfigFile() {
        return configFile;
    }

    /**
     * @param path The configFile to set.
     */
    public void setConfigFile(String path) {
        this.configFile = path;
    }

    public void setConfigFileOption(boolean b) {
        configFileOption = b;
    }

    /**
     * @return Returns the jdlRemote.
     */
    public boolean isJdlRemote() {
        return jdlRemote;
    }

    /**
     * @param jdlRemote The jdlRemote to set.
     */
    public void setJdlRemote(boolean jdlRemote) {
        this.jdlRemote = jdlRemote;
    }

    /**
     * @return Returns the remoteFilePath.
     */
    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    /**
     * @param path The remoteFilePath to set.
     */
    public void setRemoteFilePath(String path) {
        this.remoteFilePath = path;
    }

    /**
     * @return arguments to the jobExecutable
     */
    public String getJobArgument() {
        return jobArgument;
    }

    /**
     * @param jobArgument arguments to the jobExecutable
     */
    public void setJobArgument(String jobArgument) {
        this.jobArgument = jobArgument;
    }

    /**
     * @param entry string representing a file that will be in the gLite InputSandbox
     * @return true if successfully added, false if not (does not asses file properties)
     */
    public boolean addInputSBEntry(String entry) {
        if (jobInputSB == null) {
            jobInputSB = new LinkedList();
        }
        return jobInputSB.add(entry);
    }

    /**
     * @param entry string representing a file that will be in the gLite OutputSandbox
     * @return job fails in the case file does not output during job
     */
    public boolean addOutputSBEntry(String entry) {
        if (jobOutputSB == null) {
            jobOutputSB = new LinkedList();
        }
        return jobOutputSB.add(entry);
    }

    public void setDataCatalog(String dataCatalog) {
        this.dataCatalog = dataCatalog;
    }
}
