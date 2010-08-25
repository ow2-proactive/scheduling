/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 *              Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.workflow;

import org.ow2.proactive.scheduler.flow.FlowActionType;


/**
 * Tests the correctness of workflow-controlled jobs including {@link FlowActionType#LOOP} actions
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowLoopJobs extends TWorkflowJobs {

    @Override
    public final String[][] getJobs() {
        return new String[][] {

        // 1: loop on self
                { "T 0", "T1 1", "T1#1 2", "T1#2 3", "T1#3 4", "T2 5" },

                // 2: loop on simple block
                { "T 0", "T1 1", "T2 2", "T1#1 3", "T2#1 4", "T1#2 5", "T2#2 6", "T1#3 7", "T2#3 8", "T3 9" },

                // 3: loop on a complex block
                { "T 0", "T1 1", "T2 2", "T6 3", "T5 3", "T3 2", "T4 3", "T7 10", "T1#1 11", "T2#1 12",
                        "T6#1 13", "T5#1 13", "T3#1 12", "T4#1 13", "T7#1 40", "T1#2 41", "T2#2 42",
                        "T6#2 43", "T5#2 43", "T3#2 42", "T4#2 43", "T7#2 130", "T8 131", "T9 131", },

                // 4: nested loops: block -> self
                { "T 0", "T1 1", "T1#1 2", "T1#2 3", "T2 4", "T#1 5", "T1#3 6", "T1#4 7", "T1#5 8", "T2#1 9",
                        "T#2 10", "T1#6 11", "T1#7 12", "T1#8 13", "T2#2 14" },

                // 5: nested loops: block -> block
                { "T 0", "T1 1", "T5 2", "T2 2", "T3 5", "T1#1 6", "T5#1 7", "T2#1 7", "T3#1 15", "T1#2 16",
                        "T5#2 17", "T2#2 17", "T3#2 35", "T4 36", "T#1 37", "T1#3 38", "T5#3 39", "T2#3 39",
                        "T3#3 79", "T1#4 80", "T5#4 81", "T2#4 81", "T3#4 163", "T1#5 164", "T5#5 165",
                        "T2#5 165", "T3#5 331", "T4#1 332", "T#2 333", "T1#6 334", "T5#6 335", "T2#6 335",
                        "T3#6 671", "T1#7 672", "T5#7 673", "T2#7 673", "T3#7 1347", "T1#8 1348",
                        "T5#8 1349", "T2#8 1349", "T3#8 2699", "T4#2 2700" },

                // 6: nested loops: block -> block -> self
                { "T 0", "T1 1", "T2 2", "T2#1 3", "T2#2 4", "T3 5", "T1#1 6", "T2#3 7", "T2#4 8", "T2#5 9",
                        "T3#1 10", "T1#2 11", "T2#6 12", "T2#7 13", "T2#8 14", "T3#2 15", "T4 16", "T#1 17",
                        "T1#3 18", "T2#9 19", "T2#10 20", "T2#11 21", "T3#3 22", "T1#4 23", "T2#12 24",
                        "T2#13 25", "T2#14 26", "T3#4 27", "T1#5 28", "T2#15 29", "T2#16 30", "T2#17 31",
                        "T3#5 32", "T4#1 33" },

        //                

        };
    }

    @Override
    public final String getJobPrefix() {
        return "/functionaltests/workflow/descriptors/flow_loop_";
    }

    @org.junit.Test
    public void run() throws Throwable {
        internalRun();
    }
}
