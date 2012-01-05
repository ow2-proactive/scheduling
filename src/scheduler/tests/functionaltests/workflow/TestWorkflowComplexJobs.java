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
 * {@link FlowActionType#REPLICATE} actions
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowComplexJobs extends TWorkflowJobs {

    @Override
    public String getJobPrefix() {
        return "/functionaltests/workflow/descriptors/flow_complex_";
    }

    @Override
    public String[][] getJobs() {
        return new String[][] {

        // 1: loop block -> replicate block
                { "T 0 ()", "T1 1 (T)", "T3 2 (T1)", "T4 2 (T1)", "T5 5 (T3 T4)", "T1*1 1 (T)",
                        "T3*1 2 (T1*1)", "T4*1 2 (T1*1)", "T5*1 5 (T3*1 T4*1)", "T2 11 (T5 T5*1)",
                        "T#1 12 (T2)", "T1#1 13 (T#1)", "T3#1 14 (T1#1)", "T5#1 29 (T3#1 T4#1)",
                        "T4#1 14 (T1#1)", "T1#1*1 13 (T#1)", "T3#1*1 14 (T1#1*1)",
                        "T5#1*1 29 (T3#1*1 T4#1*1)", "T4#1*1 14 (T1#1*1)", "T2#1 59 (T5#1 T5#1*1)" },

                // 2: loop block -> replicate single task
                { "T 0 ()", "T1 1 (T)", "T1*1 1 (T)", "T1*2 1 (T)", "T2 4 (T1 T1*1 T1*2)", "T#1 5 (T2)",
                        "T1#1 6 (T#1)", "T1#1*1 6 (T#1)", "T1#1*2 6 (T#1)", "T2#1 19 (T1#1 T1#1*1 T1#1*2)",
                        "T#2 20 (T2#1)", "T1#2 21 (T#2)", "T1#2*1 21 (T#2)", "T1#2*2 21 (T#2)",
                        "T2#2 64 (T1#2 T1#2*1 T1#2*2)" },

                // 3: replicate single task -> loop single task
                { "T 0 ()", "T1 1 (T)", "T1*1 1 (T)", "T1*2 1 (T)", "T1#1 2 (T1)", "T1#1*1 2 (T1*1)",
                        "T1#1*2 2 (T1*2)", "T1#2 3 (T1#1)", "T1#2*1 3 (T1#1*1)", "T1#2*2 3 (T1#1*2)",
                        "T2 10 (T1#2 T1#2*1 T1#2*2)" },

                // 4: loop block -> replicate task -> loop task
                { "T 0 ()", "T1 1 (T)", "T1*1 1 (T)", "T1*2 1 (T)", "T1#1 2 (T1)", "T1#1*1 2 (T1*1)",
                        "T1#1*2 2 (T1*2)", "T1#2 3 (T1#1)", "T1#2*1 3 (T1#1*1)", "T1#2*2 3 (T1#1*2)",
                        "T2 10 (T1#2 T1#2*1 T1#2*2)", "T#1 11 (T2)", "T1#3 12 (T#1)", "T1#3*1 12 (T#1)",
                        "T1#3*2 12 (T#1)", "T1#4 13 (T1#3)", "T1#4*1 13 (T1#3*1)", "T1#4*2 13 (T1#3*2)",
                        "T1#5 14 (T1#4)", "T1#5*1 14 (T1#4*1)", "T1#5*2 14 (T1#4*2)",
                        "T2#1 43 (T1#5 T1#5*1 T1#5*2)", "T#2 44 (T2#1)", "T1#6 45 (T#2)", "T1#6*1 45 (T#2)",
                        "T1#6*2 45 (T#2)", "T1#7 46 (T1#6)", "T1#7*1 46 (T1#6*1)", "T1#7*2 46 (T1#6*2)",
                        "T1#8 47 (T1#7)", "T1#8*1 47 (T1#7*1)", "T1#8*2 47 (T1#7*2)",
                        "T2#2 142 (T1#8 T1#8*1 T1#8*2)" },

                // 5: multiple nested loop & dup
                { "T 0 ()", "T1 1 (T)", "T5 2 (T1)", "T6 3 (T5)", "T8 4 (T6)", "T6*1 3 (T5)",
                        "T8*1 4 (T6*1)", "T9 9 (T8 T8*1)", "T5#1 10 (T9)", "T6#1 11 (T5#1)",
                        "T8#1 12 (T6#1)", "T6#1*1 11 (T5#1)", "T8#1*1 12 (T6#1*1)", "T9#1 25 (T8#1 T8#1*1)",
                        "T15 26 (T9#1)", "T1#1 27 (T15)", "T5#2 28 (T1#1)", "T6#2 29 (T5#2)",
                        "T8#2 30 (T6#2)", "T6#2*1 29 (T5#2)", "T8#2*1 30 (T6#2*1)", "T9#2 61 (T8#2 T8#2*1)",
                        "T5#3 62 (T9#2)", "T6#3 63 (T5#3)", "T8#3 64 (T6#3)", "T6#3*1 63 (T5#3)",
                        "T8#3*1 64 (T6#3*1)", "T9#3 129 (T8#3 T8#3*1)", "T15#1 130 (T9#3)", "T1*1 1 (T)",
                        "T5*1 2 (T1*1)", "T6*2 3 (T5*1)", "T8*2 4 (T6*2)", "T6*3 3 (T5*1)", "T8*3 4 (T6*3)",
                        "T9*1 9 (T8*2 T8*3)", "T5#1*1 10 (T9*1)", "T6#1*2 11 (T5#1*1)", "T8#1*2 12 (T6#1*2)",
                        "T6#1*3 11 (T5#1*1)", "T8#1*3 12 (T6#1*3)", "T9#1*1 25 (T8#1*2 T8#1*3)",
                        "T15*1 26 (T9#1*1)", "T1#1*1 27 (T15*1)", "T5#2*1 28 (T1#1*1)", "T6#2*2 29 (T5#2*1)",
                        "T8#2*2 30 (T6#2*2)", "T6#2*3 29 (T5#2*1)", "T8#2*3 30 (T6#2*3)",
                        "T9#2*1 61 (T8#2*2 T8#2*3)", "T5#3*1 62 (T9#2*1)", "T6#3*2 63 (T5#3*1)",
                        "T8#3*2 64 (T6#3*2)", "T6#3*3 63 (T5#3*1)", "T8#3*3 64 (T6#3*3)",
                        "T9#3*1 129 (T8#3*2 T8#3*3)", "T15#1*1 130 (T9#3*1)", "T2 261 (T15#1 T15#1*1)" },
        //                

        };
    }

    @org.junit.Test
    public void run() throws Throwable {
        internalRun();
    }
}
