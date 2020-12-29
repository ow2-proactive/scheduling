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
package org.ow2.proactive.scheduler.signal;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.synchronization.InvalidChannelException;
import org.ow2.proactive.scheduler.synchronization.SynchronizationInternal;
import org.ow2.proactive.scheduler.synchronization.SynchronizationWrapper;


public class SignalWrapper implements Signal {

    private String originator;

    private TaskId taskId;

    private SynchronizationWrapper synchronization;

    private String jobId;

    private static final String SIGNALS_CHANNEL = PASchedulerProperties.SCHEDULER_SIGNALS_CHANNEL.getValueAsString();

    private static final String READY_PREFIX = "ready_";

    private static Logger logger = Logger.getLogger(SignalWrapper.class);

    public SignalWrapper(String originator, TaskId taskId, SynchronizationInternal synchronizationInternal) {
        this.originator = originator;
        this.taskId = taskId;
        jobId = taskId.getJobId().value();
        synchronization = new SynchronizationWrapper(originator, taskId, synchronizationInternal);
    }

    @Override
    public boolean ready(String signalName) {
        try {
            synchronization.createChannelIfAbsent(SIGNALS_CHANNEL, true);
            addSignalToJobSignals(READY_PREFIX + signalName);
        } catch (IOException | InvalidChannelException e) {
            logger.warn("Could not send ready for the signal '" + signalName + "' of taskId " + taskId, e);
        }
        return true;
    }

    public void addSignalToJobSignals(String signalName) throws IOException, InvalidChannelException {
        List<String> jobSignals = (List<String>) synchronization.get(SIGNALS_CHANNEL, jobId);
        if (jobSignals == null) {
            jobSignals = new ArrayList<>();
        }
        jobSignals.add(signalName);
        synchronization.put(SIGNALS_CHANNEL, jobId, (Serializable) jobSignals);
    }
}
