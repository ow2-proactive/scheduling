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
                { "T 0 ()", "T1 1 (T)", "T1#1 2 (T1)", "T1#2 3 (T1#1)", "T1#3 4 (T1#2)", "T2 5 (T1#3)" },

                // 2: loop on simple block
                { "T 0 ()", "T1 1 (T)", "T2 2 (T1)", "T1#1 3 (T2)", "T2#1 4 (T1#1)", "T1#2 5 (T2#1)", "T2#2 6 (T1#2)", "T1#3 7 (T2#2)", "T2#3 8 (T1#3)", "T3 9 (T2#3)" },

                // 3: loop on a complex block
                { "T 0 ()", "T1 1 (T)", "T2 2 (T1)", "T6 3 (T2)", "T5 3 (T2)", "T3 2 (T1)", "T4 3 (T3)", "T7 10 (T4 T5 T6)", "T1#1 11 (T7)", "T2#1 12 (T1#1)",
                        "T6#1 13 (T2#1)", "T5#1 13 (T2#1)", "T3#1 12 (T1#1)", "T4#1 13 (T3#1)", "T7#1 40 (T4#1 T5#1 T6#1)", "T1#2 41 (T7#1)", "T2#2 42 (T1#2)",
                        "T6#2 43 (T2#2)", "T5#2 43 (T2#2)", "T3#2 42 (T1#2)", "T4#2 43 (T3#2)", "T7#2 130 (T4#2 T5#2 T6#2)", "T8 131 (T7#2)", "T9 131 (T7#2)", },

                // 4: nested loops: block -> self
                { "T 0 ()", "T1 1 (T)", "T1#1 2 (T1)", "T1#2 3 (T1#1)", "T2 4 (T1#2)", "T#1 5 (T2)", "T1#3 6 (T#1)", "T1#4 7 (T1#3)", "T1#5 8 (T1#4)", "T2#1 9 (T1#5)",
                        "T#2 10 (T2#1)", "T1#6 11 (T#2)", "T1#7 12 (T1#6)", "T1#8 13 (T1#7)", "T2#2 14 (T1#8)" },

                // 5: nested loops: block -> block
                { "T 0 ()", "T1 1 (T)", "T5 2 (T1)", "T2 2 (T1)", "T3 5 (T2 T5)", "T1#1 6 (T3)", "T5#1 7 (T1#1)", "T2#1 7 (T1#1)", "T3#1 15 (T2#1 T5#1)", "T1#2 16 (T3#1)",
                        "T5#2 17 (T1#2)", "T2#2 17 (T1#2)", "T3#2 35 (T2#2 T5#2)", "T4 36 (T3#2)", "T#1 37 (T4)", "T1#3 38 (T#1)", "T5#3 39 (T1#3)", "T2#3 39 (T1#3)",
                        "T3#3 79 (T2#3 T5#3)", "T1#4 80 (T3#3)", "T5#4 81 (T1#4)", "T2#4 81 (T1#4)", "T3#4 163 (T2#4 T5#4)", "T1#5 164 (T3#4)", "T5#5 165 (T1#5)",
                        "T2#5 165 (T1#5)", "T3#5 331 (T2#5 T5#5)", "T4#1 332 (T3#5)", "T#2 333 (T4#1)", "T1#6 334 (T#2)", "T5#6 335 (T1#6)", "T2#6 335 (T1#6)",
                        "T3#6 671 (T2#6 T5#6)", "T1#7 672 (T3#6)", "T5#7 673 (T1#7)", "T2#7 673 (T1#7)", "T3#7 1347 (T2#7 T5#7)", "T1#8 1348 (T3#7)",
                        "T5#8 1349 (T1#8)", "T2#8 1349 (T1#8)", "T3#8 2699 (T2#8 T5#8)", "T4#2 2700 (T3#8)" },

                // 6: nested loops: block -> block -> self
                { "T 0 ()", "T1 1 (T)", "T2 2 (T1)", "T2#1 3 (T2)", "T2#2 4 (T2#1)", "T3 5 (T2#2)", "T1#1 6 (T3)", "T2#3 7 (T1#1)", "T2#4 8 (T2#3)", "T2#5 9 (T2#4)",
                        "T3#1 10 (T2#5)", "T1#2 11 (T3#1)", "T2#6 12 (T1#2)", "T2#7 13 (T2#6)", "T2#8 14 (T2#7)", "T3#2 15 (T2#8)", "T4 16 (T3#2)", "T#1 17 (T4)",
                        "T1#3 18 (T#1)", "T2#9 19 (T1#3)", "T2#10 20 (T2#9)", "T2#11 21 (T2#10)", "T3#3 22 (T2#11)", "T1#4 23 (T3#3)", "T2#12 24 (T1#4)",
                        "T2#13 25 (T2#12)", "T2#14 26 (T2#13)", "T3#4 27 (T2#14)", "T1#5 28 (T3#4)", "T2#15 29 (T1#5)", "T2#16 30 (T2#15)", "T2#17 31 (T2#16)",
                        "T3#5 32 (T2#17)", "T4#1 33 (T3#5)" },

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
