/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matsci.common.data;

import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;


/**
 * PASolveMatSciGlobalConfig global configuration of a PAsolve job
 *
 * @author The ProActive Team
 */
public class PASolveMatSciGlobalConfig implements Serializable {

    private static final long serialVersionUID = 32L;

    /**
     * Name of the Scheduler job
     */
    protected String jobName = null;

    /**
     * Description of the Scheduler job
     */
    protected String jobDescription = null;

    /**
     * Debug Mode
     **/
    protected boolean debug = false;

    /**
     * login of the user submitting the job
     **/
    protected String login;

    /**
     * The tasks are in a separate JVM process
     **/
    protected boolean fork = false;

    /**
     * The tasks are executed under the account of the current user
     **/
    protected boolean runAsMe = false;

    /**
     * Preferred Version to use
     **/
    protected String versionPref = null;

    /**
     * Versions forbidden to use
     **/
    protected HashSet<String> versionRej = new HashSet<String>();

    /**
     * Minimum version to use
     **/
    protected String versionMin = null;

    /**
     * Maximum version to use
     */
    protected String versionMax = null;

    /**
     * Transfers source to the remote engine
     */
    protected boolean transferEnv = false;

    /**
     * url of the selection script used to check Matlab or Scilab installation
     */
    protected String checkMatSciUrl = null;

    /**
     * url of a custom selection script, if any
     */
    protected String customScriptUrl = null;

    /**
     * url of a custom selection script, if any
     */
    protected boolean customScriptStatic = false;

    /**
     * Parameters of the custom script
     */
    private String customScriptParams = null;

    /**
     * uri of the local dataspace (available when executing the task)
     */
    protected URI localSpace;

    /**
     * matlab or scilab startup options on windows machines
     */
    private String[] windowsStartupOptions = null;

    /**
     * matlab or scilab startup options on linux machines
     */
    private String[] linuxStartupOptions = null;

    /**
     * Name of source zip file
     */
    protected String sourceZipFileName = null;

    /**
     * parameters of the
     */
    protected String[] scriptParams;

    /**
     * Name of env mat file (TransferEnv)
     */
    protected String envMatFileName = null;

    /**
     * Names of global variables (TransferEnv)
     */
    protected String[] envGlobalNames = null;

    /**
     * Do we zip source files before sending them ?
     */
    protected boolean zipSourceFiles;

    /**
     * name of the input space
     */
    protected String inputSpaceName = null;

    /**
     * name of the output space
     */
    protected String outputSpaceName = null;

    /**
     * priority of the job
     */
    protected String priority = null;

    /**
     * directory structure of the matsci temp directory (each element of the array is a subdirectory of the previous one)
     */
    protected String[] tempSubDirNames;

    /**
     * url of the input space
     */
    protected String inputSpaceURL = null;

    /**
     * url of the output space
     */
    protected String outputSpaceURL = null;

    public PASolveMatSciGlobalConfig() {

    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getInputSpaceURL() {
        return inputSpaceURL;
    }

    public void setInputSpaceURL(String inputSpaceURL) {
        this.inputSpaceURL = inputSpaceURL;
    }

    public String getOutputSpaceURL() {
        return outputSpaceURL;
    }

    public void setOutputSpaceURL(String outputSpaceURL) {
        this.outputSpaceURL = outputSpaceURL;
    }

    public String getCheckMatSciUrl() {
        return checkMatSciUrl;
    }

    public void setCheckMatSciUrl(String checkMatSciUrl) {
        this.checkMatSciUrl = checkMatSciUrl;
    }

    public String getCustomScriptUrl() {
        return customScriptUrl;
    }

    public void setCustomScriptUrl(String customScriptUrl) {
        this.customScriptUrl = customScriptUrl;
    }

    public boolean isCustomScriptStatic() {
        return customScriptStatic;
    }

    public void setCustomScriptStatic(boolean customScriptStatic) {
        this.customScriptStatic = customScriptStatic;
    }

    public String getCustomScriptParams() {
        return customScriptParams;
    }

    public void setCustomScriptParams(String customScriptParams) {
        this.customScriptParams = customScriptParams;
    }

    public String getVersionPref() {
        return versionPref;
    }

    public void setVersionPref(String versionPref) {
        this.versionPref = versionPref;
    }

    public HashSet<String> getVersionRej() {
        return versionRej;
    }

    public String getVersionRejAsString() {
        String answer = "";
        if (versionRej == null)
            return null;
        for (String v : versionRej) {
            answer += v + ",";
        }
        if (answer.length() > 0) {
            answer = answer.substring(0, answer.length());
        }
        return answer;
    }

    public void setVersionRejAsString(String vrej) {
        HashSet<String> vrejSet = new HashSet<String>();
        if ((vrej != null) && (vrej.length() > 0)) {
            vrej = vrej.trim();
            String[] vRejArr = vrej.split("[ ,;]+");

            for (String rej : vRejArr) {
                if (rej != null) {
                    vrejSet.add(rej);
                }
            }
        }
        versionRej = vrejSet;
    }

    public void setVersionRej(HashSet<String> versionRej) {
        this.versionRej = versionRej;
    }

    public String getVersionMin() {
        return versionMin;
    }

    public void setVersionMin(String versionMin) {
        this.versionMin = versionMin;
    }

    public String getVersionMax() {
        return versionMax;
    }

    public void setVersionMax(String versionMax) {
        this.versionMax = versionMax;
    }

    public boolean isFork() {
        return this.fork;
    }

    public void setFork(boolean fork) {
        this.fork = fork;
    }

    public boolean isRunAsMe() {
        return this.runAsMe;
    }

    public void setRunAsMe(boolean runAsMe) {
        this.runAsMe = runAsMe;
    }

    public String getEnvMatFileName() {
        return envMatFileName;
    }

    public void setEnvMatFileName(String envMatFileName) {
        this.envMatFileName = envMatFileName;
    }

    public void setEnvGlobalNames(String[] names) {
        this.envGlobalNames = names;
    }

    public String[] getEnvGlobalNames() {
        return this.envGlobalNames;
    }

    public String getInputSpaceName() {
        return inputSpaceName;
    }

    public void setInputSpaceName(String inputSpaceName) {
        this.inputSpaceName = inputSpaceName;
    }

    public String getOutputSpaceName() {
        return outputSpaceName;
    }

    public void setOutputSpaceName(String outputSpaceName) {
        this.outputSpaceName = outputSpaceName;
    }

    public String[] getScriptParams() {
        return scriptParams;
    }

    public void setScriptParams(String[] scriptParams) {
        this.scriptParams = scriptParams;
    }

    public void setLocalSpace(URI localSpaceURI) {
        this.localSpace = localSpaceURI;
    }

    public URI getLocalSpace() {
        return this.localSpace;
    }

    public boolean isZipSourceFiles() {
        return zipSourceFiles;
    }

    public void setZipSourceFiles(boolean zipSourceFiles) {
        this.zipSourceFiles = zipSourceFiles;
    }

    public boolean isTransferEnv() {
        return transferEnv;
    }

    public void setTransferEnv(boolean transferEnv) {
        this.transferEnv = transferEnv;
    }

    public String getTempSubDirName() {
        return tempSubDirNames[0];
    }

    public String[] getTempSubDirNames() {
        return tempSubDirNames;
    }

    public void setTempSubDirName(String tempSubDirName) {
        this.tempSubDirNames = new String[] { tempSubDirName };
    }

    public void setTempSubDirNames(String[] tempSubDirNames) {
        this.tempSubDirNames = tempSubDirNames;
    }

    public String getSourceZipFileName() {
        return sourceZipFileName;
    }

    public void setSourceZipFileName(String sourceZipFileName) {
        this.sourceZipFileName = sourceZipFileName;
    }

    public String[] getStartupOptions() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return this.getWindowsStartupOptions();
        } else {
            return this.getLinuxStartupOptions();
        }
    }

    public String[] getWindowsStartupOptions() {
        return windowsStartupOptions;
    }

    public void setWindowsStartupOptions(String[] windowsStartupOptions) {
        this.windowsStartupOptions = windowsStartupOptions;
    }

    public String[] getLinuxStartupOptions() {
        return linuxStartupOptions;
    }

    public void setLinuxStartupOptions(String[] linuxStartupOptions) {
        this.linuxStartupOptions = linuxStartupOptions;
    }

    public void setLinuxStartupOptionsAsString(String options) {
        if ((options != null) && (options.length() > 0)) {
            options = options.trim();
            linuxStartupOptions = options.split("[ ,;]+");

        }
    }

    public void setWindowsStartupOptionsAsString(String options) {
        if ((options != null) && (options.length() > 0)) {
            options = options.trim();
            windowsStartupOptions = options.split("[ ,;]+");

        }
    }

}
