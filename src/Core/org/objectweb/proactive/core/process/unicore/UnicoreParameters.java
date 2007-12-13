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
package org.objectweb.proactive.core.process.unicore;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.unicore.resources.PriorityValue;

import com.pallas.unicore.extensions.Usite.Type;


/**
 * @author ProActive Team (06 / 2005)
 * This class contains all the parameters supported for the
 * UnicoreProActiveClient.
 */
public class UnicoreParameters implements java.io.Serializable {
    private String jobName;
    private String keyPassword;
    private boolean submitJob;
    private boolean saveJob;
    private String unicoreDir;
    private String keyFilePath;
    private String usiteName;
    private String usiteType;
    private String usiteUrl;
    private String vsiteName;
    private int vsiteNodes;
    private int vsiteProcessors;
    private int vsiteMemory;
    private int vsiteRuntime; //runtime in seconds
    private String vsitePriority;
    private String scriptContent;
    private ArrayList<String> deployFiles;
    private ArrayList<String> deployDirs;
    private ArrayList<String> retrieveFiles; //not yet supported

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("jobName=>").append(jobName).append("\n");
        sb.append("keyPassword=>").append(keyPassword).append("\n");
        sb.append("submitJob=>").append(submitJob).append("\n");
        sb.append("saveJob=>").append(saveJob).append("\n");
        sb.append("unicoreDir=>").append(unicoreDir).append("\n");
        sb.append("keyFilePath=>").append(keyFilePath).append("\n");
        sb.append("usiteName=>").append(usiteName).append("\n");
        sb.append("usiteType=>").append(usiteType).append("\n");
        sb.append("usiteUrl=>").append(usiteUrl).append("\n");
        sb.append("vsiteName=>").append(vsiteName).append("\n");
        sb.append("vsiteNodes=>").append(vsiteNodes).append("\n");
        sb.append("vsiteProcessors=>").append(vsiteProcessors).append("\n");
        sb.append("vsiteMemory=>").append(vsiteMemory).append("\n");
        sb.append("vsiteRuntime=>").append(vsiteRuntime).append("\n");
        sb.append("vsitePriority=>").append(vsitePriority).append("\n");
        sb.append("scriptContent=>").append(scriptContent).append("\n");

        sb.append("FileTransfer Directories:\n");
        ListIterator<String> it = deployDirs.listIterator();
        while (it.hasNext())
            sb.append(it.next()).append("\n");

        sb.append("FileTransfer Files:\n");
        it = deployFiles.listIterator();
        while (it.hasNext())
            sb.append(it.next()).append("\n");

        return sb.toString();
    }

    /**
     * @return Returns the jobName.
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * @param jobName The jobName to set.
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * @return Returns the keyFilePath.
     */
    public String getKeyFilePath() {
        return keyFilePath;
    }

    /**
     * @param keyFilePath The keyFilePath to set.
     */
    public void setKeyFilePath(String keyFilePath) {
        this.keyFilePath = keyFilePath;
    }

    /**
     * @return Returns the keyPassword.
     */
    public String getKeyPassword() {
        return keyPassword;
    }

    /**
     * @param keyPassword The keyPassword to set.
     */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    /**
     * @return Returns the saveJob.
     */
    public boolean isSaveJob() {
        return saveJob;
    }

    /**
     * @param saveJob The saveJob to set.
     */
    public void setSaveJob(boolean saveJob) {
        this.saveJob = saveJob;
    }

    public void setSaveJob(String saveJob) {
        this.saveJob = Boolean.valueOf(saveJob).booleanValue();
    }

    /**
     * @return Returns the scriptContent.
     */
    public String getScriptContent() {
        return scriptContent;
    }

    /**
     * @param scriptContent The scriptContent to set.
     */
    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
    }

    /**
     * @return Returns the submitJob.
     */
    public boolean isSubmitJob() {
        return submitJob;
    }

    /**
     * @param submitJob The submitJob to set.
     */
    public void setSubmitJob(boolean submitJob) {
        this.submitJob = submitJob;
    }

    public void setSubmitJob(String submitJob) {
        this.submitJob = Boolean.valueOf(submitJob).booleanValue();
    }

    /**
     * @return Returns the unicoreDir.
     */
    public String getUnicoreDir() {
        return unicoreDir;
    }

    /**
     * @param unicoreDir The unicoreDir to set.
     */
    public void setUnicoreDir(String unicoreDir) {
        this.unicoreDir = unicoreDir;
    }

    /**
     * @return Returns the usiteName.
     */
    public String getUsiteName() {
        return usiteName;
    }

    /**
     * @param usiteName The usiteName to set.
     */
    public void setUsiteName(String usiteName) {
        this.usiteName = usiteName;
    }

    /**
     * @return Returns the usiteType.
     */
    public Type getUsiteType() {
        if (usiteType.equalsIgnoreCase("REGISTRY")) {
            return Type.REGISTRY;
        }

        return Type.CLASSIC;
    }

    /**
     * @param usiteType The usiteType to set.
     */
    public void setUsiteType(String usiteType) {
        this.usiteType = usiteType;
    }

    /**
     * @return Returns the usiteUrl.
     */
    public String getUsiteUrl() {
        return usiteUrl;
    }

    /**
     * @param usiteUrl The usiteUrl to set.
     */
    public void setUsiteUrl(String usiteUrl) {
        this.usiteUrl = usiteUrl;
    }

    /**
     * @return Returns the vsiteMemory.
     */
    public int getVsiteMemory() {
        return vsiteMemory;
    }

    /**
     * @param vsiteMemory The vsiteMemory to set.
     */
    public void setVsiteMemory(int vsiteMemory) {
        this.vsiteMemory = vsiteMemory;
    }

    public void setVsiteMemory(String vsiteMemory) {
        this.vsiteMemory = Integer.parseInt(vsiteMemory);
    }

    /**
     * @return Returns the vsiteName.
     */
    public String getVsiteName() {
        return vsiteName;
    }

    /**
     * @param vsiteName The vsiteName to set.
     */
    public void setVsiteName(String vsiteName) {
        this.vsiteName = vsiteName;
    }

    /**
     * @return Returns the vsiteNodes.
     */
    public int getVsiteNodes() {
        return vsiteNodes;
    }

    /**
     * @param vsiteNodes The vsiteNodes to set.
     */
    public void setVsiteNodes(int vsiteNodes) {
        this.vsiteNodes = vsiteNodes;
    }

    public void setVsiteNodes(String vsiteNodes) {
        this.vsiteNodes = Integer.parseInt(vsiteNodes);
    }

    /**
     * @return Returns the vsitePriority.
     */
    public PriorityValue getVsitePriority() {
        if (vsitePriority.equalsIgnoreCase("high")) {
            return PriorityValue.HIGH;
        } else if (vsitePriority.equalsIgnoreCase("low")) {
            return PriorityValue.LOW;
        } else if (vsitePriority.equalsIgnoreCase("development")) {
            return PriorityValue.DEVELOPMENT;
        } else if (vsitePriority.equalsIgnoreCase("whenever")) {
            return PriorityValue.WHENEVER;
        }

        return PriorityValue.NORMAL;
    }

    /**
     * @param vsitePriority The vsitePriority to set.
     */
    public void setVsitePriority(String vsitePriority) {
        this.vsitePriority = vsitePriority;
    }

    /**
     * @return Returns the vsiteProcessors.
     */
    public int getVsiteProcessors() {
        return vsiteProcessors;
    }

    /**
     * @param vsiteProcessors The vsiteProcessors to set.
     */
    public void setVsiteProcessors(int vsiteProcessors) {
        this.vsiteProcessors = vsiteProcessors;
    }

    public void setVsiteProcessors(String vsiteProcessors) {
        this.vsiteProcessors = Integer.parseInt(vsiteProcessors);
    }

    /**
     * @return Returns the vsiteRuntime.
     */
    public int getVsiteRuntime() {
        return vsiteRuntime;
    }

    /**
     * @param vsiteRuntime The vsiteRuntime to set.
     */
    public void setVsiteRuntime(int vsiteRuntime) {
        this.vsiteRuntime = vsiteRuntime;
    }

    public void setVsiteRuntime(String vsiteRuntime) {
        this.vsiteRuntime = Integer.parseInt(vsiteRuntime);
    }

    public void addDeploymentFile(String f) {

        /* Expected input syntax ("[]" is optional):
         *
         * srcpath[/|\]srcprefix,dstpath[/|\]dstprefix
         *
         * TODO improve de syntax error checking with regexp
         */
        String[] info = f.split(",");

        if (info.length != 2) {
            System.err.println("Skipping file, syntax error on file parameter:" + f);
            return;
        }

        deployFiles.add(f);
    }

    public void addDeploymentDir(String f) {
        String[] info = f.split(",");

        //TODO improve de syntax error checking
        if (info.length != 2) {
            System.err.println("Skipping file, syntax error on file parameter:" + f);
            return;
        }

        deployDirs.add(f);
    }

    public void addRetrieveFile(String f) {
        retrieveFiles.add(f);
    }

    public void setParameter(String name, String value) {
        if ((name.length() < 2) || (value.length() <= 0)) {
            return;
        }
        if (name.charAt(0) == '-') {
            name = name.substring(1);
        }

        if (name.equalsIgnoreCase("jobname")) {
            setJobName(value);
        } else if (name.equalsIgnoreCase("keypassword")) {
            setKeyPassword(value);
        } else if (name.equalsIgnoreCase("submitjob")) {
            setSubmitJob(value);
        } else if (name.equalsIgnoreCase("savejob")) {
            setSaveJob(value);
        } else if (name.equalsIgnoreCase("unicoredir")) {
            setUnicoreDir(value);
        } else if (name.equalsIgnoreCase("keyfilepath")) {
            setKeyFilePath(value);
        } else if (name.equalsIgnoreCase("usitename")) {
            setUsiteName(value);
        } else if (name.equalsIgnoreCase("usitetype")) {
            setUsiteType(value);
        } else if (name.equalsIgnoreCase("usiteurl")) {
            setUsiteUrl(value);
        } else if (name.equalsIgnoreCase("vsitename")) {
            setVsiteName(value);
        } else if (name.equalsIgnoreCase("vsitenodes")) {
            setVsiteNodes(value);
        } else if (name.equalsIgnoreCase("vsiteprocessors")) {
            setVsiteProcessors(value);
        } else if (name.equalsIgnoreCase("vsitememory")) {
            setVsiteMemory(value);
        } else if (name.equalsIgnoreCase("vsiteruntime")) {
            setVsiteRuntime(value);
        } else if (name.equalsIgnoreCase("vsitepriority")) {
            setVsitePriority(value);
        } else if (name.equalsIgnoreCase("deploymentFile")) {
            addDeploymentFile(value);
        } else if (name.equalsIgnoreCase("deploymentDir")) {
            addDeploymentDir(value);
        } else {
            System.err.println("Skipping unkown parameter: -" + name + " " + value);
        }
    }

    public String getCommandString() {
        StringBuilder sb = new StringBuilder();

        sb.append("java org.objectweb.proactive.core.process.unicore.UnicoreProActiveClient ");
        sb.append("-jobname " + quoteIfNeeded(jobName) + " ");
        if ((keyPassword != null) && (keyPassword.length() > 0)) {
            sb.append("-keypassword " + quoteIfNeeded(keyPassword) + " ");
        }
        sb.append("-submitjob ").append(submitJob).append(" ");
        sb.append("-saveJob ").append(saveJob).append(" ");
        sb.append("-unicoreDir ").append(quoteIfNeeded(unicoreDir)).append(" ");
        sb.append("-keyFilePath ").append(quoteIfNeeded(keyFilePath)).append(" ");
        sb.append("-usiteName ").append(quoteIfNeeded(usiteName)).append(" ");
        sb.append("-usiteType ").append(quoteIfNeeded(usiteType)).append(" ");
        sb.append("-usiteUrl ").append(quoteIfNeeded(usiteUrl)).append(" ");
        sb.append("-vsiteName ").append(quoteIfNeeded(vsiteName)).append(" ");
        sb.append("-vsiteNodes ").append(vsiteNodes).append(" ");
        sb.append("-vsiteProcessors ").append(vsiteProcessors).append(" ");
        sb.append("-vsiteMemory ").append(vsiteMemory).append(" ");
        sb.append("-vsiteRuntime ").append(vsiteRuntime).append(" ");
        sb.append("-vsitePriority ").append(vsitePriority).append(" ");

        //TODO check for white spaces!
        Iterator<String> it = deployFiles.iterator();
        while (it.hasNext()) {
            String s = it.next();
            sb.append("-deploymentFile ").append(s).append(" ");
        }

        it = deployDirs.iterator();
        while (it.hasNext()) {
            String s = it.next();
            sb.append("-deploymentDir ").append(s).append(" ");
        }

        sb.append(scriptContent);
        return sb.toString();
    }

    /**
     * Append the "mv" of the files to the desired destination.
     * @return A string for performing this on the remote site.
     */
    public String getDestMoveCommand() {
        StringBuilder sb = new StringBuilder();
        String[] filesAndDirs = getDeployAllFilesAndDirectories();

        for (int i = 0; i < filesAndDirs.length; i++) {
            String[] f = filesAndDirs[i].split(",");

            if (f.length != 2) {
                continue;
            }

            String fileSep = guessFileSep(f[1]);

            //skip if no prefix is defined
            //Noprefix happens if there is no fileSep 
            //in the middle of the String
            if ((f[1].indexOf(fileSep) >= 0) && (f[1].indexOf(fileSep) != (f[1].length() - 1))) {
                sb.append("mv ").append(getFileName(f[1])).append(" ").append(f[1]).append("\n");
            }
        }

        return sb.toString();
    }

    private String quoteIfNeeded(String s) {
        //if(s.indexOf(" ")>=0)
        //	return "\""+s+"\"";
        //return s;
        //TODO find a real solution to this problem
        return s.replaceAll(" ", "_");
    }

    public UnicoreParameters() {
        jobName = "ProActiveDescriptorDeployment";
        keyPassword = "";
        submitJob = true;
        saveJob = false;

        unicoreDir = System.getProperty("user.home") + File.separator + ".unicore";
        keyFilePath = unicoreDir + File.separator + "keystore";

        usiteName = null;
        usiteType = "CLASSIC";
        usiteUrl = null;

        vsiteName = null;
        vsiteNodes = -1;
        vsiteProcessors = -1;
        vsiteMemory = -32;
        vsiteRuntime = -300;
        vsitePriority = "normal";

        scriptContent = "";

        deployFiles = new ArrayList<String>();
        deployDirs = new ArrayList<String>();

        retrieveFiles = null; //new ArrayList(); not yet implemented
    }

    /**
     * @return All files and dirs on the deploy queue.
     */
    public String[] getDeployAllFilesAndDirectories() {
        ArrayList<String> allFiles = new ArrayList<String>();

        allFiles.addAll(deployFiles);
        allFiles.addAll(deployDirs);

        return allFiles.toArray(new String[0]);
    }

    /**
     * Finds the filename or dirname in the path, guessing the fileSep.
     * @param fullpathname Something like: /home/user/fileordir
     * @return fileordir
     */
    public static String getFileName(String fullpathname) {
        boolean isDir = false;

        String fileSep = guessFileSep(fullpathname);

        //directory can end with slash
        if (fullpathname.endsWith(fileSep)) {
            isDir = true;
            fullpathname.substring(0, fileSep.length() - 1);
        }

        //last element is the one we are looking for
        String[] splited = fullpathname.split(fileSep);

        //adding the final fileSep if it's a directory
        if (isDir) {
            return splited[splited.length - 1] + fileSep;
        }

        return splited[splited.length - 1];
    }

    /**
     * Guesses the fileseparator for the specified string
     * @param fullpathname
     * @return / or \
     */
    private static String guessFileSep(String fullpathname) {
        //guess the fileseparator
        String fileSep = "/";
        if (fullpathname.indexOf("/") < fullpathname.indexOf("\\")) {
            fileSep = "\\";
        }

        return fileSep;
    }
}
