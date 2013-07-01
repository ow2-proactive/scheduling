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

import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.core.DataSpaceServiceStarter;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class JobDataSpaceApplication implements Serializable {

    public static final Logger logger = Logger.getLogger(JobDataSpaceApplication.class);

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
     * @param inputURL  the url of the input space given in the job, if null default scheduler INPUT space URL will be used
     * @param outputURL the url of the output space given in the job, if null default scheduler OUTPUT space URL will be used
     * @param globalURL the url of the global space given in the job, if null default scheduler GLOBAL space URL will be used
     * @param userURL   the url of the user space given in the job, if null default scheduler USER space URL will be used
     * @param username  the owner of the job
     * @param jobId     unique identifier for the current job; can used to separated GLOBAL space among jobs
     */
    public void startDataSpaceApplication(String inputURL, String outputURL, String globalURL,
            String userURL, String username, JobId jobId) {
        if (!alreadyRegistered) {
            try {

                // create INPUT space
                if (inputURL != null) {
                    if (inputURL.indexOf(':') < 2) {
                        // assume this is a file path
                        inputURL = ((new File(inputURL)).toURI().toURL()).toString();
                    }
                    DataSpaceServiceStarter.createSpace(applicationId, PADataSpaces.DEFAULT_IN_OUT_NAME,
                            inputURL, null, null, true, false);
                } else {
                    String localpath = PASchedulerProperties.DATASPACE_DEFAULTINPUT_LOCALPATH
                            .getValueAsStringOrNull();
                    String hostname = PASchedulerProperties.DATASPACE_DEFAULTINPUT_HOSTNAME
                            .getValueAsStringOrNull();
                    DataSpaceServiceStarter.createSpace(applicationId, PADataSpaces.DEFAULT_IN_OUT_NAME,
                            PASchedulerProperties.DATASPACE_DEFAULTINPUT_URL.getValueAsString() + username,
                            localpath != null ? localpath + File.separator + username : null, hostname, true,
                            false);
                }
                // create OUTPUT space
                if (outputURL != null) {
                    if (outputURL.indexOf(':') < 2) {
                        // assume this is a file path
                        outputURL = ((new File(outputURL)).toURI().toURL()).toString();
                    }
                    DataSpaceServiceStarter.createSpace(applicationId, PADataSpaces.DEFAULT_IN_OUT_NAME,
                            outputURL, null, null, false, false);
                } else {
                    String localpath = PASchedulerProperties.DATASPACE_DEFAULTOUTPUT_LOCALPATH
                            .getValueAsStringOrNull();
                    String hostname = PASchedulerProperties.DATASPACE_DEFAULTOUTPUT_HOSTNAME
                            .getValueAsStringOrNull();
                    DataSpaceServiceStarter.createSpace(applicationId, PADataSpaces.DEFAULT_IN_OUT_NAME,
                            PASchedulerProperties.DATASPACE_DEFAULTOUTPUT_URL.getValueAsString() + username,
                            localpath != null ? localpath + File.separator + username : null, hostname,
                            false, false);
                }
                // create GLOBAL shared space
                if (globalURL != null) {
                    if (globalURL.indexOf(':') < 2) {
                        // assume this is a file path
                        globalURL = ((new File(globalURL)).toURI().toURL()).toString();
                    }
                    DataSpaceServiceStarter.createSpace(applicationId, SchedulerConstants.GLOBALSPACE_NAME,
                            globalURL, null, null, false, false);
                } else {
                    String localpath = PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_LOCALPATH
                            .getValueAsStringOrNull();
                    String hostname = PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_HOSTNAME
                            .getValueAsStringOrNull();

                    DataSpaceServiceStarter.createSpace(applicationId, SchedulerConstants.GLOBALSPACE_NAME,
                            PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_URL.getValueAsString(), localpath,
                            hostname, false, false);
                }

                // create USER space for this job, the application ID of this user space will be the AppId of the job
                if (userURL != null) {
                    if (userURL.indexOf(':') < 2) {
                        // assume this is a file path
                        userURL = ((new File(userURL)).toURI().toURL()).toString();
                    }
                    DataSpaceServiceStarter.createSpace(applicationId, SchedulerConstants.USERSPACE_NAME,
                            userURL, null, null, false, false);
                } else {
                    String localpath = PASchedulerProperties.DATASPACE_DEFAULTUSER_LOCALPATH
                            .getValueAsStringOrNull();
                    String hostname = PASchedulerProperties.DATASPACE_DEFAULTUSER_HOSTNAME
                            .getValueAsStringOrNull();
                    if (localpath != null) {
                        localpath = localpath + File.separator + username;
                        File localPathFile = new File(localpath);
                        if (!localPathFile.exists()) {
                            localPathFile.mkdirs();
                        }
                    }
                    DataSpaceServiceStarter.createSpace(applicationId, SchedulerConstants.USERSPACE_NAME,
                            PASchedulerProperties.DATASPACE_DEFAULTUSER_URL.getValueAsString() + username,
                            localpath, hostname, false, false);
                }
                alreadyRegistered = true;
            } catch (Exception e) {
                logger.warn("", e);
            }
        }
    }

    public void terminateDataSpaceApplication() {
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
