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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package functionaltests;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.Job2XMLTransformer;
import org.ow2.proactive.scheduler.common.job.factories.JobComparator;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory_stax;
import org.ow2.tests.FunctionalTest;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Before;


/**
 * This class tests the coherence between the {@link Job2XMLTransformer} and the
 * {@link JobFactory_stax}. For each job descriptor in the
 * "/functionaltests/descriptors/" and
 * "$pa.scheduler.home/samples/jobs_descriptors" folders it creates a
 * {@link TaskFlowJob} object using the {@link JobFactory_stax}. The java job is
 * then serialized to xml using the {@link Job2XMLTransformer} and then a new
 * {@link TaskFlowJob} is created. The 2 {@link TaskFlowJob} objects are then
 * compared using {@link JobComparator}.
 *
 * @author esalagea
 *
 */
public class TestXMLTransformer extends FunctionalTest {

    private static URL jobDescriptorsFolder = TestXMLTransformer.class
            .getResource("/functionaltests/descriptors/");

    private static File tmpFolder = new File(System.getProperty("java.io.tmpdir"));

    private static final String executablePathPropertyName = "EXEC_PATH";
    private static final String workingDirPropName = "WDIR";
    private static final String cmdPropName = "WCOM";

    /**
     * set system properties required by different job descriptors
     *
     * @throws URISyntaxException
     */
    private void setProperties() throws URISyntaxException {
        System.setProperty(executablePathPropertyName, new File(TestMultipleHostsRequest.class.getResource(
                "/functionaltests/executables/test_multiple_hosts_request.sh").toURI()).getAbsolutePath());

        System.setProperty(workingDirPropName, tmpFolder.getAbsolutePath());
        System.setProperty(cmdPropName, "echo");

    }

    @Override
    @Before
    public void prepareForTest() throws Exception {
        super.prepareForTest();
        setProperties();

    }

    @org.junit.Test
    public void run() throws Throwable {

        File folder = new File(jobDescriptorsFolder.toURI());
        Collection<File> testJobDescrFiles = FileUtils.listFiles(folder, new String[] { "xml" }, true);

        File samplesJobDescrFiles = new File(System.getProperty("pa.scheduler.home") + File.separator +
            "samples" + File.separator + "jobs_descriptors");

        System.out.println(samplesJobDescrFiles.getAbsolutePath());

        Collection<File> samples = FileUtils.listFiles(samplesJobDescrFiles, new String[] { "xml" }, true);
        samples.addAll(testJobDescrFiles);

        System.out.println("Treating " + samples.size() + " job descriptors.");

        for (File file : samples) {
            try {
                transformAndCompare(file);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("An exception occured while treating the file " + file.getAbsolutePath(),
                    e);
            }

        }
    }

    public static void main(String args[]) throws Throwable {
        TestXMLTransformer test = new TestXMLTransformer();
        test.prepareForTest();
        test.run();
    }

    /**
     * This opertations are performed:
     *
     * 1. xmlFile to java => job1
     *
     * 2. job1 = > xmlFle2
     *
     * 3. xmlFile2 to java => job2
     *
     * 4. Compare job1 and job2
     *
     */
    private void transformAndCompare(File xmlFile) throws Exception {
        System.out.println("TestXMLTransformer.transformAndCompare() " + xmlFile.getAbsolutePath());
        // xml to java => job1
        TaskFlowJob job1 = (TaskFlowJob) (JobFactory.getFactory().createJob(xmlFile.getAbsolutePath()));

        // job1 to xmlFile2
        File xmlFile2 = new File(tmpFolder, xmlFile.getName());
        Job2XMLTransformer transformer = new Job2XMLTransformer();
        transformer.job2xmlFile(job1, xmlFile2);

        // xmlFile2 to job2
        TaskFlowJob job2;
        try {
            job2 = (TaskFlowJob) (JobFactory.getFactory().createJob(xmlFile2.getAbsolutePath()));
        } catch (Exception e) {
            e.printStackTrace();
            String message = "Could not create Job object from generated xml. \n";
            message += "Generated xml content was : \n ****** " + xmlFile2.getAbsolutePath() +
                " ***********\n ";
            message += FileUtils.readFileToString(xmlFile2);
            message += "\n *************************** ";
            throw new Exception(message, e);
        }

        // compare job1 and job2
        JobComparator comparator = new JobComparator();
        if (!comparator.isEqualJob(job1, job2)) {
            String message = "Jobs are not equal for file " + xmlFile + "\n";
            message += "Reason: " + comparator.getDifferenceMessage() + "\n";
            message += "Generated xml content was : \n ****** " + xmlFile2.getAbsolutePath() +
                " *********** \n ";
            message += FileUtils.readFileToString(xmlFile2);
            message += "\n *************************** ";
            Assert.fail(message);
        }

        // delete temporary file
        FileUtils.forceDelete(xmlFile2);
    }

}
