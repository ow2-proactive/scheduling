/*
 *  *
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package unitTests;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesNodes;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.core.DataSpaceServiceStarter;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.job.JobDataSpaceApplication;


/**
 * TestDataSpaceConfiguration
 *
 * tests that a provided DataSpace configuration is correctly read and creates the right dataspaces
 *
 * @author The ProActive Team
 */
public class TestDataSpaceConfiguration {

    static String IOSPACE = System.getProperty("java.io.tmpdir") + File.separator + "scheduler_test" +
        File.separator + "space";

    FileSystemServerDeployer filesServerIn;

    String username = "demo";

    static String HOSTNAME = null;

    long appid = 666;

    static {
        try {
            HOSTNAME = java.net.InetAddress.getLocalHost().getHostName();

        } catch (Exception e) {
        }
    }

    public TestDataSpaceConfiguration() {

    }

    public Boolean runStarter() throws Exception {
        File spFile = new File(IOSPACE);
        File spFileWithUserDir = new File(IOSPACE, username);
        spFile.mkdirs();
        spFileWithUserDir.mkdirs();

        filesServerIn = new FileSystemServerDeployer("space", IOSPACE, true, true);
        String spaceurl = filesServerIn.getVFSRootURL();

        String spaceurlWithUserDir = spaceurl + username;

        ArrayList<String> expected = new ArrayList<String>();
        expected.add(spFile.toURI().toURL().toExternalForm());
        expected.add(spaceurl);

        ArrayList<String> expectedWithUserDir = new ArrayList<String>();
        expectedWithUserDir.add(spFileWithUserDir.toURI().toURL().toExternalForm());
        expectedWithUserDir.add(spaceurlWithUserDir);

        PASchedulerProperties.DATASPACE_DEFAULTINPUT_URL.updateProperty(spaceurl);
        PASchedulerProperties.DATASPACE_DEFAULTINPUT_LOCALPATH.updateProperty(IOSPACE);
        PASchedulerProperties.DATASPACE_DEFAULTINPUT_HOSTNAME.updateProperty(HOSTNAME);

        PASchedulerProperties.DATASPACE_DEFAULTOUTPUT_URL.updateProperty(spaceurl);
        PASchedulerProperties.DATASPACE_DEFAULTOUTPUT_LOCALPATH.updateProperty(IOSPACE);
        PASchedulerProperties.DATASPACE_DEFAULTOUTPUT_HOSTNAME.updateProperty(HOSTNAME);

        PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_URL.updateProperty(spaceurl);
        PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_LOCALPATH.updateProperty(IOSPACE);
        PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_HOSTNAME.updateProperty(HOSTNAME);

        PASchedulerProperties.DATASPACE_DEFAULTUSER_URL.updateProperty(spaceurl);
        PASchedulerProperties.DATASPACE_DEFAULTUSER_LOCALPATH.updateProperty(IOSPACE);
        PASchedulerProperties.DATASPACE_DEFAULTUSER_HOSTNAME.updateProperty(HOSTNAME);

        DataSpaceServiceStarter dsServiceStarter = new DataSpaceServiceStarter();
        dsServiceStarter.startNamingService();

        Set<SpaceInstanceInfo> predefinedSpaces = new HashSet<SpaceInstanceInfo>();
        NamingService namingService = dsServiceStarter.getNamingService();

        JobDataSpaceApplication jdsa = new JobDataSpaceApplication(appid,
            dsServiceStarter.getNamingService(), dsServiceStarter.getNamingServiceURL());
        jdsa.startDataSpaceApplication(null, null, null, null, username, null);

        DataSpacesNodes.configureApplication(PAActiveObject.getNode(), appid, dsServiceStarter
                .getNamingServiceURL());

        DataSpacesFileObject INPUT = PADataSpaces.resolveDefaultInput();
        DataSpacesFileObject OUTPUT = PADataSpaces.resolveDefaultOutput();
        DataSpacesFileObject GLOBAL = PADataSpaces.resolveOutput(SchedulerConstants.GLOBALSPACE_NAME);
        DataSpacesFileObject USER = PADataSpaces.resolveOutput(SchedulerConstants.USERSPACE_NAME);

        Assert.assertEquals(expectedWithUserDir, INPUT.getAllRealURIs());
        Assert.assertEquals(expectedWithUserDir, OUTPUT.getAllRealURIs());
        Assert.assertEquals(expected, GLOBAL.getAllRealURIs());
        Assert.assertEquals(expectedWithUserDir, USER.getAllRealURIs());

        jdsa.terminateDataSpaceApplication();
        return true;
    }

    @Test
    public void run() throws Throwable {

        TestDataSpaceConfiguration callee = PAActiveObject.turnActive(new TestDataSpaceConfiguration());

        callee.runStarter();
    }

    @After
    public void clean() throws ProActiveException {
        if (filesServerIn != null) {
            filesServerIn.terminate();
        }
    }
}
