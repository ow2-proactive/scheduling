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
package org.ow2.proactive.scheduler.job;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.FileSelector;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


public class JobDataSpaceApplication implements Serializable {

    private static final long serialVersionUID = 32L;

    public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.DATASPACE);

    private long applicationId;
    private String namingServiceURL;
    private NamingService namingService;

    private boolean alreadyRegistered = false;

    /**
     * Create a new instance of JobDataSpaceApplication
     *
     * @param applicationId the application Id associated to this job
     */
    public JobDataSpaceApplication(long applicationId, NamingService namingService, String namingServiceURL) {
        this.applicationId = applicationId;
        this.namingService = namingService;
        this.namingServiceURL = namingServiceURL;
    }

    /**
     * Start dataspace configuration and application for this job
     * This method will set the OUTPUT and OUTPUT fileObject that can be passed to node
     *
     * @param inputURL the input URL given in the job, if null default scheduler INPUT space URL will be used
     * @param outputURL the output URL given in the job, if null default scheduler OUTPUT space URL will be used
     * @param username the owner of the job
     * @param id unique identificator for the current job; used to separated GLOBAL space among jobs
     */
    public void startDataSpaceApplication(String inputURL, String outputURL, String username, JobId jobId) {
        if (!alreadyRegistered) {
            try {
                // create list of spaces
                Set<SpaceInstanceInfo> predefinedSpaces = new HashSet<SpaceInstanceInfo>();
                InputOutputSpaceConfiguration isc;
                InputOutputSpaceConfiguration osc;
                InputOutputSpaceConfiguration glob;

                // create INPUT space
                if (inputURL != null) {
                    isc = InputOutputSpaceConfiguration.createInputSpaceConfiguration(inputURL, null, null,
                            PADataSpaces.DEFAULT_IN_OUT_NAME);
                } else {
                    String localpath = null;
                    String hostname = null;
                    if (PASchedulerProperties.DATASPACE_DEFAULTINPUTURL_LOCALPATH.isSet() &&
                        PASchedulerProperties.DATASPACE_DEFAULTINPUTURL_HOSTNAME.isSet()) {
                        localpath = PASchedulerProperties.DATASPACE_DEFAULTINPUTURL_LOCALPATH
                                .getValueAsString() +
                            File.separator + username;
                        hostname = PASchedulerProperties.DATASPACE_DEFAULTINPUTURL_HOSTNAME
                                .getValueAsString();
                    }
                    isc = InputOutputSpaceConfiguration.createInputSpaceConfiguration(
                            PASchedulerProperties.DATASPACE_DEFAULTINPUTURL.getValueAsString() + username,
                            localpath, hostname, PADataSpaces.DEFAULT_IN_OUT_NAME);
                }
                // create OUTPUT space
                if (outputURL != null) {
                    osc = InputOutputSpaceConfiguration.createOutputSpaceConfiguration(outputURL, null, null,
                            PADataSpaces.DEFAULT_IN_OUT_NAME);
                } else {
                    String localpath = null;
                    String hostname = null;
                    if (PASchedulerProperties.DATASPACE_DEFAULTOUTPUTURL_LOCALPATH.isSet() &&
                        PASchedulerProperties.DATASPACE_DEFAULTOUTPUTURL_HOSTNAME.isSet()) {
                        localpath = PASchedulerProperties.DATASPACE_DEFAULTOUTPUTURL_LOCALPATH
                                .getValueAsString() +
                            File.separator + username;
                        hostname = PASchedulerProperties.DATASPACE_DEFAULTOUTPUTURL_HOSTNAME
                                .getValueAsString();
                    }
                    osc = InputOutputSpaceConfiguration.createOutputSpaceConfiguration(
                            PASchedulerProperties.DATASPACE_DEFAULTOUTPUTURL.getValueAsString() + username,
                            localpath, hostname, PADataSpaces.DEFAULT_IN_OUT_NAME);
                }

                if (PASchedulerProperties.DATASPACE_GLOBAL_URL.isSet()) {
                    String localpath = null;
                    String hostname = null;
                    if (PASchedulerProperties.DATASPACE_GLOBAL_URL_LOCALPATH.isSet() &&
                        PASchedulerProperties.DATASPACE_GLOBAL_URL_HOSTNAME.isSet()) {
                        localpath = PASchedulerProperties.DATASPACE_GLOBAL_URL_LOCALPATH.getValueAsString() +
                            File.separator + jobId.value();
                        hostname = PASchedulerProperties.DATASPACE_GLOBAL_URL_LOCALPATH.getValueAsString();
                    }
                    glob = InputOutputSpaceConfiguration.createOutputSpaceConfiguration(
                            PASchedulerProperties.DATASPACE_GLOBAL_URL.getValueAsString() + File.separator +
                                jobId.value(), localpath, hostname, SchedulerConstants.GLOBALSPACE_NAME);

                    predefinedSpaces.add(new SpaceInstanceInfo(applicationId, glob));
                }

                predefinedSpaces.add(new SpaceInstanceInfo(applicationId, isc));
                predefinedSpaces.add(new SpaceInstanceInfo(applicationId, osc));
                // register application
                namingService.registerApplication(applicationId, predefinedSpaces);
                alreadyRegistered = true;
            } catch (Exception e) {
                logger_dev.warn("", e);
            }
        }
    }

    public void terminateDataSpaceApplication() {
        // remove GLOBAL sub directory for this job
        if (PASchedulerProperties.DATASPACE_GLOBAL_URL.isSet()) {
            try {
                DataSpacesNodes.configureNode(PAActiveObject.getActiveObjectNode(PAActiveObject
                        .getStubOnThis()), null);
                DataSpacesNodes.configureApplication(PAActiveObject.getActiveObjectNode(PAActiveObject
                        .getStubOnThis()), this.applicationId, this.namingServiceURL);

                DataSpacesFileObject global = PADataSpaces.resolveOutput(SchedulerConstants.GLOBALSPACE_NAME);
                global.delete(FileSelector.SELECT_ALL);
                global.delete();
            } catch (Throwable e) {
                logger_dev.warn("Could not clear GLOBAL subdir", e);
            } finally {
                try {
                    DataSpacesNodes.tryCloseNodeApplicationConfig(PAActiveObject
                            .getActiveObjectNode(PAActiveObject.getStubOnThis()));
                    DataSpacesNodes.closeNodeConfig(PAActiveObject.getActiveObjectNode(PAActiveObject
                            .getStubOnThis()));
                } catch (Throwable e) {
                }
            }
        }

        try {
            namingService.unregisterApplication(applicationId);
        } catch (Exception e) {
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
}
