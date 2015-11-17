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
package org.ow2.proactive.resourcemanager;

import java.io.File;
import java.io.IOException;
import java.rmi.AlreadyBoundException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.resourcemanager.authentication.RMAuthentication;
import org.ow2.proactive.resourcemanager.common.RMConstants;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.frontend.RMConnection;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoring;
import org.ow2.proactive.resourcemanager.selection.SelectionManager;
import org.ow2.proactive.utils.FileUtils;
import org.ow2.proactive.utils.appenders.MultipleFileAppender;


/**
 * Object which performs the Resource Manager (RM) creation,
 * and provides RM's front-end active objects:
 *
 * <ul>
 *     <li>{@link RMAuthentication}</li>
 *     <li>{@link RMMonitoring}</li>
 *     <li>{@link org.ow2.proactive.resourcemanager.common.util.RMProxyUserInterface}</li>
 * </ul>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public class RMFactory {

    /** Logger of the RMFactory */
    private static final Logger logger = Logger.getLogger(RMFactory.class);

    /** RMCore interface of the created Resource manager */
    private static RMCore rmcore = null;

    /**
     * Creates and starts a Resource manager on the local host using the given initializer to configure it.
     * Only one RM can be started by JVM.
     *
     * @param initializer Use to configure the Resource Manager before starting it.
     * 		This parameter can be null, if so the Resource Manager will try to start on the JVM properties and
     * 		the "pa.rm.home" property MUST be set to the root of the RM directory.
     * @return a RM authentication that allow you to administer the RM or get its connection URL.
     *
     * @throws NodeException If the RM's node can't be created
     * @throws ActiveObjectCreationException If RMCore cannot be created
     * @throws AlreadyBoundException if a node with the same RMNode's name is already exist. 
     * @throws IOException If node and RMCore fails.
     * @throws RMException if the connection to the authentication interface fails.
     */
    public static RMAuthentication startLocal(RMInitializer initializer) throws Exception {
        if (rmcore == null) {
            if (initializer != null) {
                //configure application
                configure(initializer);
            }

            configureLog4j();

            Node nodeRM = NodeFactory.createLocalNode(PAResourceManagerProperties.RM_NODE_NAME
                    .getValueAsString(), false, null, null);
            String RMCoreName = RMConstants.NAME_ACTIVE_OBJECT_RMCORE;
            rmcore = (RMCore) PAActiveObject.newActive(RMCore.class.getName(), // the class to deploy
                    new Object[] { RMCoreName, nodeRM }, nodeRM);
            logger.debug("New RM core localy started");
            return RMConnection.waitAndJoin(null);
        } else {
            throw new RMException("RM Core already localy running");
        }
    }

    /**
     * Configure the VM to be ready to start the new RM.
     *
     * @param initializer the initializer used to configured the VM
     */
    private static void configure(RMInitializer initializer) {
        //security manager
        if (System.getProperty("java.security.manager") == null) {
            System.setProperty("java.security.manager", "");
        }
        //rm properties
        String s = initializer.getResourceManagerPropertiesConfiguration();
        if (s == null) {
            throw new IllegalArgumentException("RM properties file is not set, cannot start RM !");
        }
        System.setProperty(PAResourceManagerProperties.PA_RM_PROPERTIES_FILEPATH, s);
        //pa conf
        s = initializer.getProActiveConfiguration();
        if (s != null) {
            System.setProperty(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName(), s);
        }
        //RM home
        s = initializer.getRMHomePath();
        if (s != null) {
            System.setProperty(PAResourceManagerProperties.RM_HOME.getKey(), s);
        }
    }

    private static void configureLog4j() {

        // Log4j configuration for selection process (if enabled)
        if (PAResourceManagerProperties.RM_SELECTION_LOGS_LOCATION.isSet()) {

            String logsLocation = PAResourceManagerProperties
                    .getAbsolutePath(PAResourceManagerProperties.RM_SELECTION_LOGS_LOCATION
                            .getValueAsString());

            boolean cleanStart = PAResourceManagerProperties.RM_DB_HIBERNATE_DROPDB.getValueAsBoolean();
            if (cleanStart) {
                // removing selection logs directory
                logger.info("Removing logs " + logsLocation);
                FileUtils.removeDir(new File(logsLocation));
            }

            Logger selectionLogger = Logger.getLogger(SelectionManager.class.getPackage().getName());
            MultipleFileAppender appender = new MultipleFileAppender();
            if (PAResourceManagerProperties.RM_SELECTION_LOGS_MAX_SIZE.isSet()) {
                appender.setMaxFileSize(PAResourceManagerProperties.RM_SELECTION_LOGS_MAX_SIZE
                        .getValueAsString());
            }
            appender.setFilesLocation(logsLocation);
            selectionLogger.addAppender(appender);
        }
    }

    /**
     * Creates and starts a Resource manager on the local host.
     * This call considered that the JVM is correctly configured for starting RM.
     * The "pa.rm.home" and required JVM properties MUST be set.
     *
     * @return a RM authentication that allow you to administer the RM or get its connection URL.
     * @throws NodeException If the RM's node can't be created
     * @throws ActiveObjectCreationException If RMCore cannot be created
     * @throws AlreadyBoundException if a node with the same RMNode's name is already exist.
     * @throws IOException If node and RMCore fails.
     */
    public static RMAuthentication startLocal() throws Exception {
        return startLocal(null);
    }

    /**
     * Set Java Property "os" used by default GCM deployment file.
     */
    public static void setOsJavaProperty() {
        //set appropriate os java property used in default GCM deployment descriptor.
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            System.setProperty("os", "windows");
        } else {
            System.setProperty("os", "unix");
        }
    }
}
