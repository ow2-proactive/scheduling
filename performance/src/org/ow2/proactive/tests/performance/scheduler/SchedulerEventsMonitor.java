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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.scheduler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskInfo;


public class SchedulerEventsMonitor implements SchedulerEventListener {

    public static Set<JobStatus> completedJobStatus;

    static {
        completedJobStatus = new HashSet<JobStatus>();
        completedJobStatus.add(JobStatus.CANCELED);
        completedJobStatus.add(JobStatus.FAILED);
        completedJobStatus.add(JobStatus.FINISHED);
        completedJobStatus.add(JobStatus.KILLED);
    }

    private List<WaitCondition> waitConditions = new ArrayList<WaitCondition>();

    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        synchronized (waitConditions) {
            for (WaitCondition waitCondition : waitConditions) {
                waitCondition.schedulerStateUpdatedEvent(eventType);
            }
        }
    }

    public void jobSubmittedEvent(JobState job) {
        synchronized (waitConditions) {
            for (WaitCondition waitCondition : waitConditions) {
                waitCondition.jobSubmittedEvent(job);
            }
        }
    }

    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        synchronized (waitConditions) {
            for (WaitCondition waitCondition : waitConditions) {
                waitCondition.jobStateUpdatedEvent(notification);
            }
        }
    }

    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        synchronized (waitConditions) {
            for (WaitCondition waitCondition : waitConditions) {
                waitCondition.taskStateUpdatedEvent(notification);
            }
        }
    }

    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        synchronized (waitConditions) {
            for (WaitCondition waitCondition : waitConditions) {
                waitCondition.usersUpdatedEvent(notification);
            }
        }
    }

    public WaitCondition addWaitCondition(WaitCondition waitCondition) {
        synchronized (waitConditions) {
            waitConditions.add(waitCondition);
        }
        return waitCondition;
    }

    public final boolean waitFor(WaitCondition waitCondition, long timeout) throws InterruptedException {
        synchronized (waitConditions) {
            if (!waitConditions.contains(waitCondition)) {
                throw new IllegalArgumentException("Condition isn't related to this monitor");
            }
        }
        try {
            long endTime = System.currentTimeMillis() + timeout;
            try {
                boolean stopWait;
                while (!(stopWait = waitCondition.stopWait())) {
                    long waitTime = endTime - System.currentTimeMillis();
                    if (waitTime > 0) {
                        synchronized (waitCondition) {
                            waitCondition.wait(waitTime);
                        }
                    } else {
                        break;
                    }
                }

                // System.out.println("All events:\n" + waitCondition.getEventsLog());

                if (stopWait) {
                    return true;
                } else {
                    System.out.println("Waiting failed with timeout, all events:\n" +
                        waitCondition.getEventsLog());
                    return false;
                }
            } catch (WaitFailedException e) {
                System.out.println("Waiting failed with error: " + e.getMessage() + ", all events:\n" +
                    waitCondition.getEventsLog());
                return false;
            }
        } finally {
            synchronized (waitConditions) {
                waitConditions.remove(waitCondition);
            }
        }
    }

}
