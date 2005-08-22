/*
 * Created on Jun 21, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.core.process.unicore;

import java.util.ArrayList;
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
    private ArrayList importFiles;
    private String fileSep;

    public String toString() {
        StringBuffer sb = new StringBuffer();
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

        ListIterator it = importFiles.listIterator();
        while (it.hasNext())
            sb.append((String) it.next()).append("\n");

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

    /**
     * @return Returns the local file separator: "/" "\".
     */
    public String getFileSep() {
        return fileSep;
    }

    public void addImportFile(String f) {
        importFiles.add(f);
    }

    public String[] getImportFiles() {
        String[] files = new String[importFiles.size()];
        ListIterator it = importFiles.listIterator();

        for (int i = 0; it.hasNext(); i++)
            files[i] = (String) it.next();

        return files;
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
        } else {
            System.err.println("Skipping unkown parameter: -" + name + " " +
                value);
        }
    }

    public String getCommandString() {
        StringBuffer sb = new StringBuffer();

        sb.append(
            "java org.objectweb.proactive.core.process.unicore.UnicoreProActiveClient ");
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
        sb.append(scriptContent);

        return sb.toString();
    }

    private String quoteIfNeeded(String s) {
        //if(s.indexOf(" ")>=0)
        //	return "\""+s+"\"";
        //return s;
        return s.replaceAll(" ", "_");
    }

    public UnicoreParameters() {
        fileSep = System.getProperty("file.separator");

        jobName = "ProActiveDescriptorDeployment";
        keyPassword = "";
        submitJob = true;
        saveJob = false;

        unicoreDir = System.getProperty("user.home") + fileSep + ".unicore";
        keyFilePath = unicoreDir + fileSep + "keystore";

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

        importFiles = new ArrayList();
    }
}
