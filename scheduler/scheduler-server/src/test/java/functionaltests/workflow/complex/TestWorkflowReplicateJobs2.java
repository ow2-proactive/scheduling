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
package functionaltests.workflow.complex;

import org.junit.Test;

import functionaltests.workflow.TRepJobs;


/**
 * Tests the correctness of workflow-controlled jobs
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowReplicateJobs2 extends TRepJobs {
    @Test
    public void testWorkflowReplicateJobs2() throws Throwable {
        String prefix = "/functionaltests/workflow/descriptors/flow_duplicate_2_";

        TRepCase t1 = new TRepCase(prefix + "1.xml", 38,
            "T6,2,54 T7,1,55 T,1,0 T4,12,48 T5,4,52 T1,2,2 T3,12,36 T2,4,8");
        TRepCase t2 = new TRepCase(prefix + "2.xml", 30,
            "T6,2,46 T7,1,47 T,1,0 T4,4,40 T5,4,44 T1,2,2 T3,12,36 T2,4,8");
        TRepCase t3 = new TRepCase(prefix + "3.xml", 85,
            "T1,1,1 T3,2,4 T2,2,4 T6,2,6 T,1,0 T7,4,16 T4,4,12 T10,12,60 T5,2,6 T11,4,136 T8,4,16 "
                + "T13,4,64 T14,1,211 T9,12,60 T15,1,212 T16,1,212 T17,2,144 T18,12,72 T19,2,66 T20,12,60");

        testJobs(t1, t2, t3);
    }
}
