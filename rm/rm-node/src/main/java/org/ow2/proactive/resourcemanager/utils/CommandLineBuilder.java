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
package org.ow2.proactive.resourcemanager.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.objectweb.proactive.core.config.xml.ProActiveConfigurationParser;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.utils.PAProperties;

import com.google.common.base.Joiner;


/**
 * CommandLineBuilder is an utility class that provide users with the capability to automatise
 * the RMNodeStarter command line building. We encourage Infrastructure Manager providers to
 * use this class as it is used as central point for applying changes to the RMNodeStarter
 * properties, for instance, if the classpath needs to be updated, a call to
 *  will reflect the change.
 *
 */
public final class CommandLineBuilder implements Cloneable {
    public static final String OBFUSC = "[OBFUSCATED_CRED]";

    private static final String ADDONS_DIR = "addons";

    private String nodeName;

    private String sourceName;

    private String javaPath;

    private String rmURL;

    private String credentialsFile;

    private String credentialsValue;

    private String credentialsEnv;

    private String rmHome;

    private int nbNodes = 1;

    private Properties paPropProperties;

    private List<String> paPropList;

    private OperatingSystem targetOS = OperatingSystem.UNIX;

    private boolean detached = false;

    /**
     * To get the RMHome from a previous call to the method {@link #setRmHome(String)}. If such a call has not been made,
     * one manages to retrieve it from the PAProperties set thanks to a previous call to the method {@link #setPaProperties(java.io.File)} of {@link #setPaProperties(java.util.Map)}.
     * @return the RMHome which will be used to build the command line.
     */
    public String getRmHome() {
        if (this.rmHome != null) {
            return rmHome;
        } else {
            if (paPropProperties != null) {
                String rmHome;
                if (paPropProperties.getProperty(PAResourceManagerProperties.RM_HOME.getKey()) != null &&
                    !paPropProperties.getProperty(PAResourceManagerProperties.RM_HOME.getKey()).equals("")) {
                    rmHome = paPropProperties.getProperty(PAResourceManagerProperties.RM_HOME.getKey());
                    if (!rmHome.endsWith(String.valueOf(this.targetOS.fs))) {
                        rmHome += String.valueOf(this.targetOS.fs);
                    }
                } else {
                    if (PAResourceManagerProperties.RM_HOME.isSet()) {
                        rmHome = PAResourceManagerProperties.RM_HOME.getValueAsString();
                        if (!rmHome.endsWith(String.valueOf(this.targetOS.fs))) {
                            rmHome += String.valueOf(this.targetOS.fs);
                        }
                    } else {
                        RMNodeStarter.logger.warn("No RM Home property found in the supplied configuration. You have to launch RMNodeStarter at the root of the RM Home by yourself.");
                        rmHome = "";
                    }
                }
                return rmHome;
            }
        }
        return null;
    }

    public void setRmHome(String rmHome) {
        this.rmHome = rmHome;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    public void setTargetOS(OperatingSystem targetOS) {
        this.targetOS = targetOS;
    }

    public void setRmURL(String rmURL) {
        this.rmURL = rmURL;
    }

    public void setNumberOfNodes(int nbNodes) {
        this.nbNodes = nbNodes;
    }

    /** Call this method to indicate that the command should be run in
     *  background and that the command will not catch the hangup signal.
     *  This means that the spawned process won't be killed if the current
     *  one exits.
     */
    public void setDetached() {
        this.detached = true;
    }

    @Deprecated
    public void setPaProperties(String paProp) {
        this.setPaProperties(Arrays.asList(paProp.split(" ")));
    }

    public void setPaProperties(List<String> paPropList) {
        if (this.paPropProperties != null) {
            this.paPropProperties = null;
        }
        this.paPropList = paPropList;
    }

    public void setPaProperties(File paPropertiesFile) throws IOException {
        this.paPropProperties = new Properties();
        if (paPropertiesFile != null) {
            if (paPropertiesFile.exists() && paPropertiesFile.isFile()) {
                this.paPropProperties = ProActiveConfigurationParser.parse(paPropertiesFile.getAbsolutePath(),
                                                                           paPropProperties);
            } else {
                throw new IOException("The supplied file is not a regular file: " + paPropertiesFile.getAbsolutePath());
            }
        }
    }

    public void setPaProperties(Map<String, String> paProp) {
        this.paPropProperties = new Properties();
        for (String key : paProp.keySet()) {
            this.paPropProperties.put(key, paProp.get(key));
        }
    }

    /**
     * To retrieve the credentials file. If no such call has already been made, will try to retrieve the credentials file path
     * from the PAProperties set thanks to the methods: {@link #setPaProperties(java.io.File)} of {@link #setPaProperties(java.util.Map)}
     * @return The credentials file used to build the command line
     */
    public String getCredentialsFile() {
        if (this.credentialsFile != null) {
            RMNodeStarter.logger.trace("Credentials file retrieved from previously set value.");
            return credentialsFile;
        } else {
            if (this.credentialsEnv == null && this.credentialsValue == null) {
                String paRMKey = PAResourceManagerProperties.RM_CREDS.getKey();
                if (paPropProperties != null && paPropProperties.getProperty(paRMKey) != null &&
                    !paPropProperties.getProperty(paRMKey).equals("")) {
                    RMNodeStarter.logger.trace(paRMKey + " property retrieved from PA properties supplied by " +
                                               CommandLineBuilder.class.getName());
                    return paPropProperties.getProperty(paRMKey);
                } else {
                    if (PAResourceManagerProperties.RM_CREDS.isSet()) {
                        RMNodeStarter.logger.trace(paRMKey +
                                                   " property retrieved from PA Properties of parent Resource Manager");
                        return PAResourceManagerProperties.RM_CREDS.getValueAsString();
                    }
                }
            }
        }
        return credentialsFile;
    }

    /**
     * @return the value of the credentials used to connect as a string
     */
    public String getCredentialsValue() {
        return credentialsValue;
    }

    /**
     * Sets the credentials value field to the supplied parameter and set
     * the other field related to credentials setup to null;
     */
    public void setCredentialsValueAndNullOthers(String credentialsValue) {
        this.credentialsValue = credentialsValue;
        this.credentialsEnv = null;
        this.credentialsFile = null;
    }

    /**
     * @return the name of the node
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * @return the name of the node source to which one the node will be added
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * Build the command to launch the RMNode.
     * The required pieces of information that need to be set in order to allow the RMNode to start properly are:
     * <ul><li>{@link CommandLineBuilder#rmURL}</li><li>{@link CommandLineBuilder#nodeName}</li>
     * <li>one of {@link CommandLineBuilder#credentialsEnv}, {@link CommandLineBuilder#credentialsFile} or {@link CommandLineBuilder#credentialsValue}</li></ul>
     * @param displayCredentials if true displays the credentials in the command line if false, obfuscates them
     * @return The RMNodeStarter command line.
     * @throws java.io.IOException if you supplied a ProActive Configuration file that doesn't exist.
     */
    public String buildCommandLine(boolean displayCredentials) throws IOException {
        List<String> command = this.buildCommandLineAsList(displayCredentials);
        return Joiner.on(' ').join(command);
    }

    /**
     * Same as {@link CommandLineBuilder#buildCommandLine(boolean)} but the command is a list of String.
     * @param displayCredentials if true displays the credentials in the command line if false, obfuscates them
     * @return The RMNodeStarter command line as a list of String.
     * @throws java.io.IOException if you supplied a ProActive Configuration file that doesn't exist.
     */
    public List<String> buildCommandLineAsList(boolean displayCredentials) throws IOException {
        final ArrayList<String> command = new ArrayList<>();
        final OperatingSystem os = targetOS;
        final Properties paProp = paPropProperties;

        String rmHome = this.getRmHome();
        if (rmHome != null) {
            if (!rmHome.endsWith(os.fs)) {
                rmHome = rmHome + os.fs;
            }
        } else {
            rmHome = "";
        }

        if (detached) {
            makeDetachedCommand(command, os);
        }

        final String libRoot = rmHome + "dist" + os.fs + "lib" + os.fs;
        String javaPath = this.javaPath;
        if (javaPath != null) {
            command.add(javaPath);
        } else {
            RMNodeStarter.logger.warn("Java path isn't set in RMNodeStarter configuration.");
            command.add("java");
        }

        //building configuration
        if (paProp != null) {
            Set<Object> keys = paProp.keySet();
            for (Object key : keys) {
                command.add("-D" + key + "=" + paProp.get(key));
            }
        } else {
            if (this.paPropList != null) {
                command.addAll(this.paPropList);
            }
        }

        // forward current charset to the forked JVM
        String currentJvmCharset = PAProperties.getFileEncoding();
        command.add("-Dfile.encoding=" + currentJvmCharset);
        RMNodeStarter.logger.info("Using '" + currentJvmCharset + "' as file encoding");

        //building classpath
        command.add("-cp");
        final StringBuilder classpath = new StringBuilder(".");

        // add the content of addons dir on the classpath
        classpath.append(os.ps).append(rmHome).append(ADDONS_DIR);
        // add jars inside the addons directory
        classpath.append(os.ps).append(rmHome).append(ADDONS_DIR).append(os.fs).append("*");
        classpath.append(os.ps).append(libRoot).append("*");

        command.add(classpath.toString());
        command.add(RMNodeStarter.class.getName());

        //appending options
        String credsEnv = credentialsEnv;
        if (credsEnv != null) {
            command.add("-" + RMNodeStarter.OPTION_CREDENTIAL_ENV);
            command.add(credsEnv);
        }
        String credsFile = this.getCredentialsFile();
        if (credsFile != null) {
            command.add("-" + RMNodeStarter.OPTION_CREDENTIAL_FILE);
            command.add(credsFile);
        }
        String credsValue = this.getCredentialsValue();
        if (credsValue != null) {
            command.add("-" + RMNodeStarter.OPTION_CREDENTIAL_VAL);
            command.add(displayCredentials ? credsValue : OBFUSC);
        }
        String nodename = this.getNodeName();
        if (nodename != null) {
            command.add("-" + RMNodeStarter.OPTION_NODE_NAME);
            command.add(nodename);
        }
        String nodesource = this.getSourceName();
        if (nodesource != null) {
            command.add("-" + RMNodeStarter.OPTION_SOURCE_NAME);
            command.add(nodesource);
        }
        String rmurl = rmURL;
        if (rmurl != null) {
            command.add("-" + RMNodeStarter.OPTION_RM_URL);
            command.add(rmurl);
        }
        command.add("-" + RMNodeStarter.OPTION_WORKERS);
        command.add("" + nbNodes);
        if (detached && os.equals(OperatingSystem.UNIX)) {
            command.add("&");
        }
        return command;
    }

    @Override
    public String toString() {
        try {
            return buildCommandLine(false);
        } catch (IOException e) {
            return CommandLineBuilder.class.getName() + " with invalid configuration";
        }
    }

    private void makeDetachedCommand(ArrayList<String> command, OperatingSystem os) {
        if (os.equals(OperatingSystem.UNIX)) {
            // if the system is unix-based, we need to start the process with
            // the nohup indicator it normally goes with the end of the
            // command finished with the background indicator '&' (see the end
            // of command building)
            command.add("nohup");
        } else if (os.equals(OperatingSystem.WINDOWS)) {
            // Windows equivalent is to use the start command with /b option
            command.add("start");
            command.add("/b");
            // we must set the title of the new command prompt, otherwise it
            // takes the java command as the title of the new prompt, and the
            // second parameter as the command to execute
            command.add("\"\"");
        }
    }
}
