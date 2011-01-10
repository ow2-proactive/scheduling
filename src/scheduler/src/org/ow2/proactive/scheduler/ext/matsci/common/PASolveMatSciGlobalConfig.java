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
package org.ow2.proactive.scheduler.ext.matsci.common;

import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;


/**
 * PASolveMatSciGlobalConfig
 *
 * @author The ProActive Team
 */
public class PASolveMatSciGlobalConfig implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 30L;

    /**
     * Debug Mode
     */
    private boolean debug = false;

    /**
     * Keep remote matlab engine between tasks
     */
    private boolean keepEngine = false;

    /**
     * Preferred Version to use
     */
    private String versionPref = null;

    /**
     * Versions forbidden to use
     */
    private HashSet<String> versionRej = new HashSet<String>();

    /**
     * Minimum version to use
     */
    private String versionMin = null;

    /**
     * Maximum version to use
     */
    private String versionMax = null;

    /**
     * Transfers source to the remote engine
     */
    private boolean transferEnv = false;

    /**
     * Transfers variables to the remote engine
     */
    private boolean transferVariables = false;

    /**
     * Transfers environment to the remote engine
     */
    private boolean transferSource = false;

    private String checkMatSciUrl = null;

    private String customScriptUrl = null;

    private URI localSpace;

    /**
     * Name of source zip file
     */
    private String sourceZipFileName = null;

    /**
     * hash of zip file
     */
    private String sourceZipHash = null;

    /**
     * Name of env zip file
     */
    private String envZipFileName = null;

    private String[] scriptParams;

    /**
     * Name of env mat file
     */
    private String envMatFileName = null;

    private boolean zipEnvFile;

    private boolean zipSourceFiles;

    private String inputSpaceName = null;

    private String outputSpaceName = null;

    private String priority = null;

    private boolean timeStamp = false;

    private boolean zipInputFiles = false;

    private boolean zipOutputFiles = false;

    private String tempSubDirName;

    private String inputSpaceURL = null;

    private String outputSpaceURL = null;

    public PASolveMatSciGlobalConfig() {

    }

    public boolean isTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(boolean timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
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

    public boolean isKeepEngine() {
        return keepEngine;
    }

    public void setKeepEngine(boolean keepEngine) {
        this.keepEngine = keepEngine;
    }

    public String getEnvMatFileName() {
        return envMatFileName;
    }

    public void setEnvMatFileName(String envMatFileName) {
        this.envMatFileName = envMatFileName;
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

    public boolean isZipEnvFile() {
        return zipEnvFile;
    }

    public void setZipEnvFile(boolean zipEnvFile) {
        this.zipEnvFile = zipEnvFile;
    }

    public boolean isTransferSource() {
        return transferSource;
    }

    public void setTransferSource(boolean transferSource) {
        this.transferSource = transferSource;
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

    public boolean isTransferVariables() {
        return transferVariables;
    }

    public void setTransferVariables(boolean transferVariables) {
        this.transferVariables = transferVariables;
    }

    public String getEnvZipFileName() {
        return envZipFileName;
    }

    public void setEnvZipFileName(String envZipFileName) {
        this.envZipFileName = envZipFileName;
    }

    public boolean isZipInputFiles() {
        return zipInputFiles;
    }

    public void setZipInputFiles(boolean zipInputFiles) {
        this.zipInputFiles = zipInputFiles;
    }

    public boolean isZipOutputFiles() {
        return zipOutputFiles;
    }

    public void setZipOutputFiles(boolean zipOutputFiles) {
        this.zipOutputFiles = zipOutputFiles;
    }

    public String getTempSubDirName() {
        return tempSubDirName;
    }

    public void setTempSubDirName(String tempSubDirName) {
        this.tempSubDirName = tempSubDirName;
    }

    public String getSourceZipFileName() {
        return sourceZipFileName;
    }

    public void setSourceZipFileName(String sourceZipFileName) {
        this.sourceZipFileName = sourceZipFileName;
    }

    public String getSourceZipHash() {
        return sourceZipHash;
    }

    public void setSourceZipHash(String sourceZipHash) {
        this.sourceZipHash = sourceZipHash;
    }

}
