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

/**
 * Tests the correctness of workflow-controlled jobs
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowReplicateJobs extends TWorkflowJobs {

    // private static final String[][] jobs = 
    @Override
    public final String[][] getJobs() {
        return new String[][] {

        // 1: replicate on one single task
                { "T 0 ()", "T1 1 (T)", "T1*1 1 (T)", "T2 3 (T1 T1*1)" },

                // 2: replicate on a simple task block
                { "T 0 ()", "T1 1 (T)", "T1*1 1 (T)", "T2 2 (T1)", "T2*1 2 (T1*1)", "T3 5 (T2 T2*1)" },

                // 3: replicate on a complex task block
                { "T 0 ()", "T1 1 (T)", "T2 2 (T1)", "T3 2 (T1)", "T4 3 (T2)", "T5 3 (T2)", "T6 9 (T3 T4 T5)", "T1*1 1 (T)", "T2*1 2 (T1*1)", "T3*1 2 (T1*1)",
                        "T4*1 3 (T2*1)", "T5*1 3 (T2*1)", "T6*1 9 (T3*1 T4*1 T5*1)", "T7 19 (T6 T6*1)" },

                // 4: nested replicate: block -> single task
                { "T 0 ()", "T1 1 (T)", "T2 2 (T1)", "T3 5 (T2 T2*1)", "T1*1 1 (T)", "T2*1 2 (T1)", "T3*1 5 (T2*2 T2*3)", "T2*2 2 (T1*1)", "T2*3 2 (T1*1)", "T4 11 (T3 T3*1)" },

                // 5: nested replicate: block -> block
                { "T 0 ()", "T1 1 (T)", "T1*1 1 (T)", "T2 2 (T1)", "T2*1 2 (T1)", "T2*2 2 (T1*1)", "T2*3 2 (T1*1)", "T3 3 (T2)", "T3*1 3 (T2*1)", "T3*2 3 (T2*2)",
                        "T3*3 3 (T2*3)", "T4 7 (T3 T3*1)", "T4*1 7 (T3*2 T3*3)", "T5 15 (T4 T4*1)" },

                // 6: double replicate: single task / block
                { "T 0 ()", "T1 1 (T)", "T1*1 1 (T)", "T4 3 (T1 T1*1)", "T2 1 (T)", "T2*1 1 (T)", "T3 2 (T2)", "T3*1 2 (T2*1)", "T5 3 (T3)", "T5*1 3 (T3*1)",
                        "T7 10 (T4 T5 T5*1)" },

                // 7: nested double replicate: block -> complex block / block
                { "T 0 ()", "T2 1 (T)", "T2*1 1 (T)", "T3 2 (T2)", "T3*1 2 (T2*1)", "T5 3 (T3)", "T5*1 3 (T3*1)", "T1 1 (T)", "T1*1 1 (T)", "T8 2 (T1)",
                        "T8*1 2 (T1*1)", "T10 3 (T8)", "T10*1 3 (T8)", "T10*2 3 (T8*1)", "T10*3 3 (T8*1)", "T4 4 (T10)", "T4*1 4 (T10*1)", "T4*2 4 (T10*2)",
                        "T4*3 4 (T10*3)", "T9 4 (T10)", "T9*1 4 (T10*1)", "T9*2 4 (T10*2)", "T9*3 4 (T10*3)", "T11 9 (T4 T9)", "T11*1 9 (T4*1 T9*1)", "T11*2 9 (T4*2 T9*2)",
                        "T11*3 9 (T4*3 T9*3)", "T6 19 (T11 T11*1)", "T6*1 19 (T11*2 T11*3)", "T7 45 (T5 T5*1 T6 T6*1)" },

        //                
        };

    }

    @Override
    public final String getJobPrefix() {
        return "/functionaltests/workflow/descriptors/flow_duplicate_";
    }

    @org.junit.Test
    public void run() throws Throwable {
        internalRun();
    }
}
