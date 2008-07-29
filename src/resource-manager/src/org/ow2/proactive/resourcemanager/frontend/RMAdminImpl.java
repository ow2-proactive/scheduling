/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.frontend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.ow2.proactive.resourcemanager.common.FileToBytesConverter;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.RMCoreInterface;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * Implementation of the {@link RMAdmin} active object.
 * the RMAdmin active object object is designed to receive and perform
 * administrator commands :<BR>
 * -initiate creation and removal of {@link org.ow2.proactive.resourcemanager.nodesource.frontend.NodeSource} active objects.<BR>
 * -add nodes to static nodes sources ({@link org.ow2.proactive.resourcemanager.nodesource.gcm.GCMNodeSource}), by
 * a ProActive descriptor.<BR>
 * -remove nodes from the RM.<BR>
 * -shutdown the RM.<BR>
 *
 * @author The ProActive Team
 * @since ProActive 3.9
 *
 */
public class RMAdminImpl implements RMAdmin, Serializable, InitActive {

    /** RMCore active object of the RM */
    private RMCoreInterface rmcore;

    /** Log4J logger name for RMCore */
    private final static Logger logger = ProActiveLogger.getLogger(RMLoggers.ADMIN);

    private final static String PATTERNGCMDEPLOYMENT = PAResourceManagerProperties.RM_GCM_DEPLOYMENT_PATTERN_NAME
            .getValueAsString();

    private String gcmApplicationFile = PAResourceManagerProperties.RM_GCM_TEMPLATE_APPLICATION_FILE
            .getValueAsString();

    public PAResourceManagerProperties nb;

    /**
     * ProActive Empty constructor
     */
    public RMAdminImpl() {
    }

    /**
     * Creates the RMAdmin object
     * @param rmcore Stub of the {@link RMCore} active object of the RM.
     */
    public RMAdminImpl(RMCoreInterface rmcore) {
        this.rmcore = rmcore;
    }

    /**
     * @see org.objectweb.proactive.InitActive#initActivity(org.objectweb.proactive.Body)
     */
    public void initActivity(Body body) {
        try {
            try {
                PAActiveObject.register(PAActiveObject.getStubOnThis(), "//" +
                    PAActiveObject.getNode().getVMInformation().getHostName() + "/" +
                    RMConstants.NAME_ACTIVE_OBJECT_RMADMIN);

                //test that gcmApplicationFile is an absolute path or not
                if (!(new File(gcmApplicationFile).isAbsolute())) {
                    //file path is relative, so we complete the path with the prefix RM_Home constant
                    gcmApplicationFile = PAResourceManagerProperties.RM_HOME.getValueAsString() +
                        File.separator + gcmApplicationFile;
                }

                //check that GCM Application template file exists
                if (!(new File(gcmApplicationFile).exists())) {
                    logger
                            .info("[RMADMIN] *********  ERROR ********** Cannot find default GCMApplication template file for deployment :" +
                                gcmApplicationFile +
                                ", Resource Manager will be unable to deploy nodes by GCM Deployment descriptor");
                } else if (!checkPatternInGCMAppTemplate(gcmApplicationFile, PATTERNGCMDEPLOYMENT)) {
                    logger.info("[RMADMIN] *********  ERROR ********** pattern " + PATTERNGCMDEPLOYMENT +
                        " cannot be found in GCMApplication descriptor template " + gcmApplicationFile +
                        ", Resource Manager will be unable to deploy nodes by GCM Deployment descriptor");
                }
            } catch (NodeException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#setAllNodeSourcesPingFrequency(int)
     */
    public void setAllNodeSourcesPingFrequency(int frequency) {
        this.rmcore.setAllPingFrequency(frequency);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#setDefaultNodeSourcePingFrequency(int)
     */
    public void setDefaultNodeSourcePingFrequency(int frequency) {
        this.rmcore.setPingFrequency(frequency);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#setNodeSourcePingFrequency(int, java.lang.String)
     */
    public void setNodeSourcePingFrequency(int frequency, String sourceName) throws RMException {
        this.rmcore.setPingFrequency(frequency, sourceName);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#getNodeSourcePingFrequency(java.lang.String)
     */
    public IntWrapper getNodeSourcePingFrequency(String sourceName) throws RMException {
        return rmcore.getPingFrequency(sourceName);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#createGCMNodesource(byte[], java.lang.String)
     */
    public void createGCMNodesource(byte[] gcmDeploymentData, String sourceName) throws RMException {
        GCMApplication appl = convertGCMdeploymentDataToGCMappl(gcmDeploymentData);
        this.rmcore.createGCMNodesource(appl, sourceName);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#createDynamicNodeSource(java.lang.String, int, int, int, java.util.Vector)
     */
    public void createDynamicNodeSource(String id, int nbMaxNodes, int nice, int ttr, Vector<String> peerUrls)
            throws RMException {
        this.rmcore.createDynamicNodeSource(id, nbMaxNodes, nice, ttr, peerUrls);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#addNodes(byte[])
     */
    public void addNodes(byte[] gcmDeploymentData) throws RMException {

        GCMApplication appl = convertGCMdeploymentDataToGCMappl(gcmDeploymentData);
        this.rmcore.addingNodesAdminRequest(appl);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#addNodes(byte[], java.lang.String)
     */
    public void addNodes(byte[] gcmDeploymentData, String sourceName) throws RMException {
        GCMApplication appl = convertGCMdeploymentDataToGCMappl(gcmDeploymentData);
        this.rmcore.addingNodesAdminRequest(appl, sourceName);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#addNode(java.lang.String)
     */
    public void addNode(String nodeUrl) throws RMException {
        this.rmcore.addingNodeAdminRequest(nodeUrl);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#addNode(java.lang.String, java.lang.String)
     */
    public void addNode(String nodeUrl, String sourceName) throws RMException {
        this.rmcore.addingNodeAdminRequest(nodeUrl, sourceName);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#removeNode(java.lang.String, boolean)
     */
    public void removeNode(String nodeUrl, boolean preempt) {
        this.rmcore.nodeRemovalAdminRequest(nodeUrl, preempt);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#removeSource(java.lang.String, boolean)
     */
    public void removeSource(String sourceName, boolean preempt) throws RMException {
        this.rmcore.nodeSourceRemovalAdminRequest(sourceName, preempt);
    }

    /**
     * @see org.ow2.proactive.resourcemanager.frontend.RMAdmin#shutdown(boolean)
     */
    public void shutdown(boolean preempt) throws ProActiveException {
        this.rmcore.shutdown(preempt);
        PAActiveObject.terminateActiveObject(preempt);
    }

    /**
     * Creates a GCM application object from an Array of bytes representing a  GCM deployment xml file.
     * Creates a temporary file, write the content of gcmDeploymentData array in the file. Then it creates 
     * a GCM Application from the Resource manager GCM application template (corresponding to   
     * {@link RMConstants.templateGCMApplication}) with a node provider which is gcmDeploymentData
     * passed in parameter.
     * @param gcmDeploymentData array of bytes representing a GCM deployment file.
     * @return GCMApplication object ready to be deployed
     * @throws RMException 
     */
    private GCMApplication convertGCMdeploymentDataToGCMappl(byte[] gcmDeploymentData) throws RMException {

        GCMApplication appl = null;
        try {
            File gcmDeployment = File.createTempFile("gcmDeployment", "xml");

            FileToBytesConverter.convertByteArrayToFile(gcmDeploymentData, gcmDeployment);

            File gcmApp = File.createTempFile("gcmApplication", "xml");
            copyFile(new File(gcmApplicationFile), gcmApp);

            if (!readReplace(gcmApp.getAbsolutePath(), PATTERNGCMDEPLOYMENT, "\"" +
                gcmDeployment.getAbsolutePath() + "\"")) {
                throw new RMException("GCM deployment error, cannot replace pattern " + PATTERNGCMDEPLOYMENT +
                    "in GCM application Descriptor file used as template : " + gcmApplicationFile);
            }
            appl = PAGCMDeployment.loadApplicationDescriptor(gcmApp);
            //delete the two GCMA and GCMD temp files 
            gcmApp.delete();
            gcmDeployment.delete();

        } catch (FileNotFoundException e) {
            throw new RMException(e);
        } catch (IOException e) {
            throw new RMException(e);
        } catch (ProActiveException e) {
            throw new RMException(e);
        }

        return appl;
    }

    /**
     * Check that a string is present in file. Used for create couple GCMA-GCMD
     * from GCMApplication template used by RMAdmin 
     * @param fileName string representing the path of file on which the pattern is searched
     * @param pattern string representing the pattern to find.
     * @return true if the pattern is present, false otherwise.
     */
    private boolean checkPatternInGCMAppTemplate(String fileName, String pattern) {
        String line;
        StringBuffer sb = new StringBuffer();
        int nbLinesRead = 0;
        try {
            FileInputStream fis = new FileInputStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            while ((line = reader.readLine()) != null) {
                nbLinesRead++;
                if (line.contains(pattern)) {
                    return true;
                }
                sb.append(line + "\n");
            }
            reader.close();
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            out.write(sb.toString());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * replace a string by another into a file.
     * @param fileName file in which the replacement is attempted
     * @param oldPattern the string which has to be replaced
     * @param replPattern the replacement string.
     * @return true is the replacement succeeded, false otherwise.
     */
    private boolean readReplace(String fileName, String oldPattern, String replPattern) {
        String line;
        StringBuffer sb = new StringBuffer();
        int nbLinesRead = 0;
        try {
            FileInputStream fis = new FileInputStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            while ((line = reader.readLine()) != null) {
                nbLinesRead++;
                if (line.contains(oldPattern)) {
                    //transform the replacement pattern with quote replacement
                    //before performing the replacement
                    //in order to avoid backslashes problems with windows paths
                    String quotedReplacement = Matcher.quoteReplacement(replPattern);
                    line = Pattern.compile(oldPattern).matcher(line).replaceFirst(quotedReplacement);
                }
                sb.append(line + "\n");
            }
            reader.close();
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            out.write(sb.toString());
            out.close();

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Copy a file to another.
     * @param src source file.
     * @param dest destination file.
     * @throws RMException if the copy failed.
     */
    private void copyFile(File src, File dest) throws RMException {
        int bytes_read = 0;
        byte[] buffer = new byte[1024];

        FileInputStream in;
        FileOutputStream out;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dest);

            while ((bytes_read = in.read(buffer)) != -1)
                out.write(buffer, 0, bytes_read);
            in.close();
            out.close();

        } catch (FileNotFoundException e) {
            throw new RMException(e);
        } catch (IOException e) {
            throw new RMException(e);
        }
    }
}
