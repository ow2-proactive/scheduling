/*
 * ################################################################
 *
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package functionaltests.workflow.variables;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.objectweb.proactive.utils.OperatingSystem;

import functionaltests.utils.SchedulerFunctionalTest;


public class Test_SCHEDULING_2034 extends SchedulerFunctionalTest {

    private static URL job_desc_unix = TestModifyPropagatedVariables.class
            .getResource("/functionaltests/descriptors/Job_SCHEDULING_2034_unix.xml");

    @Test
    public void testSCHEDULING_2034() throws Throwable {
        if (OperatingSystem.unix == OperatingSystem.getOperatingSystem()) {
            schedulerHelper.testJobSubmissionAndVerifyAllResults(absolutePath(job_desc_unix));
        }
    }

    private String absolutePath(URL file) throws Exception {
        return ((new File(file.toURI())).getAbsolutePath());
    }
}
