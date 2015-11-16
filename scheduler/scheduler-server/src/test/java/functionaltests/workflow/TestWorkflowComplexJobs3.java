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
 * Tests the correctness of workflow-controlled jobs including {@link FlowActionType#LOOP} and 
 * {@link FlowActionType#IF} actions
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowComplexJobs3 extends TWorkflowJobs {

    @Override
    public String getJobPrefix() {
        return "/functionaltests/workflow/descriptors/flow_complex_3_";
    }

    @Override
    public String[][] getJobs() {
        return new String[][] {
        // loop block : if/else/continuation single task
                { "T 0 ()", "T1 1 (T)", "T2 2 (T1)", "T4 3 (T2)", "T3 -1 (T1)", "T5 4 (T4)", "T#1 5 (T5)",
                        "T1#1 6 (T#1)", "T2#1 7 (T1#1)", "T3#1 -1 (T1#1)", "T4#1 8 (T2#1)", "T5#1 9 (T4#1)" },

                // loop block : if/else/continuation single task; different configuration
                { "T 0 ()", "T1 1 (T)", "T3 2 (T1)", "T2 -1 (T1)", "T4 3 (T3)", "T1#1 4 (T4)",
                        "T3#1 5 (T1#1)", "T2#1 -1 (T1#1)", "T4#1 6 (T3#1)", "T5 7 (T4#1)" },

                // loop block : if: loop block /else/continuation
                { "T 0 ()", "T1 1 (T)", "T3 -1 (T1)", "T5 -1 (T3)", "T2 2 (T1)", "T4 3 (T2)", "T2#1 4 (T4)",
                        "T4#1 5 (T2#1)", "T2#2 6 (T4#1)", "T4#2 7 (T2#2)", "T6 8 (T4#2)", "T7 9 (T6)",
                        "T#1 10 (T7)", "T1#1 11 (T#1)", "T3#1 -1 (T1#1)", "T5#1 -1 (T3#1)", "T2#3 12 (T1#1)",
                        "T4#3 13 (T2#3)", "T2#4 14 (T4#3)", "T4#4 15 (T2#4)", "T2#5 16 (T4#4)",
                        "T4#5 17 (T2#5)", "T6#1 18 (T4#5)", "T7#1 19 (T6#1)" },

                // multiple IF / LOOP
                { "T 0 ()", "T2 -1 (T)", "T20 -1 (T2)", "T1 1 (T)", "T5 -1 (T1)", "T19 -1 (T5)",
                        "T11 2 (T1)", "T3 3 (T11)", "T14 -1 (T3)", "T4 -1 (T14)", "T6 4 (T3)", "T7 5 (T6)",
                        "T12 -10 (T7)", "T13 -1 (T12)", "T8 6 (T7)", "T10 7 (T8)", "T9 14 (T8 T10)",
                        "T8#1 15 (T9)", "T10#1 16 (T8#1)", "T9#1 32 (T8#1 T10#1)", "T8#2 33 (T9#1)",
                        "T10#2 34 (T8#2)", "T9#2 68 (T8#2 T10#2)", "T15 69 (T9#2)", "T16 70 (T15)",
                        "T17 71 (T16)", "T18 72 (T17)", "T1#1 73 (T18)", "T5#1 -1 (T1#1)", "T19#1 -1 (T5#1)",
                        "T11#1 74 (T1#1)", "T3#1 75 (T11#1)", "T14#1 -1 (T3#1)", "T4#1 -1 (T14#1)",
                        "T6#1 76 (T3#1)", "T7#1 77 (T6#1)", "T12#1 -1 (T7#1)", "T13#1 -1 (T12#1)",
                        "T8#3 78 (T7#1)", "T10#3 79 (T8#3)", "T9#3 158 (T8#3 T10#3)", "T8#4 159 (T9#3)",
                        "T10#4 160 (T8#4)", "T9#4 320 (T8#4 T10#4)", "T8#5 321 (T9#4)", "T10#5 322 (T8#5)",
                        "T9#5 644 (T8#5 T10#5)", "T15#1 645 (T9#5)", "T16#1 646 (T15#1)",
                        "T17#1 647 (T16#1)", "T18#1 648 (T17#1)", "T21 649 (T18#1)", "T#1 650 (T21)",
                        "T2#1 -1 (T#1)", "T20#1 -1 (T2#1)", "T1#2 651 (T#1)", "T5#2 -1 (T1#2)",
                        "T19#2 -1 (T5#2)", "T11#2 652 (T1#2)", "T3#2 653 (T11#2)", "T14#2 -1 (T3#2)",
                        "T4#2 -1 (T14#2)", "T6#2 654 (T3#2)", "T7#2 655 (T6#2)", "T12#2 -1 (T7#2)",
                        "T13#2 -1 (T12#2)", "T8#6 656 (T7#2)", "T10#6 657 (T8#6)", "T9#6 1314 (T8#6 T10#6)",
                        "T8#7 1315 (T9#6)", "T10#7 1316 (T8#7)", "T9#7 2632 (T8#7 T10#7)",
                        "T8#8 2633 (T9#7)", "T10#8 2634 (T8#8)", "T9#8 5268 (T8#8 T10#8)",
                        "T15#2 5269 (T9#8)", "T16#2 5270 (T15#2)", "T17#2 5271 (T16#2)",
                        "T18#2 5272 (T17#2)", "T1#3 5273 (T18#2)", "T5#3 -1 (T1#3)", "T19#3 -1 (T5#3)",
                        "T11#3 5274 (T1#3)", "T3#3 5275 (T11#3)", "T14#3 -1 (T3#3)", "T4#3 -1 (T14#3)",
                        "T6#3 5276 (T3#3)", "T7#3 5277 (T6#3)", "T12#3 -1 (T7#3)", "T13#3 -1 (T12#3)",
                        "T8#9 5278 (T7#3)", "T10#9 5279 (T8#9)", "T9#9 10558 (T8#9 T10#9)",
                        "T8#10 10559 (T9#9)", "T10#10 10560 (T8#10)", "T9#10 21120 (T8#10 T10#10)",
                        "T8#11 21121 (T9#10)", "T10#11 21122 (T8#11)", "T9#11 42244 (T8#11 T10#11)",
                        "T15#3 42245 (T9#11)", "T16#3 42246 (T15#3)", "T17#3 42247 (T16#3)",
                        "T18#3 42248 (T17#3)", "T21#1 42249 (T18#3)", "T22 42250 (T21#1)",
                        "T23 42250 (T21#1)"

                }
        //        
        };
    }

    @org.junit.Test
    public void action() throws Throwable {
        internalRun();
    }
}
