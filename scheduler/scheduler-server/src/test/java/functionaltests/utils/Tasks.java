/*
 *  *
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.utils;

import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

/**
 * Some utility methods to manipulate, compare, etc. tasks with tests.
 *
 * @author ActiveEon Team
 */
public class Tasks {

    public static boolean areEqual(InternalTask a, InternalTask b) {
        if (a == b) {
            return true;
        }

        if (a == null || a.getClass() != b.getClass()) {
            return false;
        }

        if (a.getMaxNumberOfExecutionOnFailure() != b.getMaxNumberOfExecutionOnFailure()) {
            return false;
        }

        if (a.getIterationIndex() != b.getIterationIndex()) {
            return false;
        }

        if (a.getReplicationIndex() != b.getReplicationIndex()) {
            return false;
        }

        if (!areEqual(a.getTaskInfo(), b.getTaskInfo())) {
            return false;
        }

        if (a.getDependences() != null ? !a.getDependences().equals(b.getDependences()) : b.getDependences() != null) {
            return false;
        }

        if (a.getExecuterInformation() != null ?
                !a.getExecuterInformation().equals(b.getExecuterInformation()) :
                b.getExecuterInformation() != null) {
            return false;
        }


        if (a.getNodeExclusion() != null ?
                !a.getNodeExclusion().equals(b.getNodeExclusion()) :
                b.getNodeExclusion() != null) {
            return false;
        }

        if (a.getMatchingBlock() != null ?
                !a.getMatchingBlock().equals(b.getMatchingBlock()) :
                b.getMatchingBlock() != null) {
            return false;
        }

        if (a.getJoinedBranches() != null ?
                !a.getJoinedBranches().equals(b.getJoinedBranches()) :
                b.getJoinedBranches() != null) {
            return false;
        }

        if (a.getIfBranch() != null ?
                !a.getIfBranch().equals(b.getIfBranch()) :
                b.getIfBranch() != null) {
            return false;
        }

        return a.getReplicatedFrom() != null ?
                a.getReplicatedFrom().equals(b.getReplicatedFrom()) :
                b.getReplicatedFrom() == null;
    }

    public static boolean areEqual(TaskInfo a, TaskInfo b) {
        if (a == b) {
            return true;
        }

        if (a == null || a.getClass() != b.getClass()) {
            return false;
        }

        if (a.getStartTime() != b.getStartTime()) {
            return false;
        }

        if (a.getInErrorTime() != b.getInErrorTime()) {
            return false;
        }

        if (a.getFinishedTime() != b.getFinishedTime()) {
            return false;
        }

        if (a.getScheduledTime() != b.getScheduledTime()) {
            return false;
        }

        if (a.getExecutionDuration() != b.getExecutionDuration()) {
            return false;
        }

        if (a.getProgress() != b.getProgress()) {
            return false;
        }

        if (a.getNumberOfExecutionLeft() != b.getNumberOfExecutionLeft()) {
            return false;
        }

        if (a.getNumberOfExecutionOnFailureLeft() != b.getNumberOfExecutionOnFailureLeft()) {
            return false;
        }

        if (a.getStatus() != b.getStatus()) {
            return false;
        }

        return a.getExecutionHostName() != null ?
                a.getExecutionHostName().equals(b.getExecutionHostName()) :
                b.getExecutionHostName() == null;
    }

}
