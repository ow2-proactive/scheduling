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
package performancetests.probability;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import performancetests.metrics.TaskSchedulingTimeTest;

import java.util.Arrays;
import java.util.Collection;


/**
 * The performance test calculates average time to scheduler (to dispatch) task, i.e. time to move
 * task from pending till running state.
 * Test repeats same experiment given number of times. In each experiment it submits job with a single task,
 * waits until job is finished, and then computes scheduling time, as difference between timestamp when job was submitted,
 * and timestamp when task was started.
 */
@RunWith(Parameterized.class)
public class TaskSchedulingTimeTestProb extends TaskSchedulingTimeTest {

    /**
     * initialize test with static parameters, where first argument is a number of experiments, and second
     * is a limit which average scheduling time should not cross.
     * @return
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { 2, 2000 } });
    }

    public TaskSchedulingTimeTestProb(int taskNumber, long timeLimit) {
        super(taskNumber, timeLimit);
    }

    @Override
    public String name() {
        return super.name() + "Prob";
    }
}
