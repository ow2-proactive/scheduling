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
public class TestWorkflowLoopJobs2 extends TWorkflowJobs {

    @Override
    public final String[][] getJobs() {
        return new String[][] {

        // 1: complex multiple nested loops
        { "T1 0", "T2 1", "T3 2", "T11 3", "T12 4", "T11#1 5", "T12#1 6", "T11#2 7", "T12#2 8", "T10 11",
                "T4 2", "T5 3", "T8 4", "T5#1 5", "T8#1 6", "T5#2 7", "T8#2 8", "T6 3", "T7 4", "T7#1 5",
                "T7#2 6", "T9 15", "T4#1 16", "T5#3 17", "T8#3 18", "T5#4 19", "T8#4 20", "T5#5 21",
                "T8#5 22", "T6#1 17", "T7#3 18", "T7#4 19", "T7#5 20", "T9#1 43", "T4#2 44", "T5#6 45",
                "T8#6 46", "T5#7 47", "T8#7 48", "T5#8 49", "T8#8 50", "T6#2 45", "T7#6 46", "T7#7 47",
                "T7#8 48", "T9#2 99", "T13 111", "T2#1 112", "T3#1 113", "T11#3 114", "T12#3 115",
                "T11#4 116", "T12#4 117", "T11#5 118", "T12#5 119", "T10#1 233", "T4#3 113", "T5#9 114",
                "T8#9 115", "T5#10 116", "T8#10 117", "T5#11 118", "T8#11 119", "T6#3 114", "T7#9 115",
                "T7#10 116", "T7#11 117", "T9#3 237", "T4#4 238", "T5#12 239", "T8#12 240", "T5#13 241",
                "T8#13 242", "T5#14 243", "T8#14 244", "T6#4 239", "T7#12 240", "T7#13 241", "T7#14 242",
                "T9#4 487", "T4#5 488", "T5#15 489", "T8#15 490", "T5#16 491", "T8#16 492", "T5#17 493",
                "T8#17 494", "T6#5 489", "T7#15 490", "T7#16 491", "T7#17 492", "T9#5 987", "T13#1 1221",
                "T2#2 1222", "T3#2 1223", "T11#6 1224", "T12#6 1225", "T11#7 1226", "T12#7 1227",
                "T11#8 1228", "T12#8 1229", "T10#2 2453", "T4#6 1223", "T5#18 1224", "T8#18 1225",
                "T5#19 1226", "T8#19 1227", "T5#20 1228", "T8#20 1229", "T6#6 1224", "T7#18 1225",
                "T7#19 1226", "T7#20 1227", "T9#6 2457", "T4#7 2458", "T5#21 2459", "T8#21 2460",
                "T5#22 2461", "T8#22 2462", "T5#23 2463", "T8#23 2464", "T6#7 2459", "T7#21 2460",
                "T7#22 2461", "T7#23 2462", "T9#7 4927", "T4#8 4928", "T5#24 4929", "T8#24 4930",
                "T5#25 4931", "T8#25 4932", "T5#26 4933", "T8#26 4934", "T6#8 4929", "T7#24 4930",
                "T7#25 4931", "T7#26 4932", "T9#8 9867", "T13#2 12321", "T14 12322", "T15 12322" },

        //                

        };
    }

    @Override
    public final String getJobPrefix() {
        return "/functionaltests/workflow/descriptors/flow_loop_2_";
    }

    @org.junit.Test
    public void run() throws Throwable {
        internalRun();
    }
}
