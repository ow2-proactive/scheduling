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
import org.awaitility.Awaitility;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.synchronization.InvalidChannelException;
import org.ow2.proactive.scheduler.synchronization.SynchronizationInternal;
import org.ow2.proactive.scheduler.synchronization.SynchronizationWrapper;


/**
 * SignalApiImpl acts as a high level signal service that uses the synchronization API.
 * It enables workflow tasks handle signals in an easy manner.
 *
 * It implements the methods defined in the interface @see org.ow2.proactive.scheduler.signal.SignalApi.
 *
 * @author ActiveEon Team
 * @since 24/11/2020
 */
public class SignalApiImpl implements SignalApi {

    private static final Logger logger = Logger.getLogger(SignalApiImpl.class);

    private static final String LOG_WARNING_CONSTANT = " of the job ";

    private static final String SIGNALS_CHANNEL = PASchedulerProperties.SCHEDULER_SIGNALS_CHANNEL.getValueAsString();

    protected static final String READY_PREFIX = "ready_";

    private SynchronizationWrapper synchronization;

    private String jobId;

    public SignalApiImpl(String originator, TaskId taskId, SynchronizationInternal synchronizationInternal) {
        jobId = taskId.getJobId().value();
        synchronization = new SynchronizationWrapper(originator, taskId, synchronizationInternal);
        try {
            if (!synchronization.channelExists(SIGNALS_CHANNEL)) {
                synchronization.createChannel(SIGNALS_CHANNEL, true);
            }

            if (!synchronization.containsKey(SIGNALS_CHANNEL, jobId)) {
                synchronization.put(SIGNALS_CHANNEL, jobId, new ArrayList<>());
            }
        } catch (IOException | InvalidChannelException e) {
            logger.warn("Could not instantiate Signal API for the job " + jobId, e);
        }
    }

    @Override
    public boolean readyForSignal(String signalName) throws InvalidChannelException {
        if (isReceived(signalName)) {
            removeSignal(signalName);
        }
        return addSignal(READY_PREFIX + signalName);
    }

    @Override
    public boolean isReceived(String signalName) throws InvalidChannelException {
        List<String> signals = getJobSignals();
        if (!signals.isEmpty()) {
            return signals.contains(signalName);
        } else {
            return false;
        }
    }

    @Override
    public boolean addSignal(String signalName) {
        try {
            List<String> jobSignals = getJobSignals();
            jobSignals.add(signalName);
            synchronization.put(SIGNALS_CHANNEL, jobId, (Serializable) jobSignals);
            return true;
        } catch (IOException | InvalidChannelException e) {
            logger.warn("Could not add signal " + signalName + LOG_WARNING_CONSTANT + jobId, e);
            return false;
        }
    }

    @Override
    public boolean addAllSignals(List<String> signalsSubList) {
        try {
            List<String> jobSignals = getJobSignals();
            jobSignals.addAll(signalsSubList);
            synchronization.put(SIGNALS_CHANNEL, jobId, (Serializable) jobSignals);
            return true;
        } catch (IOException | InvalidChannelException e) {
            logger.warn("Could not add the signals " + signalsSubList + LOG_WARNING_CONSTANT + jobId, e);
            return false;
        }
    }

    @Override
    public boolean removeSignal(String signalName) {
        try {
            List<String> jobSignals = getJobSignals();
            jobSignals.remove(signalName);
            synchronization.put(SIGNALS_CHANNEL, jobId, (Serializable) jobSignals);
            return true;
        } catch (IOException | InvalidChannelException e) {
            logger.warn("Could not delete the signal " + signalName + LOG_WARNING_CONSTANT + jobId, e);
            return false;
        }
    }

    @Override
    public boolean removeAllSignals(List<String> signalsSubList) {
        try {
            List<String> jobSignals = getJobSignals();
            jobSignals.removeAll(signalsSubList);
            synchronization.put(SIGNALS_CHANNEL, jobId, (Serializable) jobSignals);
            return true;
        } catch (IOException | InvalidChannelException e) {
            logger.warn("Could not delete the signals " + signalsSubList + LOG_WARNING_CONSTANT + jobId, e);
            return false;
        }
    }

    @Override
    public List<String> getJobSignals() throws InvalidChannelException {
        return (List<String>) synchronization.get(SIGNALS_CHANNEL, jobId);
    }

    @Override
    public void clearJobSignals() {
        try {
            synchronization.remove(SIGNALS_CHANNEL, jobId);
        } catch (IOException | InvalidChannelException e) {
            logger.warn("Could not clear the job" + jobId + " from the signals channel", e);
        }
    }

    @Override
    public void waitFor(String signalName) {
        Awaitility.await().until(() -> isReceived(signalName));
    }

    @Override
    public void waitForAny(List<String> signalsList) {
        Awaitility.await().until(() -> {
            List<String> signals = getJobSignals();
            return signalsList.stream().parallel().filter(signals::contains).findFirst().orElse(null) != null;
        });
    }
}
