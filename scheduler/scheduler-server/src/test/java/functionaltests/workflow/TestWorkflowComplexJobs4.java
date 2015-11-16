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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.workflow;

import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;


/**
 * Tests the correctness of workflow-controlled jobs including {@link FlowActionType#REPLICATE} and 
 * {@link FlowActionType#IF} actions
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowComplexJobs4 extends TRepJobs {
    @org.junit.Test
    public void action() throws Throwable {
        String prefix = "/functionaltests/workflow/descriptors/flow_complex_4_";

        TRepCase t1 = new TRepCase(prefix + "1.xml", 14, "T,1,0 T4,3,9 T5,1,10 T1,3,3 T3,3,-3 T2,3,6");
        TRepCase t2 = new TRepCase(prefix + "2.xml", 10,
            "T6,1,-1 T7,1,11 T,1,0 T4,1,10 T5,1,-1 T1,1,1 T3,3,9 T2,1,2");
        TRepCase t3 = new TRepCase(prefix + "3.xml", 26,
            "T6,2,4 T7,6,18 T,1,0 T4,2,28 T5,1,29 T8,6,24 T9,2,26 T1,2,2 T3,2,-2 " + "T2,2,-2");
        TRepCase t4 = new TRepCase(prefix + "4.xml", 95,
            "T1,2,-2 T3,2,6 T2,2,4 T6,6,210 T,2,2 T7,6,36 T4,6,24 T10,6,216 "
                + "T5,6,30 T11,2,-2 T12,2,-2 T13,2,220 T8,6,-6 T9,6,-6 T14,2,-2 "
                + "T15,2,222 T16,1,0 T17,1,223 T18,1,223 T21,2,218 T19,24,168 " + "T20,6,174");

        testJobs(t1, t2, t3, t4);
    }

}
