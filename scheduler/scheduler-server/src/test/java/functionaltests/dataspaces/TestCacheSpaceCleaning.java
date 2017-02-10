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

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.nodesource.dataspace.DataSpaceNodeConfigurationAgent;

import functionaltests.utils.RMTHelper;
import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * TestCacheSpaceCleaning
 * <p>
 * This test ensures that the cleaning mechanism of the cache space functions properly.
 * It starts an empty scheduler and add nodes with modified variables to reduce the cleaning and invalidation periods.
 * It then execute tasks to transfer files to the cache, and checks that files are deleted by the cleaning mechanism.
 *
 * @author The ProActive Team
 */
public class TestCacheSpaceCleaning extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    static URL jobDescriptor1 = TestCacheSpaceCleaning.class.getResource("/functionaltests/descriptors/Job_CacheSpace2.xml");

    static String dataToCopyFileName = "dataToCopy";

    static String dataToUpdateFileName = "dataToUpdate";

    static int NB_WAIT_PERIOD = 5 * 60 * 2;

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        RMFactory.setOsJavaProperty();
        // start an empty scheduler and add a node source with modified properties
        schedulerHelper = new SchedulerTHelper(true, true);
        List<String> arguments = new ArrayList<>();
        arguments.addAll(RMTHelper.setup.getJvmParametersAsList());
        arguments.add("-D" + DataSpaceNodeConfigurationAgent.NODE_DATASPACE_CACHE_CLEANING_PERIOD + "=" + 2000);
        arguments.add("-D" + DataSpaceNodeConfigurationAgent.NODE_DATASPACE_CACHE_INVALIDATION_PERIOD + "=" + 40000);
        schedulerHelper.createNodeSource("CleaningNS", 5, arguments);
    }

    @Test
    public void testCacheSpaceCleaning() throws Throwable {
        String userURI = schedulerHelper.getSchedulerInterface().getUserSpaceURIs().get(0);
        assertTrue(userURI.startsWith("file:"));
        log("User URI is " + userURI);
        String userPath = new File(new URI(userURI)).getAbsolutePath();

        // create two files, one which will be copied once
        File dataToCopyFile = createFileInUserSpace(userPath, dataToCopyFileName);
        // and one which will be copied once, updated on the server, and which should be then updated in the cache
        File dataToUpdateFile = createFileInUserSpace(userPath, dataToUpdateFileName);

        // submit the first job to transfer files.
        schedulerHelper.testJobSubmission(new File(jobDescriptor1.toURI()).getAbsolutePath());

        File cacheFolder = new File(System.getProperty("java.io.tmpdir"),
                                    DataSpaceNodeConfigurationAgent.DEFAULT_CACHE_SUBFOLDER_NAME);

        File transferredDataToCopyFile = new File(cacheFolder, dataToCopyFileName);
        File transferredDataToUpdateFile = new File(cacheFolder, dataToUpdateFileName);

        // ensure that files are present in the cache
        Assert.assertTrue("File " + dataToCopyFileName + " must be present in the cache after the job execution",
                          transferredDataToCopyFile.exists());
        Assert.assertTrue("File " + dataToUpdateFileName + " must be present in the cache after the job execution",
                          transferredDataToUpdateFile.exists());

        // change the file to update
        dataToUpdateFile.delete();
        Thread.sleep(8000);
        dataToUpdateFile.createNewFile();

        // submit the job a second time.
        schedulerHelper.testJobSubmission(new File(jobDescriptor1.toURI()).getAbsolutePath());

        // wait until the first file is deleted by the cleaning mechanism
        int nbWait = 0;
        while (transferredDataToCopyFile.exists() && nbWait < NB_WAIT_PERIOD) {
            Thread.sleep(200);
            nbWait++;
        }
        // ensure that the second file (modified at a later date), remains
        Assert.assertFalse("File " + dataToCopyFileName + " must be deleted in the cache by the cleaning mechanism",
                           transferredDataToCopyFile.exists());
        Assert.assertTrue("File " + dataToUpdateFileName +
                          " must be present in the cache after the first file is removed",
                          transferredDataToUpdateFile.exists());

        // wait until the second file is deleted by the cleaning mechanism
        nbWait = 0;
        while (transferredDataToUpdateFile.exists() && nbWait < NB_WAIT_PERIOD) {
            Thread.sleep(500);
            nbWait++;
        }
        Assert.assertFalse("File " + dataToUpdateFileName + " must be deleted in the cache by the cleaning mechanism",
                           transferredDataToUpdateFile.exists());

    }

    private File createFileInUserSpace(String userPath, String dataToCopyFileName) throws IOException {
        File dataToCopyFile = new File(userPath, dataToCopyFileName);
        if (dataToCopyFile.exists()) {
            dataToCopyFile.delete();
        }
        dataToCopyFile.createNewFile();
        return dataToCopyFile;
    }
}
