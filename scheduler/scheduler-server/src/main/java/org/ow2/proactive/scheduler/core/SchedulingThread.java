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
package org.ow2.proactive.scheduler.core;

import java.util.Timer;
import java.util.TimerTask;

import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public final class SchedulingThread extends Thread {

    private static final int SCHEDULER_TIME_OUT = PASchedulerProperties.SCHEDULER_TIME_OUT.getValueAsInt();

    private final SchedulingMethod schedulingMethod;

    private final SchedulingService service;

    public SchedulingThread(SchedulingMethod schedulingMethod, SchedulingService service) {
        super("SchedulingThread");
        this.schedulingMethod = schedulingMethod;
        this.service = service;
    }

    public void run() {
        boolean tasksStarted;

        while (!isInterrupted()) {
            try {
                tasksStarted = false;
                if (service.status == SchedulerStatus.STARTED || service.status == SchedulerStatus.PAUSED ||
                    service.status == SchedulerStatus.STOPPED) {
                    tasksStarted = schedulingMethod.schedule() > 0;
                }
                if (!tasksStarted) {
                    service.sleepSchedulingThread();
                }
            } catch (InterruptedException e) {
                break;
            } catch (Throwable t) {
                service.handleException(t);
            }
        }
    }

    protected void sleepSchedulingThread() throws InterruptedException {
        synchronized (this) {
            this.wait(SCHEDULER_TIME_OUT);
        }
    }

    protected void wakeUpSchedulingThread() {
        synchronized (this) {
            this.notifyAll();
        }
    }

}
