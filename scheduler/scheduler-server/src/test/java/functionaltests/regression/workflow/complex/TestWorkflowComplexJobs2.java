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
package functionaltests.regression.workflow.complex;

import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;
import org.junit.Test;

import functionaltests.workflow.TRepJobs;


/**
 * Tests the correctness of workflow-controlled jobs including {@link FlowActionType#LOOP} and 
 * {@link FlowActionType#REPLICATE} actions
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowComplexJobs2 extends TRepJobs {
    @Test
    public void run() throws Throwable {
        String prefix = "/functionaltests/workflow/descriptors/flow_complex_2_";

        TRepCase t1 = new TRepCase(prefix + "1.xml", 87,
            "T,3,2236 T4,3,53919 T1,27,47127 T3,27,94308 T2,27,47154");
        TRepCase t2 = new TRepCase(prefix + "2.xml", 201,
            "T6,2,1269 T7,6,54877 T,1,0 T4,1,280843 T5,1,280843 T29,2,6 T28,1,7 "
                + "T8,18,164649 T27,2,6 T26,30,2530 T9,18,164667 T25,6,1988 T24,6,488 "
                + "T23,6,2970 T22,6,976 T21,6,482 T20,1,2540 T1,1,1 T3,1,280843 "
                + "T2,1,280842 T10,18,164667 T11,18,164649 T12,6,329340 T13,2,279567 "
                + "T14,2,1271 T15,2,2554 T16,2,1271 T17,10,6385 T18,2,1273 T19,1,2 "
                + "T30,10,60 T32,2,34 T31,10,60");

        testJobs(t1, t2);
    }

}
