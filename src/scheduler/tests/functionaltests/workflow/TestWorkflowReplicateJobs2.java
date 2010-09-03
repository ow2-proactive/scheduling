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

/**
 * Tests the correctness of workflow-controlled jobs
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowReplicateJobs2 extends TWorkflowJobs {

    // private static final String[][] jobs = 
    @Override
    public final String[][] getJobs() {
        return new String[][] {

        // 1: nested replicate: block -> block -> block
                { "T 0", "T1 1", "T1*1 1", "T2 2", "T2*1 2", "T2*2 2", "T2*3 2", "T3 3", "T4 4", "T3*1 3",
                        "T4*1 4", "T3*2 3", "T4*2 4", "T3*3 3", "T4*3 4", "T3*4 3", "T4*4 4", "T3*5 3",
                        "T4*5 4", "T3*6 3", "T4*6 4", "T3*7 3", "T4*7 4", "T3*8 3", "T4*8 4", "T3*9 3",
                        "T4*9 4", "T3*10 3", "T4*10 4", "T3*11 3", "T4*11 4", "T5 13", "T5*1 13", "T5*2 13",
                        "T5*3 13", "T6 27", "T6*1 27", "T7 55" },
                // 2: nested replicate: block -> block -> single task
                { "T 0", "T1 1", "T1*1 1", "T2 2", "T2*1 2", "T2*2 2", "T2*3 2", "T3 3", "T3*1 3", "T3*2 3",
                        "T3*3 3", "T3*4 3", "T3*5 3", "T3*6 3", "T3*7 3", "T3*8 3", "T3*9 3", "T3*10 3",
                        "T3*11 3", "T4 10", "T4*1 10", "T4*2 10", "T4*3 10", "T5 11", "T5*1 11", "T5*2 11",
                        "T5*3 11", "T6 23", "T6*1 23", "T7 47" },

                // 3: complex nested replicate
                { "T 0", "T1 1", "T2 2", "T2*1 2", "T4 3", "T4*1 3", "T4*2 3", "T4*3 3", "T8 4", "T8*1 4",
                        "T8*2 4", "T8*3 4", "T20 5", "T20*1 5", "T20*2 5", "T20*3 5", "T20*4 5", "T20*5 5",
                        "T20*6 5", "T20*7 5", "T20*8 5", "T20*9 5", "T20*10 5", "T20*11 5", "T13 16",
                        "T13*1 16", "T13*2 16", "T13*3 16", "T19 33", "T19*1 33", "T3 2", "T3*1 2", "T5 3",
                        "T5*1 3", "T6 3", "T6*1 3", "T7 4", "T7*1 4", "T7*2 4", "T7*3 4", "T9 5", "T9*1 5",
                        "T9*2 5", "T9*3 5", "T9*4 5", "T9*5 5", "T9*6 5", "T9*7 5", "T9*8 5", "T9*9 5",
                        "T9*10 5", "T9*11 5", "T10 5", "T10*1 5", "T10*2 5", "T10*3 5", "T10*4 5", "T10*5 5",
                        "T10*6 5", "T10*7 5", "T10*8 5", "T10*9 5", "T10*10 5", "T10*11 5", "T18 6",
                        "T18*1 6", "T18*2 6", "T18*3 6", "T18*4 6", "T18*5 6", "T18*6 6", "T18*7 6",
                        "T18*8 6", "T18*9 6", "T18*10 6", "T18*11 6", "T11 34", "T11*1 34", "T11*2 34",
                        "T11*3 34", "T17 72", "T17*1 72", "T14 211", "T16 212", "T15 212" },

        //                
        };

    }

    @Override
    public final String getJobPrefix() {
        return "/functionaltests/workflow/descriptors/flow_duplicate_2_";
    }

    @org.junit.Test
    public void run() throws Throwable {
        internalRun();
    }
}
