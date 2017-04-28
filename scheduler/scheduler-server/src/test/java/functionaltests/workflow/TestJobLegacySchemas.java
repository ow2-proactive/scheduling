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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;

import functionaltests.utils.SchedulerFunctionalTestWithRestart;


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
public class TestJobLegacySchemas extends SchedulerFunctionalTestWithRestart {

    private static String[] jobDescriptorsLoc = { "3_0/Job_Schemas.xml", "3_1/Job_Schemas.xml", "3_2/Job_Schemas.xml",
                                                  "3_3/Job_Schemas.xml", "3_4/Job_Schemas.xml", "3_5/Job_Schemas.xml" };

    private URL[] jobDescriptors = new URL[jobDescriptorsLoc.length];

    {
        for (int i = 0; i < jobDescriptorsLoc.length; i++) {
            jobDescriptors[i] = TestJobLegacySchemas.class.getResource("/functionaltests/schemas/" +
                                                                       jobDescriptorsLoc[i]);
        }
    }

    @Test
    public void testJobLegacySchemas() throws Throwable {
        for (URL jobDescriptor : jobDescriptors) {
            log("Testing submission of job descriptor : " + jobDescriptor);
            prepareDataspaceFolder();
            String jobDescPath = new File(jobDescriptor.toURI()).getAbsolutePath();
            Job testJob = JobFactory.getFactory().createJob(jobDescPath);
            // This line prints the debug information of the job, checking that no toString method produces a NPE
            log(testJob.display());
            schedulerHelper.testJobSubmission(jobDescPath);
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
