/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.resourcemanager.nodesource.dataspace;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.dataspaces.core.BaseScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Arrays;


/**
 * DataSpaceNodeConfigurationAgent is used to configure and close DataSpaces knowledge
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 */
public class DataSpaceNodeConfigurationAgent implements Serializable {

    private static Logger logger = Logger.getLogger(DataSpaceNodeConfigurationAgent.class);

    /**
     * This property is used by scheduling when configuring node to define the location of the scratch dir and must be renamed carefully.
     * It is also defined in TaskLauncher.
     */
    protected static final String NODE_DATASPACE_SCRATCHDIR = "node.dataspace.scratchdir";

    /**
     * This property is used by scheduling when configuring node to define the location of the scratch dir. If the property is not defined,
     * the scratch location will be used to create the cache dir
     */
    protected static final String NODE_DATASPACE_CACHEDIR = "node.dataspace.cachedir";

    /**
     * Name of the CacheSpace for DataSpaces registration
     */
    public static final String CACHESPACE_NAME = "CACHESPACE";

    /**
     * Default subfolder name for the cache
     **/
    public static final String DEFAULT_CACHE_SUBFOLDER_NAME = "cache";


    private FileSystemServerDeployer cacheServer;
    private static InputOutputSpaceConfiguration cacheSpaceConfiguration;

    /**
     * Create a new instance of DataSpaceNodeConfigurationAgent
     * Used by ProActive
     */
    public DataSpaceNodeConfigurationAgent() {
    }

    public boolean configureNode() {
        try {
            // configure node for Data Spaces
            String baseScratchDir = getBaseScratchDir();
            final BaseScratchSpaceConfiguration scratchConf = new BaseScratchSpaceConfiguration(
                    (String) null, baseScratchDir);
            DataSpacesNodes.configureNode(PAActiveObject.getActiveObjectNode(PAActiveObject.getStubOnThis()),
                    scratchConf);
        } catch (Throwable t) {
            logger.error("Cannot configure dataSpace", t);
            return false;
        }
        PAActiveObject.terminateActiveObject(false);
        return true;
    }

    private String getBaseScratchDir() {
        String scratchDir;
        if (System.getProperty(NODE_DATASPACE_SCRATCHDIR) == null) {
            //if scratch dir java property is not set, set to default
            scratchDir = System.getProperty("java.io.tmpdir");
        } else {
            //else use the property
            scratchDir = System.getProperty(NODE_DATASPACE_SCRATCHDIR);
        }
        return scratchDir;
    }

    private String getCacheDir() {
        String cacheDir;
        if (System.getProperty(NODE_DATASPACE_CACHEDIR) == null) {
            //if scratch dir java property is not set, set to default
            cacheDir = (new File(getBaseScratchDir(), DEFAULT_CACHE_SUBFOLDER_NAME)).getAbsolutePath();
        } else {
            // else use the property
            cacheDir = System.getProperty(NODE_DATASPACE_CACHEDIR);
        }
        return cacheDir;
    }

    public static InputOutputSpaceConfiguration getCacheSpaceConfiguration() {
        return cacheSpaceConfiguration;
    }

    public boolean startCacheSpace() {
        if (cacheSpaceConfiguration == null) {
            try {
                cacheServer = new FileSystemServerDeployer(CACHESPACE_NAME, getCacheDir(), true, true);
                logger.info("Cache server started at " + cacheServer.getVFSRootURLs());
                String hostname = InetAddress.getLocalHost().getHostName();
                cacheSpaceConfiguration = InputOutputSpaceConfiguration.createOutputSpaceConfiguration(Arrays.asList(cacheServer.getVFSRootURLs()), getCacheDir(), hostname, CACHESPACE_NAME);

            } catch (Exception e) {
                logger.error("Error occurred when starting the cache server", e);
                return false;
            }
        }
        return true;
    }

    public BooleanWrapper closeNodeConfiguration() {

        try {
            cacheServer.terminate();

            DataSpacesNodes.closeNodeConfig(PAActiveObject
                    .getActiveObjectNode(PAActiveObject.getStubOnThis()));
        } catch (Throwable t) {
            logger.error("Cannot close dataSpace configuration !", t);
            throw new RuntimeException(t);
        }
        PAActiveObject.terminateActiveObject(false);
        return new BooleanWrapper(true);
    }

}
