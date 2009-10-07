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
 * $PROACTIVE_INITIAL_DEV$
 */
package org.ow2.proactive.resourcemanager.nodesource.dataspace;

import java.io.File;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.core.BaseScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.ow2.proactive.resourcemanager.utils.RMLoggers;


/**
 * DataSpaceNodeConfigurationAgent is used to configure and close DataSpaces knowledge
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 */
public class DataSpaceNodeConfigurationAgent implements Serializable {

    private static Logger logger = ProActiveLogger.getLogger(RMLoggers.DATASPACE);
    protected static final String NODE_DATASPACE_SCRATCHDIR = "node.dataspace.scratchdir";

    /**
     * Create a new instance of DataSpaceNodeConfigurationAgent
     * Used by ProActive
     */
    public DataSpaceNodeConfigurationAgent() {
    }

    public void configureNode() {
        try {
            // configure node for Data Spaces
            String scratchDir;
            if (System.getProperty(NODE_DATASPACE_SCRATCHDIR) == null) {
                //if scratch dir java property is not set, set to default
                scratchDir = System.getProperty("java.io.tmpdir") + File.separator + "ds_scratch";
            } else {
                //else use the property
                scratchDir = System.getProperty(NODE_DATASPACE_SCRATCHDIR);
            }
            final BaseScratchSpaceConfiguration scratchConf = new BaseScratchSpaceConfiguration(null,
                scratchDir);
            DataSpacesNodes.configureNode(PAActiveObject.getActiveObjectNode(PAActiveObject.getStubOnThis()),
                    scratchConf);
        } catch (Throwable t) {
            logger.error("Cannot configure dataSpace", t);
        }
        PAActiveObject.terminateActiveObject(false);
    }

    public void closeNodeConfiguration() {
        try {
            DataSpacesNodes.closeNodeConfig(PAActiveObject
                    .getActiveObjectNode(PAActiveObject.getStubOnThis()));
        } catch (Throwable t) {
            logger.error("Cannot close dataSpace configuration", t);
        }
        PAActiveObject.terminateActiveObject(false);
    }

}
