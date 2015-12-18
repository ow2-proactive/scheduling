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

import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.descriptor.JobDescriptor;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.job.InternalJobFactory;
import org.ow2.proactive.scheduler.task.internal.InternalTask;

/**
 * Some utility methods to manipulate, compare, etc. jobs with tests,
 *
 * @author ActiveEon Team
 */
public final class Jobs {

    public static InternalJob createJob(Job job) throws Exception {
        InternalJob internalJob = InternalJobFactory.createJob(job, Credentials.getDefaultCredentials());
        internalJob.setOwner("admin");
        return internalJob;
    }

    public static Job parseXml(String filePath) throws JobCreationException {
        return JobFactory.getFactory().createJob(filePath);
    }

    public static boolean areEqual(InternalJob a, InternalJob b) {
        if (a.getITasks().size() != b.getITasks().size()) {
            return false;
        }

        for (InternalTask at : a.getITasks()) {
            try {
                InternalTask bt = b.getTask(at.getName());

                if (!Tasks.areEqual(at, bt)) {
                    return false;
                }
            } catch (UnknownTaskException e) {
                return false;
            }
        }

        if (!areEqual(a.getJobInfo(), b.getJobInfo())) {
            return false;
        }

        return areEqual(a.getJobDescriptor(), b.getJobDescriptor());
    }

    public static boolean areEqual(JobDescriptor a, JobDescriptor b) {
        if (a == b) {
            return true;
        }

        if (a == null || a.getClass() != b.getClass()) {
            return false;
        }

        if (!a.getEligibleTasks().equals(b.getEligibleTasks())) {
            return false;
        }

        if (!a.getRunningTasks().equals(b.getRunningTasks())) {
            return false;
        }

        return a.getPausedTasks().equals(b.getPausedTasks());
    }

    public static boolean areEqual(JobInfo a, JobInfo b) {
        if (a == b) {
            return true;
        }

        if (a == null || a.getClass() != b.getClass()) {
            return false;
        }

        if (a.getSubmittedTime() != b.getSubmittedTime()) {
            return false;
        }

        if (a.getStartTime() != b.getStartTime()) {
            return false;
        }

        if (a.getFinishedTime() != b.getFinishedTime()) {
            return false;
        }

        if (a.getRemovedTime() != b.getRemovedTime()) {
            return false;
        }

        if (a.getTotalNumberOfTasks() != b.getTotalNumberOfTasks()) {
            return false;
        }

        if (a.getNumberOfPendingTasks() != b.getNumberOfPendingTasks()) {
            return false;
        }

        if (a.getNumberOfRunningTasks() != b.getNumberOfRunningTasks()) {
            return false;
        }

        if (a.getNumberOfFinishedTasks() != b.getNumberOfFinishedTasks()) {
            return false;
        }

        if (a.isToBeRemoved() != b.isToBeRemoved()) {
            return false;
        }

        if (!a.getJobId().equals(b.getJobId())) {
            return false;
        }

        if (!a.getJobOwner().equals(b.getJobOwner())) {
            return false;
        }

        if (a.getPriority() != b.getPriority()) {
            return false;
        }

        return a.getStatus() == b.getStatus();
    }

}
