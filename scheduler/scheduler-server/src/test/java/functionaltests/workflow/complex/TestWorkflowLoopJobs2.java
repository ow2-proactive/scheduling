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
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;

import functionaltests.workflow.TWorkflowJobs;


/**
 * Tests the correctness of workflow-controlled jobs including {@link FlowActionType#LOOP} actions
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowLoopJobs2 extends TWorkflowJobs {

    @Override
    public final String[][] getJobs() {
        return new String[][] {

        // 1: complex multiple nested loops
        { "T1 0 ()", "T2 1 (T1)", "T3 2 (T2)", "T11 3 (T3)", "T12 4 (T11)", "T11#1 5 (T12)",
                "T12#1 6 (T11#1)", "T11#2 7 (T12#1)", "T12#2 8 (T11#2)", "T10 11 (T3 T12#2)", "T4 2 (T2)",
                "T5 3 (T4)", "T8 4 (T5)", "T5#1 5 (T8)", "T8#1 6 (T5#1)", "T5#2 7 (T8#1)", "T8#2 8 (T5#2)",
                "T6 3 (T4)", "T7 4 (T6)", "T7#1 5 (T7)", "T7#2 6 (T7#1)", "T9 15 (T7#2 T8#2)",
                "T4#1 16 (T9)", "T5#3 17 (T4#1)", "T8#3 18 (T5#3)", "T5#4 19 (T8#3)", "T8#4 20 (T5#4)",
                "T5#5 21 (T8#4)", "T8#5 22 (T5#5)", "T6#1 17 (T4#1)", "T7#3 18 (T6#1)", "T7#4 19 (T7#3)",
                "T7#5 20 (T7#4)", "T9#1 43 (T7#5 T8#5)", "T4#2 44 (T9#1)", "T5#6 45 (T4#2)",
                "T8#6 46 (T5#6)", "T5#7 47 (T8#6)", "T8#7 48 (T5#7)", "T5#8 49 (T8#7)", "T8#8 50 (T5#8)",
                "T6#2 45 (T4#2)", "T7#6 46 (T6#2)", "T7#7 47 (T7#6)", "T7#8 48 (T7#7)",
                "T9#2 99 (T7#8 T8#8)", "T13 111 (T9#2 T10)", "T2#1 112 (T13)", "T3#1 113 (T2#1)",
                "T11#3 114 (T3#1)", "T12#3 115 (T11#3)", "T11#4 116 (T12#3)", "T12#4 117 (T11#4)",
                "T11#5 118 (T12#4)", "T12#5 119 (T11#5)", "T10#1 233 (T3#1 T12#5)", "T4#3 113 (T2#1)",
                "T5#9 114 (T4#3)", "T8#9 115 (T5#9)", "T5#10 116 (T8#9)", "T8#10 117 (T5#10)",
                "T5#11 118 (T8#10)", "T8#11 119 (T5#11)", "T6#3 114 (T4#3)", "T7#9 115 (T6#3)",
                "T7#10 116 (T7#9)", "T7#11 117 (T7#10)", "T9#3 237 (T7#11 T8#11)", "T4#4 238 (T9#3)",
                "T5#12 239 (T4#4)", "T8#12 240 (T5#12)", "T5#13 241 (T8#12)", "T8#13 242 (T5#13)",
                "T5#14 243 (T8#13)", "T8#14 244 (T5#14)", "T6#4 239 (T4#4)", "T7#12 240 (T6#4)",
                "T7#13 241 (T7#12)", "T7#14 242 (T7#13)", "T9#4 487 (T7#14 T8#14)", "T4#5 488 (T9#4)",
                "T5#15 489 (T4#5)", "T8#15 490 (T5#15)", "T5#16 491 (T8#15)", "T8#16 492 (T5#16)",
                "T5#17 493 (T8#16)", "T8#17 494 (T5#17)", "T6#5 489 (T4#5)", "T7#15 490 (T6#5)",
                "T7#16 491 (T7#15)", "T7#17 492 (T7#16)", "T9#5 987 (T7#17 T8#17)",
                "T13#1 1221 (T9#5 T10#1)", "T2#2 1222 (T13#1)", "T3#2 1223 (T2#2)", "T11#6 1224 (T3#2)",
                "T12#6 1225 (T11#6)", "T11#7 1226 (T12#6)", "T12#7 1227 (T11#7)", "T11#8 1228 (T12#7)",
                "T12#8 1229 (T11#8)", "T10#2 2453 (T3#2 T12#8)", "T4#6 1223 (T2#2)", "T5#18 1224 (T4#6)",
                "T8#18 1225 (T5#18)", "T5#19 1226 (T8#18)", "T8#19 1227 (T5#19)", "T5#20 1228 (T8#19)",
                "T8#20 1229 (T5#20)", "T6#6 1224 (T4#6)", "T7#18 1225 (T6#6)", "T7#19 1226 (T7#18)",
                "T7#20 1227 (T7#19)", "T9#6 2457 (T7#20 T8#20)", "T4#7 2458 (T9#6)", "T5#21 2459 (T4#7)",
                "T8#21 2460 (T5#21)", "T5#22 2461 (T8#21)", "T8#22 2462 (T5#22)", "T5#23 2463 (T8#22)",
                "T8#23 2464 (T5#23)", "T6#7 2459 (T4#7)", "T7#21 2460 (T6#7)", "T7#22 2461 (T7#21)",
                "T7#23 2462 (T7#22)", "T9#7 4927 (T7#23 T8#23)", "T4#8 4928 (T9#7)", "T5#24 4929 (T4#8)",
                "T8#24 4930 (T5#24)", "T5#25 4931 (T8#24)", "T8#25 4932 (T5#25)", "T5#26 4933 (T8#25)",
                "T8#26 4934 (T5#26)", "T6#8 4929 (T4#8)", "T7#24 4930 (T6#8)", "T7#25 4931 (T7#24)",
                "T7#26 4932 (T7#25)", "T9#8 9867 (T7#26 T8#26)", "T13#2 12321 (T9#8 T10#2)",
                "T14 12322 (T13#2)", "T15 12322 (T13#2)" },

        //                

        };
    }

    @Override
    public final String getJobPrefix() {
        return "/functionaltests/workflow/descriptors/flow_loop_2_";
    }

    @Test
    public void testWorkflowLoopJobs2() throws Throwable {
        internalRun();
    }
}
