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
package org.ow2.proactive.scheduler.core;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.NodeImpl;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.api.UserCredentials;
import org.objectweb.proactive.extensions.dataspaces.core.BaseScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingServiceDeployer;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceAlreadyRegisteredException;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.objectweb.proactive.extensions.vfsprovider.util.URIHelper;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.utils.Tools;


public class DataSpaceServiceStarter implements Serializable {

    public static final Logger logger = Logger.getLogger(DataSpaceServiceStarter.class);

    /**
     * Default Local Paths
     */
    private static final String DEFAULT_LOCAL = PASchedulerProperties.SCHEDULER_HOME.getValueAsString() +
                                                File.separator + "data";

    private static final String DEFAULT_LOCAL_INPUT = DEFAULT_LOCAL + File.separator + "defaultinput";

    private static final String DEFAULT_LOCAL_OUTPUT = DEFAULT_LOCAL + File.separator + "defaultoutput";

    private static final String DEFAULT_LOCAL_GLOBAL = DEFAULT_LOCAL + File.separator + "defaultglobal";

    private static final String DEFAULT_LOCAL_USER = DEFAULT_LOCAL + File.separator + "defaultuser";

    private static final String DEFAULT_LOCAL_SCRATCH = DEFAULT_LOCAL + File.separator + "scratch";

    private Map<String, HashSet<String>> spacesConfigurations = new ConcurrentHashMap<>();

    /**
     * Naming service
     */
    private String namingServiceURL;

    private transient NamingServiceDeployer namingServiceDeployer;

    private transient NamingService namingService;

    private String localhostname = null;

    /**
     * static instance
     */
    private static DataSpaceServiceStarter instance = null;

    /**
     * Local node (should be the one of the scheduler)
     */
    private NodeImpl schedulerNode = null;

    private boolean serviceStarted = false;

    /**
     * Application ID last used to configure the local node, null if none
     */
    private String appidConfigured = null;

    /**
     * Dataspace servers
     */
    private ArrayList<FileSystemServerDeployer> servers = new ArrayList<>(4);

    private DataSpaceServiceStarter() {
    }

    public static DataSpaceServiceStarter getDataSpaceServiceStarter() {
        if (instance == null) {
            instance = new DataSpaceServiceStarter();
        }
        return instance;
    }

    /**
     * StartNaming service and default file system server if needed.
     *
     * @throws Exception
     */
    public void startNamingService() throws Exception {

        schedulerNode = (NodeImpl) PAActiveObject.getNode();

        localhostname = java.net.InetAddress.getLocalHost().getHostName();

        namingServiceDeployer = new NamingServiceDeployer(true);
        namingServiceURL = namingServiceDeployer.getNamingServiceURL();
        namingService = NamingService.createNamingServiceStub(namingServiceURL);

        // configure node for Data Spaces
        final BaseScratchSpaceConfiguration scratchConf = new BaseScratchSpaceConfiguration((String) null,
                                                                                            DEFAULT_LOCAL_SCRATCH);
        DataSpacesNodes.configureNode(schedulerNode, scratchConf);

        //set default INPUT/OUTPUT spaces if needed

        PASchedulerProperties[][] confs = { { PASchedulerProperties.DATASPACE_DEFAULTINPUT_URL,
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
                    logger.debug("Starting " + humanReadableNames[i] + " server...");
                    if (!confs[i][1].isSet()) {
                        // check if the localpath is set, if not we use the default path
                        spaceDir = default_paths[i];
                        confs[i][1].updateProperty(spaceDir);
                    } else {
                        // otherwise, we build a FileServer on the provided path
                        logger.debug("Using property-defined path at " + confs[i][1].getValueAsString());
                        spaceDir = confs[i][1].getValueAsString();
                    }
                    File dir = new File(spaceDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    FileSystemServerDeployer server = startServer(spacesNames[i], humanReadableNames[i], spaceDir);

                    servers.add(server);

                    confs[i][0].updateProperty(buildServerUrlList(server.getVFSRootURLs()));
                    // use the hostname property if it is set, otherwise, use the local hostname
                    if (!confs[i][2].isSet()) {
                        confs[i][2].updateProperty(localhostname);
                    }

                    logger.debug(humanReadableNames[i] + " server local path is " + spaceDir);
                } catch (IllegalArgumentException iae) {
                    throw new IllegalArgumentException("Directory '" + spaceDir +
                                                       "' cannot be accessed. Check if directory exists or if you have read/write rights.");
                }
            }
        }

        Set<SpaceInstanceInfo> predefinedSpaces = new HashSet<>();
        namingService.registerApplication(SchedulerConstants.SCHEDULER_DATASPACE_APPLICATION_ID, predefinedSpaces);

        serviceStarted = true;

        try {
            // register the Global space
            createSpace(SchedulerConstants.SCHEDULER_DATASPACE_APPLICATION_ID,
                        SchedulerConstants.GLOBALSPACE_NAME,
                        PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_URL.getValueAsString(),
                        PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_LOCALPATH.getValueAsString(),
                        localhostname,
                        false,
                        true);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    private String buildServerUrlList(String[] urls) {
        StringBuilder builder = new StringBuilder();
        for (String url : urls) {
            if (!url.endsWith("/")) {
                builder.append(url + "/ ");
            } else {
                builder.append(url + " ");
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    private FileSystemServerDeployer startServer(String spaceName, String readableName, String spaceDir)
            throws IOException {
        FileSystemServerDeployer server = new FileSystemServerDeployer(spaceName, spaceDir, true, true);
        String[] urls = server.getVFSRootURLs();
        logger.info("Started " + readableName + " server at " + Arrays.toString(urls));
        return server;
    }

    /**
     * Helper method used to create a space configuration and register it into the naming service
     * This helper can eventually configure the local Node for the provided application ID, if the dataspace needs to be user locally.
     * It will only register the dataspace in the naming service otherwise
     * @param appID the Application ID
     * @param name the name of the dataspace
     * @param urlsproperty the space-delimited url list property of the Virtual File Systems (for different protocols)
     * @param path the path to the dataspace in the localfilesystem
     * @param hostname the host where the file server is deployed
     * @param inputConfiguration if the configuration is an InputSpace configuration (read-only)
     * @param localConfiguration if the local node needs to be configured for the provided application
     */
    public void createSpace(String appID, String name, String urlsproperty, String path, String hostname,
            boolean inputConfiguration, boolean localConfiguration)
            throws FileSystemException, URISyntaxException, ProActiveException, MalformedURLException {
        if (!serviceStarted) {
            throw new IllegalStateException("DataSpace service is not started");
        }

        if (!spacesConfigurations.containsKey(appID)) {
            if (localConfiguration) {
                if (appidConfigured != null) {
                    logger.warn("Node " + schedulerNode.getNodeInformation().getURL() + " was configured for appid = " +
                                appidConfigured + ", reconfiguring...");
                }
                DataSpacesNodes.configureApplication(schedulerNode, appID, namingService);
                logger.debug("Node " + schedulerNode.getNodeInformation().getURL() + " configured for appid = " +
                             appID);

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

        // Converts the property to an ArrayList
        ArrayList<String> finalurls = new ArrayList<>(Arrays.asList(Tools.dataSpaceConfigPropertyToUrls(urlsproperty)));

        if (inputConfiguration) {
            spaceConf = InputOutputSpaceConfiguration.createInputSpaceConfiguration(finalurls,
                                                                                    path,
                                                                                    hostname != null ? hostname
                                                                                                     : localhostname,
                                                                                    name);
        } else {
            spaceConf = InputOutputSpaceConfiguration.createOutputSpaceConfiguration(finalurls,
                                                                                     path,
                                                                                     hostname != null ? hostname
                                                                                                      : localhostname,
                                                                                     name);
        }

        namingService.register(new SpaceInstanceInfo(appID, spaceConf));

        spacesConfigurations.get(appID).add(spaceConf.getName());
        logger.debug("Space " + name + " for appid = " + appID + " with urls = " + finalurls + " registered");

    }

    public void removeSpace(String applicationId) {
        try {
            namingService.unregisterApplication(applicationId);
        } catch (Exception e) {
        }

        spacesConfigurations.remove(applicationId);
        logger.debug("Data Spaces for appid = " + applicationId + " unregistered");

    }

    public void clearSpaceConfigurations() {
        spacesConfigurations.clear();
    }

    /**
     * Similar to createSpace, but in addition it will use a provided username to append it to the given urls
     * If the localpath is provided, it will also create sub folders to the dataspace root with this username
     *
     * @param username username used to update urls and create folders
     * @param userCredentials credentials of the user
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
    public void createSpaceWithUserNameSubfolder(String username, UserCredentials userCredentials, String appID,
            String spaceName, String urls, String localpath, String hostname, boolean inputConfiguration,
            boolean localConfiguration) throws URISyntaxException, IOException, ProActiveException {
        // updates the urls with the username
        String[] urlsArray = Tools.dataSpaceConfigPropertyToUrls(urls);

        // create a local folder with the username

        if (localpath != null) {
            DefaultFileSystemManager manager = VFSFactory.createDefaultFileSystemManager(userCredentials);
            try {
                FileObject folder = manager.resolveFile(urlsArray[0] + "/" + username);
                folder.createFolder();
            } finally {
                manager.close();
            }
        }

        String[] updatedArray = urlsWithUserDir(urlsArray, username);

        String newPropertyValue = urlsToDSConfigProperty(updatedArray);

        // create the User Space for the given user
        createSpace(appID, spaceName, newPropertyValue, localpath, hostname, inputConfiguration, localConfiguration);

    }

    /**
     * Converts url array to a property separated by spaces
     * @param urls url array
     * @return property
     */
    public static String urlsToDSConfigProperty(String[] urls) {
        if (urls == null || urls.length == 0) {
            throw new IllegalArgumentException("Empty url array");
        }
        StringBuilder urlProperty = new StringBuilder();
        urlProperty.append("\"");
        for (String url : urls) {
            // in case the username contains a space, encode the total url
            urlProperty.append(url);
            urlProperty.append("\" \"");
        }

        // remove the last two characters
        urlProperty.delete(urlProperty.length() - 2, urlProperty.length() - 2);
        return urlProperty.toString();
    }

    /**
     * Appends the given userName into each member of the url array
     * @param inputUrls
     * @param username
     * @return an url array with the userName appended
     */
    public static String[] urlsWithUserDir(String[] inputUrls, String username) {
        String[] output = new String[inputUrls.length];

        for (int i = 0; i < inputUrls.length; i++) {
            String url = inputUrls[i];

            String urlToAdd;
            if (PASchedulerProperties.DATASPACE_DEFAULTUSER_IMPERSONATION.getValueAsBoolean() &&
                (url.startsWith("sftp") || url.startsWith("vsftp"))) {
                // In case of impersonation, the root folder should not be manipulated
                urlToAdd = url;
            } else if (!url.endsWith("/")) {
                urlToAdd = url + "/" + username;
            } else {
                urlToAdd = url + username;
            }

            output[i] = URIHelper.convertToEncodedURIString(urlToAdd);
        }

        return output;
    }

    /**
     * Terminate naming service and file system server if needed
     */
    public void terminateNamingService() {
        if (!serviceStarted) {
            throw new IllegalStateException("DataSpace service is not started");
        }
        try {
            DataSpacesNodes.closeNodeConfig(schedulerNode);
            namingServiceDeployer.terminate();
        } catch (Throwable t) {
        }
        for (int i = 0; i < servers.size(); i++) {
            try {
                servers.get(i).terminate();
            } catch (Throwable t) {
            }
        }
        serviceStarted = false;
    }

    /**
     * Get the namingServiceURL
     *
     * @return the namingServiceURL
     */
    public String getNamingServiceURL() {
        if (!serviceStarted) {
            throw new IllegalStateException("DataSpace service is not started");
        }

        return namingServiceURL;
    }

    /**
     * Get the namingService
     *
     * @return the namingService
     */
    public NamingService getNamingService() {
        if (!serviceStarted) {
            throw new IllegalStateException("DataSpace service is not started");
        }

        return namingService;
    }
}
