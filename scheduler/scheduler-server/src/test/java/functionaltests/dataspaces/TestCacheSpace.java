/*
 *  *
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.dataspaces;

import functionaltests.utils.SchedulerFunctionalTestNoRestart;
import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.nodesource.dataspace.DataSpaceNodeConfigurationAgent;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import static functionaltests.utils.SchedulerTHelper.log;
import static org.junit.Assert.assertTrue;

/**
 * TestCacheSpace
 * <p>
 * tests that files are correctly transferred to the cache space.
 * That transferred files remain for subsequent jobs, and that modified files are updated in the cache
 *
 * @author The ProActive Team
 */
public class TestCacheSpace extends SchedulerFunctionalTestNoRestart {
    static URL jobDescriptor1 = TestCacheSpace.class
            .getResource("/functionaltests/descriptors/Job_CacheSpace1.xml");

    static String dataToCopyFileName = "dataToCopy";
    static String dataToUpdateFileName = "dataToUpdate";

    @Test
    public void testSubmitJobWithTransferFilesToCacheSpace() throws Throwable {

        String userURI = schedulerHelper.getSchedulerInterface().getUserSpaceURIs().get(0);
        assertTrue(userURI.startsWith("file:"));
        log("User URI is " + userURI);
        String userPath = new File(new URI(userURI)).getAbsolutePath();

        // create two files, one which will be copied once
        File dataToCopyFile = createFileInUserSpace(userPath, dataToCopyFileName);
        // and one which will be copied once, updated on the server, and which should be then updated in the cache
        File dataToUpdateFile = createFileInUserSpace(userPath, dataToUpdateFileName);

        // submit the job to transfer files.
        schedulerHelper.testJobSubmission(new File(jobDescriptor1.toURI())
                .getAbsolutePath());

        File cacheFolder = new File(System.getProperty("java.io.tmpdir"), DataSpaceNodeConfigurationAgent.DEFAULT_CACHE_SUBFOLDER_NAME);

        File transferredDataToCopyFile = new File(cacheFolder, dataToCopyFileName);
        File transferredDataToUpdateFile = new File(cacheFolder, dataToUpdateFileName);

        Assert.assertTrue("File " + dataToCopyFileName + " must be present in the cache after the job execution", transferredDataToCopyFile.exists());
        Assert.assertTrue("File " + dataToUpdateFileName + " must be present in the cache after the job execution", transferredDataToUpdateFile.exists());

        long lastCopyFileModificationTime = transferredDataToCopyFile.lastModified();
        long lastUpdateFileModificationTime = transferredDataToUpdateFile.lastModified();

        // change the file to update
        dataToUpdateFile.delete();
        dataToUpdateFile.createNewFile();

        // submit the job a second time.
        schedulerHelper.testJobSubmission(new File(jobDescriptor1.toURI())
                .getAbsolutePath());

        Assert.assertTrue("File " + dataToCopyFileName + " must be present in the cache after the job execution", transferredDataToCopyFile.exists());
        Assert.assertTrue("File " + dataToUpdateFileName + " must be present in the cache after the job execution", transferredDataToUpdateFile.exists());

        // verify file modification.
        Assert.assertTrue("File " + dataToCopyFileName + " must be the same than the previous transfer", transferredDataToCopyFile.lastModified() == lastCopyFileModificationTime);
        Assert.assertTrue("File " + dataToUpdateFileName + " must be newer than the previous transfer", transferredDataToUpdateFile.lastModified() > lastUpdateFileModificationTime);


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
