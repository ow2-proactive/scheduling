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
package functionaltests.workflow;

import static functionaltests.utils.SchedulerTHelper.log;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.Job2XMLTransformer;
import org.ow2.proactive.scheduler.common.job.factories.JobComparator;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.job.factories.StaxJobFactory;

import functionaltests.job.multinodes.TestMultipleHostsRequest;


/**
 * This class tests the coherence between the {@link Job2XMLTransformer} and the
 * {@link StaxJobFactory}. For each job descriptor in the
 * "/functionaltests/descriptors/" folder (except if the file descriptor
 * contains the keyword 'invalid') it creates a {@link TaskFlowJob} object using
 * the {@link StaxJobFactory}. The java job is then serialized to xml using the
 * {@link Job2XMLTransformer} and then a new {@link TaskFlowJob} is created.
 * Both {@link TaskFlowJob} objects are then compared using {@link JobComparator}.
 *
 * @author esalagea
 */
public class TestXMLTransformer {

    private static URL jobDescriptorsFolder = TestXMLTransformer.class.getResource("/functionaltests/descriptors/");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static final String executablePathPropertyName = "EXEC_PATH";

    private static final String workingDirPropName = "WDIR";

    private static final String cmdPropName = "WCOM";

    /**
     * set system properties required by different job descriptors
     *
     * @throws URISyntaxException
     */
    private void setProperties() throws URISyntaxException {
        System.setProperty(executablePathPropertyName,
                           new File(TestMultipleHostsRequest.class.getResource("/functionaltests/executables/test_multiple_hosts_request.sh")
                                                                  .toURI()).getAbsolutePath());
        System.setProperty(workingDirPropName, folder.getRoot().getAbsolutePath());
        System.setProperty(cmdPropName, "echo");
    }

    @Before
    public void prepareForTest() throws Exception {
        setProperties();
    }

    @Test
    public void testXMLTransformer() throws Throwable {
        File folder = new File(jobDescriptorsFolder.toURI());
        Collection<File> testJobDescrFiles = FileUtils.listFiles(folder, new String[] { "xml" }, true);

        log("Treating " + testJobDescrFiles.size() + " job descriptors.");

        for (File file : testJobDescrFiles) {
            // skip descriptor files which are there to test invalid job description
            if (file.getName().contains("invalid")) {
                continue;
            }

            try {
                transformAndCompare(file);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("An exception occured while treating the file " + file.getAbsolutePath(), e);
            }

        }
    }

    /**
     * The following operations are performed:
     *
     * 1. xmlFile to java => job1
     *
     * 2. job1 = > xmlFle2
     *
     * 3. xmlFile2 to java => job2
     *
     * 4. Compare job1 and job2
     */
    private void transformAndCompare(File xmlFile) throws Exception {
        // xml to java => job1
        TaskFlowJob job1 = (TaskFlowJob) (JobFactory.getFactory().createJob(xmlFile.getAbsolutePath()));

        // job1 to xmlFile2
        File xmlFile2 = folder.newFile(xmlFile.getName());
        Job2XMLTransformer transformer = new Job2XMLTransformer();
        transformer.job2xmlFile(job1, xmlFile2);

        // xmlFile2 to job2
        TaskFlowJob job2;
        try {
            job2 = (TaskFlowJob) (JobFactory.getFactory().createJob(xmlFile2.getAbsolutePath()));
        } catch (Exception e) {
            e.printStackTrace();
            String message = "Could not create Job object from generated xml. \n";
            message += "Generated xml content was : \n ****** " + xmlFile2.getAbsolutePath() + " ***********\n ";
            message += FileUtils.readFileToString(xmlFile2);
            message += "\n *************************** ";
            throw new Exception(message, e);
        }

        // compare job1 and job2
        JobComparator comparator = new JobComparator();
        if (!comparator.isEqualJob(job1, job2)) {
            String message = "Jobs are not equal for file " + xmlFile + "\n";
            message += "Reason: " + comparator.getDifferenceMessage() + "\n";
            message += "Generated xml content was : \n ****** " + xmlFile2.getAbsolutePath() + " *********** \n ";
            message += FileUtils.readFileToString(xmlFile2);
            message += "\n *************************** ";
            Assert.fail(message);
        }
    }

}
