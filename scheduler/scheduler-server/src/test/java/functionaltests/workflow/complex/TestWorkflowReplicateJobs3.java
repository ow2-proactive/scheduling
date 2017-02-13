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
package functionaltests.workflow.complex;

import org.junit.Test;

import functionaltests.workflow.TRepJobs;


/**
 * Tests the correctness of workflow-controlled jobs
 *
 * 
 * @author mschnoor
 *
 */
public class TestWorkflowReplicateJobs3 extends TRepJobs {
    @Test
    public void testWorkflowReplicateJobs3() throws Throwable {
        String prefix = "/functionaltests/workflow/descriptors/flow_duplicate_3_";

        TRepCase t1 = new TRepCase(prefix + "1.xml", 9, "A,1,0 B,2,2 C,3,6 D,2,8 E,1,9");
        TRepCase t2 = new TRepCase(prefix + "2.xml", 20, "A,1,0 B,3,3 C,6,12 D,6,18 E,3,21 F,1,22");

        testJobs(t1, t2);
    }
}
