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
package org.ow2.proactive.scheduler.core;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.BaseScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingServiceDeployer;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceAlreadyRegisteredException;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class DataSpaceServiceStarter implements Serializable {

    public static final Logger logger = Logger.getLogger(SchedulingService.class);

    /**
     * Default Local Paths
     */
    private static final String DEFAULT_LOCAL = System.getProperty("java.io.tmpdir") + File.separator +
        "scheduling";
    private static final String DEFAULT_LOCAL_INPUT = DEFAULT_LOCAL + File.separator + "defaultinput";
    private static final String DEFAULT_LOCAL_OUTPUT = DEFAULT_LOCAL + File.separator + "defaultoutput";
    private static final String DEFAULT_LOCAL_GLOBAL = DEFAULT_LOCAL + File.separator + "defaultglobal";
    private static final String DEFAULT_LOCAL_USER = DEFAULT_LOCAL + File.separator + "defaultuser";
    private static final String DEFAULT_LOCAL_SCRATCH = DEFAULT_LOCAL + File.separator + "scratch";

    private static HashMap<Long, HashSet<String>> spacesConfigurations = new HashMap<Long, HashSet<String>>();

    /**
     * Naming service
     */
    private static String namingServiceURL;
    private static NamingServiceDeployer namingServiceDeployer;
    private static NamingService namingService;

    private static String localhostname = null;

    /**
     * Local node (should be the one of the scheduler)
     */
    private static Node schedulerNode = null;
    static {
        try {
            schedulerNode = PAActiveObject.getNode();
            localhostname = NodeFactory.getDefaultNode().getVMInformation().getHostName();
        } catch (Exception e) {

        }
    }

    /**
     * Application ID last used to configure the local node, null if none
     */
    private static Long appidConfigured = null;

    /**
     * Dataspace servers
     */
    private ArrayList<ArrayList<FileSystemServerDeployer>> servers = new ArrayList<ArrayList<FileSystemServerDeployer>>(
        4);

    public DataSpaceServiceStarter() {
    }

    /**
     * StartNaming service and default file system server if needed.
     *
     * @throws Exception
     */
    public void startNamingService() throws Exception {

        namingServiceDeployer = new NamingServiceDeployer(true);
        namingServiceURL = namingServiceDeployer.getNamingServiceURL();
        namingService = NamingService.createNamingServiceStub(namingServiceURL);

        // configure node for Data Spaces
        final BaseScratchSpaceConfiguration scratchConf = new BaseScratchSpaceConfiguration(null,
            DEFAULT_LOCAL_SCRATCH);
        DataSpacesNodes.configureNode(schedulerNode, scratchConf);

        //set default INPUT/OUTPUT spaces if needed

        PASchedulerProperties[][] confs = {
                { PASchedulerProperties.DATASPACE_DEFAULTINPUT_URL,
                        PASchedulerProperties.DATASPACE_DEFAULTINPUT_LOCALPATH,
                        PASchedulerProperties.DATASPACE_DEFAULTINPUT_HOSTNAME },
                { PASchedulerProperties.DATASPACE_DEFAULTOUTPUT_URL,
                        PASchedulerProperties.DATASPACE_DEFAULTOUTPUT_LOCALPATH,
                        PASchedulerProperties.DATASPACE_DEFAULTOUTPUT_HOSTNAME },
                { PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_URL,
                        PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_LOCALPATH,
                        PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_HOSTNAME },
                { PASchedulerProperties.DATASPACE_DEFAULTUSER_URL,
                        PASchedulerProperties.DATASPACE_DEFAULTUSER_LOCALPATH,
                        PASchedulerProperties.DATASPACE_DEFAULTUSER_HOSTNAME } };
        String[] spacesNames = { "DefaultInputSpace", "DefaultOutputSpace", "GlobalSpace", "UserSpaces" };
        String[] humanReadableNames = { "default INPUT space", "default OUTPUT space", "shared GLOBAL space",
                "USER spaces" };
        String[] default_paths = { DEFAULT_LOCAL_INPUT, DEFAULT_LOCAL_OUTPUT, DEFAULT_LOCAL_GLOBAL,
                DEFAULT_LOCAL_USER };

        for (int i = 0; i < confs.length; i++) {
            //variable used to precise exception
            String spaceDir = null;
            if (confs[i][0].isSet() && confs[i][0].getValueAsString().trim().isEmpty()) {
                logger.info("Unsetting property : " + confs[i][0].getKey());
                confs[i][0].unSet();
            }

            if (!confs[i][0].isSet()) {
                // if URL is set, if not we start a server ourselves
                try {
                    logger.info("Starting " + humanReadableNames[i] + " server...");
                    if (!confs[i][1].isSet()) {
                        // check if the localpath is set, if not we use the default path
                        spaceDir = default_paths[i];
                        confs[i][1].updateProperty(spaceDir);
                    } else {
                        // otherwise, we build a FileServer on the provided path
                        logger.info("Using property-defined path at " + confs[i][1].getValueAsString());
                        spaceDir = confs[i][1].getValueAsString();
                    }
                    File dir = new File(spaceDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    StringBuilder buildedUrl = new StringBuilder();
                    ArrayList<FileSystemServerDeployer> serverPerProtocol = new ArrayList<FileSystemServerDeployer>();
                    for (String protocol : getProtocols()) {
                        FileSystemServerDeployer server = startServer(spacesNames[i], humanReadableNames[i],
                                spaceDir, protocol, buildedUrl);
                        serverPerProtocol.add(server);
                    }
                    buildedUrl.deleteCharAt(buildedUrl.length() - 1);
                    servers.add(serverPerProtocol);
                    confs[i][0].updateProperty(buildedUrl.toString());
                    // use the hostname property if it is set, otherwise, use the local hostname
                    if (!confs[i][2].isSet()) {
                        confs[i][2].updateProperty(localhostname);
                    }

                    logger.info(humanReadableNames[i] + " server local path is " + spaceDir);
                } catch (IllegalArgumentException iae) {
                    throw new IllegalArgumentException("Directory '" + spaceDir +
                        "' cannot be accessed. Check if directory exists or if you have read/write rights.");
                }
            }
        }

        Set<SpaceInstanceInfo> predefinedSpaces = new HashSet<SpaceInstanceInfo>();
        namingService.registerApplication(SchedulerConstants.SCHEDULER_DATASPACE_APPLICATION_ID,
                predefinedSpaces);

        // register the Global space
        createSpace(SchedulerConstants.SCHEDULER_DATASPACE_APPLICATION_ID,
                SchedulerConstants.GLOBALSPACE_NAME, PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_URL
                        .getValueAsString(), PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_LOCALPATH
                        .getValueAsString(), localhostname, false, true);
    }

    private  FileSystemServerDeployer startServer(String spaceName, String readableName, String spaceDir, String protocol, StringBuilder buildedUrl) throws IOException {
        FileSystemServerDeployer server = new FileSystemServerDeployer(spaceName,
                spaceDir, true, true, protocol);
        String url = server.getVFSRootURL();
        if (!url.endsWith("/")) {
            //let URL terminate by /
            buildedUrl.append(url);
            buildedUrl.append("/ ");
        } else {
            buildedUrl.append(url);
            buildedUrl.append(" ");
        }
        logger.info("Started " + readableName + " server at " + url);
        return server;
    }

    private List<String> getProtocols() {
        ArrayList<String> protocols = new ArrayList<String>();
        protocols.add(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue());
        if (CentralPAPropertyRepository.PA_COMMUNICATION_ADDITIONAL_PROTOCOLS.isSet()) {
            if (CentralPAPropertyRepository.PA_COMMUNICATION_ADDITIONAL_PROTOCOLS.getValue() != null) {
                protocols
                        .addAll(CentralPAPropertyRepository.PA_COMMUNICATION_ADDITIONAL_PROTOCOLS.getValue());
            }
        }
        return protocols;
    }

    /**
     * Helper method used to create a space configuration and register it into the naming service
     * This helper can eventually configure the local Node for the provided application ID, if the dataspace needs to be user locally.
     * It will only register the dataspace in the naming service otherwise
     * @param appID the Application ID
     * @param name the name of the dataspace
     * @param urls the url list of the Virtual File Systems (for different protocols)
     * @param path the path to the dataspace in the localfilesystem
     * @param hostname the host where the file server is deployed
     * @param inputConfiguration if the configuration is an InputSpace configuration (read-only)
     * @param localConfiguration if the local node needs to be configured for the provided application
     */
    public static void createSpace(long appID, String name, String urls, String path, String hostname,
            boolean inputConfiguration, boolean localConfiguration) throws FileSystemException,
            URISyntaxException, ProActiveException, MalformedURLException {
        if (!spacesConfigurations.containsKey(new Long(appID))) {
            if (localConfiguration) {
                if (appidConfigured != null) {
                    logger.warn("Node " + schedulerNode.getNodeInformation().getURL() +
                        " was configured for appid = " + appidConfigured + ", reconfiguring...");
                }
                DataSpacesNodes.configureApplication(schedulerNode, appID, namingServiceURL);
                logger.info("Node " + schedulerNode.getNodeInformation().getURL() +
                    " configured for appid = " + appID);

                appidConfigured = appID;
            } else {
                namingService.registerApplication(appID, new HashSet<SpaceInstanceInfo>());
            }
            spacesConfigurations.put(appID, new HashSet<String>());
        }
        if (spacesConfigurations.get(appID).contains(name) && !PADataSpaces.DEFAULT_IN_OUT_NAME.equals(name)) {
            throw new SpaceAlreadyRegisteredException("Space " + name + " for appid=" + appID +
                " is already registered");
        }
        InputOutputSpaceConfiguration spaceConf = null;
        // We add the deployed path to a url list, this way the dataspace will always be accessed preferably by the file system
        ArrayList<String> finalurls = new ArrayList<String>();
        if (path != null) {
            finalurls.add((new File(path)).toURI().toURL().toExternalForm());
        }
        for (String url : urls.split(" ")) {
            finalurls.add(url);
        }

        if (inputConfiguration) {
            spaceConf = InputOutputSpaceConfiguration.createInputSpaceConfiguration(finalurls, path,
                    hostname != null ? hostname : localhostname, name);
        } else {
            spaceConf = InputOutputSpaceConfiguration.createOutputSpaceConfiguration(finalurls, path,
                    hostname != null ? hostname : localhostname, name);
        }

        namingService.register(new SpaceInstanceInfo(appID, spaceConf));

        spacesConfigurations.get(appID).add(spaceConf.getName());
        logger.info("Space " + name + " for appid = " + appID + " with urls = " + finalurls + " registered");

    }

    /**
     * Similar to createSpace, but in addition it will use a provided username to append it to the given urls
     * If the localpath is provided, it will also create sub folders to the dataspace root with this username
     *
     * @param username username used to update urls and create folders
     * @param appID the Application ID
     * @param spaceName the name of the dataspace
     * @param urls the url list of the Virtual File Systems (for different protocols)
     * @param localpath the path to the dataspace in the localfilesystem
     * @param hostname the host where the file server is deployed
     * @param inputConfiguration if the configuration is an InputSpace configuration (read-only)
     * @param localConfiguration if the local node needs to be configured for the provided application
     * @throws URISyntaxException
     * @throws MalformedURLException
     * @throws ProActiveException
     * @throws FileSystemException
     */
    public static void createSpaceWithUserNameSubfolder(String username, long appID, String spaceName,
            String urls, String localpath, String hostname, boolean inputConfiguration,
            boolean localConfiguration) throws URISyntaxException, IOException, ProActiveException {
        // create a local folder with the username

        if (localpath != null) {
            localpath = localpath + File.separator + username;
            File localPathFile = new File(localpath);
            if (!localPathFile.exists()) {
                FileUtils.forceMkdir(localPathFile);
            }
        }

        // updates the urls with the username
        StringBuilder updatedUrls = new StringBuilder();
        String[] urlarray = urls.split(" ");
        for (String url : urlarray) {
            updatedUrls.append(url);
            if (!url.endsWith("/"))
                updatedUrls.append("/");
            updatedUrls.append(username);
            updatedUrls.append(" ");
        }
        // create the User Space for the given user
        DataSpaceServiceStarter.createSpace(appID, spaceName, updatedUrls.toString(), localpath, hostname,
                inputConfiguration, localConfiguration);

    }

    /**
     * Terminate naming service and file system server if needed
     */
    public void terminateNamingService() {
        try {
            DataSpacesNodes.closeNodeConfig(NodeFactory.getDefaultNode());
            namingServiceDeployer.terminate();
        } catch (Throwable t) {
        }
        for (int i = 0; i < servers.size(); i++) {
            for (int j = 0; j < servers.get(i).size(); i++) {
                try {
                    servers.get(i).get(j).terminate();
                } catch (Throwable t) {
                }
            }
        }
    }

    /**
     * Get the namingServiceURL
     *
     * @return the namingServiceURL
     */
    public String getNamingServiceURL() {
        return namingServiceURL;
    }

    /**
     * Get the namingService
     *
     * @return the namingService
     */
    public NamingService getNamingService() {
        return namingService;
    }
}
