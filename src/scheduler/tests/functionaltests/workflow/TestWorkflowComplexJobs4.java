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
public class TestWorkflowComplexJobs4 extends TWorkflowJobs {

    @Override
    public String getJobPrefix() {
        return "/functionaltests/workflow/descriptors/flow_complex_4_";
    }

    @Override
    public String[][] getJobs() {
        return new String[][] {
                { "T 0", "T1 1", "T2 2", "T3 -1", "T4 3", "T1*1 1", "T2*1 2", "T3*1 -1", "T4*1 3", "T1*2 1",
                        "T2*2 2", "T3*2 -1", "T4*2 3", "T5 10" },

                { "T 0", "T1 1", "T5 -1", "T6 -1", "T2 2", "T3 3", "T3*1 3", "T3*2 3", "T4 10", "T7 11" },

                { "T 0", "T1 1", "T2 -1", "T3 -1", "T6 2", "T7 3", "T8 4", "T7*1 3", "T8*1 4", "T7*2 3",
                        "T8*2 4", "T9 13", "T4 14", "T1*1 1", "T2*1 -1", "T3*1 -1", "T6*1 2", "T7*3 3",
                        "T8*3 4", "T7*4 3", "T8*4 4", "T7*5 3", "T8*5 4", "T9*1 13", "T4*1 14", "T5 29" },

                { "T16 0", "T 1", "T1 -1", "T14 -1", "T2 2", "T11 -1", "T12 -1", "T3 3", "T4 4", "T5 5",
                        "T7 6", "T19 7", "T19*1 7", "T19*2 7", "T19*3 7", "T20 29", "T6 35", "T10 36",
                        "T8 -1", "T9 -1", "T4*1 4", "T5*1 5", "T7*1 6", "T19*4 7", "T19*5 7", "T19*6 7",
                        "T19*7 7", "T20*1 29", "T6*1 35", "T10*1 36", "T8*1 -1", "T9*1 -1", "T4*2 4",
                        "T5*2 5", "T7*2 6", "T19*8 7", "T19*9 7", "T19*10 7", "T19*11 7", "T20*2 29",
                        "T6*2 35", "T10*2 36", "T8*2 -1", "T9*2 -1", "T21 109", "T13 110", "T15 111",

                        "T*1 1", "T1*1 -1", "T14*1 -1", "T2*1 2", "T11*1 -1", "T12*1 -1", "T3*1 3", "T4*3 4",
                        "T5*3 5", "T7*3 6", "T19*12 7", "T19*13 7", "T19*14 7", "T19*15 7", "T20*3 29",
                        "T6*3 35", "T10*3 36", "T8*3 -1", "T9*3 -1", "T4*4 4", "T5*4 5", "T7*4 6",
                        "T19*16 7", "T19*17 7", "T19*18 7", "T19*19 7", "T20*4 29", "T6*4 35", "T10*4 36",
                        "T8*4 -1", "T9*4 -1", "T4*5 4", "T5*5 5", "T7*5 6", "T19*20 7", "T19*21 7",
                        "T19*22 7", "T19*23 7", "T20*5 29", "T6*5 35", "T10*5 36", "T8*5 -1", "T9*5 -1",
                        "T21*1 109", "T13*1 110", "T15*1 111",

                        "T18 223", "T17 223" }
        //            
        };
    }

    @org.junit.Test
    public void run() throws Throwable {
        internalRun();
    }
}
