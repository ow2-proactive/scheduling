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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package functionaltests.dataspaces;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import functionaltests.utils.SchedulerFunctionalTest;


/**
 * TestSubmitJobWithUnaccessibleDataSpaces
 *
 * This test submits a job with wrongly configured dataspaces (actually the wrong configuration is done in the scheduler directly).
 * It consists of a single task which will try to transfer back and forth files from those wrongly configured spaces
 * as the task does nothing the expected behaviour is that the task succeed and problems only appear in the logs
 *
 * @author The ProActive Team
 */
public class TestSubmitJobWithUnaccessibleDataSpaces extends SchedulerFunctionalTest {

    static URL jobDescriptor = TestSubmitJobWithUnaccessibleDataSpaces.class
            .getResource("/functionaltests/dataspaces/Job_DataSpaceUnacc.xml");

    static URL configFile = TestSubmitJobWithUnaccessibleDataSpaces.class
            .getResource("/functionaltests/dataspaces/schedulerPropertiesWrongSpaces.ini");

    @Test
    public void testSubmitJobWithUnaccessibleDataSpaces() throws Throwable {
        schedulerHelper.startScheduler((new File(configFile.toURI())).getAbsolutePath());
        schedulerHelper.testJobSubmissionAndVerifyAllResults(new File(jobDescriptor.toURI())
                .getAbsolutePath());
    }

}
