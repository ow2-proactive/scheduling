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
package functionaltests.dataspaces;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.ow2.proactive.scheduler.job.TaskDataSpaceApplication;
import org.ow2.proactive.utils.Tools;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;


/**
 * TestDataSpaceConfiguration
 *
 * tests that a provided DataSpace configuration is correctly read and creates the right dataspaces
 *
 * @author The ProActive Team
 */
public class TestDataSpaceConfiguration extends SchedulerFunctionalTestNoRestart {

    static String IOSPACE = System.getProperty("java.io.tmpdir") + File.separator + "scheduler test" + File.separator +
                            "my space"; // evil spaces provided

    FileSystemServerDeployer filesServerIn;

    String username = "de mo"; // another evil space

    static String HOSTNAME = null;

    String appid = "666";

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
        spFileWithUserDir.deleteOnExit();
        spFile.deleteOnExit();

        filesServerIn = new FileSystemServerDeployer("space", IOSPACE, true, true);
        String[] spaceurls = filesServerIn.getVFSRootURLs();

        String[] userdirUrls = DataSpaceServiceStarter.urlsWithUserDir(spaceurls, username);

        ArrayList<String> expected = new ArrayList<>();
        expected.addAll(Arrays.asList(spaceurls));

        ArrayList<String> expectedWithUserDir = new ArrayList<>();
        expectedWithUserDir.addAll(Arrays.asList(userdirUrls));

        PASchedulerProperties.DATASPACE_DEFAULTINPUT_URL.updateProperty(DataSpaceServiceStarter.urlsToDSConfigProperty(spaceurls));
        PASchedulerProperties.DATASPACE_DEFAULTINPUT_LOCALPATH.updateProperty(IOSPACE);
        PASchedulerProperties.DATASPACE_DEFAULTINPUT_HOSTNAME.updateProperty(HOSTNAME);

        PASchedulerProperties.DATASPACE_DEFAULTOUTPUT_URL.updateProperty(DataSpaceServiceStarter.urlsToDSConfigProperty(spaceurls));
        PASchedulerProperties.DATASPACE_DEFAULTOUTPUT_LOCALPATH.updateProperty(IOSPACE);
        PASchedulerProperties.DATASPACE_DEFAULTOUTPUT_HOSTNAME.updateProperty(HOSTNAME);

        PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_URL.updateProperty(DataSpaceServiceStarter.urlsToDSConfigProperty(spaceurls));
        PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_LOCALPATH.updateProperty(IOSPACE);
        PASchedulerProperties.DATASPACE_DEFAULTGLOBAL_HOSTNAME.updateProperty(HOSTNAME);

        PASchedulerProperties.DATASPACE_DEFAULTUSER_URL.updateProperty(DataSpaceServiceStarter.urlsToDSConfigProperty(spaceurls));
        PASchedulerProperties.DATASPACE_DEFAULTUSER_LOCALPATH.updateProperty(IOSPACE);
        PASchedulerProperties.DATASPACE_DEFAULTUSER_HOSTNAME.updateProperty(HOSTNAME);

        DataSpaceServiceStarter dsServiceStarter = DataSpaceServiceStarter.getDataSpaceServiceStarter();
        dsServiceStarter.startNamingService();

        Set<SpaceInstanceInfo> predefinedSpaces = new HashSet<>();
        NamingService namingService = dsServiceStarter.getNamingService();

        TaskDataSpaceApplication jdsa = new TaskDataSpaceApplication(appid, dsServiceStarter.getNamingService());
        jdsa.startDataSpaceApplication(null, null, null, null, username, null);

        DataSpacesNodes.configureApplication(PAActiveObject.getNode(), appid, dsServiceStarter.getNamingServiceURL());

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
    public void testDataSpaceConfiguration() throws Throwable {

        TestDataSpaceConfiguration callee = PAActiveObject.turnActive(new TestDataSpaceConfiguration());
        try {
            callee.runStarter();
        } finally {
            PAActiveObject.terminateActiveObject(callee, true);
        }
    }

    @Test
    public void testPropertyParsing() throws Throwable {
        Assert.assertArrayEquals(new String[0], Tools.dataSpaceConfigPropertyToUrls("  \"\"  "));
        Assert.assertArrayEquals(new String[0], Tools.dataSpaceConfigPropertyToUrls("  "));
        Assert.assertArrayEquals(new String[] { "a" }, Tools.dataSpaceConfigPropertyToUrls(" \"a\"  "));
        Assert.assertArrayEquals(new String[] { "a" }, Tools.dataSpaceConfigPropertyToUrls("a"));
        Assert.assertArrayEquals(new String[] { "a" }, Tools.dataSpaceConfigPropertyToUrls(" a  "));
        Assert.assertArrayEquals(new String[] { "a b" }, Tools.dataSpaceConfigPropertyToUrls(" \"a b\"  "));
        Assert.assertArrayEquals(new String[] { "a", "b" }, Tools.dataSpaceConfigPropertyToUrls(" a b  "));
        Assert.assertArrayEquals(new String[] { "a b c" }, Tools.dataSpaceConfigPropertyToUrls(" \"a b c\"  "));
        Assert.assertArrayEquals(new String[] { "a", "b", "c" }, Tools.dataSpaceConfigPropertyToUrls("  a b c  "));
        Assert.assertArrayEquals(new String[] { "a b c", "d e f" },
                                 Tools.dataSpaceConfigPropertyToUrls(" \"a b c\"    \"d e f\"   "));
        Assert.assertArrayEquals(new String[] { "a b c d e f" },
                                 Tools.dataSpaceConfigPropertyToUrls("   \"a b c d e f\"   "));
        Assert.assertArrayEquals(new String[] { "a", "b", "c", "d", "e", "f" },
                                 Tools.dataSpaceConfigPropertyToUrls("   a b c   d e    f "));
    }

    @After
    public void clean() throws ProActiveException {
        if (filesServerIn != null) {
            filesServerIn.terminate();
        }
    }
}
