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
 * Tests the correctness of workflow-controlled jobs including {@link FlowActionType#REPLICATE},
 * {@link FlowActionType#IF} and {@link FlowActionType#LOOP} actions
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowComplexJobs5 extends TWorkflowJobs {

    @Override
    public String getJobPrefix() {
        return "/functionaltests/workflow/descriptors/flow_complex_5_";
    }

    @Override
    public String[][] getJobs() {
        return new String[][] {
                //
                { "T 0", "T1 1", "T2 2", "T3 -1", "T4 3", "T1#1 4", "T2#1 5", "T3#1 -1", "T4#1 6", "T1#2 7",
                        "T2#2 8", "T3#2 -1", "T4#2 9", "T1*1 1", "T2*1 2", "T3*1 -1", "T4*1 3", "T1#1*1 4",
                        "T2#1*1 5", "T3#1*1 -1", "T4#1*1 6", "T1#2*1 7", "T2#2*1 8", "T3#2*1 -1", "T4#2*1 9",
                        "T1*2 1", "T2*2 2", "T3*2 -1", "T4*2 3", "T1#1*2 4", "T2#1*2 5", "T3#1*2 -1",
                        "T4#1*2 6", "T1#2*2 7", "T2#2*2 8", "T3#2*2 -1", "T4#2*2 9", "T5 28" },

                { "T 0", "T1 1", "T2 -1", "T3 -1", "T4 2", "T5 3", "T4#1 4", "T5#1 5", "T4#2 6", "T5#2 7",
                        "T6 8", "T1*1 1", "T2*1 -1", "T3*1 -1", "T4*1 2", "T5*1 3", "T4#1*1 4", "T5#1*1 5",
                        "T4#2*1 6", "T5#2*1 7", "T6*1 8", "T1*2 1", "T2*2 -1", "T3*2 -1", "T4*2 2", "T5*2 3",
                        "T4#1*2 4", "T5#1*2 5", "T4#2*2 6", "T5#2*2 7", "T6*2 8", "T7 25", "T#1 26",
                        "T1#1 27", "T2#1 -1", "T3#1 -1", "T4#3 28", "T5#3 29", "T4#4 30", "T5#4 31",
                        "T4#5 32", "T5#5 33", "T6#1 34", "T1#1*1 27", "T2#1*1 -1", "T3#1*1 -1", "T4#3*1 28",
                        "T5#3*1 29", "T4#4*1 30", "T5#4*1 31", "T4#5*1 32", "T5#5*1 33", "T6#1*1 34",
                        "T1#1*2 27", "T2#1*2 -1", "T3#1*2 -1", "T4#3*2 28", "T5#3*2 29", "T4#4*2 30",
                        "T5#4*2 31", "T4#5*2 32", "T5#5*2 33", "T6#1*2 34", "T7#1 103", "T#2 104",
                        "T1#2 105", "T2#2 -1", "T3#2 -1", "T4#6 106", "T5#6 107", "T4#7 108", "T5#7 109",
                        "T4#8 110", "T5#8 111", "T6#2 112", "T1#2*1 105", "T2#2*1 -1", "T3#2*1 -1",
                        "T4#6*1 106", "T5#6*1 107", "T4#7*1 108", "T5#7*1 109", "T4#8*1 110", "T5#8*1 111",
                        "T6#2*1 112", "T1#2*2 105", "T2#2*2 -1", "T3#2*2 -1", "T4#6*2 106", "T5#6*2 107",
                        "T4#7*2 108", "T5#7*2 109", "T4#8*2 110", "T5#8*2 111", "T6#2*2 112", "T7#2 337" },

        //            
        };
    }

    @org.junit.Test
    public void run() throws Throwable {
        internalRun();
    }
}
