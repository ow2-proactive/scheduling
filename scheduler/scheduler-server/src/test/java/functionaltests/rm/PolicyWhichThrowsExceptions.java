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
package functionaltests.rm;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.JobDescriptor;
import org.ow2.proactive.scheduler.descriptor.EligibleTaskDescriptor;
import org.ow2.proactive.scheduler.policy.ExtendedSchedulerPolicy;
import org.ow2.proactive.utils.NodeSet;

import performancetests.helper.LogProcessor;

import java.util.LinkedList;
import java.util.List;


public class PolicyWhichThrowsExceptions extends ExtendedSchedulerPolicy {

    static final Logger logger = Logger.getLogger(PolicyWhichThrowsExceptions.class);

    boolean thrown = false;

    @Override
    public boolean isTaskExecutable(NodeSet selectedNodes, EligibleTaskDescriptor task) {
        if (LogProcessor.linesThatMatch("job 1 started").size() == 1 &&
            LogProcessor.linesThatMatch("job 1 finished").size() == 0 && thrown == false) {
            thrown = true;
            throw new Error("This error is thrown to perform reconnection to RM isTaskExecutable");
        } else {
            return super.isTaskExecutable(selectedNodes, task);
        }
    }
}
