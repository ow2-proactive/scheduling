/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests.workflow.complex;

import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;

import functionaltests.workflow.TRepJobs;


/**
 * Tests the correctness of workflow-controlled jobs including {@link FlowActionType#LOOP} and 
 * {@link FlowActionType#REPLICATE} actions
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowComplexJobs extends TRepJobs {
    @org.junit.Test
    public void testWorkflowComplexJobs() throws Throwable {
        String prefix = "/functionaltests/workflow/descriptors/flow_complex_";

        TRepCase t1 = new TRepCase(prefix + "1.xml", 20, "T,2,12 T4,4,32 T5,4,68 T1,4,28 T3,4,32 T2,2,70");
        TRepCase t2 = new TRepCase(prefix + "2.xml", 15, "T,3,25 T1,9,84 T2,3,87");
        TRepCase t3 = new TRepCase(prefix + "3.xml", 11, "T,1,0 T1,9,18 T2,1,10");
        TRepCase t4 = new TRepCase(prefix + "4.xml", 33, "T,3,55 T1,27,549 T2,3,195");
        TRepCase t5 = new TRepCase(prefix + "5.xml",
                                   58,
                                   "T6,16,424 T,1,0 T5,8,204 T8,16,440 T9,8,448 T15,4,312 T1,4,56 " + "T2,1,261");

        testJobs(t1, t2, t3, t4, t5);
    }

}
