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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.resourcemanager.nodesource.policy;

import java.util.Timer;
import java.util.TimerTask;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.utils.NamesConvertor;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;


public class ReleaseResourcesWhenSchedulerIdle extends SchedulerAwarePolicy implements InitActive,
        SchedulerEventListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 30L;
	private transient Timer timer;
    private int activeJobs = 0;

    @Configurable(description = "ms")
    private long idleTime = 60 * 1000; // 1 min by default
    private boolean resourcesReleased = true;
    private ReleaseResourcesWhenSchedulerIdle thisStub;

    public ReleaseResourcesWhenSchedulerIdle() {
    }

    /**
     * Configure a policy with given parameters.
     * @param policyParameters parameters defined by user
     */
    @Override
    public BooleanWrapper configure(Object... policyParameters) {
        super.configure(policyParameters);
        try {
            timer = new Timer(true);
            idleTime = Long.parseLong(policyParameters[4].toString());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(e);
        }
        return new BooleanWrapper(true);
    }

    public void initActivity(Body body) {
        thisStub = (ReleaseResourcesWhenSchedulerIdle) PAActiveObject.getStubOnThis();
    }

    @Override
    public BooleanWrapper activate() {
        BooleanWrapper activationStatus = super.activate();
        if (!activationStatus.getBooleanValue()) {
            return activationStatus;
        }

        activeJobs = state.getPendingJobs().size() + state.getRunningJobs().size();
        debug("Policy is activated. Current number of jobs is " + activeJobs);
        return new BooleanWrapper(true);
    }

    @Override
    protected SchedulerEvent[] getEventsList() {
        return new SchedulerEvent[] { SchedulerEvent.JOB_RUNNING_TO_FINISHED,
                SchedulerEvent.JOB_PENDING_TO_FINISHED, SchedulerEvent.JOB_SUBMITTED };
    }

    @Override
    protected SchedulerEventListener getSchedulerListener() {
        return thisStub;
    }

    @Override
    public String getDescription() {
        return "Releases all resources when scheduler is idle for specified\ntime. Acquires them back on job submission.";
    }

    @Override
    public String toString() {
        return NamesConvertor.beautifyName(this.getClass().getSimpleName()) + " [idle time: " + idleTime +
            " ms]";
    }

    @Override
    public void jobSubmittedEvent(JobState jobState) {
        activeJobs++;
        debug("Job is submitted. Total number of jobs is " + activeJobs);
        timer.cancel();
        if (activeJobs > 0 && resourcesReleased) {
            synchronized (timer) {
                acquireAllNodes();
                resourcesReleased = false;
            }
        }
    }

    @Override
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        switch (notification.getEventType()) {
            case JOB_PENDING_TO_FINISHED:
            case JOB_RUNNING_TO_FINISHED:
                activeJobs--;
                debug("Job is finished. Total number of jobs is " + activeJobs);
                if (activeJobs == 0 && !resourcesReleased) {
                    debug("Schedule task to release resources in " + idleTime);
                    timer = new Timer(true);
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            synchronized (timer) {
                                thisStub.removeAllNodes(false);
                                resourcesReleased = true;
                            }
                        }
                    }, idleTime);
                }
                break;
        }
    }
}
