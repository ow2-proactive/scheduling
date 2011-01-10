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

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingServiceDeployer;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public final class DataSpaceServiceStarter implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 30L;
	private static final String DEFAULT_LOCAL = System.getProperty("java.io.tmpdir") + File.separator +
        "scheduling";
    private static final String DEFAULT_LOCAL_INPUT = DEFAULT_LOCAL + File.separator + "defaultinput";
    private static final String DEFAULT_LOCAL_OUTPUT = DEFAULT_LOCAL + File.separator + "defaultoutput";

    private String namingServiceURL;
    private NamingServiceDeployer namingServiceDeployer;
    private NamingService namingService;

    private FileSystemServerDeployer inputFilesServer = null;
    private FileSystemServerDeployer outputFilesServer = null;

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
        //set default INPUT/OUTPUT spaces if needed
        String hostname = PAActiveObject.getActiveObjectNode(PAActiveObject.getStubOnThis())
                .getVMInformation().getHostName();
        //variable used to precise exception
        String currentDir = null;
        try {
            //INPUT
            if (!PASchedulerProperties.DATASPACE_DEFAULTINPUTURL.isSet()) {
                currentDir = DEFAULT_LOCAL_INPUT;
                File dir = new File(DEFAULT_LOCAL_INPUT);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                inputFilesServer = new FileSystemServerDeployer("defaultSchedulingInput",
                    DEFAULT_LOCAL_INPUT, true, true);
                String url = inputFilesServer.getVFSRootURL();
                PASchedulerProperties.DATASPACE_DEFAULTINPUTURL.updateProperty(url);
                PASchedulerProperties.DATASPACE_DEFAULTINPUTURL_LOCALPATH.updateProperty(DEFAULT_LOCAL_INPUT);
                PASchedulerProperties.DATASPACE_DEFAULTINPUTURL_HOSTNAME.updateProperty(hostname);
            }
            //OUTPUT
            if (!PASchedulerProperties.DATASPACE_DEFAULTOUTPUTURL.isSet()) {
                currentDir = DEFAULT_LOCAL_OUTPUT;
                File dir = new File(DEFAULT_LOCAL_OUTPUT);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                outputFilesServer = new FileSystemServerDeployer("defaultSchedulingOutput",
                    DEFAULT_LOCAL_OUTPUT, true, true);
                String url = outputFilesServer.getVFSRootURL();
                PASchedulerProperties.DATASPACE_DEFAULTOUTPUTURL.updateProperty(url);
                PASchedulerProperties.DATASPACE_DEFAULTOUTPUTURL_LOCALPATH
                        .updateProperty(DEFAULT_LOCAL_OUTPUT);
                PASchedulerProperties.DATASPACE_DEFAULTOUTPUTURL_HOSTNAME.updateProperty(hostname);
            }
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("Directory '" + currentDir +
                "' cannot be accessed. Check directory exists or you have read/write right.");
        }
        //let URL terminate by /
        String url = PASchedulerProperties.DATASPACE_DEFAULTINPUTURL.getValueAsString();
        if (!url.endsWith("/")) {
            PASchedulerProperties.DATASPACE_DEFAULTINPUTURL.updateProperty(url + "/");
        }
        url = PASchedulerProperties.DATASPACE_DEFAULTOUTPUTURL.getValueAsString();
        if (!url.endsWith("/")) {
            PASchedulerProperties.DATASPACE_DEFAULTOUTPUTURL.updateProperty(url + "/");
        }
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
        try {
            if (inputFilesServer != null) {
                inputFilesServer.terminate();
            }
            if (outputFilesServer != null) {
                outputFilesServer.terminate();
            }
        } catch (Throwable t) {
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
