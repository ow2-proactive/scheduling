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
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTestWithCustomConfigAndRestart;
import functionaltests.utils.SchedulerTHelper;


/**
 * TestSubmitJobWithUnaccessibleDataSpaces
 *
 * This test submits a job with wrongly configured dataspaces (actually the wrong configuration is done in the scheduler directly).
 * It consists of a single task which will try to transfer back and forth files from those wrongly configured spaces
 * as the task does nothing the expected behaviour is that the task succeed and problems only appear in the logs
 *
 * @author The ProActive Team
 */
public class TestSubmitJobWithUnaccessibleDataSpaces extends SchedulerFunctionalTestWithCustomConfigAndRestart {

    static URL jobDescriptor = TestSubmitJobWithUnaccessibleDataSpaces.class.getResource("/functionaltests/dataspaces/Job_DataSpaceUnacc.xml");

    static URL jobDescriptor33 = TestSubmitJobWithUnaccessibleDataSpaces.class.getResource("/functionaltests/dataspaces/Job_DataSpaceUnacc3.3.xml");

    static URL configFile = TestSubmitJobWithUnaccessibleDataSpaces.class.getResource("/functionaltests/dataspaces/schedulerPropertiesWrongSpaces.ini");

    @BeforeClass
    public static void startDedicatedScheduler() throws Exception {
        schedulerHelper = new SchedulerTHelper(true, new File(configFile.toURI()).getAbsolutePath());
    }

    @Test
    public void testSubmitJobWithUnaccessibleDataSpaces() throws Throwable {

        schedulerHelper.testJobSubmission(new File(jobDescriptor.toURI()).getAbsolutePath());
    }

    @Test
    public void testSubmitJobWithUnaccessibleDataSpacesSchema33Compatability() throws Throwable {

        schedulerHelper.testJobSubmission(new File(jobDescriptor33.toURI()).getAbsolutePath());
    }

}
