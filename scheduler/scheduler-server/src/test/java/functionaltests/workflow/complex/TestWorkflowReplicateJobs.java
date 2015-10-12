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
public class TestWorkflowReplicateJobs extends TRepJobs {
    @Test
    public void run() throws Throwable {
        String prefix = "/functionaltests/workflow/descriptors/flow_duplicate_";

        TRepCase t1 = new TRepCase(prefix + "1.xml", 4, "T,1,0 T1,2,2 T2,1,3");
        TRepCase t2 = new TRepCase(prefix + "2.xml", 6, "T,1,0 T1,2,2 T3,1,5 T2,2,4");
        TRepCase t3 = new TRepCase(prefix + "3.xml", 14,
            "T6,2,18 T7,1,19 T,1,0 T4,2,6 T5,2,6 T1,2,2 T3,2,4 T2,2,4");
        TRepCase t4 = new TRepCase(prefix + "4.xml", 10, "T,1,0 T4,1,11 T1,2,2 T3,2,10 T2,4,8");
        TRepCase t5 = new TRepCase(prefix + "5.xml", 14, "T,1,0 T4,2,14 T5,1,15 T1,2,2 T3,4,12 T2,4,8");
        TRepCase t6 = new TRepCase(prefix + "6.xml", 11, "T7,1,10 T,1,0 T4,1,3 T5,2,6 T1,2,2 T3,2,4 T2,2,2");
        TRepCase t7 = new TRepCase(prefix + "7.xml", 30,
            "T6,2,38 T7,1,45 T,1,0 T4,4,16 T10,4,12 T5,2,6 T11,4,36 T8,2,4 T9,4,16 T1,2,2 T3,2,4 " + "T2,2,2");

        testJobs(t1, t2, t3, t4, t5, t6, t7);

    }

}
