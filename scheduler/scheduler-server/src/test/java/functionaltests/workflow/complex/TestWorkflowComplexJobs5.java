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

import org.junit.Test;
import org.ow2.proactive.scheduler.common.task.flow.FlowActionType;

import functionaltests.workflow.TRepJobs;


/**
 * Tests the correctness of workflow-controlled jobs including {@link FlowActionType#REPLICATE},
 * {@link FlowActionType#IF} and {@link FlowActionType#LOOP} actions
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class TestWorkflowComplexJobs5 extends TRepJobs {
    @Test
    public void testWorkflowComplexJobs5() throws Throwable {
        String prefix = "/functionaltests/workflow/descriptors/flow_complex_5_";

        TRepCase t1 = new TRepCase(prefix + "1.xml", 38, "T,1,0 T4,9,54 T5,1,28 T1,9,36 T3,9,-9 T2,9,45");
        TRepCase t2 = new TRepCase(prefix + "2.xml",
                                   96,
                                   "T6,9,462 T7,3,465 T,3,130 T4,27,1278 T5,27,1305 T1,9,399 T3,9,-9 " + "T2,9,-9");

        testJobs(t1, t2);
    }
}
