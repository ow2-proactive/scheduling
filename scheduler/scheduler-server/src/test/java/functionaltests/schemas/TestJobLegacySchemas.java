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
package functionaltests.schemas;

import functionaltests.SchedulerConsecutive;
import functionaltests.SchedulerTHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;


/**
 * This class tests a job submission to the scheduler, but with job descriptors of older scheduling releases.
 * <p>
 * The jobs are created to cover the most features as possible, somehow to check backward compatibility of features from
 * older schema versions.
 * <p>
 * If this test breaks, there are two possible scenarios:
 * - enforce backward compatibility by supporting both the old and the new behavior
 * - declares the version of the schema that should be supported
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.4.0
 */
public class TestJobLegacySchemas extends SchedulerConsecutive {

    private static String[] jobDescriptorsLoc = { "3_0/Job_Schemas.xml", "3_1/Job_Schemas.xml",
            "3_2/Job_Schemas.xml" };

    private URL[] jobDescriptors = new URL[jobDescriptorsLoc.length];

    {
        for (int i = 0; i < jobDescriptorsLoc.length; i++) {
            jobDescriptors[i] = TestJobLegacySchemas.class.getResource("/functionaltests/schemas/" +
                    jobDescriptorsLoc[i]);
        }
    }

    @Test
    public void run() throws Throwable {
        for (URL jobDescriptor : jobDescriptors) {
            logger.info("Testing submission of job descriptor : " + jobDescriptor);
            prepareDataspaceFolder();
            String jobDescPath = new File(jobDescriptor.toURI()).getAbsolutePath();
            Job testJob = JobFactory.getFactory().createJob(jobDescPath);
            // This line prints the debug information of the job, checking that no toString method produces a NPE
            SchedulerTHelper.log(testJob.display());
            SchedulerTHelper.testJobSubmissionAndVerifyAllResults(jobDescPath);
        }
    }

    private void prepareDataspaceFolder() throws IOException {
        File ds = new File(PAResourceManagerProperties.RM_HOME.getValueAsString(),
                "/scheduler/scheduler-server/build/JobLegacySchemas_dataspace");

        if (ds.exists()) {
            File[] filesToDelete = ds.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("myfileout") || name.endsWith(".log");
                }
            });
            for (File f : filesToDelete) {
                FileUtils.forceDelete(f);
            }
        } else {
            FileUtils.forceMkdir(ds);
        }
    }

}