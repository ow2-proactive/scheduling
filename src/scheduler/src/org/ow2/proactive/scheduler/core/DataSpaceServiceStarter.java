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
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
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
    private FileSystemServerDeployer[] servers = new FileSystemServerDeployer[4];

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
                    servers[i] = new FileSystemServerDeployer(spacesNames[i], spaceDir, true, true);
                    String url = servers[i].getVFSRootURL();
                    confs[i][0].updateProperty(url);
                    // use the hostname property if it is set, otherwise, use the local hostname
                    if (!confs[i][2].isSet()) {
                        confs[i][2].updateProperty(localhostname);
                    }
                    logger.info("Started " + humanReadableNames[i] + " server at " + url);
                    logger.info(humanReadableNames[i] + " server local path is " + spaceDir);
                } catch (IllegalArgumentException iae) {
                    throw new IllegalArgumentException("Directory '" + spaceDir +
                        "' cannot be accessed. Check if directory exists or if you have read/write rights.");
                }
            }
            //let URL terminate by /
            String url = confs[i][0].getValueAsString();
            if (!url.endsWith("/")) {
                confs[i][0].updateProperty(url + "/");
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

    /**
     * Helper method used to create a space configuration and register it into the naming service
     * This helper can eventually configure the local Node for the provided application ID, if the dataspace needs to be user locally.
     * It will only register the dataspace in the naming service otherwise
     * @param appID the Application ID
     * @param name the name of the dataspace
     * @param url the url of the dataspace
     * @param path the path to the dataspace in the localfilesystem
     * @param hostname the host where the file server is deployed
     * @param inputConfiguration if the configuration is an InputSpace configuration (read-only)
     * @param localConfiguration if the local node needs to be configured for the provided application
     */
    public static void createSpace(long appID, String name, String url, String path, String hostname,
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
        ArrayList<String> urls = new ArrayList<String>();
        if (path != null) {
            urls.add((new File(path)).toURI().toURL().toExternalForm());
        }
        urls.add(url);
        if (inputConfiguration) {
            spaceConf = InputOutputSpaceConfiguration.createInputSpaceConfiguration(urls, path,
                    hostname != null ? hostname : localhostname, name);
        } else {
            spaceConf = InputOutputSpaceConfiguration.createOutputSpaceConfiguration(urls, path,
                    hostname != null ? hostname : localhostname, name);
        }

        namingService.register(new SpaceInstanceInfo(appID, spaceConf));

        spacesConfigurations.get(appID).add(spaceConf.getName());
        logger.info("Space " + name + " for appid = " + appID + " with urls = " + urls + " registered");

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
        for (int i = 0; i < servers.length; i++) {
            try {
                servers[i].terminate();
            } catch (Throwable t) {
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
